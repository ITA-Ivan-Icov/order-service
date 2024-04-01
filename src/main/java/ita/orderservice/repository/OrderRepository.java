package ita.orderservice.repository;

import io.quarkus.mongodb.panache.reactive.ReactivePanacheMongoRepository;
import io.smallrye.mutiny.Uni;
import ita.orderservice.model.Order;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.Response;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class OrderRepository implements ReactivePanacheMongoRepository<Order> {

    private static final Logger logger = LoggerFactory.getLogger(OrderRepository.class);

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
            return null;
        }).onFailure().invoke(throwable -> logger.error("Failed to add new order", throwable)).replaceWithVoid();
    }

    public Uni<Response> getAllOrders() {
        return findAll().list().onItem().transformToUni(orders -> {
            String logMessage = "Retrieved all orders";
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
                                });
                    })
                    .onFailure().recoverWithNull();

        } catch (Exception ex) {
            logger.error("Invalid ObjectId format for id: " + id, ex);
            return Uni.createFrom().failure(ex);
        }
    }
}
