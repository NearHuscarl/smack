package com.nearhuscarl.smack.Controllers

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.nearhuscarl.smack.Models.User
import com.nearhuscarl.smack.R
import com.nearhuscarl.smack.Services.UserDataService
import kotlinx.android.synthetic.main.activity_create_user.*
import java.util.*

class CreateUserActivity : AppCompatActivity() {
    var selectedPhotoUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_user)

        loginBtn.setOnClickListener {
            val loginIntent = Intent(applicationContext, LoginActivity::class.java)
            startActivity(loginIntent)
            finish()
        }

        selectphoto_button_register.setOnClickListener {
            Log.d("CreateUserActivity", "Try to show photo selector")

            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, 0)
        }

        createSpinner.visibility = View.INVISIBLE
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 0 && resultCode == Activity.RESULT_OK && data != null) {
            Log.d("Activity_create_uri", "Photo was selected")

            selectedPhotoUri = data.data

            val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, selectedPhotoUri)

            selectphoto_imageview_register.setImageBitmap(bitmap)
            selectphoto_button_register.alpha = 0f
        }
    }

    private fun uploadImageToFirebaseStorage() {
        val filename = UUID.randomUUID().toString()
        val ref = FirebaseStorage.getInstance().getReference("/userAvatars/$filename")

        ref.putFile(selectedPhotoUri!!)
                .addOnSuccessListener { task ->
                    Log.d(null, "Successfully uploaded image: ${task.metadata?.path}")

                    ref.downloadUrl.addOnSuccessListener {
                        Log.d(null, "File Location: $it")

                        saveUserToFirebaseDatabase(it.toString())
                    }
                }
                .addOnFailureListener {
                    Log.d(null, "Failed to upload image to storage: ${it.message}")
                }
    }

    private fun saveUserToFirebaseDatabase(profileImageUrl: String) {

        val uid = FirebaseAuth.getInstance().uid
        val name = createUserNameTxt.text.toString().trim()
        val email = createEmailTxt.text.toString().trim()
        val userRef = FirebaseDatabase.getInstance().getReference("/users/$uid")

        val user = User(profileImageUrl, "", email, "", "", "", name, "")

        userRef.setValue(user)
                .addOnSuccessListener {
                    Log.d("CreateUserActivity", "Sign up successfully")
                    UserDataService.id = FirebaseAuth.getInstance().currentUser!!.uid
                    UserDataService.name = FirebaseAuth.getInstance().currentUser!!.displayName.toString()
                    UserDataService.email = email

                    val intent = Intent(applicationContext, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                }
                .addOnFailureListener {
                    Log.d("CreateUserActivity", "Failed to set value to database: ${it.message}")
                }
    }

    fun createUserClicked(view: View) {
        val userName = createUserNameTxt.text.toString().trim()
        val password = createPasswordTxt.text.toString().trim()
        val email = createEmailTxt.text.toString().trim()

        if (userName.isNotEmpty() && email.isNotEmpty() && password.isNotEmpty() && selectedPhotoUri != null ) {
            FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener {
                        if (!it.isSuccessful)
                            return@addOnCompleteListener

                        enableSpinner(true)
                        uploadImageToFirebaseStorage()
                        Log.d(null, "Successfully created user with uid: ${it.result.user.uid}")
                    }
                    .addOnFailureListener{
                        Log.d(null, "Failed to create user: ${it.message}")
                        Toast.makeText(this, "Failed to create user", Toast.LENGTH_SHORT).show()
                    }
        } else {
            errorToast("Make sure username, email and password are filled in.")
        }
    }

    private fun errorToast(errorMsg: String = "Something went wrong, please try again.") {
        Toast.makeText(this, errorMsg, Toast.LENGTH_LONG).show()
        enableSpinner(false)
    }

    private fun enableSpinner(enable: Boolean) {
        if (enable) {
            createSpinner.visibility = View.VISIBLE
        } else {
            createSpinner.visibility = View.INVISIBLE
        }

        createUserBtn.isEnabled = !enable
    }
}