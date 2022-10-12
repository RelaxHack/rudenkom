package com.company.ms;

import ch.qos.logback.classic.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.concurrent.*;

/**
 * Creates threads for sending metrics and destroy these threads when an error occurs
 */
@Service
public class ThreadManager {

    private static final Logger logger = (Logger) LoggerFactory.getLogger(ThreadManager.class);
    private final ConcurrentMap<String, Long> retryConnectionList = new ConcurrentHashMap<>();
    private static final int MAX_CORE_POOL_SIZE = 10;
    private static final long START_RETRY_TIME_SEC = 5;
    private static final long MAX_RETRY_TIME_SEC = 160;
    private ScheduledExecutorService scheduledExecutorService;

    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @Autowired
    private SimulatorPropertyManager simulatorPropertyManager;

    /**
     * Creates MetricSender threads, with fixed core pool size
     */
    @PostConstruct
    public void startThreads() {
        determiningCorePoolSize();
        for (SimulatorConfig sm : simulatorPropertyManager.getSm()) {
            runThread(sm);
        }
    }

    /**
     * Destroys all threads when an error occurs
     */
    @PreDestroy
    public void destroyAllThreads() {
        if (!scheduledExecutorService.isShutdown()) {
            scheduledExecutorService.shutdown();
            applicationContext.close();
        }
        logger.info("Application close");
    }

    /**
     * Sets or increments delay for the next attempt to connect and return it
     *
     * @param ip   IP address of simulator
     * @param port port of simulator
     * @return delay for the next attempt to connect
     */
    public long incrementRetryConnectionTime(String ip, int port) {
        if (retryConnectionList.get(ip + port) != null) {
            if (retryConnectionList.get(ip + port) < MAX_RETRY_TIME_SEC) {
                retryConnectionList.put(ip + port, retryConnectionList.get(ip + port) * 2);
            }
        } else {
            retryConnectionList.put(ip + port, START_RETRY_TIME_SEC);
        }
        return retryConnectionList.get(ip + port);
    }

    /**
     * Removes configs from retry connection list after success connect
     *
     * @param ip   IP address of simulator
     * @param port port of simulator
     */
    public void removeFromRetryConnectionListAfterSuccess(String ip, int port) {
        if (retryConnectionList.get(ip + port) != null) {
            retryConnectionList.remove(ip + port);
            logger.info("Connect success. IP simulator - {}:{}. ", ip, port);
        }
    }

    /**
     * Determining core pool size depending on the number of configurations
     */
    protected void determiningCorePoolSize() {
        if (simulatorPropertyManager.getSm().size() <= MAX_CORE_POOL_SIZE) {
            scheduledExecutorService = Executors.newScheduledThreadPool(simulatorPropertyManager.getSm().size());
        } else {
            scheduledExecutorService = Executors.newScheduledThreadPool(MAX_CORE_POOL_SIZE);
        }
    }

    /**
     * Launches thread with certain parameters
     *
     * @param sm configuration parameters
     */
    private void runThread(SimulatorConfig sm) {
        scheduledExecutorService.schedule(new MetricSender(sm.getIp(), sm.getPath(), sm.getPort()
                        , sm.getFreq_sec(), this),
                sm.getFreq_sec(), TimeUnit.SECONDS);
    }

    public ScheduledExecutorService getScheduledExecutorService() {
        return scheduledExecutorService;
    }

    public ConcurrentMap<String, Long> getRetryConnectionList() {
        return retryConnectionList;
    }

    protected void setScheduledExecutorService(ScheduledExecutorService scheduledExecutorService) {
        this.scheduledExecutorService = scheduledExecutorService;
    }

    protected void setApplicationContext(ConfigurableApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

}
