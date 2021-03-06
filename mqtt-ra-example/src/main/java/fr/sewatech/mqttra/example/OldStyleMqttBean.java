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

import javax.annotation.Resource;
import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Old style MDB
 *
 * @author Alexis Hassler
 */
@MessageDriven(
    messageListenerInterface = MqttListener.class,
    activationConfig = {
            @ActivationConfigProperty(propertyName = "topicName", propertyValue = "swt/Question"),
            @ActivationConfigProperty(propertyName = "qosLevel", propertyValue = "1")

    })
public class OldStyleMqttBean implements MessageListener {

    private static final Logger logger = Logger.getLogger(OldStyleMqttBean.class.getName());
    private static final String RA_JNDI_NAME = "java:/mqtt/AnswerCF";

    @Resource(name = RA_JNDI_NAME)
    MqttConnectionFactory connectionFactory;

    @Override
    public void onMessage(Message message) {
        System.out.println("Message received " + message.asText() + " in " + this.getClass().getName() + " on Topic " + message.getTopic());

        answer("Ah que " + message.asText());
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
