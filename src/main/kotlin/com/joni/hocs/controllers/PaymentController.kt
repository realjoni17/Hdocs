package com.joni.hocs.controllers

import com.google.api.core.ApiFuture
import com.google.cloud.firestore.FieldPath
import com.google.cloud.firestore.Firestore
import com.google.cloud.firestore.FirestoreOptions
import com.google.cloud.firestore.QuerySnapshot
import com.joni.hocs.models.Order
import com.joni.hocs.models.OrderItem
import com.joni.hocs.models.Service
import com.joni.hocs.models.UserCart

import org.json.JSONObject
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.Instant
import com.razorpay.RazorpayClient
import com.razorpay.RazorpayException
import jakarta.annotation.PostConstruct
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value

@RestController
@RequestMapping("/api")
class PaymentController (private val firestore: Firestore){


    private lateinit var razorpayClient: RazorpayClient

    @Value("\${razorpay.api.key.id}")
    private lateinit var razorpayApiKeyId: String

    @Value("\${razorpay.api.key.secret}")
    private lateinit var razorpayApiKeySecret: String

    private val logger: Logger = LoggerFactory.getLogger(PaymentController::class.java)

    @jakarta.annotation.PostConstruct
    fun init() {
        razorpayClient = RazorpayClient(razorpayApiKeyId, razorpayApiKeySecret)
    }

    @PostMapping("/payment/{userId}")
    fun processPayment(@PathVariable userId: String): ResponseEntity<PaymentResponse> {
        return try {
            // Fetch the user's cart items from Firestore
            val userCartsSnapshot = firestore.collection("userCarts")
                .whereEqualTo("userId", userId)
                .get()
            val snapshot = userCartsSnapshot.get()
            val userCarts = snapshot.documents.mapNotNull { it.toObject(UserCart::class.java) }

            if (userCarts.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(PaymentResponse("No items in the cart."))
            }

            // Retrieve service details for each item in the cart
            val serviceIds = userCarts.map { it.serviceId }
            val servicesSnapshot : ApiFuture<QuerySnapshot> = firestore.collection("services")
                .whereIn(FieldPath.documentId(), serviceIds)
                .get()
            val snap = servicesSnapshot.get()
            val services = snap.documents.associateBy { it.id }.mapValues { it.value.toObject(Service::class.java) }

            // Calculate the total price
            var totalPrice = 0.0
            val orderItems = mutableListOf<OrderItem>()

            for (cart in userCarts) {
                val service = services[cart.serviceId]
                if (service != null) {
                    val itemTotalPrice = service.price * cart.quantity
                    totalPrice += itemTotalPrice
                    orderItems.add(OrderItem(serviceId = cart.serviceId, quantity = cart.quantity))

                    logger.info("Service ID: ${service.id}, Price: ${service.price}, Quantity: ${cart.quantity}, Item Total Price: $itemTotalPrice")
                } else {
                    logger.warn("Service with ID ${cart.serviceId} not found")
                }
            }

            logger.info("Calculated Total Price: $totalPrice")

            // Create payment options as a JSONObject
            val paymentOptions = JSONObject().apply {
                put("amount", (totalPrice * 100).toInt()) // Convert to paise
                put("currency", "INR")
                put("payment_capture", 1)
            }

            // Create a payment order
            val razorpayOrder = razorpayClient.orders.create(paymentOptions)
            val paymentId = razorpayOrder.toJson().optString("id", "")

            // Create and save the order in Firestore
            val order = Order(
                userId = userId,
                items = orderItems,
                totalPrice = totalPrice,
                deliveryStatus = "Pending",
                createdAt = System.currentTimeMillis() / 1000 // Convert to seconds
            )
            firestore.collection("orders").add(order)

            // Clear the user's cart
            firestore.collection("userCarts").document(userId).delete()

            // Return a success response with payment ID and total amount
            ResponseEntity.ok(PaymentResponse("Payment ID: $paymentId, Total Amount: $totalPrice"))
        } catch (e: RazorpayException) {
            logger.error("RazorpayException occurred while processing payment for user $userId: ${e.message}", e)
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(PaymentResponse("Payment processing failed: ${e.message}"))
        } catch (e: Exception) {
            logger.error("Exception occurred while processing payment for user $userId: ${e.message}", e)
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(PaymentResponse("An unexpected error occurred: ${e.message}"))
        }
    }

    data class PaymentResponse(val message: String)
 //   data class UserCart(val userId: String, val serviceId: String, val quantity: Int)
  //  data class OrderItem(val serviceId: String, val quantity: Int)
   // data class Order(val userId: String, val items: List<OrderItem>, val totalPrice: Double, val deliveryStatus: String, val createdAt: Long)
//    data class Service(val id: String? = null, val price: Double = 0.0)
}
