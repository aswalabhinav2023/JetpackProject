package com.example.jetpackhomework

import android.app.Application
import android.content.Context

class BookApplication: Application() {
    init {
        var app = this
    }
    companion object{
        private lateinit var  app: BookApplication
        fun getAppContext(): Context = app.applicationContext
    }
}