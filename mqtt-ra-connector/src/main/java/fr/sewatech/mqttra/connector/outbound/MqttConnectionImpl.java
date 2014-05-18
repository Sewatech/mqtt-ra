package fr.sewatech.mqttra.connector.outbound;

import fr.sewatech.mqttra.api.MqttConnection;
import org.fusesource.mqtt.client.BlockingConnection;
import org.fusesource.mqtt.client.MQTT;
import org.fusesource.mqtt.client.QoS;

import javax.resource.ResourceException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Alexis Hassler
 */
public class MqttConnectionImpl implements MqttConnection {
    private static final Logger logger = Logger.getLogger(MqttConnectionImpl.class.getName());

    private QoS defaultQos;
    private String defaultTopic;
    private MqttManagedConnection mc;

    public MqttConnectionImpl(MqttManagedConnection mc, MqttConnectionRequestInfo connectionRequestInfo) throws ResourceException {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("Creating a new connection to " + connectionRequestInfo.getServerUrl() + " for login " + connectionRequestInfo.getUserName());
        }
        this.mc = mc;

        this.setDefaultQos(connectionRequestInfo.getQos());
        this.setDefaultTopic(connectionRequestInfo.getTopicName());
    }

    @Override
    public void publish(String topicName, String message, QoS qos) {
        try {
            if (logger.isLoggable(Level.FINE)) {
                logger.fine("Trying to publish message " + message + " on topic " + topicName);
            }
            mc.getBlockingConnection().publish(topicName, message.getBytes(), qos, true);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void publish(String topicName, String message) {
        publish(topicName, message, defaultQos);
    }

    @Override
    public void publish(String message) {
        publish(defaultTopic, message);
    }

    public void setDefaultQos(QoS defaultQos) {
        this.defaultQos = defaultQos;
    }

    public void setDefaultTopic(String defaultTopic) {
        this.defaultTopic = defaultTopic;
    }

    public void close() {
        mc.closeConnection(this);
    }
}
