package com.joni.hocs.models

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document


@Document(collection = "orders")
data class Order(
    @Id val id: String? = null,
    val userId: String,
    val items: List<OrderItem>,
    val totalPrice: Double,
    val deliveryStatus: String,
    val createdAt: Long
)

data class OrderItem(
    val serviceId: String,
    val quantity: Int
)


@Document(collection = "orderstatus")
data class OrderStatus(
    @Id
    val id: String,
    var deliveryStatus: String
)

data class OrderStatusUpdateRequest(
    val deliveryStatus: String
)