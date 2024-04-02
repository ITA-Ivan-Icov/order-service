package jms;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.jms.ConnectionFactory;
import jakarta.jms.JMSContext;

@ApplicationScoped
public class Sender {

    @Inject
    private ConnectionFactory connectionFactory;

    private JMSContext context;

    @PostConstruct
    public void initialize() {
        context = connectionFactory.createContext();
    }

    @PreDestroy
    public void close() {
        if (context != null) {
            context.close();
        }
    }

    public JMSContext getContext() {
        return connectionFactory.createContext();
    }
}
