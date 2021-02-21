package com.charlesawoodson.namegame.api

import com.charlesawoodson.namegame.api.model.Profile
import io.reactivex.Observable
import retrofit2.http.GET

interface WillowTreeApi {
    @GET("/api/v1.0/profiles")
    fun getProfiles(): Observable<List<Profile>>
}