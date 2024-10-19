package com.joni.hocs.controllers

import com.google.cloud.firestore.FieldPath
import com.google.cloud.firestore.Firestore
import com.google.cloud.firestore.FirestoreOptions
import com.joni.hocs.models.Service
import com.joni.hocs.models.UserCart

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.beans.factory.annotation.Autowired



@RestController
@RequestMapping("/api/cart")
class CartController(private val firestore: Firestore) {



    @GetMapping("/{userId}")
    fun getUserCart(@PathVariable userId: String): ResponseEntity<Any> {
        val userCartsSnapshot = firestore.collection("userCarts")
            .whereEqualTo("userId", userId)
            .get()
        val snap = userCartsSnapshot.get()

        val userCarts = snap.documents.mapNotNull { it.toObject(UserCart::class.java) }

        if (userCarts.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(mapOf("error" to "No items found in cart"))
        }

        val serviceIds = userCarts.map { it.serviceId }
        val servicesSnapshot = firestore.collection("services")
            .whereIn(FieldPath.documentId(), serviceIds)
            .get()

         val snapshot = servicesSnapshot.get()
        val serviceMap = snapshot.documents.associateBy { it.id }
        val response = userCarts.mapNotNull { cart ->
            serviceMap[cart.serviceId]?.let { service ->
                CartItemWithService(cart, service.toObject(Service::class.java)!!)
            }
        }

        return ResponseEntity.ok(response)
    }

    @PostMapping("/{userId}/{serviceId}")
    fun addItemToCart(@PathVariable userId: String, @PathVariable serviceId: String): ResponseEntity<Any> {
        val existingItemSnapshot = firestore.collection("userCarts")
            .whereEqualTo("userId", userId)
            .whereEqualTo("serviceId", serviceId)
            .get()
           val esnap = existingItemSnapshot.get()
        if (esnap.documents.isNotEmpty()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(mapOf("error" to "Item already exists in the cart"))
        }

        val newItem = UserCart(serviceId = serviceId, userId = userId, quantity = 1)
        firestore.collection("userCarts").add(newItem)

        return ResponseEntity.status(HttpStatus.CREATED).body(mapOf("inserted_id" to newItem.serviceId, "item" to newItem))
    }

    @DeleteMapping("/{userId}/{serviceId}")
    fun deleteItemFromCart(@PathVariable userId: String, @PathVariable serviceId: String): ResponseEntity<Any> {
        val resultSnapshot = firestore.collection("userCarts")
            .whereEqualTo("userId", userId)
            .whereEqualTo("serviceId", serviceId)
            .get()
       val rsnap = resultSnapshot.get()
        if (rsnap.documents.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(mapOf("error" to "Item not found in cart"))
        }

        rsnap.documents.forEach { document ->
            firestore.collection("userCarts").document(document.id).delete()
        }

        return ResponseEntity.ok(mapOf("message" to "Item removed from cart"))
    }

    data class CartItemWithService(
        val cartItem: UserCart,
        val service: Service
    )

    data class UserCart(
        val serviceId: String = "",
        val userId: String = "",
        val quantity: Int = 1
    )

    data class Service(
        val id: String? = null,
        val price: Double = 0.0,
        // Add other service properties as needed
    )
}
