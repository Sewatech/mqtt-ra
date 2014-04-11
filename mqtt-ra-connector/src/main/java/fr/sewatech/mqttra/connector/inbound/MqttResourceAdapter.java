package fr.sewatech.mqttra.connector.inbound;

import fr.sewatech.mqttra.api.Message;
import fr.sewatech.mqttra.api.MqttMessageListener;
import fr.sewatech.mqttra.connector.outbound.MqttManagedConnectionFactory;
import org.fusesource.hawtdispatch.Task;
import org.fusesource.mqtt.client.CallbackConnection;
import org.fusesource.mqtt.client.MQTT;

import javax.resource.ResourceException;
import javax.resource.spi.*;
import javax.resource.spi.endpoint.MessageEndpointFactory;
import javax.resource.spi.work.Work;
import javax.resource.spi.work.WorkException;
import javax.transaction.xa.XAResource;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
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
    public void endpointActivation(MessageEndpointFactory mdbFactory, ActivationSpec activationSpec) throws ResourceException {
        logger.fine("endpoint activation");
        final ActivationSpecBean spec = ActivationSpecBean.class.cast(activationSpec);

        try {
            BlockingQueue<MqttMessageListener> pool = initializeEndpointsPool(mdbFactory, spec);
            MqttMessageListener endPointProxy = createEndPointProxy(pool);

            createConnection(mdbFactory, spec)
                .listener(new MqttClientListener(endPointProxy));

        } catch (Exception e) {
            throw new ResourceException(e);
        }
    }

    @Override
    public void endpointDeactivation(MessageEndpointFactory mdbFactory, ActivationSpec activationSpec) {
        logger.fine("endpoint deactivation");
        Key key = new Key(mdbFactory, activationSpec);
        try {
            final CallbackConnection connection = connections.remove(key);
            if (connection != null) {
                connection.suspend();  // in order to skip other messages in the topic
                connection.getDispatchQueue()
                          .execute(new Task() {
                              @Override
                              public void run() {
                                  connection.kill(new LoggingCallback<Void>("disconnect"));
                              }
                          });
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }



    private BlockingQueue<MqttMessageListener> initializeEndpointsPool(MessageEndpointFactory mdbFactory, ActivationSpecBean spec) throws UnavailableException {
        int poolSize = spec.getPoolSize();
        logger.fine("Initializing pool with " + poolSize + " connections");
        BlockingQueue<MqttMessageListener> pool = new ArrayBlockingQueue<>(poolSize);
        for (int i = 0; i < poolSize; i++) {
            pool.add(MqttMessageListener.class.cast(mdbFactory.createEndpoint(null)));
        }
        return pool;
    }

    private MqttMessageListener createEndPointProxy(final BlockingQueue<MqttMessageListener> pool) {
        return new MqttMessageListener() {
            @Override
            public void onMessage(final Message message) {
                final MqttMessageListener listener;
                try {
                    listener = pool.take();

                    bootstrapContext.getWorkManager().startWork(new Work() {
                        @Override
                        public void run() {
                            try {
                                listener.onMessage(message);
                                pool.add(listener);
                            } catch (Throwable e) {
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

        };
    }

    private CallbackConnection createConnection(final MessageEndpointFactory mdbFactory, final ActivationSpecBean spec) throws URISyntaxException {
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

                connection.subscribe(spec.buildTopicArray(), new LoggingCallback<byte[]>("subscribe"));

                Key key = new Key(mdbFactory, spec);
                connections.put(key, connection);
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

        Key(MessageEndpointFactory factory, ActivationSpec activationSpec) {
            this.factory = factory;
            this.activationSpec = activationSpec;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Key that = (Key) o;

            return Objects.equals(this.factory, that.factory)
                    && Objects.equals(this.activationSpec, that.activationSpec);
        }

        @Override
        public int hashCode() {
            return Objects.hash(this.factory, this.activationSpec);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MqttResourceAdapter that = (MqttResourceAdapter) o;

        if (!bootstrapContext.equals(that.bootstrapContext)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = connections.hashCode();
        return result;
    }
}
