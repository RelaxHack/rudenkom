package com.company.ms;

import ch.qos.logback.classic.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.Socket;
import java.util.concurrent.TimeUnit;

/**
 * Sends metrics to server socket
 * Runnable implement
 * Client part socket connection
 */
public class MetricSender implements Runnable {

    private static final Logger logger = (Logger) LoggerFactory.getLogger(MetricSender.class);
    private final String ip;
    private final int port;
    private final long freq_sec;
    private final ThreadManager threadManager;
    private final String filePath;
    private Socket clientSocket;
    private BufferedWriter socketOutputStream;


    /**
     * Constructs a new thread with required values
     *
     * @param ip            IP for socket connection with server
     * @param filePath      metric simulator file path
     * @param port          port for socket connection with server
     * @param freq_sec      simulator send frequency
     * @param threadManager needed for destroy all threads when an error occurs
     */
    public MetricSender(String ip, String filePath, int port, long freq_sec
            , ThreadManager threadManager) {
        this.ip = ip;
        this.filePath = filePath;
        this.port = port;
        this.freq_sec = freq_sec;
        this.threadManager = threadManager;
    }

    /**
     * Implementation condition Runnable
     * Sends metrics from metric simulator file to server socket
     */
    @Override
    public void run() {
        try {
            startConnection();
            pushMetric(socketOutputStream);
            stopConnection();
            nextStartThread(freq_sec);
        } catch (SocketConnectionException e) {
            logger.error("Failed connect to socket", e);
        } catch (Exception e) {
            logger.error("Metric Sender error", e);
            destroy();
        }
    }

    /**
     * Reads metric simulator file and pushes it in output stream
     *
     * @param socketOutputStream output stream to write to it
     * @throws MetricSenderException if an push metric error occurs
     */
    private void pushMetric(BufferedWriter socketOutputStream) throws MetricSenderException {
        try (BufferedReader inputFile = new BufferedReader(new FileReader(filePath))) {
            String lineContents = inputFile.readLine();
            if (lineContents == null) {
                logger.error("File {} is empty. IP simulator - {}", filePath, ip);
                throw new MetricSenderException("File is empty " + filePath);
            }
            while (lineContents != null) {
                logger.debug("Write to socket: {}", lineContents);
                socketOutputStream.write(lineContents + "\n");
                socketOutputStream.flush();
                lineContents = inputFile.readLine();
            }
        } catch (Exception e) {
            logger.error("Push metric error. Metric simulator file - {}. IP simulator - {}", filePath, ip, e);
            throw new MetricSenderException("Push metric error", e);
        }
    }

    /**
     * Starts connection with server and opens output stream
     *
     * @throws MetricSenderException if an start connection error occurs
     */
    protected void startConnection() throws MetricSenderException {
        try {
            clientSocket = createSocket(ip, port);
            socketOutputStream = createSocketOutputStream(clientSocket);
            threadManager.removeFromRetryConnectionListAfterSuccess(ip, port);
        } catch (IOException e) {
            if (clientSocket == null) {
                logger.error("Failed connect to socket. IP simulator - {}:{}", ip, port);
                retryConnection();
                throw new SocketConnectionException("Failed connect to socket. IP simulator - " + ip + ":" + port);
            } else {
                throw new MetricSenderException("Create socket output stream error", e);
            }
        }
    }

    /**
     * Closes connection with server and output stream
     *
     * @throws MetricSenderException if an stop connection error occurs
     */
    private void stopConnection() throws MetricSenderException {
        closeClientSocket();
        closeSocketOutputStream();
    }

    /**
     * Trying to close the socket
     *
     * @throws MetricSenderException if an close socket error occurs
     */
    private void closeClientSocket() throws MetricSenderException {
        if (clientSocket != null) {
            try {
                clientSocket.close();
            } catch (IOException e) {
                logger.error("Cannot close socket", e);
                throw new MetricSenderException("Cannot close socket", e);
            }
        }
    }

    /**
     * Trying to close the output stream
     *
     * @throws MetricSenderException if an close output stream error occurs
     */
    private void closeSocketOutputStream() throws MetricSenderException {
        if (socketOutputStream != null) {
            try {
                socketOutputStream.close();
            } catch (IOException e) {
                logger.error("Cannot close socket output stream", e);
                throw new MetricSenderException("Cannot close socket output stream", e);
            }
        }
    }

    /**
     * Retries connection on failure
     */
    private void retryConnection() {
        long nextAttemptRetryConnectionSec = threadManager.incrementRetryConnectionTime(ip, port);
        logger.warn("IP simulator - {}:{} - retry connection after {} seconds", ip, port, nextAttemptRetryConnectionSec);
        nextStartThread(nextAttemptRetryConnectionSec);
    }

    /**
     * Continues thread chain.
     *
     * @param delay the time from now to delay execution
     */
    protected void nextStartThread(long delay) {
        threadManager.getScheduledExecutorService().schedule(new MetricSender(ip, filePath, port, freq_sec, threadManager),
                delay, TimeUnit.SECONDS);
    }

    /**
     * Destroys all threads and stops connection.
     */
    protected void destroy() {
        try {
            //stopConnection();
            closeSocketOutputStream();
            closeClientSocket();
        } catch (Exception e) {
            logger.error("Close connection error", e);
        }
        threadManager.destroyAllThreads();
    }

    protected Socket createSocket(String ip, int port) throws IOException {
        return new Socket(ip, port);
    }

    protected BufferedWriter createSocketOutputStream(Socket clientSocket) throws IOException {
        return new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
    }

}
