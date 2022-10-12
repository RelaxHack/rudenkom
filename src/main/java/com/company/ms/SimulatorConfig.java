package com.company.ms;

/**
 * Create simulator metrics objects
 */
public class SimulatorConfig {
    private String path;
    private String ip;
    private int port;
    private boolean enable;
    private long freq_sec;

    /**
     * Gets metric simulator file path
     *
     * @return Path to simulator file
     */
    public String getPath() {
        return path;
    }

    /**
     * Sets metric simulator file path set in the config file
     *
     * @param path Path to simulator file
     */
    public void setPath(String path) {
        this.path = path;
    }

    /**
     * Gets IP where will simulator go
     *
     * @return IP address of simulator
     */
    public String getIp() {
        return ip;
    }

    /**
     * Sets simulator sending IP set in the config file
     *
     * @param ip IP address of simulator
     */
    public void setIp(String ip) {
        this.ip = ip;
    }

    /**
     * Gets port where will simulator go
     *
     * @return Port of simulator
     */
    public int getPort() {
        return port;
    }

    /**
     * Sets simulator sending port set in the config file
     *
     * @param port Port of simulator
     */
    public void setPort(int port) {
        this.port = port;
    }

    /**
     * Checks the inclusion marker the simulator into operation
     *
     * @return Inclusion marker of simulator
     */
    public boolean isEnable() {
        return enable;
    }

    /**
     * Sets inclusion marker set in the config file
     *
     * @param enable Inclusion marker of simulator
     */
    public void setEnable(boolean enable) {
        this.enable = enable;
    }

    /**
     * Gets socket sending frequency
     *
     * @return Simulator send frequency
     */
    public long getFreq_sec() {
        return freq_sec;
    }

    /**
     * Sets socket sending frequency set in the config file
     *
     * @param freq_sec Simulator send frequency
     */
    public void setFreq_sec(long freq_sec) {
        this.freq_sec = freq_sec;
    }
}
