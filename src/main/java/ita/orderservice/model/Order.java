package ita.orderservice.model;

import io.quarkus.mongodb.panache.common.MongoEntity;
import io.quarkus.mongodb.panache.reactive.ReactivePanacheMongoEntity;
import lombok.Data;

@Data
@MongoEntity(database = "order-service", collection = "orders")
public class Order extends ReactivePanacheMongoEntity {
    private String clientName;
    private String startDate;
    private String endDate;
    private String phoneNumber;
    private String insurance;
    private String email;
}