package com.charlesawoodson.namegame.api

import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.moshi.MoshiConverterFactory

object WillowTreeServiceFactory {

    private const val BASE_URL = "https://willowtreeapps.com"

    private fun retrofit(): Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
        .addConverterFactory(MoshiConverterFactory.create())
        .build()

    val willowTreeApi: WillowTreeService = retrofit().create(WillowTreeService::class.java)

}