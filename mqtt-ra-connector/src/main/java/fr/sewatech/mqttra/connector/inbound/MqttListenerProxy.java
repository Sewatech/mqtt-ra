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
import fr.sewatech.mqttra.api.MqttListener;

import javax.resource.spi.BootstrapContext;
import javax.resource.spi.work.Work;
import javax.resource.spi.work.WorkException;
import java.lang.reflect.Method;
import java.util.concurrent.BlockingQueue;

/**
 * Proxy between the resource adapter and the MqttListener. Manage a pool of listeners and distribute the received messages across them.
 *
 * @author Alexis Hassler
 */
public class MqttListenerProxy implements MqttListener {

    private BlockingQueue<MqttListener> pool;
    private BootstrapContext bootstrapContext;

    public MqttListenerProxy(BootstrapContext bootstrapContext, BlockingQueue<MqttListener> pool) {
        this.bootstrapContext = bootstrapContext;
        this.pool = pool;
    }

    public void onMessage(final Message message, final Method method) {
        final MqttListener listener;
        try {
            listener = pool.take();

            bootstrapContext.getWorkManager().startWork(new Work() {
                @Override
                public void run() {
                    try {
                        method.invoke(listener, message);
                        pool.add(listener);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }

                @Override
                public void release() {
                }
            });
        } catch (WorkException | InterruptedException e) {
            throw new RuntimeException(e);
        }

    }
}
