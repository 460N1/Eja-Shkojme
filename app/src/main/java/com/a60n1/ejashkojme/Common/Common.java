package com.a60n1.ejashkojme.Common;

import com.a60n1.ejashkojme.Remote.IGoogleAPI;
import com.a60n1.ejashkojme.Remote.RetrofitClient;

public class Common {
    private static final String baseURL = "https://maps.googlepis.com";

    public static IGoogleAPI getGoogleAPI() {
        return RetrofitClient.getClient(baseURL).create(IGoogleAPI.class);
    }
}
