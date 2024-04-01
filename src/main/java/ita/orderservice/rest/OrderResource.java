package ita.orderservice.rest;
import io.smallrye.mutiny.Uni;
import ita.orderservice.repository.OrderRepository;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/order")
public class OrderResource {

    @Inject
    OrderRepository orderRepository;

    @POST
    @Produces(MediaType.TEXT_PLAIN)
    public Uni<Void> createOrder(@QueryParam("clientName") String clientName,
                                      @QueryParam("startDate") String startDate,
                                      @QueryParam("endDate") String endDate,
                                      @QueryParam("phoneNumber") String phoneNumber,
                                      @QueryParam("insurance") String insurance,
                                      @QueryParam("email") String email) {
        return orderRepository.addOrder(clientName, startDate, endDate, phoneNumber, insurance, email).replaceWithVoid();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<Response> getAllOrders() {
        return orderRepository.getAllOrders();
    }

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<Response> getOrderById(@PathParam("id") String id) {
        return orderRepository.getOrderById(id);
    }

    @PUT
    @Path("/{id}")
    @Produces(MediaType.TEXT_PLAIN)
    public Uni<Void> updateOrder(@PathParam("id") String id,
                                      @QueryParam("clientName") String clientName,
                                      @QueryParam("startDate") String startDate,
                                      @QueryParam("endDate") String endDate,
                                      @QueryParam("phoneNumber") String phoneNumber,
                                      @QueryParam("insurance") String insurance,
                                      @QueryParam("email") String email) {
        return orderRepository.updateOrder(id, clientName, startDate, endDate, phoneNumber, insurance, email);
    }

    @DELETE
    @Path("/{id}")
    @Produces(MediaType.TEXT_PLAIN)
    public Uni<Void> deleteOrder(@PathParam("id") String id) {
        return orderRepository.deleteOrder(id);
    }
}