package fr.sewatech.mqttra.api;

/**
 * @author Alexis Hassler
 */
public interface MqttListener {

    void onMessage(Message message);
}
