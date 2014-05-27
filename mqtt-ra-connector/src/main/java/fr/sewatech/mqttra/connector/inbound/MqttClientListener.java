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
package fr.sewatech.mqttra.connector.inbound;

import fr.sewatech.mqttra.api.Message;
import org.fusesource.hawtbuf.Buffer;
import org.fusesource.hawtbuf.UTF8Buffer;
import org.fusesource.mqtt.client.Listener;

import java.lang.reflect.Method;

/**
* @author Alexis Hassler
*/
class MqttClientListener implements Listener {
    private final MqttListenerProxy mdb;
    private Method method;

    public MqttClientListener(MqttListenerProxy mdb, Method method) {
        this.mdb = mdb;
        this.method = method;
    }

    @Override
    public void onConnected() {
    }

    @Override
    public void onDisconnected() {
    }

    @Override
    public void onPublish(UTF8Buffer topic, Buffer message, Runnable ack) {
        mdb.onMessage(new Message(topic.toString(), message.toByteArray()), method);
        ack.run();
    }

    @Override
    public void onFailure(Throwable value) {
    }
}
