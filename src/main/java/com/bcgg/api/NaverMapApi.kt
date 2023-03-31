package com.bcgg.api

import retrofit2.http.GET
import retrofit2.http.Query

interface NaverMapApi {

    @GET("/map-direction/v1/driving")
    fun getRealPath(
        @Query("start") start: String, // {lat},{lon}
        @Query("goal") goal: String // {lat},{lon}

    )
}