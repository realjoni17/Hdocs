package com.joni.hocs.controllers

import com.google.cloud.firestore.Firestore
import com.google.cloud.firestore.FirestoreOptions
import com.google.cloud.firestore.SetOptions

import com.joni.hocs.models.User
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController



@RestController
@RequestMapping("/users")
@Validated
class UserController(private val firestore: Firestore) {



    @PostMapping
    fun createUser(@Valid @RequestBody newUser: User): ResponseEntity<User> {
        return try {
            val userRef = firestore.collection("users").document() // Auto-generate document ID
            val userId = userRef.id // Get the generated ID

            // Save the user to Firestore
            val savedUser = newUser.copy(id = userId)
            userRef.set(savedUser, SetOptions.merge())

            ResponseEntity(savedUser, HttpStatus.CREATED)
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(null) // Consider returning an error message
        }
    }
}
