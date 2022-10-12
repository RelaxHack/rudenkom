package com.company.ms;

import ch.qos.logback.classic.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;

/**
 * Loads simulator metrics parameters from configuration and filter them
 */
@Component
@ConfigurationProperties("app")
public class SimulatorPropertyManager {

    private static final Logger logger = (Logger) LoggerFactory.getLogger(SimulatorPropertyManager.class);
    private static final String DEFAULT_FREQ_SEC = "10";
    private List<SimulatorConfig> sm;

    @Value("${default_freq_sec:" + DEFAULT_FREQ_SEC + "}")
    public String defaultFreqSec;

    /**
     * Gets metric simulator List
     *
     * @return Simulators metric list
     */
    public List<SimulatorConfig> getSm() {
        return sm;
    }

    /**
     * Sets metric simulators from config List
     *
     * @param sm Simulators metric list
     */
    public void setSm(List<SimulatorConfig> sm) {
        this.sm = sm;
    }

    /**
     * Filters simulators metric list from invalid parameters
     */
    @PostConstruct
    public void filterSimulatorConfig() {
        if (defaultFreqSec.equals("")) {
            defaultFreqSec = DEFAULT_FREQ_SEC;
        }
        List<SimulatorConfig> smList = getSm();
        List<SimulatorConfig> filteredSmList = new ArrayList<>();
        for (SimulatorConfig sm : smList) {
            if (sm.isEnable()
                    && sm.getIp() != null
                    && sm.getPath() != null) {
                sm.setFreq_sec(sm.getFreq_sec() != 0 ? sm.getFreq_sec() : Long.parseLong(defaultFreqSec));
                filteredSmList.add(sm);
                logger.debug("SM configuration with IP {} is processed", sm.getIp());
            }
        }
        setSm(filteredSmList);
    }

    protected void setDefaultFreqSec(String defaultFreqSec) {
        this.defaultFreqSec = defaultFreqSec;
    }

}
