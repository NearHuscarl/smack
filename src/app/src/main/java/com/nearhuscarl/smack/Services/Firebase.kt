package com.nearhuscarl.smack.Services

import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ServerValue

object Firebase {
    lateinit var database: DatabaseReference

    fun Initialize()
    {
        // Cache data for offline usage
        FirebaseDatabase.getInstance().setPersistenceEnabled(true)

        database = FirebaseDatabase.getInstance().reference
    }

    fun GetServerTimeStamp() : Map<String, String>
    {
        return ServerValue.TIMESTAMP
    }
}