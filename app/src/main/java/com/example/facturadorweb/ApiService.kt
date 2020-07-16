package com.example.facturadorweb

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST


interface ApiService {
    @Headers("Content-Type: application/json")
    @POST("Usuario")
    fun createSesion(@Body post: RequestLogin?): Call<ResponseLogin>

    @Headers("Content-Type: application/json")
    @POST("Productos")
    fun sendProduct(@Body post: RequestProduct?): Call<RESP_TRANSACCION>
}