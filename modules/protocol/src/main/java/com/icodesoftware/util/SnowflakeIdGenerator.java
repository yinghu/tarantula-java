package com.icodesoftware.util;

import java.time.Instant;

public class SnowflakeIdGenerator {

    private static final int BITS_OF_NODE_ID = 10;
    private static final int BITS_OF_SEQUENCE = 12;

    private static final long MAX_NODE_ID = (1L << BITS_OF_NODE_ID) - 1; //1023
    private static final long MAX_SEQUENCE = (1L << BITS_OF_SEQUENCE) - 1; //4095

    private final long nodeId;
    private final long epochStart;

    private long lastTimestamp = -1L;
    private long sequence = 0L;

    public SnowflakeIdGenerator(long nodeId,long epoch){
        if(nodeId<0 || nodeId> MAX_NODE_ID) throw new IllegalArgumentException("0-1023 range");
        this.nodeId = nodeId;
        this.epochStart = epoch;
    }

    public synchronized long snowflakeId() {
        long currentTimestamp = epochTimestamp();
        if(currentTimestamp < lastTimestamp) throw new RuntimeException("wrong system clock setting");

        if (currentTimestamp == lastTimestamp) {
            sequence = (sequence + 1) & MAX_SEQUENCE;
            if(sequence == 0){
                while (currentTimestamp == lastTimestamp){
                    currentTimestamp = epochTimestamp();
                }
            }
        }
        else {
            sequence = 0;
        }
        lastTimestamp = currentTimestamp;
        long id = currentTimestamp << (BITS_OF_NODE_ID + BITS_OF_SEQUENCE) | (nodeId << BITS_OF_SEQUENCE) | sequence;
        return id;
    }
    private long epochTimestamp() {
        return Instant.now().toEpochMilli() - epochStart;
    }

    public long[] fromSnowflakeId(long snowflakeId) {
        long nodeIdMask = ((1L << BITS_OF_NODE_ID) - 1) << BITS_OF_SEQUENCE;
        long sequenceMask = (1L << BITS_OF_SEQUENCE) - 1;
        long timestamp = (snowflakeId >> (BITS_OF_NODE_ID + BITS_OF_SEQUENCE)) + epochStart;
        long nodeId = (snowflakeId & nodeIdMask) >> BITS_OF_SEQUENCE;
        long sequence = snowflakeId & sequenceMask;
        return new long[]{timestamp, nodeId, sequence};
    }
}
