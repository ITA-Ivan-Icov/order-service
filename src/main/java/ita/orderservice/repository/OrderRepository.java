package ita.orderservice.repository;

import io.quarkus.mongodb.panache.reactive.ReactivePanacheMongoRepository;
import io.smallrye.mutiny.Uni;
import ita.orderservice.model.Order;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.jms.JMSContext;
import jakarta.jms.Queue;
import jakarta.ws.rs.core.Response;
import jms.Sender;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class OrderRepository implements ReactivePanacheMongoRepository<Order> {

    private static final Logger logger = LoggerFactory.getLogger(OrderRepository.class);

    @Inject
    Sender sender;

    public Uni<Void> addOrder(String clientName, String startDate, String endDate, String phoneNumber, String insurance, String email) {
        Order order = new Order();
        order.setClientName(clientName);
        order.setStartDate(startDate);
        order.setEndDate(endDate);
        order.setPhoneNumber(phoneNumber);
        order.setInsurance(insurance);
        order.setEmail(email);
        return order.persist().replaceWithVoid().onItem().transform(ignored -> {
            String logMessage = "New order added by client: " + clientName;
            try (JMSContext context = sender.getContext()) {
                Queue queue = context.createQueue("Orders queue");
                context.createProducer().send(queue, logMessage);
                logger.info(logMessage);
            } catch (Exception e) {
                logger.error("Failed to send log message", e);
            }
            return null;
        }).onFailure().invoke(throwable -> logger.error("Failed to add new order", throwable)).replaceWithVoid();
    }

    public Uni<Response> getAllOrders() {
        return findAll().list().onItem().transformToUni(orders -> {
            String logMessage = "Retrieved all orders";
            try (JMSContext context = sender.getContext()) {
                Queue queue = context.createQueue("Orders queue");
                context.createProducer().send(queue, logMessage);
                logger.info(logMessage);
            } catch (Exception e) {
                logger.error("Failed to send log message", e);
            }
            if (orders.isEmpty()) {
                return Uni.createFrom().item(Response.status(Response.Status.NOT_FOUND).entity("No orders found").build());
            } else {
                return Uni.createFrom().item(Response.ok(orders).build());
            }
        }).onFailure().recoverWithItem(throwable -> {
            logger.error("Failed to get all orders", throwable);
            return Response.serverError().entity("Failed to retrieve orders").build();
        });
    }

    public Uni<Response> getOrderById(String id) {
        ObjectId objectId = new ObjectId(id);
        return findById(objectId).onItem().transformToUni(order -> {
            String logMessage = "Retrieved order by id: " + id;
            try (JMSContext context = sender.getContext()) {
                Queue queue = context.createQueue("Orders queue");
                context.createProducer().send(queue, logMessage);
                logger.info(logMessage);
            } catch (Exception e) {
                logger.error("Failed to send log message", e);
            }
            if (order != null) {
                return Uni.createFrom().item(Response.ok(order).build());
            } else {
                return Uni.createFrom().item(Response.status(Response.Status.NOT_FOUND).entity("Order with id: " + id + " not found").build());
            }
        }).onFailure().recoverWithItem(throwable -> {
            logger.error("Failed to get order with id: " + id, throwable);
            return Response.serverError().entity("Failed to retrieve order").build();
        });
    }

    public Uni<Void> deleteOrder(String id) {
        ObjectId objectId = new ObjectId(id);
        return deleteById(objectId).replaceWithVoid()
                .onItem().invoke(ignored -> {
                    String logMessage = "Deleted order with id: " + id;
                    try (JMSContext context = sender.getContext()) {
                        Queue queue = context.createQueue("Orders queue");
                        context.createProducer().send(queue, logMessage);
                        logger.info(logMessage);
                    } catch (Exception e) {
                        logger.error("Failed to send log message", e);
                    }
                })
                .onFailure().invoke(throwable -> logger.error("Failed to delete order with id: " + id, throwable));
    }

    public Uni<Void> updateOrder(String id, String clientName, String startDate, String endDate, String phoneNumber, String insurance, String email) {
        try {
            ObjectId objectId = new ObjectId(id);
            return findById(objectId)
                    .onItem().ifNotNull().transformToUni(order -> {
                        order.setClientName(clientName);
                        order.setStartDate(startDate);
                        order.setEndDate(endDate);
                        order.setPhoneNumber(phoneNumber);
                        order.setInsurance(insurance);
                        order.setEmail(email);
                        return order.update().replaceWithVoid()
                                .onItem().invoke(ignored -> {
                                    String logMessage = "Updated order with id: " + id;
                                    try (JMSContext context = sender.getContext()) {
                                        Queue queue = context.createQueue("Orders queue");
                                        context.createProducer().send(queue, logMessage);
                                        logger.info(logMessage);
                                    } catch (Exception e) {
                                        logger.error("Failed to send log message", e);
                                    }
                                });
                    })
                    .onFailure().recoverWithNull();

        } catch (Exception ex) {
            logger.error("Invalid ObjectId format for id: " + id, ex);
            return Uni.createFrom().failure(ex);
        }
    }
}
