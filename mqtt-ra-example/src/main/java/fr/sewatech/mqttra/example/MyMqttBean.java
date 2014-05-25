package fr.sewatech.mqttra.example;

import fr.sewatech.mqttra.api.*;
import org.fusesource.mqtt.client.QoS;

import javax.annotation.Resource;
import javax.ejb.MessageDriven;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Alexis Hassler
 */
@MessageDriven
public class MyMqttBean implements MqttMessageListener {

    private static final Logger logger = Logger.getLogger(MyMqttBean.class.getName());
    private static final String RA_JNDI_NAME = "java:/mqtt/AnswerCF";

    @Resource(name= RA_JNDI_NAME)
    MqttConnectionFactory connectionFactory;

    @Topic(name = "swt/Question", qos = QoS.AT_LEAST_ONCE)
    public void onQuestion(Message message) {
        System.out.println("Message received " + new String(message.getPayload()) + " in " + this.getClass().getName() + " on Topic " + message.getTopic());

        answer("OK");
    }

    @Topic(name = "swt/QuestionToo", qos = QoS.EXACTLY_ONCE)
    public void onQuestionToo(Message message) {
        System.out.println("Message received " + new String(message.getPayload()) + " in " + this.getClass().getName() + " on Topic " + message.getTopic());

        answer("OK second time");
    }


    private void answer(String message) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine(this.getClass().getName() + " will answer " + message);
        }
        try {
            MqttConnection connection = connectionFactory.getConnection();
            connection.publish(message);
            connection.close();
        } catch (Exception e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
        }
    }
}
