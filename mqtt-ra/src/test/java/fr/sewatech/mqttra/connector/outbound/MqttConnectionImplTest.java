package fr.sewatech.mqttra.connector.outbound;

import org.fusesource.mqtt.client.BlockingConnection;
import org.fusesource.mqtt.client.QoS;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class MqttConnectionImplTest {

    public static final String MESSAGE = "message";
    public static final String TOPIC_NAME = "topic";
    public static final String DEFAULT_TOPIC_NAME = "default topic";

    @InjectMocks
    MqttConnectionImpl connection;

    @Mock
    MqttManagedConnection mc;
    @Mock
    MqttConnectionRequestInfo connectionRequestInfo;
    @Mock
    BlockingConnection blockingConnection;

    @Before
    public void init() throws Exception {
        MockitoAnnotations.initMocks(this);
        when(mc.getBlockingConnection()).thenReturn(blockingConnection);
        connection.setDefaultQos(QoS.AT_LEAST_ONCE);
        connection.setDefaultTopic(DEFAULT_TOPIC_NAME);
    }

    @Test
    public void should_publish_send_message_to_connecion() throws Exception {
        connection.publish(TOPIC_NAME, MESSAGE, QoS.EXACTLY_ONCE);
        verify(blockingConnection).publish(TOPIC_NAME, MESSAGE.getBytes(), QoS.EXACTLY_ONCE, true);
    }

    @Test
    public void should_publish_send_message_to_connecion_with_default_qos() throws Exception {
        connection.publish(TOPIC_NAME, MESSAGE);
        verify(blockingConnection).publish(TOPIC_NAME, MESSAGE.getBytes(), QoS.AT_LEAST_ONCE, true);
    }

    @Test
    public void should_publish_send_message_to_connecion_on_default_topic() throws Exception {
        connection.publish(MESSAGE);
        verify(blockingConnection).publish(DEFAULT_TOPIC_NAME, MESSAGE.getBytes(), QoS.AT_LEAST_ONCE, true);
    }

    @Test
    public void should_close_close_managed_connection() throws Exception {
        connection.close();
        verify(mc).closeConnection(connection);
    }
}