package fr.sewatech.mqttra.connector.outbound;

import fr.sewatech.mqttra.api.MqttConnection;
import org.fusesource.mqtt.client.BlockingConnection;
import org.fusesource.mqtt.client.MQTT;

import javax.resource.NotSupportedException;
import javax.resource.ResourceException;
import javax.resource.spi.*;
import javax.security.auth.Subject;
import javax.transaction.xa.XAResource;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import static fr.sewatech.mqttra.connector.outbound.MqttConnectionRequestInfo.merge;

public class MqttManagedConnection implements javax.resource.spi.ManagedConnection {

    private static final Logger logger = Logger.getLogger(MqttManagedConnection.class.getName());


    private PrintWriter logWriter;
    private final MqttConnectionRequestInfo defaultConnectionRequestInfo;
    private MqttConnectionRequestInfo currentConnectionRequestInfo;

    private BlockingConnection blockingConnection;
    private MqttConnection mqttConnection;
    private List<ConnectionEventListener> listeners = new ArrayList<>();

    public MqttManagedConnection(MqttConnectionRequestInfo connectionRequestInfo) {
        this.defaultConnectionRequestInfo = connectionRequestInfo;
        this.currentConnectionRequestInfo = connectionRequestInfo;
    }

    @Override
    public MqttConnection getConnection(Subject subject, ConnectionRequestInfo cxRequestInfo) throws ResourceException {
        logger.fine("Getting connection for cxRequestInfo " + System.identityHashCode(cxRequestInfo));

        try {
            mqttConnection = new MqttConnectionImpl(this, currentConnectionRequestInfo);

            MqttConnectionRequestInfo newRequestInfo = merge().aCopyOf(cxRequestInfo).with(defaultConnectionRequestInfo);
            if (blockingConnection == null || !currentConnectionRequestInfo.equals(newRequestInfo)) {
                currentConnectionRequestInfo = newRequestInfo;

                MQTT mqtt = new MQTT();
                mqtt.setHost(currentConnectionRequestInfo.getServerUrl());
                mqtt.setUserName(currentConnectionRequestInfo.getUserName());
                mqtt.setPassword(currentConnectionRequestInfo.getPassword());
                blockingConnection = mqtt.blockingConnection();
                blockingConnection.connect();
            }
        } catch (Exception e) {
            throw new ResourceException(e);
        }

        return mqttConnection;
    }

    @Override
    public void destroy() throws ResourceException {
        try {
            logger.fine("Destroying");
        } catch (Exception e) {
            throw new ResourceException(e);
        } finally {
            this.mqttConnection = null;
        }
    }

    @Override
    public void cleanup() throws ResourceException {
        logger.fine("Would like to cleanup, but nothing done");
        currentConnectionRequestInfo = defaultConnectionRequestInfo;
    }

    @Override
    public void associateConnection(Object connection) throws ResourceException {
        this.mqttConnection = (MqttConnection) connection;
    }

    @Override
    public void addConnectionEventListener(ConnectionEventListener listener) {
        if (listener == null) {
            throw new IllegalArgumentException("Listener is null");
        }
        logger.fine("Connection listener added : " + listener.toString());
        listeners.add(listener);
    }

    @Override
    public void removeConnectionEventListener(ConnectionEventListener listener) {
        if (listener == null) {
            throw new IllegalArgumentException("Listener is null");
        }
        logger.fine("Connection listener removed : " + listener.toString());
        listeners.remove(listener);
    }

    @Override
    public XAResource getXAResource() throws ResourceException {
        throw new NotSupportedException("XAResource not supported");
    }

    @Override
    public LocalTransaction getLocalTransaction() throws ResourceException {
        throw new NotSupportedException("LocalTransaction not supported");
    }

    @Override
    public ManagedConnectionMetaData getMetaData() throws ResourceException {
        return new MqttManagedConnectionMetaData();
    }

    @Override
    public void setLogWriter(PrintWriter out) throws ResourceException {
        this.logWriter = out;
    }

    @Override
    public PrintWriter getLogWriter() throws ResourceException {
        return logWriter;
    }

    void closeConnection(MqttConnection handle) {
        ConnectionEvent event = new ConnectionEvent(this, ConnectionEvent.CONNECTION_CLOSED);
        event.setConnectionHandle(handle);

        for (ConnectionEventListener listener : listeners) {
            logger.fine("Connection closed for listener : " + listener.toString());
            listener.connectionClosed(event);
        }
    }

    public BlockingConnection getBlockingConnection() {
        return blockingConnection;
    }
}
