package com.tarantula.platform.service.persistence;


import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class DataStoreOperationSummary {

    public static final String PENDING_UPDATE_SIZE = "pendingUpdateSize";
    public static final String PENDING_BACKUP_SIZE = "pendingBackupSize";

    public static final String LAST_DATA_BATCH_SIZE = "lastDataBatchSize";
    public static final String LAST_INTEGRATION_BATCH_SIZE = "lastIntegrationBatchSize";

    public static final  String REPLICATION_NODE_NUMBER = "replicationNodeNumber";
    public static final  String CACHE_MISS_NUMBER = "cacheMissNumber";

    public static final  String DAILY_TOTAL_UPDATES = "dailyTotalUpdates";
    public static final  String DAILY_TOTAL_BYTES_UPDATED = "dailyTotalBytesUpdated";
    public static final  String AVERAGE_BYTES_PER_UPDATE = "averageBytesPerUpdate";


    public AtomicLong dailyTotalDataUpdates = new AtomicLong(0);
    public AtomicLong dailyTotalDataBytesUpdated = new AtomicLong(0);


    public AtomicLong dailyTotalIntegrationUpdates = new AtomicLong(0);
    public AtomicLong dailyTotalIntegrationBytesUpdated = new AtomicLong(0);

    public AtomicInteger lastDataBatchSize = new AtomicInteger(0);
    public AtomicInteger lastIntegrationBatchSize = new AtomicInteger(0);

    public AtomicLong lastSyncUpdates = new AtomicLong(0);
    public AtomicLong lastEvictUpdates = new AtomicLong(0);

    public AtomicInteger pendingUpdates = new AtomicInteger(0);
    public AtomicInteger pendingBackups = new AtomicInteger(0);


}
