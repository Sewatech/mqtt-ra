package fr.sewatech.mqttra.example;

import fr.sewatech.mqttra.api.*;
import org.fusesource.mqtt.client.QoS;

import javax.annotation.Resource;
import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;

/**
 * @author Alexis Hassler
 */
@MessageDriven
public class MyMqttBean implements MqttMessageListener {

    public static final String RA_JNDI_NAME = "${mqttra.jndiname}";

    @Resource(name= RA_JNDI_NAME)
    MqttConnectionFactory connectionFactory;

    @Topic(name = "swt/Question", qos = QoS.AT_LEAST_ONCE)
    public void onQuestion(Message message) {
        System.out.println("Message received " + new String(message.getPayload()) + " in " + this.getClass().getName() + " on Topic " + message.getTopic());

        answer("OK");
    }

    @Topic(name = "swt/Question", qos = QoS.EXACTLY_ONCE)
    public void onQuestionToo(Message message) {
        System.out.println("Message received " + new String(message.getPayload()) + " in " + this.getClass().getName() + " on Topic " + message.getTopic());

        answer("OK second time");
    }


    private void answer(String message) {
        try {
            MqttConnection connection = connectionFactory.getConnection();
            connection.publish(message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
