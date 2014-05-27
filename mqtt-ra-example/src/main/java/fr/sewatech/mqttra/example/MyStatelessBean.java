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
