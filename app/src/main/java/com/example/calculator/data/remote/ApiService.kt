package com.example.calculator.data.remote

import com.example.calculator.data.model.Customer
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface ApiService {
    @GET("api/users")
    suspend fun getCustomers(): List<Customer>

    @POST("api/users")
    suspend fun createCustomer(@Body customer: Customer): Customer

    @PUT("api/users/{id}")
    suspend fun updateCustomer(@Path("id") id: Int, @Body customer: Customer): Customer

    @DELETE("api/users/{id}")
    suspend fun deleteCustomer(@Path("id") id: Int): Response<Unit>
}