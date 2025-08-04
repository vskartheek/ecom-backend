package com.ecommerce.project.model;


import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name="order_items")
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long orderItemId;

    @ManyToOne
    @JoinColumn(name="product_id")
    private Product product;

    @ManyToOne
    @JoinColumn(name = "order_id")
    private Order order;


    private Integer quantity;
    private Double discount;
    private double orderedProductPrice;

}
