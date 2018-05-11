
package org.kaaproject.kaa.demo.datacollection;

import org.kaaproject.kaa.client.DesktopKaaPlatformContext;
import org.kaaproject.kaa.client.Kaa;
import org.kaaproject.kaa.client.KaaClient;
import org.kaaproject.kaa.client.SimpleKaaClientStateListener;
import org.kaaproject.kaa.client.logging.strategies.RecordCountLogUploadStrategy;
import org.kaaproject.kaa.schema.sample.SamplePeriodConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;


public class Main {

    private static final Logger LOG = LoggerFactory.getLogger(Main.class);

    private static final int LOGS_DEFAULT_THRESHOLD = 1;
    private static final int MAX_SECONDS_TO_INIT_KAA = 2;
    private static final int MAX_SECONDS_BEFORE_STOP = 3;
    private static int samplePeriodInSeconds = 1;

    private static ScheduledExecutorService executor;
    private static ScheduledFuture<?> executorHandle;

    public static void main(String[] args) {

        Results results = new Results();

        LOG.info("--= Weather controller started =--");

        KaaClient kaaClient = Kaa.newClient(new DesktopKaaPlatformContext(), new SimpleKaaClientStateListener() {
            @Override
            public void onStarted() {
                LOG.info("--= Kaa client started =--");
            }

            @Override
            public void onStopped() {
                LOG.info("--= Kaa client stopped =--");
            }
        }, true);

         //Set record count strategy for uploading every log record as soon as it is created.
        kaaClient.setLogUploadStrategy(new RecordCountLogUploadStrategy(LOGS_DEFAULT_THRESHOLD));

        MeasureSender measureSender = new MeasureSender(kaaClient, LOG, results);

        kaaClient.addConfigurationListener(configuration -> {
            LOG.info("--= Endpoint configuration was updated =--");
            displayConfiguration(configuration);

            Integer newSamplePeriod = configuration.getSamplePeriod();
            if ((newSamplePeriod != null) && (newSamplePeriod > 0)) {
                changeMeasurementPeriod(newSamplePeriod, LOG, measureSender);
            } else {
                LOG.warn("Sample period value (= {}) in updated configuration is wrong, so ignore it.", newSamplePeriod);
            }
        });

        kaaClient.start();
        sleepForSeconds(MAX_SECONDS_TO_INIT_KAA);

        startMeasurement(measureSender);

        LOG.info("*** Press Enter to stop sending log records ***");
        waitForAnyInput();

        stopMeasurement();

        kaaClient.stop();
        displayResults(results);
        LOG.info("--= Data collection demo stopped =--");
    }

    private static void startMeasurement(MeasureSender measureSender) {
        executor = Executors.newSingleThreadScheduledExecutor();
        executorHandle =  executor.scheduleAtFixedRate(measureSender, 0, samplePeriodInSeconds, TimeUnit.SECONDS);
        LOG.info("--= Temperature measurement is started =--");
    }

    private static void changeMeasurementPeriod(Integer newPeriod, Logger LOG, MeasureSender measureSender) {
        if (executorHandle != null) {
            executorHandle.cancel(false);
        }
        samplePeriodInSeconds = newPeriod;
        executorHandle =  executor.scheduleAtFixedRate(measureSender, 0, samplePeriodInSeconds, TimeUnit.SECONDS);
        LOG.info("Set new sample period = {} seconds.", samplePeriodInSeconds);
    }

    private static void stopMeasurement() {
        LOG.info("Stopping measurements...");
        try {
            executor.awaitTermination(MAX_SECONDS_BEFORE_STOP, TimeUnit.SECONDS);
            executor.shutdownNow();
            LOG.info("--= Temperature measurement is finished =--");
        } catch (InterruptedException e) {
            LOG.warn("Can't stop temperature measurement correctly.", e);
        }
    }

    private static void displayConfiguration(SamplePeriodConfiguration configuration) {
        LOG.info("Configuration = {}", configuration.toString());
    }

    private static void sleepForSeconds(int seconds) {
        try {
            TimeUnit.SECONDS.sleep(seconds);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static void waitForAnyInput() {
        try {
            System.in.read();
        } catch (IOException e) {
            LOG.warn("Error happens when waiting for user input.", e);
        }
    }

    private static void displayResults(Results results) {
        LOG.info("--= Measurement summary =--");
        LOG.info("Current sample period = {} seconds", samplePeriodInSeconds);
        LOG.info("Total weather samples sent = {}", results.getSentRecordsCount());
        LOG.info("Total confirmed = {}", results.getConfirmationsCount());
    }
}
