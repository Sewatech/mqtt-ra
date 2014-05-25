package fr.sewatech.mqttra.example;

import fr.sewatech.mqttra.api.Message;
import fr.sewatech.mqttra.api.MqttConnectionFactory;
import fr.sewatech.mqttra.api.MqttMessageListener;

import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.inject.Inject;

/**
 * Won't receive any message : no @Topic method
 *
 * @author Alexis Hassler
 */
@MessageDriven(activationConfig = {
        @ActivationConfigProperty(propertyName = "topicName", propertyValue = "swt/Question"),
        @ActivationConfigProperty(propertyName = "qosLevel", propertyValue = "1")

})
public class OtherMqttBean implements MqttMessageListener {

    @Inject
    MqttConnectionFactory connectionFactory;

    public void onMessage(Message message) {
        System.out.println("Message received in " + this.getClass().getName() + " on Topic " + message.getTopic());
    }
}
