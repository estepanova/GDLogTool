package com.griddynamics.logtool;

import org.apache.log4j.Category;
import org.apache.log4j.spi.LoggingEvent;
import org.jboss.netty.channel.MessageEvent;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.junit.*;

import java.net.InetSocketAddress;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.mockito.Mockito.*;


public class ConsumerHandlerTest {

    private final DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss");
    private final DateTimeFormatter timeFormatter = DateTimeFormat.forPattern("HH:mm:ss");

    private Storage mockedStorage;
    private SearchServer mockedSearch;
    private ConsumerHandler testHandler;
    

    @Before
    public void init() {
        mockedStorage = mock(Storage.class);
        mockedSearch = mock(SearchServer.class);
        testHandler = new ConsumerHandler(mockedStorage, mockedSearch);
    }

    @Test
    public void testMessageRecieved() {
        long datetime = System.currentTimeMillis();
        DateTime date = new DateTime(datetime);
        String timestamp = date.toString(dateTimeFormatter);
        String message = "Test message";

        Category logger = mock(Category.class);
        LoggingEvent testEvent = new LoggingEvent(null, logger, datetime, null, message, null);
        testEvent.setProperty("application", "testApp.testInstance");

        InetSocketAddress testAddress = new InetSocketAddress("testhost", 4444);

        MessageEvent testMessage = mock(MessageEvent.class);
        when(testMessage.getRemoteAddress()).thenReturn(testAddress);
        when(testMessage.getMessage()).thenReturn(testEvent);

        testHandler.messageReceived(null, testMessage);

        String[] pathToVerify = new String[3];
        pathToVerify[0] = "testApp";
        pathToVerify[1] = "testhost";
        pathToVerify[2] = "testInstance";
        message = timeFormatter.print(datetime) + " " + message;
        verify(mockedStorage).addMessage(pathToVerify, timestamp, message);

        Map<String, String> mapToVerify = new LinkedHashMap<String, String>();
        mapToVerify.put("application", pathToVerify[0]);
        mapToVerify.put("host", pathToVerify[1]);
        mapToVerify.put("instance", pathToVerify[2]);
        verify(mockedSearch).index(mapToVerify);
    }
}
