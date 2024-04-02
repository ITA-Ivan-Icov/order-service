package jms;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.jms.JMSConsumer;
import jakarta.jms.JMSContext;
import org.slf4j.Logger;

import org.slf4j.LoggerFactory;

import jakarta.jms.*;

@ApplicationScoped
public class Receiver {

    @Inject
    ConnectionFactory connectionFactory;

    @Resource(mappedName = "java:/jms/queue/itMQ")
    Queue logQueue;

    private final Logger logger = LoggerFactory.getLogger(Receiver.class);

    @PostConstruct
    public void init() {
        JMSContext context = connectionFactory.createContext();
        JMSConsumer consumer = context.createConsumer(logQueue);
        consumer.setMessageListener(message -> {
            try {
                String logMessage = message.getBody(String.class);
                logger.info("Received log message: {}", logMessage);
            } catch (JMSException e) {
                logger.error("Failed to process log message", e);
            }
        });
    }
}
