package com.example.jetpackhomework

import Book
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.lang.Exception
import java.net.ConnectException
import java.net.UnknownHostException

class BooksViewModel(private val stateHandle: SavedStateHandle): ViewModel() {
    private var api: BooksApi
    private var booksDao = BooksDb.getDaoInstance(BookApplication.getAppContext())
    val state = mutableStateOf(emptyList<Book>())
    private val errorHandler = CoroutineExceptionHandler{_, e ->
        e.printStackTrace()
    }

    init{
        val retrofit: Retrofit = Retrofit.Builder()
            .addConverterFactory(
                GsonConverterFactory.create()
            )
            .baseUrl("https://booklist-6d5b2-default-rtdp.firebaseio.com/")
            .build()
        api = retrofit.create(BooksApi::class.java)

        getbooks()
    }

    private fun getbooks() {
        viewModelScope.launch(errorHandler) {
            val books = getAllBooks()
                state.value = books
        }
    }

   private suspend fun getAllBooks(): List<Book>{
       return withContext(Dispatchers.IO){
           try {
               val books = api.getBooks()
               booksDao.addAll(books)
               return@withContext books
           }catch (e: Exception){
               when(e){
                   is UnknownHostException,
                   is ConnectException,
                   is HttpException -> {
                           throw Exception("Error")
                       }else -> throw e
               }
           }
           return@withContext booksDao.getAll()
       }
   }

    private suspend fun refreshCache(){
        val remotebooks = api.getBooks()
        val finishedBooks = booksDao.getAllFinished()
        booksDao.addAll(remotebooks)
        booksDao.updateAll(
            finishedBooks.map{
                PartialBook_finished(it.id,true)
            }
        )
    }

    fun toggleFinished(id: Int){
        val books = state.value.toMutableList()
        val bookIndex = books.indexOfFirst { it.id == id }
        val book =books[bookIndex]
        books[bookIndex] = book.copy(finished = !book.finished)
        viewModelScope.launch {
            val updatedBooks = toggleFinishedDb(id, book.finished)
            state.value = updatedBooks
        }
    }

    private suspend fun toggleFinishedDb(id: Int, oldValue: Boolean) =
        withContext(Dispatchers.IO){
            booksDao.update(
                PartialBook_finished(
                    id = id,
                    finished = !oldValue
                )
            )
            booksDao.getAll()
        }



}



