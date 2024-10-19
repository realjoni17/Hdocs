package com.joni.hocs.controllers

import com.google.api.core.ApiFuture
import com.google.cloud.firestore.FieldPath
import com.google.cloud.firestore.Firestore
import com.google.cloud.firestore.FirestoreOptions
import com.google.cloud.firestore.QuerySnapshot
import com.joni.hocs.models.Service
import com.joni.hocs.models.UserCart

import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*



@RestController
@RequestMapping("/services")
@Validated
class ServiceController(private val firestore: Firestore) {



    @PostMapping
    fun createService(@Valid @RequestBody newService: Service): ResponseEntity<Service> {
        return try {
            val serviceRef = firestore.collection("services").document() // Auto-generate document ID
            val savedService = newService.copy(id = serviceRef.id)
            serviceRef.set(savedService)

            ResponseEntity(savedService, HttpStatus.CREATED)
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null)
        }
    }

    @GetMapping
    fun getServices(): ResponseEntity<List<Service>> {
        return try {
            val future: ApiFuture<QuerySnapshot> = firestore.collection("services").get()
            val querySnapshot: QuerySnapshot = future.get() // Blocking call to get the result
            val services: List<Service> = querySnapshot.documents.mapNotNull { it.toObject(Service::class.java) }
            ResponseEntity.ok(services)
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(emptyList())
        }
    }

    @PostMapping("/cart/{userId}/{serviceId}")
    fun addServiceToCart(
        @PathVariable userId: String,
        @PathVariable serviceId: String
    ): ResponseEntity<Map<String, String>> {
        return try {
            val userCart = UserCart(userId = userId, serviceId = serviceId, quantity = 1)
            val userCartRef = firestore.collection("userCarts").document() // Auto-generate document ID
            userCartRef.set(userCart)

            ResponseEntity.ok(mapOf("message" to "Service added to cart"))
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(mapOf("error" to "Failed to add service to cart"))
        }
    }

    @GetMapping("/cart/{userId}")
    fun getUserTotalServices(@PathVariable userId: String): ResponseEntity<List<Service>> {
        return try {
            // Get user carts based on userId
            val futureCarts: ApiFuture<QuerySnapshot> = firestore.collection("userCarts")
                .whereEqualTo("userId", userId)
                .get()
            val userCartsSnapshot: QuerySnapshot = futureCarts.get() // Blocking call to get the result

            // Map user carts to UserCart objects
            val userCarts: List<UserCart> = userCartsSnapshot.documents.mapNotNull { it.toObject(UserCart::class.java) }

            // Extract service IDs from user carts
            val serviceIds = userCarts.map { it.serviceId }

            // Retrieve services based on the extracted service IDs
            val futureServices: ApiFuture<QuerySnapshot> = firestore.collection("services")
                .whereIn(FieldPath.documentId(), serviceIds)
                .get()
            val servicesSnapshot: QuerySnapshot = futureServices.get() // Blocking call to get the result

            // Map services to Service objects
            val services: List<Service> = servicesSnapshot.documents.mapNotNull { it.toObject(Service::class.java) }

            ResponseEntity.ok(services)
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(emptyList())
        }
    }
}
