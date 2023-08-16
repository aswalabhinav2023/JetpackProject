package com.example.booktracker

import Book
import androidx.room.*
import com.example.jetpackhomework.PartialBook_finished

@Dao
interface BooksDao {
    @Query("SELECT * FROM books")
    suspend fun getAll(): List<Book>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addAll(books: List<Book>)

    @Update(entity = Book::class)
    suspend fun update(partialBook: PartialBook_finished)

    @Update(entity = Book::class)
    suspend fun updateAll(partialBook: List<PartialBook_finished>)

    @Query("SELECT * FROM books WHERE r_finished = 1")
    suspend fun getAllFinished(): List<Book>

    @Query("SELECT * FROM books WHERE r_id = :id")
    suspend fun getBook(id: Int): Book

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun add(book: Book)
}