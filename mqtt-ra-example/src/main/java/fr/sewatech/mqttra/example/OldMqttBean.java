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
public class OldMqttBean implements MessageListener {

    @Inject
    MqttConnectionFactory connectionFactory;

    public void onMessage(Message message) {
        System.out.println("Message received " + new String(message.getPayload()) + " in " + this.getClass().getName() + " on Topic " + message.getTopic());
    }
}
