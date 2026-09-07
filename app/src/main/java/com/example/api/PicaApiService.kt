package com.example.api

import retrofit2.http.*

interface PicaApiService {

    @GET("categories")
    suspend fun getCategories(): PicaResponse<CategoriesData>

    @GET("comics")
    suspend fun getComics(
        @Query("c") category: String? = null,
        @Query("t") tag: String? = null,
        @Query("a") author: String? = null,
        @Query("s") sort: String = "dd", // dd: 新到旧, da: 旧到新, ld: 最多爱心, vd: 最多指名
        @Query("page") page: Int = 1
    ): PicaResponse<ComicsPageData>

    @GET("comics/random")
    suspend fun getRandomComics(): PicaResponse<RandomComicsData>

    @GET("comics/leaderboard")
    suspend fun getLeaderboard(
        @Query("tt") timeType: String = "H24", // H24: 24小时, D7: 7天, D30: 30天
        @Query("ct") contentType: String = "VC"
    ): PicaResponse<LeaderboardData>

    @GET("comics/{comicId}")
    suspend fun getComicDetail(
        @Path("comicId") comicId: String
    ): PicaResponse<ComicDetailData>

    @GET("comics/{comicId}/eps")
    suspend fun getComicEpisodes(
        @Path("comicId") comicId: String,
        @Query("page") page: Int = 1
    ): PicaResponse<EpsData>

    @GET("comics/{comicId}/order/{order}/pages")
    suspend fun getComicPages(
        @Path("comicId") comicId: String,
        @Path("order") order: Int,
        @Query("page") page: Int = 1
    ): PicaResponse<PagesData>

    @POST("auth/sign-in")
    suspend fun signIn(
        @Body body: Map<String, String>
    ): PicaResponse<SignInResponse>
}
