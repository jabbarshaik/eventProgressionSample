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
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service("eventProgressionService")
public class EventProgressionServiceImpl implements EventProgressionService {

    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    private List<String> onNetworkEvents = Arrays.asList("OE", "SIT", "ESA", "MON","RC");

    private List<String> offNetworkEvents = Arrays.asList("IF", "IE", "ID", "RX","RE");

    @Autowired
    TransactionRepository transactionRepository;

    @Autowired
    StatusHistoryLogRepository statusHistoryLogRepository;

    @Autowired
    EquipmentLatestStatusRepository equipmentLatestStatusRepository;


    @Override
    public EquipmentEventResponse processEquipmentEvent(List<EquipmentEventRequest> requests) {

        // Step 1 : check if the incoming event is is onNetworkEvent ...then create a Transaction with that event...
        EquipmentEventResponse response = new EquipmentEventResponse();

        try {

            for(EquipmentEventRequest request : requests){
                Transaction transaction;
                Transaction existingTransaction;
                if (onNetworkEvents.contains(request.getEventCode())) {
                    transaction = prepareTransaction(request);
                    existingTransaction = transaction;
                } else {
                    //get the transaction object using the equipment number in the request

                    final Transaction byEquipNumberAndStatus = transactionRepository.findByEquipNumberAndStatus(request.getEquipmentNumber(), "A");

                    if (byEquipNumberAndStatus == null) {
                        transaction = prepareTransaction(request);
                        existingTransaction = transaction;
                    } else {
                        existingTransaction = new Transaction();
                        BeanUtils.copyProperties(byEquipNumberAndStatus,existingTransaction);

                        byEquipNumberAndStatus.setShipmentNumber(request.getShipmentNumber());
                        byEquipNumberAndStatus.setSegmentNumber(request.getSegmentNumber());
                        byEquipNumberAndStatus.setCurrentEventCode(request.getEventCode());
                        byEquipNumberAndStatus.setEquipNumber(request.getEquipmentNumber());



                        transaction = transactionRepository.save(byEquipNumberAndStatus);
                    }

                }

                // Save into Status History log with all the request values

                prepareStatusHistoryLog(transaction, request);

                // Step - 3 validate business rules and save into equipment latest status

                final EquipmentLatestStatus equipmentLatestStatus = prepareEquipmentLatestStatus(request,existingTransaction,transaction);

                prepareResponse(response, equipmentLatestStatus);
            }


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

    private EquipmentLatestStatus prepareEquipmentLatestStatus(EquipmentEventRequest request, Transaction existingTransaction, Transaction currentTransaction) {

        //step 1 : get the latest equipment status and if the event is onNetwork event save the equipment status

        EquipmentLatestStatus equipmentLatestStatus = null;

        final String[] streetAndEquipStatus = getStreetAndEquipStatus(request);

        if(streetAndEquipStatus != null){

            if (onNetworkEvents.contains(request.getEventCode())) {

                equipmentLatestStatus = equipmentLatestStatusRepository.findByTransaction(existingTransaction);

                if(equipmentLatestStatus != null){

                    equipmentLatestStatus.setStreetStatus(streetAndEquipStatus[0]);
                    equipmentLatestStatus.setEquipStatus(streetAndEquipStatus[1]);
                    equipmentLatestStatus.setEquipNumber(request.getEquipmentNumber());
                    equipmentLatestStatus.setEventType(request.getEventCode());

                    return equipmentLatestStatusRepository.save(equipmentLatestStatus);

                }else{
                     return prepareEquipmentLatestStatusObject(existingTransaction,request, streetAndEquipStatus[0], streetAndEquipStatus[1]);
                }


            } else {
                //find the equip latest status with the equipment number from the request

                equipmentLatestStatus = equipmentLatestStatusRepository.findByEquipNumber(request.getEquipmentNumber());


                if ((equipmentLatestStatus != null && onNetworkEvents.contains(equipmentLatestStatus.getEventType())) ||
                        (equipmentLatestStatus != null && canUpdateEquipLatestStatus(equipmentLatestStatus, request,existingTransaction,currentTransaction))) {

                    equipmentLatestStatus.setStreetStatus(streetAndEquipStatus[0]);
                    equipmentLatestStatus.setEquipStatus(streetAndEquipStatus[1]);
                    equipmentLatestStatus.setEventType(request.getEventCode());
                    equipmentLatestStatus.setEventDate(request.getEventDate());
                    equipmentLatestStatus.setSegmentNumber(request.getSegmentNumber());

                    return equipmentLatestStatusRepository.save(equipmentLatestStatus);

                } else if (equipmentLatestStatus == null) {
                    return prepareEquipmentLatestStatusObject(existingTransaction,request, streetAndEquipStatus[0], streetAndEquipStatus[1]);
                }
                return equipmentLatestStatus;
            }

        }else {
            LOGGER.error("Street and Equip Status are not found for the Event Type ::: " + request.getEventCode());
        }
        return equipmentLatestStatus;
    }

    private Boolean canUpdateEquipLatestStatus(EquipmentLatestStatus byEquipNumberLatestStatus, EquipmentEventRequest request, Transaction existingTransaction, Transaction currentTransaction) {

        /*
            TODO -- need to check the exception cases

            1) if we get X3 check if the current event is OA from the last segment then don't update the Equipment Latest Status --- done
            2) if we get OA check if the current event is X3 from the next immediate segment then process

         */

        final Optional<SegmentOrder> previousSegmentOrder = request.getSegmentOrders().stream().filter(s -> StringUtils.equalsIgnoreCase(s.getSegmentNumber(), existingTransaction.getSegmentNumber())).findFirst();

        final Optional<SegmentOrder> requestedSegmentOrder = request.getSegmentOrders().stream().filter(s -> StringUtils.equalsIgnoreCase(s.getSegmentNumber(), request.getSegmentNumber())).findFirst();

        if (previousSegmentOrder.isPresent() && requestedSegmentOrder.isPresent()) {

            if(StringUtils.equalsIgnoreCase("X3",request.getEventCode())) {
                return validateExceptionCaseForX3(previousSegmentOrder.get(),requestedSegmentOrder.get(),byEquipNumberLatestStatus);
            }else if (StringUtils.equalsIgnoreCase("OA",request.getEventCode()) &&
                    ! previousSegmentOrder.get().getPriorityOrder().equals(requestedSegmentOrder.get().getPriorityOrder())) {
                    return  validateExceptionCaseForOA(previousSegmentOrder.get(),requestedSegmentOrder.get(),byEquipNumberLatestStatus);
            } else if (requestedSegmentOrder.get().getPriorityOrder()  >  previousSegmentOrder.get().getPriorityOrder()) {
                return Boolean.TRUE;
            } else if (previousSegmentOrder.get().getPriorityOrder().equals(requestedSegmentOrder.get().getPriorityOrder())) {
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

    /*
            if we get X3 check if the current event is OA from the last segment then don't update the Equipment Latest Status --- done
     */
    private Boolean validateExceptionCaseForX3(SegmentOrder previousSegmentOrder, SegmentOrder requestedSegmentOrder,EquipmentLatestStatus byEquipNumberLatestStatus) {

         if(StringUtils.equalsIgnoreCase("OA",byEquipNumberLatestStatus.getEventType()) &&
                        previousSegmentOrder.getPriorityOrder().equals(requestedSegmentOrder.getPriorityOrder() - 1)){
            return  Boolean.FALSE;
        }
        return  Boolean.TRUE;
    }

    /*
          if we get OA check if the current event is X3 from the next immediate segment then process
     */
    private Boolean validateExceptionCaseForOA(SegmentOrder previousSegmentOrder, SegmentOrder requestedSegmentOrder,EquipmentLatestStatus byEquipNumberLatestStatus) {


        if(StringUtils.equalsIgnoreCase("X3",byEquipNumberLatestStatus.getEventType()) &&
                requestedSegmentOrder.getPriorityOrder() == previousSegmentOrder.getPriorityOrder() -1){
            return  Boolean.TRUE;
        }
        return  Boolean.FALSE;
    }

    private String[] getStreetAndEquipStatus(EquipmentEventRequest request) {

        String preparingJoinString;

        final List<String> onNetworkRezEvents = onNetworkEvents.stream().filter( s ->  ! StringUtils.equalsIgnoreCase("ESA",s)).collect(Collectors.toList());

        if (onNetworkRezEvents.contains(request.getEventCode())) {
            preparingJoinString = request.getEventCode();
        } else if (StringUtils.isNotEmpty(request.getLoadOption())) {
            List<String> code = Arrays.asList(request.getSegmentType(), request.getEventCode(), request.getLoadOption());
            preparingJoinString = code.stream().collect(Collectors.joining("_"));
        } else {
            List<String> code = Arrays.asList(request.getSegmentType(), request.getEventCode());
            preparingJoinString = code.stream().collect(Collectors.joining("_"));
        }

        final List<String> equipStreet = EventCodeMappingEnum.getEquipStreet(preparingJoinString);

        if (!CollectionUtils.isEmpty(equipStreet)) {
            String[] streetEquipStatus = new String[equipStreet.size()];
            return equipStreet.toArray(streetEquipStatus);
        }
        return null;
    }

    private EquipmentLatestStatus prepareEquipmentLatestStatusObject(Transaction transaction,EquipmentEventRequest request, String streetStatus, String equipmentStatus) {

        EquipmentLatestStatus latestStatus = new EquipmentLatestStatus();

        latestStatus.setEquipNumber(request.getEquipmentNumber());
        latestStatus.setEquipStatus(equipmentStatus);
        latestStatus.setStreetStatus(streetStatus);
        latestStatus.setEventType(request.getEventCode());
        latestStatus.setEventDate(request.getEventDate());
        latestStatus.setSegmentNumber(request.getSegmentNumber());
        latestStatus.setTransaction(transaction);

        return equipmentLatestStatusRepository.save(latestStatus);
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


        // Step1 if RC Check the rez

        final Transaction dbTransactionStatus;

        if(StringUtils.equalsIgnoreCase("RC", request.getEventCode())){
            dbTransactionStatus = transactionRepository.findByRezTrackingNumberAndStatus(request.getTrackingNum(),"A");
        }else if(StringUtils.equalsIgnoreCase("OE", request.getEventCode())){
            dbTransactionStatus = transactionRepository.findByRezTrackingNumberAndStatus(request.getTrackingNum(),"A");
        }else if(StringUtils.equalsIgnoreCase("ESA", request.getEventCode())){
            //TODO --- how to set Transaction to Pending
            dbTransactionStatus = transactionRepository.findByEquipNumberAndStatus(request.getEquipmentNumber(), "A");
        }else{
            dbTransactionStatus = transactionRepository.findByEquipNumberAndStatus(request.getEquipmentNumber(), "A");
        }

        if(dbTransactionStatus == null){

            Transaction transaction = new Transaction();

            transaction.setEquipNumber(request.getEquipmentNumber());
            transaction.setShipmentNumber(request.getShipmentNumber());
            transaction.setRezTrackingNumber(request.getTrackingNum());
            transaction.setProgramName(request.getProgramName());
            transaction.setCustRefNumber(request.getCustRefNumber());
            transaction.setStartEventCode(request.getEventCode());
            transaction.setCurrentEventCode(request.getEventCode());
            transaction.setStartDate(LocalDateTime.now().toString());

            if (StringUtils.equalsIgnoreCase("MON", request.getEventCode())) {
                transaction.setStatus("F");
            }else if(StringUtils.equalsIgnoreCase("ESA", request.getEventCode())) {
                transaction.setStatus("P");
            }else{
                transaction.setStatus("A");
            }


            return transactionRepository.save(transaction);

        }else{

            dbTransactionStatus.setEquipNumber(request.getEquipmentNumber());
            dbTransactionStatus.setRezTrackingNumber(request.getTrackingNum());
            dbTransactionStatus.setCurrentEventCode(request.getEventCode());
            dbTransactionStatus.setShipmentNumber(request.getShipmentNumber());
            dbTransactionStatus.setSegmentNumber(request.getSegmentNumber());

            return transactionRepository.save(dbTransactionStatus);
        }
    }
}


