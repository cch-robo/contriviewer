package com.example.kanetaka.problem.contriviewer.infra.githubapi.overview

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

private const val BASE_URL = "https://api.github.com/repos/googlesamples/android-architecture-components/"

interface OverviewServiceApi {
    @GET("contributors")
    suspend fun getContributors(): List<ContributorsModel>
}

private val moshi = Moshi.Builder()
    .add(KotlinJsonAdapterFactory())
    .build()

private val retrofit = Retrofit.Builder()
    .addConverterFactory(MoshiConverterFactory.create(moshi))
    .baseUrl(BASE_URL)
    .build()

object OverviewService {
    val retrofitService : OverviewServiceApi by lazy { retrofit.create(OverviewServiceApi::class.java) }
}