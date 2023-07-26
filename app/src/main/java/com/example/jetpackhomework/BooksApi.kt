package com.example.jetpackhomework




import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query


interface BooksApi {
    @GET("books.json")
    suspend fun getBooks(): List<Book>


    @GET("books.json?orderBy=\"r_id\"")
    suspend fun getbook(@Query("equal To") id: Int): Map<String, Book>
}