package com.nearhuscarl.smack.Services

import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.StorageReference
import com.google.firebase.database.ServerValue
import com.google.firebase.storage.FirebaseStorage

object Firebase {
    lateinit var database: DatabaseReference
    lateinit var storage: StorageReference

    fun initialize()
    {
        // Cache data for offline usage
        FirebaseDatabase.getInstance().setPersistenceEnabled(true)

        database = FirebaseDatabase.getInstance().reference
        storage = FirebaseStorage.getInstance().reference
    }

    fun getServerTimeStamp() : Map<String, String>
    {
        return ServerValue.TIMESTAMP
    }
}