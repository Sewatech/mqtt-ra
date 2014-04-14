package fr.sewatech.mqttra.example;

import fr.sewatech.mqttra.api.MqttConnection;
import fr.sewatech.mqttra.api.MqttConnectionFactory;

import javax.annotation.Resource;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;

/**
 * @author Alexis Hassler
 */
@Stateless @LocalBean
public class MyStatelessBean {
    private static final String QUESTION_TOPIC_NAME = "swt/Question";

    @Resource(name="MqttDashboardCF")
    MqttConnectionFactory connectionFactory;

    public void ask(String message) {
        try {
            MqttConnection connection = connectionFactory.getConnection();
            connection.publish(QUESTION_TOPIC_NAME, message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void answer(String message) {
        try {
            MqttConnection connection = connectionFactory.getConnection();
            connection.publish(message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
