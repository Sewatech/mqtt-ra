package fr.sewatech.mqttra.api;

/**
 * Interface for the old fashion MDBs
 *
 * @author Alexis Hassler
 */
public interface MessageListener extends MqttListener {
    public void onMessage(Message message);
}
