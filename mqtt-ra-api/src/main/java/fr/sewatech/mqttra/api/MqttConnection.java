package fr.sewatech.mqttra.api;

import org.fusesource.mqtt.client.QoS;

/**
 * @author Alexis Hassler
 */
public interface MqttConnection {

    void publish(String topicName, String message, QoS qos);

    void publish(String topicName, String message);

    void publish(String message);

    void close();
}
