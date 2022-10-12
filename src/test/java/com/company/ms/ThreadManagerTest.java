package com.company.ms;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;

import static org.junit.Assert.assertNull;

@RunWith(SpringRunner.class)
@SpringBootTest
public class ThreadManagerTest {

    private static final String IP = "127.0.0.1";
    private static final int PORT = 1111;
    private static final String FILE_PATH = "file_path";
    private static final long FREQ = 3;
    private static final long START_RETRY_TIME_SEC = 5;

    @Mock
    private ScheduledExecutorService scheduledExecutorService;

    @MockBean
    private SimulatorPropertyManager simulatorPropertyManager;

    @MockBean
    private ConfigurableApplicationContext applicationContext;

    @SpyBean
    private ThreadManager threadManager;

    @Before
    public void init() {
        threadManager.setScheduledExecutorService(scheduledExecutorService);
        threadManager.setApplicationContext(applicationContext);
    }

    @Test
    public void startThreadsTest() {
        Mockito.doNothing().when(threadManager).determiningCorePoolSize();
        Mockito.when(simulatorPropertyManager.getSm()).thenReturn(createSmList());
        threadManager.startThreads();
        Mockito.verify(scheduledExecutorService, Mockito.times(3)).schedule(
                (Runnable) Mockito.anyObject(), Mockito.eq(FREQ), Mockito.anyObject());
    }

    @Test
    public void incrementRetryConnectionTimeTest() {
        long expectedValue = 5;
        long actualValue = threadManager.incrementRetryConnectionTime(IP, PORT);
        Assert.assertEquals(expectedValue, actualValue);
        long expectedValueTwoTimes = expectedValue * 2;
        long actualValueTwoTimes = threadManager.incrementRetryConnectionTime(IP, PORT);
        Assert.assertEquals(expectedValueTwoTimes, actualValueTwoTimes);
        long expectedValueMax = 160;
        threadManager.getRetryConnectionList().put(IP + PORT, (long) 160);
        long actualValueMax = threadManager.incrementRetryConnectionTime(IP, PORT);
        Assert.assertEquals(expectedValueMax, actualValueMax);
    }

    @Test
    public void removeFromRetryConnectionListTest() {
        threadManager.getRetryConnectionList().put(IP + PORT, START_RETRY_TIME_SEC);
        threadManager.removeFromRetryConnectionListAfterSuccess(IP, PORT);
        assertNull(threadManager.getRetryConnectionList().get(IP + PORT));
    }

    @Test
    public void destroyAllThreads() {
        threadManager.destroyAllThreads();
        Mockito.verify(scheduledExecutorService, Mockito.times(1)).shutdown();
        Mockito.verify(applicationContext, Mockito.times(1)).close();
    }

    private List<SimulatorConfig> createSmList() {
        List<SimulatorConfig> sm = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            sm.add(createSc());
        }
        return sm;
    }

    private SimulatorConfig createSc() {
        SimulatorConfig sc = new SimulatorConfig();
        sc.setIp(IP);
        sc.setPath(FILE_PATH);
        sc.setFreq_sec(FREQ);
        sc.setPort(PORT);
        return sc;
    }
}