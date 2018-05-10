package org.kaaproject.kaa.demo.datacollection;

import java.util.concurrent.atomic.AtomicInteger;

public class Results {
    private volatile AtomicInteger sentRecordsCount = new AtomicInteger(0);
    private volatile AtomicInteger confirmationsCount = new AtomicInteger(0);

    public void incrementSendRecordCount(){
        sentRecordsCount.incrementAndGet();
    }

    public void incrementConfirmationCount(){
        confirmationsCount.incrementAndGet();
    }

    public AtomicInteger getSentRecordsCount() {
        return sentRecordsCount;
    }

    public AtomicInteger getConfirmationsCount() {
        return confirmationsCount;
    }

}
