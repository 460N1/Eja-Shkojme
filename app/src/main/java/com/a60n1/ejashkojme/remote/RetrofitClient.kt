package com.a60n1.ejashkojme.remote

import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory

object RetrofitClient {
    private var retrofit: Retrofit? = null
    fun getClient(baseURL: String?): Retrofit? {
        if (retrofit == null) {
            retrofit = Retrofit.Builder()
                    .baseUrl(baseURL!!)
                    .addConverterFactory(ScalarsConverterFactory.create())
                    .build()
        }
        return retrofit
    }
}