package com.joni.hocs

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserRecord
import com.mongodb.client.MongoClients
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.SimpleMongoClientDatabaseFactory
import org.springframework.stereotype.Service
import java.util.concurrent.ExecutionException

/*
@Configuration
class MongoConfig {

    @Bean
    fun mongoTemplate(): MongoTemplate {
        return MongoTemplate(SimpleMongoClientDatabaseFactory(MongoClients.create("mongodb+srv://jaunivashisth:<db_password>@cluster0.b5jld1e.mongodb.net/?retryWrites=true&w=majority&appName=Cluster0"), "Hdocs"))
    }
}*/
@Service

class AuthService {

    private val auth: FirebaseAuth by lazy {
        FirebaseAuth.getInstance()
    }

    fun verifyUid(uid: String): UserRecord? {
        return try {
            val userRecord = auth.getUserAsync(uid).get()
            println("User verified: ${userRecord.uid}")
            userRecord
        } catch (e: InterruptedException) {
            println("Failed to verify user: ${e.message}")
            null
        } catch (e: ExecutionException) {
            println("Failed to verify user: ${e.message}")
            null
        }
    }
}
