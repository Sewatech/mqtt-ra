package fr.sewatech.mqttra.example;

import javax.annotation.Resource;
import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.ejb.MessageDrivenContext;
import javax.jms.*;

/**
 * @author Alexis Hassler
 */
//@MessageDriven(activationConfig = {
//        @ActivationConfigProperty(propertyName="destinationType",
//                                  propertyValue="javax.jms.Queue"),
//        @ActivationConfigProperty(propertyName="destination",
//                                  propertyValue="/queue/source"),
//        @ActivationConfigProperty(propertyName="jndiParameters",
//                                  propertyValue="java.naming.factory.initial=org.jnp.interfaces.NamingContextFactory;java.naming.provider.url=JBM_HOST:1099;java.naming.factory.url.pkgs=org.jboss.naming:org.jnp.interfaces"),
//        @ActivationConfigProperty(propertyName="connectionFactory",
//                                  propertyValue="XAConnectionFactory")
//})
//@ResourceAdapter("generic-jms-ra-<VERSION>.rar")
public class MyJmsBean implements MessageListener {

    @Resource
    private MessageDrivenContext context;

    @Resource(name = "jms/AnswerCF")
    ConnectionFactory connectionFactory;

    @Override
    public void onMessage(Message message) {
        if (message instanceof TextMessage) {
            try {
                System.out.println("Message received " + ((TextMessage) message).getText()
                        + " in " + this.getClass().getName()
                        + " on Topic " + message.getJMSDestination());
                answer("OK");
            } catch (JMSException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void answer(String message) {
        try {
            Connection connection = connectionFactory.createConnection();
            // ...
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
