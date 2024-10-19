package com.joni.hocs.controllers

import com.google.cloud.firestore.Firestore
import com.google.cloud.firestore.FirestoreOptions
import com.joni.hocs.models.OrderStatus
import com.joni.hocs.models.OrderStatusUpdateRequest

import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*



@RestController
@RequestMapping("/api/orders")
class OrderController(private val firestore: Firestore) {


    @PutMapping("/{orderId}")
    fun updateOrderStatus(
        @PathVariable orderId: String,
        @RequestBody @Valid updateRequest: OrderStatusUpdateRequest
    ): ResponseEntity<Map<String, String>> {
        return try {
            // Fetch the order from Firestore
            val orderDocRef = firestore.collection("orders").document(orderId)
            val orderSnapshot = orderDocRef.get()
            val snap = orderSnapshot.get()

            if (!snap.exists()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(mapOf("error" to "Order not found"))
            }

            // Update the delivery status
            orderDocRef.update("deliveryStatus", updateRequest.deliveryStatus)

            ResponseEntity.ok(mapOf("status" to "Order status updated"))
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(mapOf("error" to "Failed to update order status: ${e.message}"))
        }
    }

    @GetMapping("/{orderId}")
    fun getOrderStatus(@PathVariable orderId: String): ResponseEntity<OrderStatus> {
        return try {
            // Fetch the order from Firestore
            val orderDocRef = firestore.collection("orders").document(orderId)
            val orderSnapshot = orderDocRef.get()
            val snap = orderSnapshot.get()

            if (!snap.exists()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(null)
            }

            // Convert the document to OrderStatus object
            val orderStatus = snap.toObject(OrderStatus::class.java)

            ResponseEntity.ok(orderStatus)
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(null)
        }
    }

    data class OrderStatusUpdateRequest(val deliveryStatus: String)
    data class OrderStatus(val userId: String, val items: List<OrderItem>, val totalPrice: Double, val deliveryStatus: String)
    data class OrderItem(val serviceId: String, val quantity: Int)
}
