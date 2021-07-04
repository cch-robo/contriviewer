package com.example.kanetaka.problem.contriviewer.infra.githubapi.overview

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

// 課題で提供された、GitHub APIは、下記の通りです。
// URL = "https://api.github.com/repos/googlesamples/android-architecture-components/contributors"
//
// 上記は、ページネーションを使っていないため、デフォルトの30件しか取得できません。
// コントリビュータは、60名以上おられるため、ページ最大件数 100件を指定して取得します。
private const val BASE_URL =
    "https://api.github.com/repos/googlesamples/android-architecture-components/"

interface OverviewServiceApi {
    @GET("contributors")
    suspend fun getContributors(
        @Query("per_page") items: Int,
        @Query("page") page: Int,
        @Query("anon") case: Boolean
    ): List<OverviewModel>
}

private val moshi = Moshi.Builder()
    .add(KotlinJsonAdapterFactory())
    .build()

private val retrofit = Retrofit.Builder()
    .addConverterFactory(MoshiConverterFactory.create(moshi))
    .baseUrl(BASE_URL)
    .build()

object OverviewService {
    val retrofitService: OverviewServiceApi by lazy { retrofit.create(OverviewServiceApi::class.java) }
}