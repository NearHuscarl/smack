package com.nearhuscarl.smack.Models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import com.google.firebase.database.DataSnapshot


@Parcelize
class User(
        val avatar: String,
        val birthday: String,
        val email: String,
        val gender: String,
        val job: String,
        val location: String,
        val name: String,
        val phone: String
) : Parcelable {
    constructor() : this("", "", "", "",
                      "", "", "", "")
}
