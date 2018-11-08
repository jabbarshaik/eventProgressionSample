package com.werner.service;

import com.werner.model.EquipmentLatestStatus;
import com.werner.model.StatusHistoryLog;
import com.werner.model.Transaction;
import com.werner.repository.EquipmentLatestStatusRepository;
import com.werner.repository.StatusHistoryLogRepository;
import com.werner.repository.TransactionRepository;
import com.werner.util.BundledSegmentEventPriority;
import com.werner.util.DestinationBundledSegmentEventPriority;
import com.werner.util.DraySegmentEventPriority;
import com.werner.util.EventCodeMappingEnum;
import com.werner.util.OriginBundledSegmentEventPriority;
import com.werner.util.RailSegmentEventPriority;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service("eventProgressionService")
public class EventProgressionServiceImpl implements EventProgressionService {

    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    private List<String> onNetworkEvents = Arrays.asList("OE", "IT", "ESA", "MON", "RC");

    private List<String> offNetworkEvents = Arrays.asList("IF", "IE", "ID", "MOFN", "SOFN");

    private List<String> cancellationEvents = Arrays.asList("RE", "RX", "TC", "FC");


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
                if ((StringUtils.equalsIgnoreCase("RC", request.getEventCode()) && StringUtils.isBlank(request.getEquipmentNumber())) || onNetworkEvents.contains(request.getEventCode())) {
                    transaction = prepareTransaction(request);
                    existingTransaction = transaction;
                } else if (offNetworkEvents.contains(request.getEventCode())) {
                    /*
                    FIND TRANSACTIONS BY TRACKING NUMBER
                    IF EXISTS
                        UPDATE THEM AS HISTORIC
                    IF NOT EXISTS
                        FIND ACTIVE BY EQUIPMENT NUMBER AND SHIPMENT NUMBER
                           IF EXISTS
                                UPDATE THEM AS HISTORIC
                           IF NOT EXISTS
                           //TODO NOT SURE ON HOW TO HANDLE SOFN
                                CREATE TRANSACTION AS 'H'
                     */

                    final Transaction byRezTrackingNumber = transactionRepository.findByRezTrackingNumber(request.getTrackingNum());

                    if (byRezTrackingNumber != null) {

                        byRezTrackingNumber.setStatus("H");
                        byRezTrackingNumber.setEndDate(LocalDateTime.now().toString());
                        byRezTrackingNumber.setEndEventCode(request.getEventCode());

                        transaction = transactionRepository.save(byRezTrackingNumber);
                        existingTransaction = transaction;
                    } else {
                        final Transaction activeTransByEquipAndShipment = transactionRepository.findByEquipNumberAndShipmentNumberAndStatus(request.getEquipmentNumber(), request.getShipmentNumber(), "A");

                        if (activeTransByEquipAndShipment != null) {
                            activeTransByEquipAndShipment.setStatus("H");
                            activeTransByEquipAndShipment.setEndDate(LocalDateTime.now().toString());
                            activeTransByEquipAndShipment.setEndEventCode(request.getEventCode());

                            transaction = transactionRepository.save(activeTransByEquipAndShipment);
                            existingTransaction = transaction;
                        }
                    }

                } else if (cancellationEvents.contains(request.getEventCode())) {

                    transaction = processCancellationEvents(request);
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


                    final Transaction dbTransaction;

                    //TODO --- need to validate H if we get two transactions
                    // need to check with shipment number and equipment number ...
//                    final EquipmentLatestStatus byEquipNumber = equipmentLatestStatusRepository.findByEquipNumber(request.getEquipmentNumber());
//                     Optional<SegmentOrder> previousSegmentOrder = Optional.empty();
//                    if(byEquipNumber != null){
//                        previousSegmentOrder = request.getSegmentOrders().stream().filter(s -> StringUtils.equalsIgnoreCase(s.getSegmentNumber(), byEquipNumber.getSegmentNumber())).findFirst();
//                    }


                    if ((request.getSegmentType() != null && request.getSegmentType().equalsIgnoreCase("ER")) && validateERWithDrop(request)) {
                        dbTransaction = transactionRepository.findByEquipNumberAndStatus(request.getEquipmentNumber(), "A");
                    } else {
                        dbTransaction = transactionRepository.findByEquipNumberAndShipmentNumber(request.getEquipmentNumber(), request.getShipmentNumber());
                    }


                    if (dbTransaction != null) {

                        if (StringUtils.equalsIgnoreCase(dbTransaction.getStatus(), "P")) {

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
                        } else if (StringUtils.equalsIgnoreCase(dbTransaction.getStatus(), "A") || StringUtils.equalsIgnoreCase(dbTransaction.getStatus(), "H")) {

                            existingTransaction = new Transaction();

                            BeanUtils.copyProperties(dbTransaction, existingTransaction);

                            if (StringUtils.isNotEmpty(request.getShipmentNumber()) && StringUtils.isBlank(dbTransaction.getShipmentNumber())) {
                                dbTransaction.setShipmentNumber(request.getShipmentNumber());
                            }

                            if ((request.getSegmentType() != null && request.getSegmentType().equalsIgnoreCase("ER")) && validateERWithDrop(request) || StringUtils.isNotEmpty(request.getSegmentNumber()) && StringUtils.isBlank(dbTransaction.getSegmentNumber())) {
                                dbTransaction.setSegmentNumber(request.getSegmentNumber());
                            }
                            dbTransaction.setCurrentEventCode(request.getEventCode());
                            if (StringUtils.isNotEmpty(request.getEquipmentNumber()) && StringUtils.isBlank(dbTransaction.getEquipNumber())) {
                                dbTransaction.setEquipNumber(request.getEquipmentNumber());
                            }


                            transaction = transactionRepository.save(dbTransaction);

                        }

                    } else {
                        final Transaction byEquipNumberAndRezTrackingNumberNotNull = transactionRepository.findByEquipNumberAndStatusAndRezTrackingNumberNotNull(request.getEquipmentNumber(), "A");

                        if (byEquipNumberAndRezTrackingNumberNotNull != null) {
                            if (StringUtils.equalsIgnoreCase(byEquipNumberAndRezTrackingNumberNotNull.getStatus(), "P") ||
                                    StringUtils.equalsIgnoreCase(byEquipNumberAndRezTrackingNumberNotNull.getStatus(), "A") ||
                                    StringUtils.equalsIgnoreCase(byEquipNumberAndRezTrackingNumberNotNull.getStatus(), "H")) {
                                existingTransaction = new Transaction();

                                BeanUtils.copyProperties(byEquipNumberAndRezTrackingNumberNotNull, existingTransaction);

                                if (!request.getSegmentType().equalsIgnoreCase("ER")) {
                                    byEquipNumberAndRezTrackingNumberNotNull.setShipmentNumber(request.getShipmentNumber());
                                    byEquipNumberAndRezTrackingNumberNotNull.setSegmentNumber(request.getSegmentNumber());
                                }
                                byEquipNumberAndRezTrackingNumberNotNull.setCurrentEventCode(request.getEventCode());
                                byEquipNumberAndRezTrackingNumberNotNull.setEquipNumber(request.getEquipmentNumber());
                                if (StringUtils.equalsIgnoreCase(byEquipNumberAndRezTrackingNumberNotNull.getStatus(), "P")) {
                                    byEquipNumberAndRezTrackingNumberNotNull.setStatus("A");
                                }

                                transaction = transactionRepository.save(byEquipNumberAndRezTrackingNumberNotNull);
                            }
                        } else {
                            transaction = prepareTransaction(request);
                            existingTransaction = transaction;
                        }

                    }
                }

                // Save into Status History log with all the request values
                StatusHistoryLog statusHistoryLog = prepareStatusHistoryLog(transaction, request);

                // Step - 3 validate business rules and save into equipment latest status
                EquipmentLatestStatus equipmentLatestStatus = new EquipmentLatestStatus();

                if(offNetworkEvents.contains(request.getEventCode()) || StringUtils.equalsIgnoreCase("A",transaction.getStatus())){
                    equipmentLatestStatus = prepareEquipmentLatestStatus(request, existingTransaction, transaction,statusHistoryLog);
                }

                prepareResponse(response, equipmentLatestStatus);

            }


        } catch (Exception ex) {
            response.setErrorMessage("Exception while processing event, " + ex.getMessage());
            LOGGER.error("Exception ::, ", ex);
            return response;
        }

        return response;
    }

    private Transaction processCancellationEvents(EquipmentEventRequest request) {

        final Transaction transaction;

        if (Arrays.asList("RX", "RE").contains(request.getEventCode())) {
            transaction = transactionRepository.findByRezTrackingNumber(request.getTrackingNum());

            return updateTransactionWithStatus(request, transaction, StringUtils.equalsIgnoreCase("RE", request.getEventCode()) ? "E" : "C");
        } else if (StringUtils.equalsIgnoreCase("TC", request.getEventCode())) {

            transaction = transactionRepository.findByEquipNumberAndRezTrackingNumber(request.getEquipmentNumber(), request.getTrackingNum());

           return updateTransactionWithStatus(request, transaction, "C");
        } else if (StringUtils.equalsIgnoreCase("FC", request.getEventCode())) {

            transaction = transactionRepository.findByEquipNumberAndRezTrackingNumber(request.getEquipmentNumber(), request.getTrackingNum());

            /*step 1 change H to A

            remove end date and end event

            update ELS with SHL (find the status history log with segmentOrderDesc,segmentEventOrder desc)

            updateTransactionWithStatus(EquipmentEventRequest request, Transaction transaction,String status)

        */

           updateTransactionWithStatus(request, transaction, "A");


            StatusHistoryLog statusHistoryLog = statusHistoryLogRepository.findTopByTransactionAndSegmentPriorityNotNullOrderBySegmentPriorityDescSegmentEventPriorityDesc(transaction);

            EquipmentLatestStatus equipmentLatestStatus = equipmentLatestStatusRepository.findByEquipNumber(request.getEquipmentNumber());

            if (equipmentLatestStatus != null && statusHistoryLog != null) {
                equipmentLatestStatus.setStreetStatus(statusHistoryLog.getStreetStatus());
                equipmentLatestStatus.setEquipStatus(statusHistoryLog.getEquipStatus());
                equipmentLatestStatus.setEventType(statusHistoryLog.getEventType());

                equipmentLatestStatusRepository.save(equipmentLatestStatus);

            }


        }

        return  null;
    }

    private Transaction updateTransactionWithStatus(EquipmentEventRequest request, Transaction transaction, String status) {

        if (transaction != null) {

            transaction.setStatus(status);
            transaction.setEndDate(LocalDateTime.now().toString());
            transaction.setEndEventCode(request.getEventCode());

            if (StringUtils.equalsIgnoreCase("FC", request.getEventCode())) {
                transaction.setEndEventCode(null);
                transaction.setEndDate(null);
            }

            return transactionRepository.save(transaction);
        }

        return null;
    }

    private boolean validateERWithDrop(EquipmentEventRequest request) {

        final EquipmentLatestStatus byEquipNumber = equipmentLatestStatusRepository.findByEquipNumber(request.getEquipmentNumber());
        Optional<SegmentOrder> previousSegmentOrder = Optional.empty();
        if (byEquipNumber != null) {
            previousSegmentOrder = request.getSegmentOrders().stream().filter(s -> StringUtils.equalsIgnoreCase(s.getSegmentNumber(), byEquipNumber.getSegmentNumber())).findFirst();
        }


        return request.getSegmentType() != null && request.getSegmentType().equalsIgnoreCase("ER") && byEquipNumber != null &&
                (StringUtils.equalsIgnoreCase(byEquipNumber.getLoadOption(), "Drop") || (previousSegmentOrder.isPresent() && StringUtils.equalsIgnoreCase(previousSegmentOrder.get().getSegmentType(), "ER")));
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


    private EquipmentLatestStatus prepareEquipmentLatestStatus(EquipmentEventRequest request, Transaction existingTransaction, Transaction newTransaction, StatusHistoryLog statusHistoryLog) {

        //step 1 : get the latest equipment status and if the event is onNetwork event save the equipment status

        EquipmentLatestStatus equipmentLatestStatus;

        final String[] streetAndEquipStatus = StringUtils.isNotBlank(request.getEquipmentNumber()) ? getStreetAndEquipStatus(request) : null;

        StatusHistoryLog dbStatusHistoryLog = statusHistoryLogRepository.findTopByTransactionAndSegmentPriorityNotNullOrderBySegmentPriorityDescSegmentEventPriorityDesc(newTransaction);

        if (streetAndEquipStatus != null) {

            if (onNetworkEvents.contains(request.getEventCode())) {

                equipmentLatestStatus = equipmentLatestStatusRepository.findByEquipNumber(request.getEquipmentNumber());

                if (equipmentLatestStatus == null) {
                    return prepareEquipmentLatestStatusObject(newTransaction, request, streetAndEquipStatus[0], streetAndEquipStatus[1]);
                } else if (dbStatusHistoryLog == null) {

                    equipmentLatestStatus.setStreetStatus(streetAndEquipStatus[0]);
                    equipmentLatestStatus.setEquipStatus(streetAndEquipStatus[1]);
                    equipmentLatestStatus.setEquipNumber(request.getEquipmentNumber());
                    equipmentLatestStatus.setEventType(request.getEventCode());
//                    equipmentLatestStatus.setTransaction(newTransaction);
                    equipmentLatestStatus.setLoadOption(request.getLoadOption());
                    equipmentLatestStatus.setSegmentNumber(request.getSegmentNumber());


                    return equipmentLatestStatusRepository.save(equipmentLatestStatus);
                }

            } else if (offNetworkEvents.contains(request.getEventCode()) && existingTransaction != null) {

                equipmentLatestStatus = equipmentLatestStatusRepository.findByEquipNumber(request.getEquipmentNumber());

                if (equipmentLatestStatus != null) {
                    equipmentLatestStatus.setStreetStatus("OFNET");
                    equipmentLatestStatus.setEquipStatus("EMPTY");
                    equipmentLatestStatus.setEventType(request.getEventCode());


                    return equipmentLatestStatusRepository.save(equipmentLatestStatus);
                }

            } else if (cancellationEvents.contains(request.getEventCode())) {
                return new EquipmentLatestStatus();
            } else {
                //find the equip latest status with the equipment number from the request

                equipmentLatestStatus = equipmentLatestStatusRepository.findByEquipNumber(request.getEquipmentNumber());

                //equipmentLatestStatus = equipmentLatestStatusRepository.findByTransaction(existingTransaction);


                if ((equipmentLatestStatus != null && onNetworkEvents.contains(equipmentLatestStatus.getEventType())) ||
                        (equipmentLatestStatus != null && canUpdateEquipLatestStatus(equipmentLatestStatus, request,statusHistoryLog))) {

                    equipmentLatestStatus.setStreetStatus(streetAndEquipStatus[0]);
                    equipmentLatestStatus.setEquipStatus(streetAndEquipStatus[1]);
                    equipmentLatestStatus.setEventType(request.getEventCode());
                    equipmentLatestStatus.setEventDate(request.getEventDate());
                    equipmentLatestStatus.setSegmentNumber(request.getSegmentNumber());
//                    equipmentLatestStatus.setTransaction(newTransaction);
                    equipmentLatestStatus.setLoadOption(request.getLoadOption());


                    return equipmentLatestStatusRepository.save(equipmentLatestStatus);

                } else if (equipmentLatestStatus == null) {
                    return prepareEquipmentLatestStatusObject(existingTransaction, request, streetAndEquipStatus[0], streetAndEquipStatus[1]);
                }
                return equipmentLatestStatus;
            }

        } else {
            LOGGER.error("Street and Equip Status are not found for the Event Type ::: " + request.getEventCode());
            return new EquipmentLatestStatus();
        }
        return equipmentLatestStatus;
    }


    private Integer getSegmentOrder(EquipmentEventRequest request) {

        // TODO --- Find active shipment for the equipment and get all the segment details

        if (request.getSegmentNumber() != null) {
            if (StringUtils.equalsIgnoreCase("UICD", request.getEventCode())) {
                if (StringUtils.containsIgnoreCase(request.getStreetStatusDesc(), "Destination")) {
                    return request.getSegmentOrders().stream().filter(s -> StringUtils.equalsIgnoreCase(s.getSegmentType(), "D")).findFirst().get().getPriorityOrder();
                } else if (StringUtils.containsIgnoreCase(request.getStreetStatusDesc(),"Origin")) {
                    return request.getSegmentOrders().stream().filter(s -> StringUtils.equalsIgnoreCase(s.getSegmentType(), "O")).findFirst().get().getPriorityOrder();
                }
            }
            if (request.getSegmentOrders().stream().anyMatch(s -> StringUtils.equalsIgnoreCase(s.getSegmentNumber(), request.getSegmentNumber()))) {
                return request.getSegmentOrders().stream().filter(s -> StringUtils.equalsIgnoreCase(s.getSegmentNumber(), request.getSegmentNumber())).findFirst().get().getPriorityOrder();
            }
        }
        return null;
    }


    private Integer getSegmentEventOrder(EquipmentEventRequest request) {

        // TODO --- Find active shipment for the equipment and get all the segment details and active segment

        String loadOption = StringUtils.isNotBlank(request.getLoadOption()) ? request.getLoadOption() : "Live";

        List<String> keys = EventCodeMappingEnum.getKeyUsingValue(Arrays.asList(request.getStreetStatusDesc(), request.getEquipStatusDesc()));
        if (request.getSegmentNumber() != null) {
            if (StringUtils.equalsIgnoreCase("UICD", request.getEventCode())) {
                if (StringUtils.containsIgnoreCase(request.getStreetStatusDesc(),"Destination")) {
                    List<String> d = keys.stream().filter(p -> StringUtils.startsWithIgnoreCase(p, "D_") && StringUtils.endsWithIgnoreCase(p, loadOption)).collect(Collectors.toList());
                    //D_X1_DROP 3
                    //D_CD_DROP 4
                    request.setSegmentType("D");
                    List<Integer> integers = new ArrayList<>();
                    d.forEach(d1 -> {
                        String[] s = d1.split("_");
                        integers.add(DraySegmentEventPriority.getDraySegmentEventPriority(s[1]));
                    });
                    return integers.stream().mapToInt(Integer::intValue).max().isPresent() ? integers.stream().mapToInt(Integer::intValue).max().getAsInt() : null;
                } else if (StringUtils.containsIgnoreCase(request.getStreetStatusDesc(),"Origin")) {
                    List<String> d = keys.stream().filter(p -> StringUtils.startsWithIgnoreCase(p, "O_") && StringUtils.endsWithIgnoreCase(p, loadOption)).collect(Collectors.toList());
                    //D_X1_DROP 3
                    //D_CD_DROP 4
                    request.setSegmentType("O");
                    List<Integer> integers = new ArrayList<>();
                    d.forEach(d1 -> {
                        String[] s = d1.split("_");
                        integers.add(DraySegmentEventPriority.getDraySegmentEventPriority(s[1]));
                    });
                    return integers.stream().mapToInt(Integer::intValue).max().isPresent() ? integers.stream().mapToInt(Integer::intValue).max().getAsInt() : null;
                }
            } else {
                switch (request.getSegmentType()) {

                    case "O":
                    case "D":
                    case "X":
                        return DraySegmentEventPriority.getDraySegmentEventPriority(request.getEventCode());
                    case "R":
                    case "ER":
                        return RailSegmentEventPriority.getRailSegmentEventPriority(request.getEventCode());
                    case "DB":
                        return DestinationBundledSegmentEventPriority.getDestinationBundledSegmentEventPriority(request.getEventCode());
                    case "OB":
                        return OriginBundledSegmentEventPriority.getOriginBundledSegmentEventPriority(request.getEventCode());
                    case "B":
                        return BundledSegmentEventPriority.getBundledSegmentEventPriority(request.getEventCode());
                    default:
                        return null;

                }
            }
        }
        return null;
    }

    private Boolean canUpdateEquipLatestStatus(EquipmentLatestStatus byEquipNumberLatestStatus, EquipmentEventRequest request, StatusHistoryLog statusHistoryLog) {

        /*

            1) if we get X3 check if the current event is OA from the last segment then don't update the Equipment Latest Status --- done
            2) if we get OA check if the current event is X3 from the next immediate segment then process  --- done

         */

        // TODO --- need to save segment priority
        /*     if (StringUtils.isNotBlank(request.getSegmentType()) && request.getSegmentType().equalsIgnoreCase("ER")){
         *//*
            Step 1: Get ELS based on Equipment number
            Step 2: Get the previous segment order based on the segment number from step 1
             *//*
             *
             * if
             *

        }*/

        final Optional<SegmentOrder> previousSegmentOrder = request.getSegmentOrders().stream().filter(s -> StringUtils.equalsIgnoreCase(s.getSegmentNumber(), byEquipNumberLatestStatus.getSegmentNumber())).findFirst();

        final Optional<SegmentOrder> requestedSegmentOrder = request.getSegmentOrders().stream().filter(s -> StringUtils.equalsIgnoreCase(s.getSegmentNumber(), request.getSegmentNumber())).findFirst();

        /*if(request.getSegmentType() != null &&  request.getSegmentType().equalsIgnoreCase("ER") && byEquipNumberLatestStatus.getLoadOption().equalsIgnoreCase("DROP")){
            Integer existingRailEventCode = RailSegmentEventPriority.getRailSegmentEventPriority(byEquipNumberLatestStatus.getEventType());
            return RailSegmentEventPriority.getRailSegmentEventPriority(request.getEventCode()) >  (existingRailEventCode != null ? existingRailEventCode : 0);
        }*/

        if (request.getSegmentType() != null && request.getSegmentType().equalsIgnoreCase("ER") && byEquipNumberLatestStatus != null && StringUtils.equalsIgnoreCase("DROP",byEquipNumberLatestStatus.getLoadOption())) {
            if (previousSegmentOrder.isPresent()) {
                    return RailSegmentEventPriority.getRailSegmentEventPriority(request.getEventCode()) > RailSegmentEventPriority.getRailSegmentEventPriority(byEquipNumberLatestStatus.getEventType());
//                } else if (byEquipNumberLatestStatus.getLoadOption().equalsIgnoreCase("DROP")) {
//                    Integer existingRailEventCode = RailSegmentEventPriority.getRailSegmentEventPriority(byEquipNumberLatestStatus.getEventType());
//                    return RailSegmentEventPriority.getRailSegmentEventPriority(request.getEventCode()) > (existingRailEventCode != null ? existingRailEventCode : 0);
//                }
            }else {
                return Boolean.TRUE;
            }

        } else {
            if (previousSegmentOrder.isPresent() && requestedSegmentOrder.isPresent()) {

                if (StringUtils.equalsIgnoreCase("X3", request.getEventCode())) {
                    return validateExceptionCaseForX3(previousSegmentOrder.get(), requestedSegmentOrder.get(), byEquipNumberLatestStatus);
                } else if (StringUtils.equalsIgnoreCase("OA", request.getEventCode()) &&
                        !previousSegmentOrder.get().getPriorityOrder().equals(requestedSegmentOrder.get().getPriorityOrder())) {
                    return validateExceptionCaseForOA(previousSegmentOrder.get(), requestedSegmentOrder.get(), byEquipNumberLatestStatus);
                } else if (requestedSegmentOrder.get().getPriorityOrder() > previousSegmentOrder.get().getPriorityOrder()) {
                    return Boolean.TRUE;
                } else if (previousSegmentOrder.get().getPriorityOrder().equals(requestedSegmentOrder.get().getPriorityOrder())) {

                        if (RailSegmentEventPriority.getRailSegmentEventPriority(request.getEventCode()) != null
                                && Arrays.asList("R", "ER").contains(requestedSegmentOrder.get().getSegmentType()) &&
                                    RailSegmentEventPriority.getRailSegmentEventPriority(byEquipNumberLatestStatus.getEventType()) != null) {
                            return RailSegmentEventPriority.getRailSegmentEventPriority(request.getEventCode()) > RailSegmentEventPriority.getRailSegmentEventPriority(byEquipNumberLatestStatus.getEventType());
                        } else if (DraySegmentEventPriority.getDraySegmentEventPriority(request.getEventCode()) != null && Arrays.asList("O", "D", "X").contains(requestedSegmentOrder.get().getSegmentType())) {
                            return DraySegmentEventPriority.getDraySegmentEventPriority(request.getEventCode()) > DraySegmentEventPriority.getDraySegmentEventPriority(byEquipNumberLatestStatus.getEventType());
                        } else if (BundledSegmentEventPriority.getBundledSegmentEventPriority(request.getEventCode()) != null && Arrays.asList("B").contains(requestedSegmentOrder.get().getSegmentType())) {
                            return BundledSegmentEventPriority.getBundledSegmentEventPriority(request.getEventCode()) > BundledSegmentEventPriority.getBundledSegmentEventPriority(byEquipNumberLatestStatus.getEventType());
                        } else if (OriginBundledSegmentEventPriority.getOriginBundledSegmentEventPriority(request.getEventCode()) != null && Arrays.asList("OB").contains(requestedSegmentOrder.get().getSegmentType())) {
                            return OriginBundledSegmentEventPriority.getOriginBundledSegmentEventPriority(request.getEventCode()) > OriginBundledSegmentEventPriority.getOriginBundledSegmentEventPriority(byEquipNumberLatestStatus.getEventType());
                        } else if (DestinationBundledSegmentEventPriority.getDestinationBundledSegmentEventPriority(request.getEventCode()) != null && Arrays.asList("DB").contains(requestedSegmentOrder.get().getSegmentType())) {
                            return DestinationBundledSegmentEventPriority.getDestinationBundledSegmentEventPriority(request.getEventCode()) > DestinationBundledSegmentEventPriority.getDestinationBundledSegmentEventPriority(byEquipNumberLatestStatus.getEventType());
                        } else if(StringUtils.equalsIgnoreCase("UICD", request.getEventCode())){
                            if(StringUtils.equalsIgnoreCase("D", request.getSegmentType())){
                                Integer segmentEventPriority = DraySegmentEventPriority.getDraySegmentEventPriority(byEquipNumberLatestStatus.getEventType());

                                if(segmentEventPriority == null){
                                    segmentEventPriority = findSegmentEventPriorityUsingStreetAndEquipStatus(request,byEquipNumberLatestStatus.getStreetStatus(),byEquipNumberLatestStatus.getEquipStatus(),"D_");
                                }

                                return statusHistoryLog.getSegmentEventPriority() > segmentEventPriority ;
                            }else if (StringUtils.equalsIgnoreCase("O", request.getSegmentType())){

                                Integer segmentEventPriority = DraySegmentEventPriority.getDraySegmentEventPriority(byEquipNumberLatestStatus.getEventType());

                                if(segmentEventPriority == null){
                                    segmentEventPriority = findSegmentEventPriorityUsingStreetAndEquipStatus(request,byEquipNumberLatestStatus.getStreetStatus(),byEquipNumberLatestStatus.getEquipStatus(),"O_");
                                }

                                return statusHistoryLog.getSegmentEventPriority() > segmentEventPriority;
                            }
                        }




                /*if (SegmentEventPriority.getSegmentPriority(byEquipNumberLatestStatus.getEventType()) != null &&
                        SegmentEventPriority.getSegmentPriority(request.getEventCode()) != null &&
//                        SegmentEventPriority.getSegmentPriority(request.getEventCode()).compareTo(SegmentEventPriority.getSegmentPriority(byEquipNumberLatestStatus.getEventType())) >= 1 )
                        SegmentEventPriority.getSegmentPriority(request.getEventCode()) > SegmentEventPriority.getSegmentPriority(byEquipNumberLatestStatus.getEventType())) {
                        return Boolean.TRUE;
                } else {
                    return Boolean.FALSE;
                }*/
                }
            }
        }
        return Boolean.FALSE;
    }


    private Integer findSegmentEventPriorityUsingStreetAndEquipStatus(EquipmentEventRequest request,String streetStatus, String equipStatus,String segmentType){

        String loadOption  = StringUtils.isNotBlank(request.getEventCode()) ? request.getEventCode() : "Live";

        List<String> keys = EventCodeMappingEnum.getKeyUsingValue(Arrays.asList(streetStatus, equipStatus));

        List<String> d = keys.stream().filter(p -> StringUtils.startsWithIgnoreCase(p, segmentType) && StringUtils.endsWithIgnoreCase(p, loadOption)).collect(Collectors.toList());
        //D_X1_DROP 3
        //D_CD_DROP 4
        List<Integer> integers = new ArrayList<>();
        d.forEach(d1 -> {
            String[] s = d1.split("_");
            integers.add(DraySegmentEventPriority.getDraySegmentEventPriority(s[1]));
        });
        return integers.stream().mapToInt(Integer::intValue).max().isPresent() ? integers.stream().mapToInt(Integer::intValue).max().getAsInt() : null;
    }

    /*
            if we get X3 check if the current event is OA from the last segment then don't update the Equipment Latest Status --- done
     */
    private Boolean validateExceptionCaseForX3(SegmentOrder previousSegmentOrder, SegmentOrder requestedSegmentOrder, EquipmentLatestStatus byEquipNumberLatestStatus) {

        if (requestedSegmentOrder.getPriorityOrder() - 1 == 0) {
            return requestedSegmentOrder.getPriorityOrder() > previousSegmentOrder.getPriorityOrder();

        } else if (previousSegmentOrder.getPriorityOrder().equals(requestedSegmentOrder.getPriorityOrder() - 1) &&
                StringUtils.equalsIgnoreCase("OA", byEquipNumberLatestStatus.getEventType())
        ) {
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

//        final List<String> onNetworkRezEvents = onNetworkEvents.stream().filter(s -> !StringUtils.equalsIgnoreCase("ESA", s)).collect(Collectors.toList());


        if (onNetworkEvents.contains(request.getEventCode())) {
            preparingJoinString = request.getEventCode();
            if (StringUtils.equalsIgnoreCase(request.getEventCode(), "ESA")) {
                List<String> code = Arrays.asList(request.getEventCode(), request.getLoadOption());
                preparingJoinString = code.stream().collect(Collectors.joining("_"));
            }
        } else if (offNetworkEvents.contains(request.getEventCode())) {
            preparingJoinString = request.getEventCode();
        } else if (StringUtils.equalsIgnoreCase("UICD",request.getEventCode())){
            String[] streetEquipStatus = new String[2];
            streetEquipStatus[0] = request.getStreetStatusDesc();
            streetEquipStatus[1] = request.getEquipStatusDesc();
            return  streetEquipStatus;
        }else if (StringUtils.isNotEmpty(request.getLoadOption())) {
            List<String> code = Arrays.asList(request.getSegmentType(), request.getEventCode(), request.getLoadOption());
            preparingJoinString = code.stream().collect(Collectors.joining("_"));
        }else {
            if (request.getSegmentOrders().stream().anyMatch(s -> s.getSegmentType().equalsIgnoreCase("X")) && request.getEventCode().equalsIgnoreCase("OA")) {
                final Optional<SegmentOrder> first = request.getSegmentOrders().stream().filter(s -> s.getPriorityOrder().equals(2)).findFirst();
                if (first.isPresent() && first.get().getSegmentNumber().equalsIgnoreCase(request.getSegmentNumber())) {
                    //Cross town tansit
                    List<String> code = Arrays.asList("RX", request.getEventCode());
                    preparingJoinString = code.stream().collect(Collectors.joining("_"));

                } else {
                    List<String> code = Arrays.asList(request.getSegmentType(), request.getEventCode());
                    preparingJoinString = code.stream().collect(Collectors.joining("_"));
                }
            } else {
                List<String> code = Arrays.asList(request.getSegmentType(), request.getEventCode());
                preparingJoinString = code.stream().collect(Collectors.joining("_"));
            }


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
//        latestStatus.setTransaction(transaction);
        latestStatus.setLoadOption(request.getLoadOption());

        return equipmentLatestStatusRepository.save(latestStatus);
    }


    private StatusHistoryLog prepareStatusHistoryLog(Transaction transaction, EquipmentEventRequest request) {

        //need to find the segment and segment event priority order


        StatusHistoryLog historyLog = new StatusHistoryLog();

        historyLog.setTransaction(transaction);

        historyLog.setSegmentPriority(getSegmentOrder(request));
        if (request.getSegmentType() != null || StringUtils.equalsIgnoreCase("UICD", request.getEventCode())) {
            historyLog.setSegmentEventPriority(getSegmentEventOrder(request));
        }

        if (StringUtils.isNotBlank(request.getEquipmentNumber())) {
            historyLog.setEquipNumber(request.getEquipmentNumber());
        }

        historyLog.setEventDate(request.getEventDate());

        if (StringUtils.isNotBlank(request.getTrackingNum())) {
            historyLog.setTrackingNumber(request.getTrackingNum());
        }

        historyLog.setLoadOption(request.getLoadOption());

        if (StringUtils.isNotBlank(request.getEventCarrier())) {

            historyLog.setEventCarrier(request.getEventCarrier());
        }

        if (StringUtils.isNotBlank(request.getEventSplc())) {

            historyLog.setEventSplc(request.getEventSplc());
        }

        historyLog.setEventType(request.getEventCode());

        if (StringUtils.isNotBlank(request.getShipmentNumber())) {

            historyLog.setShipmentNumber(request.getShipmentNumber());
        }

        if (StringUtils.isNotBlank(request.getSegmentNumber())) {

            historyLog.setSegmentNumber(request.getSegmentNumber());
        }

        String[] streetAndEquipStatus = getStreetAndEquipStatus(request);

        if (streetAndEquipStatus != null) {
            historyLog.setStreetStatus(streetAndEquipStatus[0]);
            historyLog.setEquipStatus(streetAndEquipStatus[1]);
        }
        return statusHistoryLogRepository.save(historyLog);

    }


    private Transaction validateESAforTransaction(EquipmentEventRequest request) {
        /*
            IF SEGMENT TYPE IS ER
                STEP 1: FIND THE EQUIPMENT LOAD OPTION FROM ELS
                IF DROP
                    FIND ACTIVE TRANSACTION BY EQUIPMENT NUMBER
                ELSE
                    FIND ACTIVE TRANSACTION BY EQUIPMENT AND SHIPMENT NUMBER
             */
        if (StringUtils.isNotBlank(request.getSegmentType()) && request.getSegmentType().equalsIgnoreCase("ER")) {
            final EquipmentLatestStatus byEquipNumber = equipmentLatestStatusRepository.findByEquipNumber(request.getEquipmentNumber());
            if (byEquipNumber.getLoadOption().equalsIgnoreCase("DROP")) {
                return transactionRepository.findByEquipNumberAndStatus(request.getEquipmentNumber(), "A");
            } else {
                return transactionRepository.findByEquipNumberAndShipmentNumber(request.getEquipmentNumber(), request.getShipmentNumber());
            }
        } else {
            return transactionRepository.findByEquipNumberAndShipmentNumber(request.getEquipmentNumber(), request.getShipmentNumber());
        }
    }

    private Transaction prepareTransaction(EquipmentEventRequest request) {

        final Transaction dbTransactionStatus;

        if (Arrays.asList("RC", "OE", "IT").contains(request.getEventCode())) {
            dbTransactionStatus = transactionRepository.findByRezTrackingNumber(request.getTrackingNum());
        } else if (StringUtils.equalsIgnoreCase("ESA", request.getEventCode())) {
            dbTransactionStatus = validateESAforTransaction(request);
        } else {
            dbTransactionStatus = transactionRepository.findByEquipNumberAndStatus(request.getEquipmentNumber(), "A");
        }

        if (dbTransactionStatus == null) {

            final Transaction byEquipNumberAndStatusInAndShipmentNumberNotNull = validateOEWhenInitialTransactionIsNull(request);

            if (byEquipNumberAndStatusInAndShipmentNumberNotNull != null)
                return byEquipNumberAndStatusInAndShipmentNumberNotNull;


            final Transaction byEquipNumberAndRezTrackingNumberNotNull = validateESAWhenInitialTransactionIsNull(request);

            if (byEquipNumberAndRezTrackingNumberNotNull != null) {
                return byEquipNumberAndRezTrackingNumberNotNull;
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
            transaction.setStatus("A");

            return transactionRepository.save(transaction);

        } else {

            dbTransactionStatus.setEquipNumber(request.getEquipmentNumber());

            if (validateInputRequest(request.getTrackingNum(), dbTransactionStatus.getRezTrackingNumber())) {
                dbTransactionStatus.setRezTrackingNumber(request.getTrackingNum());
            }

            dbTransactionStatus.setCurrentEventCode(request.getEventCode());

            if (validateInputRequest(request.getShipmentNumber(), dbTransactionStatus.getShipmentNumber())) {
                dbTransactionStatus.setShipmentNumber(request.getShipmentNumber());
            }

            if (validateInputRequest(request.getSegmentNumber(), dbTransactionStatus.getSegmentNumber())) {
                dbTransactionStatus.setSegmentNumber(request.getSegmentNumber());
            }

            return transactionRepository.save(dbTransactionStatus);
        }
    }


    private Boolean validateInputRequest(String inputRequestValue, String existingValue) {
        return StringUtils.isNotEmpty(inputRequestValue) && StringUtils.isBlank(existingValue);
    }


    /*
              IF OE WITH NEW TRACKING NUM
                  IF EXISTS
                      LOG IT AND ELS AS PER BL
                  IF NOT EXISTS
                      STEP 1: FIND P/A WITH EQUIPMENT NUMBER ONLY AND SHIPMENT NOT NULL
                          IF EXISTS
                              UPDATE TRANSACTION WITH TRACKING NUMBER
                          IF NOT EXISTS
                              CREATE A NEW TRANSACTION WITH P STATUS

                              //TODO --- Assuming that older transaction with same equipment will be closed by off network  events
           */

    private Transaction validateOEWhenInitialTransactionIsNull(EquipmentEventRequest request) {

        if (StringUtils.equalsIgnoreCase("OE", request.getEventCode())) {

            final List<Transaction> byEquipNumberAndStatusInAndShipmentNumberNotNull = transactionRepository.findByEquipNumberAndStatusInAndShipmentNumberNotNull(request.getEquipmentNumber(), Arrays.asList("A", "P"));

            if (CollectionUtils.isNotEmpty(byEquipNumberAndStatusInAndShipmentNumberNotNull)) {

                byEquipNumberAndStatusInAndShipmentNumberNotNull.forEach(t -> t.setRezTrackingNumber(request.getTrackingNum()));

                return transactionRepository.saveAll(byEquipNumberAndStatusInAndShipmentNumberNotNull).iterator().next();
            }

        }
        return null;
    }


     /*
            IF ESA
                IF EXISTS
                    LOG IT AND ELS AS PER BL
                IF NOT EXISTS
                   STEP 1:  FIND P WITH SHIPMENT NUMBER ONLY AND EQUIPMENT NOT NULL
                                IF EXISTS
                                    CANCEL P TRANSACTION
                                IF NOT EXISTS
                                    FIND P WITH EQUIPMENT NUMBER ONLY AND TRACKING NOT NULL
                                        IF EXISTS
                                               UPDATE TRANSACTION WITH SHIPMENT NUMBER
                    STEP 2: FIND A WITH EQUIPMENT ONLY
                        MARK THEM AS HISTORIC
                    STEP 3: CREATE A NEW TRANSACTION WITH P STATUS
             */

    private Transaction validateESAWhenInitialTransactionIsNull(EquipmentEventRequest request) {

        if (StringUtils.equalsIgnoreCase("ESA", request.getEventCode())) {

            //if ESA received ... get all the Pending transactions on the Shipment ...if any mark then as Cancelled


            // find by equipment and shipment is null and rez tracking is null

            Transaction transaction = transactionRepository.findByShipmentNumberAndStatus(request.getShipmentNumber(), "A");

            if(transaction == null){

                transaction = transactionRepository.findByEquipNumberAndStatusAndShipmentNumberIsNullAndRezTrackingNumberIsNotNull(request.getEquipmentNumber(), "A");

                if(transaction != null){
                    transaction.setShipmentNumber(request.getShipmentNumber());
                    transaction.setCurrentEventCode(request.getEventCode());

                    return  transactionRepository.save(transaction);
                }else {

                    final Transaction byEquipNumberAndStatus = transactionRepository.findByEquipNumberAndStatus(request.getEquipmentNumber(), "A");

                    if (byEquipNumberAndStatus != null) {
                        byEquipNumberAndStatus.setStatus("H");

                        transactionRepository.save(byEquipNumberAndStatus);
                    }
                }

            }else{
                /*
                Step 1:
                    Step 1.1: Update transaction equipment number with new equipment number
                    Step 1.2: Find Status history logs based on transaction and update equipment number
                    Step 1.3: find the highest event of the transaction from status history log and update the new equipment status
                Step 2: Find the latest E1 historic transaction() find by equipment number and status as H
                    Yes:
                        2.1 Check for End dates
                            No: Mark transaction as A
                        2.2 Update equipment to its latest statues based on status history log of the transaction
                    No:
                        2.1 Mark E1 equipment as IF as its ELS
                 */

                Transaction existingTransaction = new Transaction();
                BeanUtils.copyProperties(transaction,existingTransaction);


                transaction.setEquipNumber(request.getEquipmentNumber());
                transaction.setCurrentEventCode(request.getEventCode());

                Transaction savedTransaction = transactionRepository.save(transaction);

                List<StatusHistoryLog> statusHistoryLogs = statusHistoryLogRepository.findByTransaction(transaction);


                if(CollectionUtils.isNotEmpty(statusHistoryLogs)){

                    statusHistoryLogs.stream().forEach(statusHistoryLog -> statusHistoryLog.setEquipNumber(request.getEquipmentNumber()));

                    statusHistoryLogRepository.saveAll(statusHistoryLogs);

                    EquipmentLatestStatus equipmentLatestStatus = equipmentLatestStatusRepository.findByEquipNumber(request.getEquipmentNumber());

                        StatusHistoryLog statusHistoryLog = statusHistoryLogRepository.findTopByTransactionAndSegmentPriorityNotNullOrderBySegmentPriorityDescSegmentEventPriorityDesc(transaction);

                        if(statusHistoryLog != null){
                            if (equipmentLatestStatus != null){
                                equipmentLatestStatus.setStreetStatus(statusHistoryLog.getStreetStatus());
                                equipmentLatestStatus.setEquipStatus(statusHistoryLog.getEquipStatus());
                                equipmentLatestStatus.setEventType(statusHistoryLog.getEventType());

                                equipmentLatestStatusRepository.save(equipmentLatestStatus);
                            }else{
                                EquipmentLatestStatus latestStatus = new EquipmentLatestStatus();

                                latestStatus.setEquipNumber(statusHistoryLog.getEquipNumber());
                                latestStatus.setEquipStatus(statusHistoryLog.getEquipStatus());
                                latestStatus.setStreetStatus(statusHistoryLog.getStreetStatus());
                                latestStatus.setEventType(statusHistoryLog.getEventType());
                                latestStatus.setLoadOption(request.getLoadOption());
                                latestStatus.setSegmentNumber(statusHistoryLog.getSegmentNumber());

                                equipmentLatestStatusRepository.save(latestStatus);
                            }

                        }


                }


                Transaction historicTransaction = transactionRepository.findByEquipNumberAndStatusIn(existingTransaction.getEquipNumber(), Arrays.asList("A", "H"));

                if(historicTransaction != null){

                    if(StringUtils.isBlank(historicTransaction.getEndDate())){

                        historicTransaction.setStatus("A");
                        transactionRepository.save(historicTransaction);


                        StatusHistoryLog statusHistoryLog = statusHistoryLogRepository.findTopByTransactionAndSegmentPriorityNotNullOrderBySegmentPriorityDescSegmentEventPriorityDesc(historicTransaction);

                        if(statusHistoryLog != null){

                            EquipmentLatestStatus existingEquipmentLatestStatus = equipmentLatestStatusRepository.findByEquipNumber(existingTransaction.getEquipNumber());

                            if(existingEquipmentLatestStatus != null){

                                existingEquipmentLatestStatus.setStreetStatus(statusHistoryLog.getStreetStatus());
                                existingEquipmentLatestStatus.setEquipStatus(statusHistoryLog.getEquipStatus());

                                equipmentLatestStatusRepository.save(existingEquipmentLatestStatus);
                            }

                        }
                    }

                }else{

                    //Mark equipment has OFNET

                    EquipmentLatestStatus existingEquipmentLatestStatus = equipmentLatestStatusRepository.findByEquipNumber(existingTransaction.getEquipNumber());

                    if(existingEquipmentLatestStatus != null){

                        existingEquipmentLatestStatus.setEquipStatus("EMPTY");
                        existingEquipmentLatestStatus.setStreetStatus("OFNET");

                        equipmentLatestStatusRepository.save(existingEquipmentLatestStatus);
                    }
                }

                return savedTransaction;


            }



/*
            final List<Transaction> transactions = transactionRepository.findByShipmentNumberAndStatusAndEquipNumberNotNull(request.getShipmentNumber(), "A");

            if (!CollectionUtils.isEmpty(transactions)) {

                transactions.forEach(transaction1 -> transaction1.setStatus("C"));

                transactionRepository.saveAll(transactions);
            } else {

               Transaction byEquipNumberAndRezTrackingNumberNotNull = transactionRepository.findByEquipNumberAndStatusAndRezTrackingNumberNotNull(request.getEquipmentNumber(), "A");

                if (byEquipNumberAndRezTrackingNumberNotNull != null) {

                        byEquipNumberAndRezTrackingNumberNotNull.setShipmentNumber(request.getShipmentNumber());
                        byEquipNumberAndRezTrackingNumberNotNull.setCurrentEventCode(request.getEventCode());

                    return transactionRepository.save(byEquipNumberAndRezTrackingNumberNotNull);
                }
            }

            final Transaction byEquipNumberAndStatus = transactionRepository.findByEquipNumberAndStatus(request.getEquipmentNumber(), "A");

            if (byEquipNumberAndStatus != null) {
                byEquipNumberAndStatus.setStatus("H");

                transactionRepository.save(byEquipNumberAndStatus);
            }*/
        }
        return null;
    }
}


