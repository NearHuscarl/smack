package com.nearhuscarl.smack.Controllers

import android.app.Activity
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.nearhuscarl.smack.R
import com.nearhuscarl.smack.Services.Firebase
import com.nearhuscarl.smack.Services.UserDataService
import com.nearhuscarl.smack.Utilities.*
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_user_setting.*
import android.app.DatePickerDialog
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import java.util.*


class UserSettingActivity : AppCompatActivity() {

    private var avatarUrl = ""
    private var newAvatarUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_setting)
        birthdayTextView.keyListener = null
        birthdayTextView.onFocusChangeListener = onBirthdayFocusChange

        val userRef = Firebase.database.child("$USERS_REF/${UserDataService.id}")

        userRef.addListenerForSingleValueEvent(onLoadingUserInfo)
    }

    private val onLoadingUserInfo = object : ValueEventListener {
        override fun onDataChange(dataSnapshot: DataSnapshot) {

            avatarUrl = dataSnapshot.child(AVATAR_REF).value.toString()
            val name = dataSnapshot.child(NAME_REF).value.toString()
            val email = dataSnapshot.child(EMAIL_REF).value.toString()
            val phone = dataSnapshot.child(PHONE_REF).value.toString()
            val gender = dataSnapshot.child(GENDER_REF).value.toString()
            val birthday = dataSnapshot.child(BIRTHDAY_REF).value.toString()
            val location = dataSnapshot.child(LOCATION_REF).value.toString()
            val job = dataSnapshot.child(JOB_REF).value.toString()

            Picasso.get()
                    .load(avatarUrl)
                    .resize(400, 0)
                    .placeholder(R.drawable.profiledefault)
                    .into(avatarImageView)

            displayNameTextView.text = name
            emailTextView.setText(email)
            phoneTextView.setText(phone)

            if (gender == "Male")
                maleRadioBtn.isChecked = true
            else if (gender == "Female")
                femaleRadioBtn.isChecked = true

            birthdayTextView.setText(birthday)
            locationEditText.setText(location)
            jobTextView.setText(job)
        }

        override fun onCancelled(databaseError: DatabaseError) {
            Log.e("ERROR", "onNewChannel:onCancelled", databaseError.toException())
        }
    }

    fun saveSettingBtnClicked(view: View) {
        enableSpinner(true)
        saveSettingBtn.isEnabled = false

        if (newAvatarUri != null) {
            val imageUploadPathRef = Firebase.storage.child("$USER_AVATARS_REF/${UserDataService.id}.jpg")

            imageUploadPathRef.putFile(newAvatarUri!!).continueWithTask { task ->
                if (!task.isSuccessful)
                    throw task.exception!!
                imageUploadPathRef.downloadUrl
            }.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    avatarUrl = task.result.toString()
                    updateSetting()
                }
            }
        }
        else
            updateSetting()
    }

    private fun updateSetting() {
        val name = displayNameTextView.text.toString()
        val email = emailTextView.text.toString()
        val phone = phoneTextView.text.toString()

        var gender = ""
        if (maleRadioBtn.isChecked)
            gender = "Male"
        else if (femaleRadioBtn.isChecked)
            gender = "Female"

        val birthday = birthdayTextView.text.toString()
        val location = locationEditText.text.toString()
        val job = jobTextView.text.toString()
        val userSettingMap = HashMap<String, Any>()


        userSettingMap[AVATAR_REF] = avatarUrl
        userSettingMap[NAME_REF] = name
        userSettingMap[EMAIL_REF] = email
        userSettingMap[PHONE_REF] = phone
        userSettingMap[GENDER_REF] = gender
        userSettingMap[BIRTHDAY_REF] = birthday
        userSettingMap[LOCATION_REF] = location
        userSettingMap[JOB_REF] = job

        Firebase.database.child("$USERS_REF/${UserDataService.id}").setValue(userSettingMap)

        Toast.makeText(this,
                "User setting has been saved!",
                Toast.LENGTH_LONG)
                .show()

        finish()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == GALLERY_PICK && resultCode == Activity.RESULT_OK) {
            if (data == null)
                return
            newAvatarUri = data.data

            Picasso.get()
                    .load(newAvatarUri)
                    .resize(400, 0)
                    .placeholder(R.drawable.profiledefault)
                    .into(avatarImageView)
        }
    }

    fun avatarImageViewClicked(view: View) {
        val galleryIntent = Intent()

        galleryIntent.type = "image/*"
        galleryIntent.action = Intent.ACTION_GET_CONTENT

        startActivityForResult(Intent.createChooser(galleryIntent, "Select new avatar"), GALLERY_PICK)
    }

    private val onBirthdayFocusChange = View.OnFocusChangeListener { v, hasFocus ->
        if (hasFocus) {
            openBirthdayDialog()
        }
    }

    fun birthdayTextViewClicked(view: View) {
        if (birthdayTextView.isFocused) {
            openBirthdayDialog()
        }
    }

    private fun openBirthdayDialog() {
        val calender = Calendar.getInstance()
        val dateDialog = DatePickerDialog(this@UserSettingActivity,
                birthdayPickerListen,
                calender.get(Calendar.YEAR),
                calender.get(Calendar.MONTH),
                calender.get(Calendar.DAY_OF_MONTH))

        dateDialog.show()
    }

    private val birthdayPickerListen = DatePickerDialog.OnDateSetListener { view, year, month, dayOfMonth ->
        birthdayTextView.setText("$dayOfMonth/${month+1}/$year")
    }

    private fun enableSpinner(enable: Boolean) {
        if (enable)
            spinner.visibility = View.VISIBLE
        else
            spinner.visibility = View.INVISIBLE
    }
}
