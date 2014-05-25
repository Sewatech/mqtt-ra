package fr.sewatech.mqttra.connector.inbound;

import fr.sewatech.mqttra.api.MqttListener;
import org.fusesource.mqtt.client.QoS;
import org.fusesource.mqtt.client.Topic;

import javax.resource.ResourceException;
import javax.resource.spi.Activation;
import javax.resource.spi.ActivationSpec;
import javax.resource.spi.InvalidPropertyException;
import javax.resource.spi.ResourceAdapter;
import java.util.Objects;

/**
 * @author Alexis Hassler
 */
@Activation(messageListeners = MqttListener.class)
public class ActivationSpecBean implements ActivationSpec {
    private ResourceAdapter resourceAdapter;

    private String topicName = "";
    private int qosLevel = 0;
    private String serverUrl = "tcp://localhost:1883";
    private int poolSize = 3;
    private String userName;
    private String password;

    public String getTopicName() {
        return topicName;
    }

    public void setTopicName(String topicName) {
        this.topicName = topicName;
    }

    public int getQosLevel() {
        return qosLevel;
    }

    public void setQosLevel(int qosLevel) {
        this.qosLevel = qosLevel;
    }
    public QoS getQos() {
        return QoS.values()[qosLevel];
    }

    public String getServerUrl() {
        return serverUrl;
    }

    public void setServerUrl(String serverUrl) {
        this.serverUrl = serverUrl;
    }

    public int getPoolSize() {
        return poolSize;
    }

    public void setPoolSize(int poolSize) {
        this.poolSize = poolSize;
    }


    public void setUserName(String userName) {
        this.userName = userName;
    }
    public void setPassword(String password) {
        this.password = password;
    }

    public String getUserName() {
        return userName;
    }

    public String getPassword() {
        return password;
    }


    Topic[] buildTopicArray() {
        return new Topic[]{buildTopic()};
    }
    Topic buildTopic() {
        return new Topic(topicName, getQos());
    }

    @Override
    public void validate() throws InvalidPropertyException {
        validateNotNullOrEmpty("serverUrl", serverUrl);
    }

    private void validateNotNullOrEmpty(String propertyName, String value) throws InvalidPropertyException {
        if (value == null || value.isEmpty()) {
            throw new InvalidPropertyException(propertyName + " is required");
        }
    }

    @Override
    public ResourceAdapter getResourceAdapter() {
        return resourceAdapter;
    }

    @Override
    public void setResourceAdapter(ResourceAdapter resourceAdapter) throws ResourceException {
        this.resourceAdapter = resourceAdapter;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ActivationSpecBean that = (ActivationSpecBean) o;
        return Objects.equals(this.qosLevel, that.qosLevel)
                && Objects.equals(this.serverUrl, that.serverUrl)
                && Objects.equals(this.topicName, that.topicName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(topicName, qosLevel, serverUrl);
    }


    @Override
    public String toString() {
        return "ActivationSpecBean{" +
                "topicName=" + topicName +
                ", qosLevel=" + qosLevel +
                ", serverUrl='" + serverUrl +
                '}';
    }
}
