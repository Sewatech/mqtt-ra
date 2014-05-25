package fr.sewatech.mqttra.example;

import fr.sewatech.mqttra.api.Message;
import fr.sewatech.mqttra.api.MessageListener;
import fr.sewatech.mqttra.api.MqttConnectionFactory;
import fr.sewatech.mqttra.api.MqttListener;

import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.inject.Inject;

/**
 * Won't receive any message : no @Topic method
 *
 * @author Alexis Hassler
 */
@MessageDriven(
    messageListenerInterface = MqttListener.class,
    activationConfig = {
        @ActivationConfigProperty(propertyName = "topicName", propertyValue = "swt/Question"),
        @ActivationConfigProperty(propertyName = "qosLevel", propertyValue = "1")

})
public class OtherMqttBean implements MessageListener {

    @Inject
    MqttConnectionFactory connectionFactory;

    public void onMessage(Message message) {
        System.out.println("Message received " + new String(message.getPayload()) + " in " + this.getClass().getName() + " on Topic " + message.getTopic());
    }
}
