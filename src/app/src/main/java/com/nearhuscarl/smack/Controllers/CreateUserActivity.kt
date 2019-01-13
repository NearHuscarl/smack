package com.nearhuscarl.smack.Controllers

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.support.v4.content.LocalBroadcastManager
import android.util.Log
import android.view.View
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
import com.google.android.gms.tasks.OnCanceledListener
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthSettings
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.nearhuscarl.smack.Models.User
import com.nearhuscarl.smack.R
import com.nearhuscarl.smack.Services.AuthService
import com.nearhuscarl.smack.Services.Firebase
import com.nearhuscarl.smack.Services.UserDataService
import com.nearhuscarl.smack.Utilities.BROADCAST_USER_DATA_CHANGE
import kotlinx.android.synthetic.main.activity_create_user.*
import kotlinx.android.synthetic.main.notification_template_lines_media.view.*
import java.util.*
import kotlin.collections.HashMap

class CreateUserActivity : AppCompatActivity() {

    lateinit var mAuth: FirebaseAuth
    lateinit var mData: DatabaseReference
    lateinit var mStorage: FirebaseStorage
    val radio: RadioButton = findViewById(radioGroupGender.checkedRadioButtonId)

    override fun onCreate(savedInstanceState: Bundle?) {

        mStorage = FirebaseStorage.getInstance()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_user)

        returnLoginBtn.setOnClickListener {
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

//        radioGroupGender.setOnCheckedChangeListener(
//                RadioGroup.OnCheckedChangeListener { group, checkedId ->
//                    val radio: RadioButton = findViewById(checkedId)
//                }
//        )

//        var id: Int = radioGroupGender.checkedRadioButtonId
//        if (id!=-1){
//            val radio:RadioButton = findViewById(id)
//            Toast.makeText(applicationContext,"On button click : ${radio.text}",
//                    Toast.LENGTH_SHORT).show()
//        }

    }

    fun radio_button_click(view: View){
        // Get the clicked radio button instance
        val radio: RadioButton = findViewById(radioGroupGender.checkedRadioButtonId)
    }

    var selectedPhotoUri: Uri? = null

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
        if (selectedPhotoUri == null) return

        val filename = UUID.randomUUID().toString()
        val ref = FirebaseStorage.getInstance().getReference("/userAvatars/$filename")

        ref.putFile(selectedPhotoUri!!)
                .addOnSuccessListener {
                    Log.d(null, "Successfully uploaded image: ${it.metadata?.path}")

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

        val name = createUserNameTxt.text.toString().trim()
        val email = createEmailTxt.text.toString().trim()
        var birthDay = birthdayTxt.text.toString().trim()
        var job = jobTxt.text.toString().trim()
        var location = createLocationTxt.text.toString().trim()
        var phone = phone_txt.text.toString().trim()
        var gender = radio.text.toString().trim()

        val uid = FirebaseAuth.getInstance().uid ?: ""
        val ref = FirebaseDatabase.getInstance().getReference("/users/$uid")

        val user = User(profileImageUrl, birthDay, email, "", job, location, name, phone)

        ref.setValue(user)
                .addOnSuccessListener {
                    Log.d("CreateUserActivity", "Finally we saved the user to Firebase Database")
                    val Intent = Intent(applicationContext, LoginActivity::class.java)
                    startActivity(Intent)
                    finish()
                }
                .addOnFailureListener {
                    Log.d("CreateUserActivity", "Failed to set value to database: ${it.message}")
                }
    }

    fun createUserClicked(view: View) {

        mAuth = FirebaseAuth.getInstance()
        mData = FirebaseDatabase.getInstance().getReference("Users")

        val userName = createUserNameTxt.text.toString().trim()
        val password = createPasswordTxt.text.toString().trim()
        val email = createEmailTxt.text.toString().trim()

        if (userName.isNotEmpty() && email.isNotEmpty() && password.isNotEmpty() ) {
            FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener {
                        if (!it.isSuccessful) return@addOnCompleteListener
                        uploadImageToFirebaseStorage()
                        enableSpinner(true)
                        Log.d(null, "Successfully created user with uid: ${it.result.user.uid}")
                        Toast.makeText(this,"Successfully Sign up, please log in. ", Toast.LENGTH_LONG).show()
                    }
                    .addOnFailureListener{
                        Log.d(null, "Failed to create user: ${it.message}")
                        Toast.makeText(this, "Failed to create user: ${it.message}", Toast.LENGTH_SHORT).show()
                    }
        } else {
            errorToast("Make sure username, email and password are filled in.")
        }
    }

    fun errorToast(errorMsg: String = "Something went wrong, please try again.") {
        Toast.makeText(this, errorMsg, Toast.LENGTH_LONG).show()
        enableSpinner(false)
    }

    fun enableSpinner(enable: Boolean) {
        if (enable) {
            createSpinner.visibility = View.VISIBLE
        } else {
            createSpinner.visibility = View.INVISIBLE
        }

        createUserBtn.isEnabled = !enable
    }
}