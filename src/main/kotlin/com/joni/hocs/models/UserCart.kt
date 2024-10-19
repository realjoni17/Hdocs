package com.joni.hocs.models

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document("usercarts")
data class UserCart(
    @Id val id: String? = null,
    val userId: String,
    val serviceId: String,
    val quantity : Int
)