package com.example.jetpackhomework




import retrofit2.Call
import retrofit2.http.GET


interface BooksApi {
    @GET("books.json")
    suspend fun getBooks(): Call<List<Book>>
}