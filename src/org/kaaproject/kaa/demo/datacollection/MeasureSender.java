package org.kaaproject.kaa.demo.datacollection;

import org.kaaproject.kaa.client.KaaClient;
import org.kaaproject.kaa.client.logging.BucketInfo;
import org.kaaproject.kaa.client.logging.RecordInfo;
import org.kaaproject.kaa.client.logging.future.RecordFuture;
import org.kaaproject.kaa.schema.sample.Weather;
import org.slf4j.Logger;
import java.util.*;


public class MeasureSender implements Runnable {

    private final org.slf4j.Logger LOG;
    private Random rand = new Random();
    private final int MIN_TEMPERATURE = -25;
    private final int MAX_TEMPERATURE = 45;
    private Results results;

    private KaaClient kaaClient;

    MeasureSender(KaaClient kaaClient, Logger LOG, Results results) {
        this.kaaClient = kaaClient;
        this.LOG = LOG;
        this.results = results;
    }

    @Override
    public void run() {
        results.incrementSendRecordCount();
        Weather record = generateTemperatureSample();
        RecordFuture future = kaaClient.addLogRecord(record); // submit log record for sending to Kaa node
        LOG.info("Log record {} submitted for sending", record.toString());
        try {
            RecordInfo recordInfo = future.get(); // wait for log record delivery error
            BucketInfo bucketInfo = recordInfo.getBucketInfo();
            LOG.info("Received log record delivery info. Bucket Id [{}]. Record delivery time [{} ms].",
                    bucketInfo.getBucketId(), recordInfo.getRecordDeliveryTimeMs());
            results.incrementConfirmationCount();
        } catch (Exception e) {
            LOG.error("Exception was caught while waiting for log's delivery report.", e);
        }
    }

    private Weather generateTemperatureSample() {
        Integer temperature = MIN_TEMPERATURE + rand.nextInt((MAX_TEMPERATURE - MIN_TEMPERATURE) + 1);
        return new Weather(temperature, temperature, temperature, "sunny");
    }

}
