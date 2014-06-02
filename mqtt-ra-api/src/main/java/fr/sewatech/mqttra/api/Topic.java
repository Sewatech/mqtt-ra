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

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Interface to mark MQTT listener methods
 *
 * @author Alexis Hassler
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface Topic {
    /**
     *  Name of the topic
     */
    public String name();
    /**
     *  Quality of Service level of the connection with the topic
     */
    public QoS qos();
}
