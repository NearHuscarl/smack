package com.nearhuscarl.smack.Controllers

import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import com.nearhuscarl.smack.R
import kotlinx.android.synthetic.main.activity_login.*
import android.text.TextUtils
import com.google.firebase.auth.FirebaseAuth
import com.nearhuscarl.smack.Services.UserDataService

class LoginActivity : AppCompatActivity() {

    lateinit var mAuth :  FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        loginSpinner.visibility = View.INVISIBLE

        forgetPassword_txt.setOnClickListener {
            val forgotPassIntent = Intent(applicationContext, ForgotPasswordActivity::class.java)
            startActivity(forgotPassIntent)
            finish()
        }
    }

    fun loginLoginBtnClicked(view: View) {

        hideKeyboard()

        val email = loginEmailTxt.text.toString().trim()
        val pass_word = loginPasswordTxt.text.toString().trim()

        if (TextUtils.isEmpty(email)) {
            loginEmailTxt.error = "Enter email"
            return
        } else {
            if (TextUtils.isEmpty(pass_word)) {
                loginEmailTxt.error = "Enter password"
                return
            }
        }
        loginUser(email, pass_word)
    }

    private fun loginUser(email: String, password: String) {
        enableSpinner(true)
        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        UserDataService.id = FirebaseAuth.getInstance().currentUser!!.uid
                        UserDataService.name = FirebaseAuth.getInstance().currentUser!!.displayName.toString()
                        UserDataService.email = email

                        val intent = Intent(this, MainActivity::class.java)
                        startActivity(intent)
                        finish()
                    } else {
                        Toast.makeText(this, "Authentication failed", Toast.LENGTH_LONG).show()
                        enableSpinner(false)
                    }
                }
    }

    fun loginCreateUserBtnClicked(view: View) {
        val createUserIntent = Intent(this, CreateUserActivity::class.java)
        startActivity(createUserIntent)
        finish()
    }

    fun errorToast(errorMsg: String = "Something went wrong, please try again.") {
        Toast.makeText(this, errorMsg, Toast.LENGTH_LONG).show()
        enableSpinner(false)
    }

    fun enableSpinner(enable: Boolean) {
        if (enable) {
            loginSpinner.visibility = View.VISIBLE
        } else {
            loginSpinner.visibility = View.INVISIBLE
        }
        loginLoginBtn.isEnabled = !enable
        loginCreateUserBtn.isEnabled = !enable
    }

    fun hideKeyboard() {
        val inputManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

        if (inputManager.isAcceptingText) {
            inputManager.hideSoftInputFromWindow(currentFocus.windowToken, 0)
        }
    }
}
