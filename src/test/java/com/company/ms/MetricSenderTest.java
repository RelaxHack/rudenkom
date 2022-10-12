package com.company.ms;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class MetricSenderTest {

    private final String IP = "127.0.0.1";
    private final int PORT = 1111;
    private final String FILE_PATH_IN = "src/test/resources/testFileIn.txt";
    private final long FREQ = 10;
    private final ConcurrentMap<String, Long> retryConnectionList = new ConcurrentHashMap<>();

    private ThreadManager threadManager;
    private MetricSender metricSender;
    private BufferedWriter bufferedWriter;
    private Socket socket;

    @Before
    public void init() throws IOException {
        threadManager = Mockito.mock(ThreadManager.class);
        metricSender = Mockito.spy(new MetricSender(IP, FILE_PATH_IN, PORT, FREQ, threadManager));
        socket = Mockito.mock(Socket.class);
        Mockito.when(socket.getOutputStream()).thenReturn(Mockito.mock(OutputStream.class));
        Mockito.doReturn(socket).when(metricSender).createSocket(Mockito.anyString(), Mockito.anyInt());
        bufferedWriter = Mockito.mock(BufferedWriter.class);
        Mockito.doReturn(bufferedWriter).when(metricSender).createSocketOutputStream(socket);
        Mockito.doReturn(retryConnectionList).when(threadManager).getRetryConnectionList();
        Mockito.doNothing().when(metricSender).nextStartThread(FREQ);
    }

    @Test
    public void pushMetricTest() throws IOException {
        metricSender.run();
        Mockito.verify(bufferedWriter, Mockito.times(1)).write("123\n");
        Mockito.verify(bufferedWriter, Mockito.times(1)).write("456\n");
        Mockito.verify(bufferedWriter, Mockito.times(1)).write("678\n");
        Mockito.verify(bufferedWriter, Mockito.times(1)).write("910\n");
        verifyNextStartThread(1);
    }

    @Test
    public void destroyTest() throws IOException, MetricSenderException {
        metricSender.startConnection();
        metricSender.destroy();
        verifyDestroy(1, 1);
        verifyNextStartThread(0);
    }

    @Test
    public void closeOutputStreamExceptionTest() throws IOException {
        Mockito.doThrow(new IOException("")).when(bufferedWriter).close();
        metricSender.run();
        verifyDestroy(1, 2);
        verifyNextStartThread(0);
    }

    @Test
    public void closeSocketConnectionExceptionTest() throws IOException {
        Mockito.doThrow(new IOException("")).when(socket).close();
        metricSender.run();
        verifyDestroy(2, 1);
        verifyNextStartThread(0);
    }

    @Test
    public void createSocketOutputStreamTest() throws IOException {
        Mockito.doThrow(IOException.class).when(metricSender).createSocketOutputStream(socket);
        try {
            metricSender.startConnection();
        } catch (MetricSenderException e) {
            verifyNextStartThread(0);
        }
    }

    @Test
    public void pushMetricWriteExceptionTest() throws IOException {
        Mockito.doThrow(new IOException("")).when(bufferedWriter).write(Mockito.anyString());
        metricSender.run();
        verifyDestroy(1, 1);
        verifyNextStartThread(0);
    }

    @Test
    public void pushMetricEmptyFileExceptionTest() throws IOException {
        metricSender = Mockito.spy(new MetricSender(IP, "src/test/resources/emptyFile.txt", PORT, FREQ, threadManager));
        Mockito.doReturn(socket).when(metricSender).createSocket(Mockito.anyString(), Mockito.anyInt());
        Mockito.doReturn(bufferedWriter).when(metricSender).createSocketOutputStream(socket);
        Mockito.doNothing().when(metricSender).nextStartThread(FREQ);
        metricSender.run();
        verifyDestroy(1, 1);
        verifyNextStartThread(0);
    }

    @Test
    public void retryConnectionTest() throws IOException, MetricSenderException {
        Mockito.doNothing().when(metricSender).nextStartThread(Mockito.anyLong());
        Mockito.doThrow(IOException.class).when(metricSender).createSocket(IP, PORT);
        try {
            metricSender.startConnection();
        } catch (SocketConnectionException e) {
            Mockito.verify(metricSender, Mockito.times(1)).nextStartThread(Mockito.anyLong());
        }
    }

    private void verifyNextStartThread(int wantedCallNextStartThread) {
        Mockito.verify(metricSender, Mockito.times(wantedCallNextStartThread)).nextStartThread(FREQ);
    }

    private void verifyDestroy(int wantedSocketClose, int wantedBufferedWriterClose) throws IOException {
        Mockito.verify(bufferedWriter, Mockito.times(wantedBufferedWriterClose)).close();
        Mockito.verify(socket, Mockito.times(wantedSocketClose)).close();
        Mockito.verify(threadManager, Mockito.times(1)).destroyAllThreads();
    }
}