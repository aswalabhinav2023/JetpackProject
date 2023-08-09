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

class BookTrackerViewModel(private val stateHandle: SavedStateHandle): ViewModel() {
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
        val id = stateHandle.get<Int>("book_id") ?: 0
        getBook(id)
    }

    private fun getbooks() {
        viewModelScope.launch(errorHandler) {
            val books = getAllBooks()
                state.value = books.restoreFinishedField()
        }
    }

   private suspend fun getAllBooks(): List<Book>{
       return withContext(Dispatchers.ID){
           try {
               val books = api.getBooks()
               booksDao.addAll(books)
               return@withContext books
           }catch (e: Exception){
               when(e){
                   is UnknownHostException,
                   is ConnectException,
                   is HttpException -> {
                           return@withContext booksDao.getAll()
                       }else -> throw e
               }
           }

       }
   }

    private fun List<Book>.restoreFinishedField(): List<Book> {
        stateHandle.get<List<Int>?>("finished")?.let {selectedIds ->
            val booksMap = this.associateBy { it.id },toMutableMap()
            selectedIds.forEach {id ->
               val book = booksMap[id] ?: return@forEach
               booksMap[id] = book.copy(finished = true)
            }
            return this
        }
    }

    override fun onCleared() {
        super.onCleared()
        job.cancel()
    }


    fun toggleFinished(id: Int){
        val books = state.value.toMutableList()
        val bookIndex = books.indexOfFirst { it.id == id }
        val book =books[bookIndex]
        books[bookIndex] = book.copy(finished = !book.finished)
        state.value = books
    }

    private suspend fun toggleFinished(id: Int, oldValue: Boolean) =
        withContext(Dispatchers.IO){
            booksDao.Update(
                PartialBook_finished(
                    id = id,
                    finished = !oldValue
                )
            )
        }






}



