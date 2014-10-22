/**
 * Copyright 2014 Sewatech
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package fr.sewatech.mqttra.example;

import fr.sewatech.mqttra.api.*;
import org.fusesource.mqtt.client.QoS;

import javax.ejb.MessageDriven;
import javax.inject.Inject;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Alexis Hassler
 */
@MessageDriven
public class NewFashionMqttBean implements MqttListener {

    private static final Logger logger = Logger.getLogger(NewFashionMqttBean.class.getName());

    @Inject
    MqttConnectionFactory connectionFactory;

    @Topic(name = "swt/Question", qos = QoS.AT_LEAST_ONCE)
    public void onQuestion(Message message) {
        System.out.println("Message received " + new String(message.getPayload()) + " in " + this.getClass().getName() + " on Topic " + message.getTopic());

        answer("Echo to Question : " + message.asText());
    }

    @Topic(name = "swt/OtherQuestion", qos = QoS.EXACTLY_ONCE)
    public void onQuestionToo(Message message) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("Message received " + new String(message.getPayload()) + " in " + this.getClass().getName() + " on Topic " + message.getTopic());
        }
        System.out.println("Message received " + new String(message.getPayload()) + " in " + this.getClass().getName() + " on Topic " + message.getTopic());

        answer("Echo to OtherQuestion : " + message.asText());
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
