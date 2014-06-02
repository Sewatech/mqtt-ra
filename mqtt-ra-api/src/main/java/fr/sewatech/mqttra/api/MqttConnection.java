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
package fr.sewatech.mqttra.api;

import org.fusesource.mqtt.client.QoS;

/**
 * MQTT outbound connection
 *
 * @author Alexis Hassler
 */
public interface MqttConnection {

    /**
     * Publish a text message in a specific topic, with a specific qos level
     *
     * @param topicName Topic
     * @param message Text message
     * @param qos Qality of service level
     */
    void publish(String topicName, String message, QoS qos);

    /**
     * Publish a text message in a specific topic, with the default qos level
     *
     * @param topicName Topic
     * @param message Text message
     */
    void publish(String topicName, String message);

    /**
     * Publish a text message in the default topic, with the default qos level
     *
     * @param message Text message
     */
    void publish(String message);

    void close();
}
