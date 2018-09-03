package com.nearhuscarl.smack.Services

import android.content.Context
import android.content.Intent
import android.support.v4.content.LocalBroadcastManager
import android.util.Log
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.nearhuscarl.smack.Controllers.App
import com.nearhuscarl.smack.Utilities.*
import org.json.JSONException
import org.json.JSONObject

object AuthService {

    fun registerUser(email: String, password: String, complete: (Boolean) -> Unit) {

        val jsonBody = JSONObject()

        jsonBody.put("email", email)
        jsonBody.put("password", password)

        val requestBody = jsonBody.toString()
        val registerRequest = object : StringRequest(Method.POST, REGISTER_URL, Response.Listener { response ->
            complete(true)
        }, Response.ErrorListener { error ->
            Log.d("ERROR", "Could not register user: ${error.message}")
            complete(false)
        }) {
            override fun getBodyContentType(): String {
                return "application/json; charset=utf-8"
            }

            override fun getBody(): ByteArray {
                return requestBody.toByteArray()
            }
        }

        App.sharedPrefs.requestQueue.add(registerRequest)
    }

    fun loginUser(email: String, password: String, complete: (Boolean) -> Unit) {

        val jsonBody = JSONObject()

        jsonBody.put("email", email)
        jsonBody.put("password", password)

        val requestBody = jsonBody.toString()
        val loginRequest = object : JsonObjectRequest(Method.POST, LOGIN_URL, null, Response.Listener { response ->

            try {
                App.sharedPrefs.userEmail = response.getString("user")
                App.sharedPrefs.authToken = response.getString("token")
                App.sharedPrefs.isLoggedIn = true
                complete(true)
            } catch (e: JSONException) {
                Log.d("JSON", "exception: " + e.message)
                complete(false)
            }

        }, Response.ErrorListener { error ->
            Log.d("ERROR", "Could not login user: ${error.localizedMessage}")
            complete(false)
        }) {
            override fun getBodyContentType(): String {
                return "application/json; charset=utf-8"
            }

            override fun getBody(): ByteArray {
                return requestBody.toByteArray()
            }
        }

        App.sharedPrefs.requestQueue.add(loginRequest)
    }

    fun createUser(name: String, email: String, avatarName: String, avatarColor: String,
                   complete: (Boolean) -> Unit) {

        val jsonBody = JSONObject()

        jsonBody.put("name", name)
        jsonBody.put("email", email)
        jsonBody.put("avatarName", avatarName)
        jsonBody.put("avatarColor", avatarColor)

        val requestBody = jsonBody.toString()
        val createRequest = object : JsonObjectRequest(Method.POST, CREATE_USER_URL, null,
                Response.Listener { response ->

            try {
                UserDataService.name = response.getString("name")
                UserDataService.email = response.getString("email")
                UserDataService.avatarName = response.getString("avatarName")
                UserDataService.avatarColor = response.getString("avatarColor")
                UserDataService.id = response.getString("_id")

                complete(true)
            } catch (e: JSONException) {
                Log.d("JSON", "exception: " + e.message)
                complete(false)
            }

        }, Response.ErrorListener { error ->
            Log.d("ERROR", "Could not add user: ${error.localizedMessage}")
            complete(false)
        }) {
            override fun getHeaders(): MutableMap<String, String> {
                val headers = HashMap<String, String>()

                headers.put("Authorization", "Bearer ${App.sharedPrefs.authToken}")
                return headers
            }

            override fun getBodyContentType(): String {
                return "application/json; charset=utf-8"
            }

            override fun getBody(): ByteArray {
                return requestBody.toByteArray()
            }
        }

        App.sharedPrefs.requestQueue.add(createRequest)
    }

    fun findUserByEmail(context: Context, complete: (Boolean) -> Unit) {
        val findUserRequest = object : JsonObjectRequest(Request.Method.GET, "$GET_USER_URL/${App.sharedPrefs.userEmail}",
                null, Response.Listener { response ->
            try {
                UserDataService.name = response.getString("name")
                UserDataService.email = response.getString("email")
                UserDataService.avatarName = response.getString("avatarName")
                UserDataService.avatarColor = response.getString("avatarColor")
                UserDataService.id = response.getString("_id")

                val userDataChange = Intent(BROADCAST_USER_DATA_CHANGE)

                LocalBroadcastManager.getInstance(context).sendBroadcast(userDataChange)
                complete(true)
            } catch (e: JSONException) {
                Log.d("JSON", "Exception: ${e.localizedMessage}")
                complete(false)
            }
        }, Response.ErrorListener { error ->
            Log.d("ERROR", "Could not find user")
            complete(false)
        }) {
            override fun getHeaders(): MutableMap<String, String> {
                val headers = HashMap<String, String>()

                headers.put("Authorization", "Bearer ${App.sharedPrefs.authToken}")
                return headers
            }
        }

        App.sharedPrefs.requestQueue.add(findUserRequest)
    }
}