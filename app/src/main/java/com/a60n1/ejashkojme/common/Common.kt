package com.a60n1.ejashkojme.common

import com.a60n1.ejashkojme.remote.IGoogleAPI
import com.a60n1.ejashkojme.remote.RetrofitClient

object Common {
    private const val baseURL = "https://maps.googlepis.com"
    @JvmStatic
    val googleAPI: IGoogleAPI
        get() = RetrofitClient.getClient(baseURL)!!.create(IGoogleAPI::class.java)
}