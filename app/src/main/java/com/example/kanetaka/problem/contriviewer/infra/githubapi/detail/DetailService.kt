package com.example.kanetaka.problem.contriviewer.infra.githubapi.detail

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Url

private const val BASE_URL = "https://api.github.com/users/"

interface DetailServiceApi {
    @GET
    suspend fun getContributor(@Url login: String): ContributorModel
}

private val moshi = Moshi.Builder()
    .add(KotlinJsonAdapterFactory())
    .build()

private val retrofit = Retrofit.Builder()
    .addConverterFactory(MoshiConverterFactory.create(moshi))
    .baseUrl(BASE_URL)
    .build()

object DetailService {
    val retrofitService: DetailServiceApi by lazy { retrofit.create(DetailServiceApi::class.java) }
}