package com.mycompany.testflight.data.settings

interface ServerUrlStore {
    fun read(): String?
    fun write(url: String)
    fun clear()
}
