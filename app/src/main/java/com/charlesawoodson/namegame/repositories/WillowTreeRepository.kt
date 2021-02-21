package com.charlesawoodson.namegame.repositories

import com.charlesawoodson.namegame.api.WillowTreeApi
import com.charlesawoodson.namegame.api.model.Profile
import io.reactivex.Observable

class WillowTreeRepository(private val willowTreeApi: WillowTreeApi) {

    fun getProfiles(): Observable<List<Profile>> {
        return willowTreeApi.getProfiles()
    }

}