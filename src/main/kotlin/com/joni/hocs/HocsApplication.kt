package com.joni.hocs

import com.google.auth.oauth2.GoogleCredentials
import com.google.cloud.firestore.Firestore
import com.google.cloud.firestore.FirestoreOptions
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import jakarta.annotation.PostConstruct
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.io.InputStream

@SpringBootApplication
class HocsApplication

fun main(args: Array<String>) {
	runApplication<HocsApplication>(*args)
}


@Configuration
class FirebaseConfig {

	private lateinit var firebaseApp: FirebaseApp

	@PostConstruct
	fun init() {
		val serviceAccount: InputStream = this.javaClass.classLoader.getResourceAsStream("api.json")
			?: throw IllegalArgumentException("Service account key file not found")

		val options = FirebaseOptions.builder()
			.setCredentials(GoogleCredentials.fromStream(serviceAccount))
			.build()

		firebaseApp = FirebaseApp.initializeApp(options)
	}

	@Bean
	fun getFirebaseApp(): FirebaseApp {
		return firebaseApp
	}

	@Bean
	fun firestore(): Firestore {
		// Ensure Firestore is initialized with the correct Firebase app
		return FirestoreOptions.getDefaultInstance()
			.toBuilder()
			.setProjectId(firebaseApp.options.projectId) // Use the project ID from FirebaseApp
			.setCredentials(GoogleCredentials.fromStream(this.javaClass.classLoader.getResourceAsStream("api.json")))
			.build()
			.service
	}
}
