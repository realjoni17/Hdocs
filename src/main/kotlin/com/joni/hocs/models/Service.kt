package com.joni.hocs.models

import jakarta.validation.constraints.NotBlank
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document("services")
data class Service(
    @Id val id: String? = null,
    @field:NotBlank val name: String,
    @field:NotBlank val description: String,
    val price : Int
)
