package com.werner.service;

import com.werner.model.EquipmentLatestStatus;
import com.werner.model.StatusHistoryLog;
import com.werner.model.Transaction;
import com.werner.repository.EquipmentLatestStatusRepository;
import com.werner.repository.StatusHistoryLogRepository;
import com.werner.repository.TransactionRepository;
import com.werner.util.EventCodeMappingEnum;
import com.werner.util.SegmentEventPriority;
import com.werner.vo.EquipmentEventRequest;
import com.werner.vo.EquipmentEventResponse;
import com.werner.vo.SegmentOrder;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service("eventProgressionService")
public class EventProgressionServiceImpl implements EventProgressionService {

    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    private List<String> onNetworkEvents = Arrays.asList("OE", "SIT", "SAU", "IL");

    @Autowired
    TransactionRepository transactionRepository;

    @Autowired
    StatusHistoryLogRepository statusHistoryLogRepository;

    @Autowired
    EquipmentLatestStatusRepository equipmentLatestStatusRepository;


    @Override
    public EquipmentEventResponse processEquipmentEvent(EquipmentEventRequest request) {

        // Step 1 : check if the incoming event is is onNetworkEvent ...then create a Transaction with that event...
        EquipmentEventResponse response = new EquipmentEventResponse();

        try {
            Transaction transaction;

            if (onNetworkEvents.contains(request.getEventCode())) {
                transaction = prepareTransaction(request);
            } else {
                //get the transaction object using the equipment number in the request

                final Transaction byEquipNumberAndStatus = transactionRepository.findByEquipNumberAndStatus(request.getEquipmentNumber(), "A");

                if (byEquipNumberAndStatus == null) {
                    transaction = prepareTransaction(request);
                } else {
                    byEquipNumberAndStatus.setShipmentNumber(request.getShipmentNumber());
                    byEquipNumberAndStatus.setSegmentNumber(request.getSegmentNumber());

                    transaction = transactionRepository.save(byEquipNumberAndStatus);
                }

            }

            // Save into Status History log with all the request values

            prepareStatusHistoryLog(transaction, request);

            // Step - 3 validate business rules and save into equipment latest status

            final EquipmentLatestStatus equipmentLatestStatus = prepareEquipmentLatestStatus(request);

            prepareResponse(response, equipmentLatestStatus);

        } catch (Exception ex) {
            response.setErrorMessage("Exception while processing event");
            LOGGER.error("Exception ::, ", ex);
            return response;
        }

        return response;
    }

    @Override
    public EquipmentLatestStatus getLatestEquipStatus(String equipmentNumber) {

        return equipmentLatestStatusRepository.findByEquipNumber(equipmentNumber);
    }

    private void prepareResponse(EquipmentEventResponse response, EquipmentLatestStatus equipmentLatestStatus) {

        response.setEquipNumber(equipmentLatestStatus.getEquipNumber());
        response.setEquipmentStatus(equipmentLatestStatus.getEquipStatus());
        response.setStreetStatus(equipmentLatestStatus.getStreetStatus());
        response.setMessage("Processed Successfully");

    }

    private EquipmentLatestStatus prepareEquipmentLatestStatus(EquipmentEventRequest request) {

        //step 1 : get the latest equipment status and if the event is onNetwork event save the equipment status

        EquipmentLatestStatus equipmentLatestStatus = new EquipmentLatestStatus();
        if (onNetworkEvents.contains(request.getEventCode())) {
            equipmentLatestStatus = prepareEquipmentLatestStatusObject(equipmentLatestStatus, request, "Origin Street", "Empty");

            return equipmentLatestStatusRepository.save(equipmentLatestStatus);
        } else {
            //find the equip latest status with the equipment number from the request

            final EquipmentLatestStatus byEquipNumberLatestStatus = equipmentLatestStatusRepository.findByEquipNumber(request.getEquipmentNumber());


            if ((byEquipNumberLatestStatus != null && StringUtils.equalsIgnoreCase(byEquipNumberLatestStatus.getEventType(), "OE")) ||
                    (byEquipNumberLatestStatus != null && canUpdateEquipLatestStatus(byEquipNumberLatestStatus, request))) {

                String[] streetAndEquipStatus = getStreetAndEquipStatus(request);

                if (streetAndEquipStatus != null) {
                    byEquipNumberLatestStatus.setStreetStatus(streetAndEquipStatus[0]);
                    byEquipNumberLatestStatus.setEquipStatus(streetAndEquipStatus[1]);
                    byEquipNumberLatestStatus.setEventType(request.getEventCode());
                    byEquipNumberLatestStatus.setEventDate(request.getEventDate());
                    byEquipNumberLatestStatus.setSegmentNumber(request.getSegmentNumber());

                    return equipmentLatestStatusRepository.save(byEquipNumberLatestStatus);
                } else {
                    LOGGER.error("Street and Equip Status are not found for the Event Type ::: " + request.getEventCode());
                }

            } else if (byEquipNumberLatestStatus == null) {

                String[] streetAndEquipStatus = getStreetAndEquipStatus(request);

                if (streetAndEquipStatus != null) {
                    equipmentLatestStatus = prepareEquipmentLatestStatusObject(equipmentLatestStatus, request, streetAndEquipStatus[0], streetAndEquipStatus[1]);

                    return equipmentLatestStatusRepository.save(equipmentLatestStatus);
                } else {
                    LOGGER.error("Street and Equip Status are not found for the Event Type ::: " + request.getEventCode());
                }
            }
            return byEquipNumberLatestStatus;
        }

    }

    private Boolean canUpdateEquipLatestStatus(EquipmentLatestStatus byEquipNumberLatestStatus, EquipmentEventRequest request) {

        final Optional<SegmentOrder> currentLatestStatusOrder = request.getSegmentOrders().stream().filter(s -> StringUtils.equalsIgnoreCase(s.getSegmentNumber(), byEquipNumberLatestStatus.getSegmentNumber())).findFirst();

        final Optional<SegmentOrder> requestedSegmentOrder = request.getSegmentOrders().stream().filter(s -> StringUtils.equalsIgnoreCase(s.getSegmentNumber(), request.getSegmentNumber())).findFirst();

        if (currentLatestStatusOrder.isPresent() && requestedSegmentOrder.isPresent()) {

            if(StringUtils.equalsIgnoreCase("X3",request.getEventCode())){
                return validateExceptionCase(currentLatestStatusOrder.get(),requestedSegmentOrder.get(), request,byEquipNumberLatestStatus);
            } else if (requestedSegmentOrder.get().getPriorityOrder()  >  currentLatestStatusOrder.get().getPriorityOrder()) {
                return Boolean.TRUE;
            } else if (currentLatestStatusOrder.get().getPriorityOrder().equals(requestedSegmentOrder.get().getPriorityOrder())) {
                if (SegmentEventPriority.getSegmentPriority(byEquipNumberLatestStatus.getEventType()) != null &&
                        SegmentEventPriority.getSegmentPriority(request.getEventCode()) != null &&
//                        SegmentEventPriority.getSegmentPriority(request.getEventCode()).compareTo(SegmentEventPriority.getSegmentPriority(byEquipNumberLatestStatus.getEventType())) >= 1 )
                        SegmentEventPriority.getSegmentPriority(request.getEventCode()) > SegmentEventPriority.getSegmentPriority(byEquipNumberLatestStatus.getEventType())) {
                    return Boolean.TRUE;
                } else {
                    return Boolean.FALSE;
                }
            }
        }
        return Boolean.FALSE;

    }

    private Boolean validateExceptionCase(SegmentOrder currentLatestStatusOrder, SegmentOrder requestedSegmentOrder, EquipmentEventRequest request,EquipmentLatestStatus byEquipNumberLatestStatus) {


         if(StringUtils.equalsIgnoreCase("X3",request.getEventCode())
                && StringUtils.equalsIgnoreCase("OA",byEquipNumberLatestStatus.getEventType()) && currentLatestStatusOrder.getPriorityOrder().equals(requestedSegmentOrder.getPriorityOrder() - 1)){
            return  Boolean.FALSE;
        }
        return  Boolean.TRUE;
    }

    private String[] getStreetAndEquipStatus(EquipmentEventRequest request) {

        String preparingJoinString;
        if(StringUtils.isNotEmpty(request.getLoadOption())){
            List<String> code = Arrays.asList(request.getSegmentType(), request.getEventCode(), request.getLoadOption());
            preparingJoinString = code.stream().collect(Collectors.joining("_"));
        }else{
            List<String> code = Arrays.asList(request.getSegmentType(),request.getEventCode());
            preparingJoinString = code.stream().collect(Collectors.joining("_"));
        }

        final List<String> equipStreet = EventCodeMappingEnum.getEquipStreet(preparingJoinString);

        if (!CollectionUtils.isEmpty(equipStreet)) {
            String[] streetEquipStatus = new String[equipStreet.size()];
            return equipStreet.toArray(streetEquipStatus);
        }
        return null;
    }

    private EquipmentLatestStatus prepareEquipmentLatestStatusObject(EquipmentLatestStatus latestStatus, EquipmentEventRequest request, String streetStatus, String equipmentStatus) {


        latestStatus.setEquipNumber(request.getEquipmentNumber());
        latestStatus.setEquipStatus(equipmentStatus);
        latestStatus.setStreetStatus(streetStatus);
        latestStatus.setEventType(request.getEventCode());
        latestStatus.setEventDate(request.getEventDate());
        latestStatus.setSegmentNumber(request.getSegmentNumber());

        return latestStatus;
    }

    private void prepareStatusHistoryLog(Transaction transaction, EquipmentEventRequest request) {

        StatusHistoryLog historyLog = new StatusHistoryLog();

        historyLog.setTransaction(transaction);
        historyLog.setEquipNumber(request.getEquipmentNumber());
        historyLog.setEventDate(request.getEventDate());
        historyLog.setTrackingNumber(request.getTrackingNum());
        historyLog.setLoadOption(request.getLoadOption());
        historyLog.setEventCarrier(request.getEventCarrier());
        historyLog.setEventSplc(request.getEventSplc());
        historyLog.setEventType(request.getEventCode());
        historyLog.setShipmentNumber(request.getShipmentNumber());
        historyLog.setSegmentNumber(request.getSegmentNumber());

        String[] streetAndEquipStatus = getStreetAndEquipStatus(request);

        if(streetAndEquipStatus != null){
            historyLog.setStreetStatus(streetAndEquipStatus[0]);
            historyLog.setEquipStatus(streetAndEquipStatus[1]);
        }
        statusHistoryLogRepository.save(historyLog);

    }

    private Transaction prepareTransaction(EquipmentEventRequest request) {

        final Transaction dbTransactionStatus = transactionRepository.findByEquipNumberAndStatus(request.getEquipmentNumber(), "A");

        if(dbTransactionStatus == null){

            Transaction transaction = new Transaction();

            transaction.setEquipNumber(request.getEquipmentNumber());
            transaction.setShipmentNumber(request.getShipmentNumber());
            transaction.setRezTrackingNumber(request.getTrackingNum());

            if (StringUtils.equalsIgnoreCase("MON", request.getEventCode())) {
                transaction.setStatus("F");
            }else{
                transaction.setStatus("A");
            }


            return transactionRepository.save(transaction);

        }else{

            dbTransactionStatus.setRezTrackingNumber(request.getTrackingNum());

            return transactionRepository.save(dbTransactionStatus);
        }
    }
}


