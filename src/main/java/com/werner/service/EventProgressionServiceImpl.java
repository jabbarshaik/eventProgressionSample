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
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service("eventProgressionService")
public class EventProgressionServiceImpl implements EventProgressionService {

    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    private List<String> onNetworkEvents = Arrays.asList("OE", "SIT", "ESA", "MON", "RC");

    private List<String> offNetworkEvents = Arrays.asList("IF", "IE", "ID", "RX", "RE");

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

            for (EquipmentEventRequest request : requests) {
                Transaction transaction = null;
                Transaction existingTransaction = null;
                if (onNetworkEvents.contains(request.getEventCode())) {
                    transaction = prepareTransaction(request, "P");
                    existingTransaction = transaction;
                } else {
                    /*
                    CHECK FOR SHIPMENT NUMBER IN REQUEST
                        IF NOT EXISTS
                            GET SHIPMENT NUMBER BY BOL NO IN REZ1 REQUEST
                     */
                    /* FIND BY EQUIP NUMBER AND SHIPMENT NUMBER
                    *   IF EXISTS
                    *       STEP 1:
                    *           IF PENDING
                    *               STEP 1: FIND ALL ACTIVE TRANSACTIONS WITH EQUIPMENT NUMBER AND SHIPMENT NUMBER NOT NULL
                    *                   IF EXISTS
                    *                       MAKE THEM HISTORIC
                    *               STEP 2: FIND ALL PENDING TRANSACTIONS WITH EQUIPMENT NUMBER
                    *                           IF EXISTS
                    *                               MERGE ALL TRANSACTIONS AND DELETE ONE OF THE PENDING TRANSACTION
                    *               STEP 3: MARK TRANSACTION AS ACTIVE
                    *           IF ACTIVE
                    *              LOG EVENT
                    *              BUSINESS LOGIC TO SET ELS
                    *           IF HISTORIC
                    *               LOG EVENT
                    *   IF NOT EXISTS
                    *               STEP 1: FIND TRANSACTION BY EQUIPMENT NUMBER AND TRACKING NUM NOT NULL
                    *                   IF EXISTS
                    *                       IF PENDING
                    *                           STEP 1: UPDATE TRACKING NUMBER OR ANY FURTHER INFORAMATION ON TRANSACTION
                    *                           STEP 2: LOG EVENT
                    *                           STEP 3: BUSINESS LOGIC TO SET ELS
                    *                       IF ACTIVE
                    *                           STEP 2: LOG EVENT
                    *                           STEP 3: BUSINESS LOGIC TO SET ELS
                    *                       IF HISTORIC
                    *                           STEP 3: BUSINESS LOGIC TO SET ELS
                    *                   IF NOT EXISTS
                    *                       CREATE A NEW TRANSACTION WITH RESPECTIVE EVENT AND ACTIVE
                    *                       LOG EVENT
                    *                       BUSINESS LOGIC TO SET ELS
                    */


                    final Transaction byEquipNumberAndShipmentNumber = transactionRepository.findByEquipNumberAndShipmentNumber(request.getEquipmentNumber(), request.getShipmentNumber());

                    if (byEquipNumberAndShipmentNumber != null) {

                        if (StringUtils.equalsIgnoreCase(byEquipNumberAndShipmentNumber.getStatus(), "P")) {

                            final List<Transaction> byEquipNumberAndStatusInAndShipmentNumberNotNull = transactionRepository.findByEquipNumberAndStatusInAndShipmentNumberNotNull(request.getEquipmentNumber(), Arrays.asList("A"));

                            if (CollectionUtils.isNotEmpty(byEquipNumberAndStatusInAndShipmentNumberNotNull)) {
                                byEquipNumberAndStatusInAndShipmentNumberNotNull.forEach(t -> t.setStatus("H"));
                                transactionRepository.saveAll(byEquipNumberAndStatusInAndShipmentNumberNotNull);
                            }

                            final List<Transaction> pendingTransactionsByEquipment = transactionRepository.findByEquipNumberAndStatusInOrderByCreateDateDesc(request.getEquipmentNumber(), Arrays.asList("P"));

                            if (CollectionUtils.isNotEmpty(pendingTransactionsByEquipment)) {

                                if (CollectionUtils.size(pendingTransactionsByEquipment) <= 1) {
                                    //update the transaction

                                    existingTransaction = new Transaction();

                                    final Transaction transaction1 = pendingTransactionsByEquipment.get(0);

                                    BeanUtils.copyProperties(transaction1, existingTransaction);

                                    transaction1.setShipmentNumber(request.getShipmentNumber());
                                    transaction1.setSegmentNumber(request.getSegmentNumber());
                                    transaction1.setCurrentEventCode(request.getEventCode());
                                    transaction1.setEquipNumber(request.getEquipmentNumber());
                                    transaction1.setStatus("A");

                                    transaction = transactionRepository.save(transaction1);

                                } else if (CollectionUtils.size(pendingTransactionsByEquipment) == 2) {

                                    existingTransaction = new Transaction();

                                    final Transaction transaction1 = pendingTransactionsByEquipment.get(0);
                                    final Transaction transaction2 = pendingTransactionsByEquipment.get(1);

                                    BeanUtils.copyProperties(transaction2, transaction1);

                                    final List<StatusHistoryLog> byTransaction = statusHistoryLogRepository.findByTransaction(transaction2);


                                    if (CollectionUtils.isNotEmpty(byTransaction)) {
                                        byTransaction.forEach(statusHistoryLog -> statusHistoryLog.setTransaction(transaction1));

                                        statusHistoryLogRepository.saveAll(byTransaction);
                                    }

                                    BeanUtils.copyProperties(transaction1, existingTransaction);
                                    transactionRepository.delete(transaction2);

                                    transaction = transaction1;
                                    existingTransaction = transaction;
                                }
                            }
                        } else if (StringUtils.equalsIgnoreCase(byEquipNumberAndShipmentNumber.getStatus(), "A") || StringUtils.equalsIgnoreCase(byEquipNumberAndShipmentNumber.getStatus(), "H")) {

                            existingTransaction = new Transaction();

                            BeanUtils.copyProperties(byEquipNumberAndShipmentNumber, existingTransaction);

                            byEquipNumberAndShipmentNumber.setShipmentNumber(request.getShipmentNumber());
                            byEquipNumberAndShipmentNumber.setSegmentNumber(request.getSegmentNumber());
                            byEquipNumberAndShipmentNumber.setCurrentEventCode(request.getEventCode());
                            byEquipNumberAndShipmentNumber.setEquipNumber(request.getEquipmentNumber());

                            transaction = transactionRepository.save(byEquipNumberAndShipmentNumber);

                        }

                    } else {
                        final Transaction byEquipNumberAndRezTrackingNumberNotNull = transactionRepository.findByEquipNumberAndRezTrackingNumberNotNull(request.getEquipmentNumber());

                        if (byEquipNumberAndRezTrackingNumberNotNull != null) {
                            if (StringUtils.equalsIgnoreCase(byEquipNumberAndRezTrackingNumberNotNull.getStatus(), "P") ||
                                    StringUtils.equalsIgnoreCase(byEquipNumberAndRezTrackingNumberNotNull.getStatus(), "A") ||
                                    StringUtils.equalsIgnoreCase(byEquipNumberAndRezTrackingNumberNotNull.getStatus(), "H")) {
                                existingTransaction = new Transaction();

                                BeanUtils.copyProperties(byEquipNumberAndRezTrackingNumberNotNull, existingTransaction);

                                byEquipNumberAndRezTrackingNumberNotNull.setShipmentNumber(request.getShipmentNumber());
                                byEquipNumberAndRezTrackingNumberNotNull.setSegmentNumber(request.getSegmentNumber());
                                byEquipNumberAndRezTrackingNumberNotNull.setCurrentEventCode(request.getEventCode());
                                byEquipNumberAndRezTrackingNumberNotNull.setEquipNumber(request.getEquipmentNumber());

                                transaction = transactionRepository.save(byEquipNumberAndRezTrackingNumberNotNull);
                            }
                        } else {
                            transaction = prepareTransaction(request, "A");
                            existingTransaction = transaction;
                        }

                    }
                }

                // Save into Status History log with all the request values
                prepareStatusHistoryLog(transaction, request);

                // Step - 3 validate business rules and save into equipment latest status

                final EquipmentLatestStatus equipmentLatestStatus = prepareEquipmentLatestStatus(request, existingTransaction, transaction);

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


    private EquipmentLatestStatus prepareEquipmentLatestStatus(EquipmentEventRequest request, Transaction existingTransaction, Transaction newTransaction) {

        //step 1 : get the latest equipment status and if the event is onNetwork event save the equipment status

        EquipmentLatestStatus equipmentLatestStatus = null;

        final String[] streetAndEquipStatus = getStreetAndEquipStatus(request);

        if (streetAndEquipStatus != null) {

            if (onNetworkEvents.contains(request.getEventCode())) {

                equipmentLatestStatus = equipmentLatestStatusRepository.findByTransaction(existingTransaction);

                if (equipmentLatestStatus == null && existingTransaction.getStatus().equalsIgnoreCase("P")) {
                    return prepareEquipmentLatestStatusObject(newTransaction, request, streetAndEquipStatus[0], streetAndEquipStatus[1]);
                } else if (equipmentLatestStatus != null && ! StringUtils.equalsIgnoreCase("A", equipmentLatestStatus.getEventType())) {

                    equipmentLatestStatus.setStreetStatus(streetAndEquipStatus[0]);
                    equipmentLatestStatus.setEquipStatus(streetAndEquipStatus[1]);
                    equipmentLatestStatus.setEquipNumber(request.getEquipmentNumber());
                    equipmentLatestStatus.setEventType(request.getEventCode());
                    equipmentLatestStatus.setTransaction(newTransaction);

                    return equipmentLatestStatusRepository.save(equipmentLatestStatus);
                }


                /*if (equipmentLatestStatus == null) {

                   *//* equipmentLatestStatus.setStreetStatus(streetAndEquipStatus[0]);
                    equipmentLatestStatus.setEquipStatus(streetAndEquipStatus[1]);
                    equipmentLatestStatus.setEquipNumber(request.getEquipmentNumber());
                    equipmentLatestStatus.setEventType(request.getEventCode());

                    return equipmentLatestStatusRepository.save(equipmentLatestStatus);*//*

                } else {
                    return prepareEquipmentLatestStatusObject(existingTransaction, request, streetAndEquipStatus[0], streetAndEquipStatus[1]);
                }*/


            } else {
                //find the equip latest status with the equipment number from the request

                equipmentLatestStatus = equipmentLatestStatusRepository.findByEquipNumber(request.getEquipmentNumber());


                if ((equipmentLatestStatus != null && onNetworkEvents.contains(equipmentLatestStatus.getEventType())) ||
                        (equipmentLatestStatus != null && canUpdateEquipLatestStatus(equipmentLatestStatus, request, existingTransaction))) {

                    equipmentLatestStatus.setStreetStatus(streetAndEquipStatus[0]);
                    equipmentLatestStatus.setEquipStatus(streetAndEquipStatus[1]);
                    equipmentLatestStatus.setEventType(request.getEventCode());
                    equipmentLatestStatus.setEventDate(request.getEventDate());
                    equipmentLatestStatus.setSegmentNumber(request.getSegmentNumber());
                    equipmentLatestStatus.setTransaction(newTransaction);


                    return equipmentLatestStatusRepository.save(equipmentLatestStatus);

                } else if (equipmentLatestStatus == null) {
                    return prepareEquipmentLatestStatusObject(existingTransaction, request, streetAndEquipStatus[0], streetAndEquipStatus[1]);
                }
                return equipmentLatestStatus;
            }

        } else {
            LOGGER.error("Street and Equip Status are not found for the Event Type ::: " + request.getEventCode());
        }
        return equipmentLatestStatus;
    }

    private Boolean canUpdateEquipLatestStatus(EquipmentLatestStatus byEquipNumberLatestStatus, EquipmentEventRequest request, Transaction existingTransaction) {

        /*
            TODO -- need to check the exception cases

            1) if we get X3 check if the current event is OA from the last segment then don't update the Equipment Latest Status --- done
            2) if we get OA check if the current event is X3 from the next immediate segment then process

         */

        final Optional<SegmentOrder> previousSegmentOrder = request.getSegmentOrders().stream().filter(s -> StringUtils.equalsIgnoreCase(s.getSegmentNumber(), existingTransaction.getSegmentNumber())).findFirst();

        final Optional<SegmentOrder> requestedSegmentOrder = request.getSegmentOrders().stream().filter(s -> StringUtils.equalsIgnoreCase(s.getSegmentNumber(), request.getSegmentNumber())).findFirst();

        if (previousSegmentOrder.isPresent() && requestedSegmentOrder.isPresent()) {

            if (StringUtils.equalsIgnoreCase("X3", request.getEventCode())) {
                return validateExceptionCaseForX3(previousSegmentOrder.get(), requestedSegmentOrder.get(), byEquipNumberLatestStatus);
            } else if (StringUtils.equalsIgnoreCase("OA", request.getEventCode()) &&
                    !previousSegmentOrder.get().getPriorityOrder().equals(requestedSegmentOrder.get().getPriorityOrder())) {
                return validateExceptionCaseForOA(previousSegmentOrder.get(), requestedSegmentOrder.get(), byEquipNumberLatestStatus);
            } else if (requestedSegmentOrder.get().getPriorityOrder() > previousSegmentOrder.get().getPriorityOrder()) {
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
    private Boolean validateExceptionCaseForX3(SegmentOrder previousSegmentOrder, SegmentOrder requestedSegmentOrder, EquipmentLatestStatus byEquipNumberLatestStatus) {

        if (StringUtils.equalsIgnoreCase("OA", byEquipNumberLatestStatus.getEventType()) &&
                previousSegmentOrder.getPriorityOrder().equals(requestedSegmentOrder.getPriorityOrder() - 1)) {
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }

    /*
          if we get OA check if the current event is X3 from the next immediate segment then process
     */
    private Boolean validateExceptionCaseForOA(SegmentOrder previousSegmentOrder, SegmentOrder requestedSegmentOrder, EquipmentLatestStatus byEquipNumberLatestStatus) {


        if (StringUtils.equalsIgnoreCase("X3", byEquipNumberLatestStatus.getEventType()) &&
                requestedSegmentOrder.getPriorityOrder() == previousSegmentOrder.getPriorityOrder() - 1) {
            return Boolean.TRUE;
        }
        return Boolean.FALSE;
    }

    private String[] getStreetAndEquipStatus(EquipmentEventRequest request) {

        String preparingJoinString;

        final List<String> onNetworkRezEvents = onNetworkEvents.stream().filter(s -> !StringUtils.equalsIgnoreCase("ESA", s)).collect(Collectors.toList());

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

    private EquipmentLatestStatus prepareEquipmentLatestStatusObject(Transaction transaction, EquipmentEventRequest request, String streetStatus, String equipmentStatus) {

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

    private StatusHistoryLog prepareStatusHistoryLog(Transaction transaction, EquipmentEventRequest request) {

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

        if (streetAndEquipStatus != null) {
            historyLog.setStreetStatus(streetAndEquipStatus[0]);
            historyLog.setEquipStatus(streetAndEquipStatus[1]);
        }
        return statusHistoryLogRepository.save(historyLog);

    }

    private Transaction prepareTransaction(EquipmentEventRequest request, String transactionStatus) {


        // Step1 if RC Check the rez

        //MON -- check with only equipment

        //ESA -- check with equip + ship

        final Transaction dbTransactionStatus;

        if (StringUtils.equalsIgnoreCase("RC", request.getEventCode())) {
            dbTransactionStatus = transactionRepository.findByRezTrackingNumber(request.getTrackingNum());
        } else if (StringUtils.equalsIgnoreCase("OE", request.getEventCode())) {
            dbTransactionStatus = transactionRepository.findByRezTrackingNumber(request.getTrackingNum());
        } else if (StringUtils.equalsIgnoreCase("ESA", request.getEventCode())) {

            //TODO --- how to set Transaction to Pending

            // TODO -- change to shipment number

            dbTransactionStatus = transactionRepository.findByEquipNumberAndShipmentNumber(request.getEquipmentNumber(), request.getSegmentNumber());

            if (dbTransactionStatus != null) {

                // if transaction  is Active or Historic  make ESA as ESA ... return that transaction

                if (StringUtils.equalsIgnoreCase("A", dbTransactionStatus.getStatus()) || StringUtils.equalsIgnoreCase("H", dbTransactionStatus.getStatus())) {
                    return dbTransactionStatus;
                }

                if (StringUtils.equalsIgnoreCase("P", dbTransactionStatus.getStatus())) {
                    return dbTransactionStatus;
                }

                //Find transcation based on equipment number only
                //Step 1: if Exists
                ///if Pending/Active
                //Merge update transaction with shp number
                //step 2: if not exists
                //Do nothing

                if (StringUtils.equalsIgnoreCase("P", dbTransactionStatus.getStatus()) || StringUtils.equalsIgnoreCase("A", dbTransactionStatus.getStatus())) {
                    dbTransactionStatus.setShipmentNumber(request.getShipmentNumber());

                    return transactionRepository.save(dbTransactionStatus);
                }

            }
        } else if (StringUtils.equalsIgnoreCase("IT", request.getEventCode())) {
            dbTransactionStatus = transactionRepository.findByRezTrackingNumber(request.getTrackingNum());
        } else {
            dbTransactionStatus = transactionRepository.findByEquipNumberAndStatus(request.getEquipmentNumber(), "A");
        }

        if (dbTransactionStatus == null) {


/*
            if (StringUtils.equalsIgnoreCase("MON", request.getEventCode())) {
                transaction.setStatus("F");
            }else*/

            /*
                IF OE
                    IF EXISTS
                        LOG IT AND ELS AS PER BL
                    IF NOT EXISTS
                        FIND P/A WITH EQUIPMENT NUMBER ONLY AND SHIPMENT NOT NULL
                            IF EXISTS
                                UPDATE TRANSACTION WITH TRACKING NUMBER
                            IF NOT EXISTS
                                CREATE A NEW TRANSACTION WITH P STATUS
             */

            if (StringUtils.equalsIgnoreCase("OE", request.getEventCode())) {

                final List<Transaction> byEquipNumberAndStatusInAndShipmentNumberNotNull = transactionRepository.findByEquipNumberAndStatusInAndShipmentNumberNotNull(request.getEquipmentNumber(), Arrays.asList("A", "P"));

                if (!CollectionUtils.isEmpty(byEquipNumberAndStatusInAndShipmentNumberNotNull)) {

                    byEquipNumberAndStatusInAndShipmentNumberNotNull.forEach(t -> t.setRezTrackingNumber(request.getTrackingNum()));

                    return transactionRepository.saveAll(byEquipNumberAndStatusInAndShipmentNumberNotNull).iterator().next();
                }

            }

            /*

            IF ESA
                IF EXISTS
                    LOG IT AND ELS AS PER BL
                IF NOT EXISTS
                    FIND P WITH SHIPMENT NUMBER ONLY AND EQUIPMENT NOT NULL
                        IF EXISTS
                            CANCEL P TRANSACTION
                        IF NOT EXISTS
                            FIND P WITH EQUIPMENT NUMBER ONLY AND TRACKING NOT NULL
                                IF EXISTS
                                    UPDATE TRANSACTION WITH SHIPMENT NUMBER
                                IF NOT EXISTS
                                    CREATE A NEW TRANSACTION WITH P STATUS

             */

            if (StringUtils.equalsIgnoreCase("ESA", request.getEventCode())) {

                //if ESA received ... get all the Pending transactions on the Shipment ...if any mark then as Cancelled

                final List<Transaction> transactions = transactionRepository.findByShipmentNumberAndStatusAndEquipNumberNotNull(request.getShipmentNumber(), "P");

                if (!CollectionUtils.isEmpty(transactions)) {

                    transactions.forEach(transaction1 -> transaction1.setStatus("C"));

                    transactionRepository.saveAll(transactions);
                }


                final List<Transaction> byEquipNumberAndRezTrackingNumberNotNull = transactionRepository.findByEquipNumberAndStatusAndRezTrackingNumberNotNull(request.getEquipmentNumber(), "P");

                if (!CollectionUtils.isEmpty(byEquipNumberAndRezTrackingNumberNotNull)) {
                    byEquipNumberAndRezTrackingNumberNotNull.forEach(t -> t.setShipmentNumber(request.getShipmentNumber()));

                    return transactionRepository.saveAll(byEquipNumberAndRezTrackingNumberNotNull).iterator().next();
                }

            }

            Transaction transaction = new Transaction();

            transaction.setEquipNumber(request.getEquipmentNumber());
            transaction.setShipmentNumber(request.getShipmentNumber());
            transaction.setRezTrackingNumber(request.getTrackingNum());
            transaction.setProgramName(request.getProgramName());
            transaction.setCustRefNumber(request.getCustRefNumber());
            transaction.setStartEventCode(request.getEventCode());
            transaction.setCurrentEventCode(request.getEventCode());
            transaction.setStartDate(LocalDateTime.now().toString());


            transaction.setStatus(transactionStatus);
         /*   }else{
                transaction.setStatus("P");
            }*/


            return transactionRepository.save(transaction);

        } else {

            dbTransactionStatus.setEquipNumber(request.getEquipmentNumber());
            dbTransactionStatus.setRezTrackingNumber(request.getTrackingNum());
            dbTransactionStatus.setCurrentEventCode(request.getEventCode());
            dbTransactionStatus.setShipmentNumber(request.getShipmentNumber());
            dbTransactionStatus.setSegmentNumber(request.getSegmentNumber());

            return transactionRepository.save(dbTransactionStatus);
        }
    }
}


