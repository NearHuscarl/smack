package com.nearhuscarl.smack.Controllers

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.nearhuscarl.smack.R
import com.nearhuscarl.smack.Services.Firebase
import com.nearhuscarl.smack.Utilities.*
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_user_profile.*

class UserProfileActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_profile)

        val userId = intent.getStringExtra(USER_ID_REF)
        val userRef = Firebase.database.child("$USERS_REF/$userId")

        userRef.addListenerForSingleValueEvent(onLoadingUserInfo)
    }

    private val onLoadingUserInfo = object : ValueEventListener {
        override fun onDataChange(dataSnapshot: DataSnapshot) {

            val avatarUrl = dataSnapshot.child(AVATAR_REF).value.toString()
            val name = dataSnapshot.child(NAME_REF).value.toString()
            val email = dataSnapshot.child(EMAIL_REF).value.toString()
            val phone = dataSnapshot.child(PHONE_REF).value.toString()
            val gender = dataSnapshot.child(GENDER_REF).value.toString()
            val birthday = dataSnapshot.child(BIRTHDAY_REF).value.toString()
            val location = dataSnapshot.child(LOCATION_REF).value.toString()
            val job = dataSnapshot.child(JOB_REF).value.toString()
            val registerDate = dataSnapshot.child(REGISTER_DATE_REF).value.toString()

            Picasso.get()
                    .load(avatarUrl)
                    .resize(400, 0)
                    .placeholder(R.drawable.profiledefault)
                    .into(avatarImageView)

            displayNameTextView.text = getInfoOrEmpty(name)
            emailTextView.text = getInfoOrEmpty(email)
            phoneTextView.text = getInfoOrEmpty(phone)
            genderTextView.text = getInfoOrEmpty(gender)
            birthdayTextView.text = getInfoOrEmpty(birthday)
            locationTextView.text = getInfoOrEmpty(location)
            jobTextView.text = getInfoOrEmpty(job)
            registerDateTextView.text = getInfoOrEmpty(registerDate)
        }

        private fun getInfoOrEmpty(str: String): String {
            if (str.trim().isEmpty())
                return "Empty"
            else
                return str
        }

        override fun onCancelled(databaseError: DatabaseError) {
            Log.e("ERROR", "onNewChannel:onCancelled", databaseError.toException())
        }
    }
}
