package com.example.kafka;


import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.RecordMetadata;

import java.util.logging.Logger;

public class CustomCallback implements Callback {
    private int seq;

    private static final Logger logger = Logger.getLogger(CustomCallback.class.getName());

    public CustomCallback(int seq) {
        this.seq = seq;
    }

    @Override
    public void onCompletion(RecordMetadata metadata, Exception exception) {

        if (exception == null) {
            logger.info("seq:{} partition:{} offset:{}" + seq + metadata.partition() + metadata.offset());

        } else {
            logger.info("exception error from broker : " + exception.getMessage());
        }
    }
}
