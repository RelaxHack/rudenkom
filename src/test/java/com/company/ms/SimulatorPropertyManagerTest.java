package com.company.ms;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class SimulatorPropertyManagerTest {

    private final String IP = "127.0.0.1";
    private SimulatorPropertyManager simulatorPropertyManager;


    @Before
    public void init() {
        simulatorPropertyManager = new SimulatorPropertyManager();
        simulatorPropertyManager.setDefaultFreqSec("10");
    }

    @Test
    public void filterSimulatorConfigValidTest() {
        List<SimulatorConfig> correctList = new ArrayList<>();
        correctList.add(createSc(IP, true));
        correctList.add(createSc(IP, true));
        simulatorPropertyManager.setSm(correctList);
        simulatorPropertyManager.filterSimulatorConfig();
        List<SimulatorConfig> resultList = simulatorPropertyManager.getSm();
        assertEquals(2, resultList.size());
    }

    @Test
    public void filterSimulatorConfigEnableTest() {
        List<SimulatorConfig> correctList = new ArrayList<>();
        correctList.add(createSc(IP, false));
        correctList.add(createSc(IP, true));
        simulatorPropertyManager.setSm(correctList);
        simulatorPropertyManager.filterSimulatorConfig();
        List<SimulatorConfig> resultList = simulatorPropertyManager.getSm();
        assertEquals(1, resultList.size());
        assertEquals(true, resultList.get(0).isEnable());
    }

    @Test
    public void filterSimulatorConfigIncorrectConfigTest() {
        List<SimulatorConfig> incorrectList = new ArrayList<>();
        incorrectList.add(createSc(IP, true));
        incorrectList.add(createSc(null, true));
        simulatorPropertyManager.setSm(incorrectList);
        simulatorPropertyManager.filterSimulatorConfig();
        List<SimulatorConfig> resultList = simulatorPropertyManager.getSm();
        assertEquals(1, resultList.size());
        assertEquals(IP, resultList.get(0).getIp());
    }

    private SimulatorConfig createSc(String ip, boolean enable) {
        SimulatorConfig sc = new SimulatorConfig();
        sc.setIp(ip);
        sc.setPath("file_path");
        sc.setEnable(enable);
        return sc;
    }

}