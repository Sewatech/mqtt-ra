package fr.sewatech.mqttra.connector.outbound;

import org.fusesource.mqtt.client.QoS;

import javax.resource.spi.ConnectionRequestInfo;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

import static java.util.Objects.equals;

/**
 * @author Alexis Hassler
 */
public class MqttConnectionRequestInfo implements ConnectionRequestInfo {

    private static final Logger logger = Logger.getLogger(MqttConnectionRequestInfo.class.getName());

    private String serverUrl;
    private int qosLevel = -1;
    private String topicName;
    private String userName;
    private String password;

    static MqttConnectionRequestInfoMerger merge() {
        return new MqttConnectionRequestInfoMerger();
    }
    static MqttConnectionRequestInfoMerger merge(ConnectionRequestInfo connectionRequestInfo) {
        return new MqttConnectionRequestInfoMerger((MqttConnectionRequestInfo) connectionRequestInfo);
    }

    public String getServerUrl() {
        return serverUrl;
    }

    public void setServerUrl(String serverUrl) {
        this.serverUrl = serverUrl;
    }

    public void setQosLevel(int qosLevel) {
        this.qosLevel = qosLevel;
    }

    public QoS getQos() {
        return QoS.values()[qosLevel];
    }

    public String getTopicName() {
        return topicName;
    }

    public void setTopicName(String topicName) {
        this.topicName = topicName;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MqttConnectionRequestInfo that = (MqttConnectionRequestInfo) o;

        if (logger.isLoggable(Level.FINE)) {
            logger.fine("Comparing : " + this + " and " + that);
        }

        return Objects.equals(qosLevel, that.qosLevel)
                && Objects.equals(password, that.password)
                && Objects.equals(serverUrl, that.serverUrl)
                && Objects.equals(topicName, that.topicName)
                && Objects.equals(userName, that.userName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(serverUrl, qosLevel, topicName, userName, password);
    }

    @Override
    public String toString() {
        return "MqttConnectionRequestInfo : " + serverUrl + "/" + topicName;
    }

    static class MqttConnectionRequestInfoMerger {
        private MqttConnectionRequestInfo first;

        public MqttConnectionRequestInfoMerger() {
        }

        public MqttConnectionRequestInfoMerger(MqttConnectionRequestInfo first) {
            this.first = first;
        }

        MqttConnectionRequestInfoMerger aCopyOf(ConnectionRequestInfo original) {
            MqttConnectionRequestInfo first = (MqttConnectionRequestInfo) original;
            this.first = new MqttConnectionRequestInfo();
            this.first.serverUrl = first.serverUrl;
            this.first.topicName = first.topicName;
            this.first.userName = first.userName;
            this.first.password = first.password;
            this.first.qosLevel = first.qosLevel;
            return this;
        }

        MqttConnectionRequestInfo with(MqttConnectionRequestInfo connectionRequestInfo) {
            if (first.qosLevel < 0) {
                first.qosLevel = connectionRequestInfo.qosLevel;
            }
            if (isNullOrEmpty(first.serverUrl)) {
                first.serverUrl = connectionRequestInfo.serverUrl;
            }
            if (isNullOrEmpty(first.topicName)) {
                first.topicName = connectionRequestInfo.topicName;
            }
            if (isNullOrEmpty(first.userName)) {
                first.userName = connectionRequestInfo.userName;
            }
            if (isNullOrEmpty(first.password)) {
                first.password = connectionRequestInfo.password;
            }
            return first;
        }

        private boolean isNullOrEmpty(String value) {
            return value == null || value.isEmpty();
        }
    }


}
