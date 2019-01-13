package com.nearhuscarl.smack.Controllers

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.nearhuscarl.smack.R
import kotlinx.android.synthetic.main.activity_forgot_password.*
import kotlinx.android.synthetic.main.activity_login.*

class ForgotPasswordActivity : AppCompatActivity() {

    lateinit var mAuth:FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_password)
    }

    fun forgotPassSendMailBtnClicked(view: View) {
        val email = forgotPassEmailTxt.text.toString().trim()
        if (TextUtils.isEmpty(email)) {
            loginEmailTxt.error = "Enter email"
            return
        }
        resetPassWord(email)
    }

    private fun resetPassWord(email: String) {

        mAuth = FirebaseAuth.getInstance()
        mAuth.sendPasswordResetEmail(email).addOnCompleteListener{
            if (it.isComplete){
                val loginIntent = Intent(applicationContext, LoginActivity::class.java)
                startActivity(loginIntent)
                Toast.makeText(applicationContext, "Check your email to reset your password", Toast.LENGTH_LONG).show()
            }
        }
    }

}
