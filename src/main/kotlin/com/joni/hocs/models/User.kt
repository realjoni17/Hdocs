package com.joni.hocs.models

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document


@Document("users")
data class User(
    @Id val id: String? = null,
    val name: String,
    val email: String
)