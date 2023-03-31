package com.bcgg.network

import com.google.gson.annotations.SerializedName

data class NaverMapKey(
    @SerializedName("X-NCP-APIGW-API-KEY-ID") val clientId: String,
    @SerializedName("X-NCP-APIGW-API-KEY") val clientSecret: String
)
