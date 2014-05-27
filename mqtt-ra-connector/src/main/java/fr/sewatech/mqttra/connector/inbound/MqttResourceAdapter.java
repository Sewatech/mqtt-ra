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
import fr.sewatech.mqttra.api.MessageListener;
import fr.sewatech.mqttra.api.MqttListener;
import fr.sewatech.mqttra.api.Topic;
import org.fusesource.hawtdispatch.Task;
import org.fusesource.mqtt.client.CallbackConnection;
import org.fusesource.mqtt.client.MQTT;
import org.fusesource.mqtt.client.QoS;

import javax.resource.ResourceException;
import javax.resource.spi.*;
import javax.resource.spi.endpoint.MessageEndpointFactory;
import javax.transaction.xa.XAResource;
import java.lang.reflect.Method;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

import static javax.resource.spi.TransactionSupport.TransactionSupportLevel.NoTransaction;

/**
 * @author Alexis Hassler
 */
@Connector(
        vendorName = "sewatech", version = "0.1", eisType = "MQTT Broker",
        transactionSupport = NoTransaction)
public class MqttResourceAdapter implements ResourceAdapter {
    private static final Logger logger = Logger.getLogger(MqttResourceAdapter.class.getName());

    Map<Key, CallbackConnection> connections = new HashMap<>();
    private BootstrapContext bootstrapContext;

    @Override
    public void start(BootstrapContext bootstrapContext) throws ResourceAdapterInternalException {
        this.bootstrapContext = bootstrapContext;
    }

    @Override
    public void stop() {
    }

    @Override
    public void endpointActivation(MessageEndpointFactory mdbFactory, ActivationSpec activationSpec)
            throws ResourceException {
        logger.fine("endpoint activation");
        ActivationSpecBean spec = ActivationSpecBean.class.cast(activationSpec);

        try {
            BlockingQueue<MqttListener> pool = initializeEndpointsPool(mdbFactory, spec);
            MqttListenerProxy endPointProxy = createEndPointProxy(pool);

            Class<?> endpointClass = mdbFactory.getEndpointClass();

            if (isOldFashion(endpointClass)) {
                Method method = endpointClass.getDeclaredMethod("onMessage", Message.class);
                createConnection(mdbFactory, method, spec)
                        .listener(new MqttClientListener(endPointProxy, method));
            }

            for (Method method : endpointClass.getMethods()) {
                if (method.isAnnotationPresent(Topic.class)) {
                    createConnection(mdbFactory, method, spec)
                            .listener(new MqttClientListener(endPointProxy, method));
                }
            }

        } catch (Exception e) {
            throw new ResourceException(e);
        }
    }

    @Override
    public void endpointDeactivation(MessageEndpointFactory mdbFactory, ActivationSpec activationSpec) {
        logger.fine("Endpoint deactivation");

        try {
            Class<?> endpointClass = mdbFactory.getEndpointClass();

            if (isOldFashion(endpointClass)) {
                Method method = endpointClass.getDeclaredMethod("onMessage", Message.class);
                unregisterConnection(new Key(mdbFactory, activationSpec, method));
            }

            for (Method method : endpointClass.getMethods()) {
                if (method.isAnnotationPresent(Topic.class)) {
                    unregisterConnection(new Key(mdbFactory, activationSpec, method));
                }
            }
        } catch (Throwable e) {
            logger.log(Level.WARNING, "Unable to deactivate an endpoint", e);
        }
    }

    private boolean isOldFashion(Class<?> endpointClass) {
        return MessageListener.class.isAssignableFrom(endpointClass);
    }

    private void unregisterConnection(Key key) {
        final CallbackConnection connection = connections.remove(key);
        if (connection == null) {
            logger.fine("Cannot find connection for key " + key);
        } else {
            logger.fine("Connection found for key " + key);
            connection.suspend();  // in order to skip other messages in the topic
            connection.getDispatchQueue()
                    .execute(new Task() {
                        @Override
                        public void run() {
                            connection.kill(new LoggingCallback<Void>("disconnect"));
                        }
                    });
        }
    }


    private BlockingQueue<MqttListener> initializeEndpointsPool(MessageEndpointFactory mdbFactory, ActivationSpecBean spec)
            throws UnavailableException {
        int poolSize = spec.getPoolSize();
        logger.fine("Initializing pool with " + poolSize + " connections");
        BlockingQueue<MqttListener> pool = new ArrayBlockingQueue<>(poolSize);
        for (int i = 0; i < poolSize; i++) {
            pool.add(MqttListener.class.cast(mdbFactory.createEndpoint(null)));
        }
        return pool;
    }

    private MqttListenerProxy createEndPointProxy(final BlockingQueue<MqttListener> pool) {
        return new MqttListenerProxy(bootstrapContext, pool);
    }

    private CallbackConnection createConnection(final MessageEndpointFactory mdbFactory, final Method method, final ActivationSpecBean spec)
            throws URISyntaxException {
        logger.fine("Creating connection to " + spec.getUserName() + " with login " + spec.getUserName());
        MQTT mqtt = new MQTT();
        mqtt.setUserName(spec.getUserName());
        mqtt.setPassword(spec.getPassword());
        mqtt.setHost(spec.getServerUrl());
        final CallbackConnection connection = mqtt.callbackConnection();

        connection.connect(new LoggingCallback<Void>("connect") {
            @Override
            public void onSuccess(Void value) {
                super.onSuccess(value);

                Topic annotation = method.getAnnotation(Topic.class);
                    String name;
                    QoS qos;
                if (annotation == null) {
                    name = spec.getTopicName();
                    qos = spec.getQos();
                } else {
                    name = annotation.name() == null ? spec.getTopicName() : annotation.name();
                    qos = annotation.qos() == null ? spec.getQos() : annotation.qos();
                }
                connection.subscribe(
                            new org.fusesource.mqtt.client.Topic[]{new org.fusesource.mqtt.client.Topic(name, qos)},
                            new LoggingCallback<byte[]>("subscribe"));

                Key key = new Key(mdbFactory, spec, method);
                connections.put(key, connection);
                logger.fine("Connection registered under key " + key);
            }
        });

        return connection;
    }

    @Override
    public XAResource[] getXAResources(ActivationSpec[] activationSpecs) throws ResourceException {
        logger.fine("Asked fir XA resources, but none to provide");
        return new XAResource[0];
    }

    class Key {
        private MessageEndpointFactory factory;
        private ActivationSpec activationSpec;
        private Method method;

        Key(MessageEndpointFactory factory, ActivationSpec activationSpec, Method method) {
            this.factory = factory;
            this.activationSpec = activationSpec;
            this.method = method;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Key that = (Key) o;

            return Objects.equals(this.factory, that.factory)
                    && Objects.equals(this.activationSpec, that.activationSpec)
                    && Objects.equals(this.method, that.method);
        }

        @Override
        public int hashCode() {
            return Objects.hash(this.factory, this.activationSpec, this.method);
        }

        @Override
        public String toString() {
            return factory.getClass().getSimpleName() + " / " + activationSpec;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MqttResourceAdapter that = (MqttResourceAdapter) o;

        return Objects.equals(this.bootstrapContext, that.bootstrapContext);
    }

    @Override
    public int hashCode() {
        return Objects.hash(bootstrapContext);
    }
}
