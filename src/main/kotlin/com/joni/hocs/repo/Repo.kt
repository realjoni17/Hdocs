/*
package com.joni.hocs.repo

import com.joni.hocs.models.*
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository

@Repository
interface UserRepository : MongoRepository<User, String>


@Repository
interface UserCartRepository : MongoRepository<UserCart, String> {
    fun findByUserId(userId: String): List<UserCart>
    fun findByUserIdAndServiceId(userId: String, serviceId: String): UserCart?
}

@Repository
interface ServiceRepository : MongoRepository<Service, String> {
    fun findAllById(ids: List<String>): List<Service>
}

@Repository
interface OrderRepository : MongoRepository<Order, String>

@Repository
interface OrderStatusRepository : MongoRepository<OrderStatus, String>*/
