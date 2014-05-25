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
