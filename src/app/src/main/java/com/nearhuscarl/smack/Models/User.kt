package com.nearhuscarl.smack.Models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import com.google.firebase.database.DataSnapshot


@Parcelize
class User(val username: String, val profileImageUrl: String): Parcelable {
    constructor() : this("", "")
}
