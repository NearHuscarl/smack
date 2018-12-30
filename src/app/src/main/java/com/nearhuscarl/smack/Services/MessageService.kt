package com.nearhuscarl.smack.Services

import android.content.Context
import android.util.Log
import com.android.volley.Response
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.Volley
import com.nearhuscarl.smack.Controllers.App
import com.nearhuscarl.smack.Models.Channel
import com.nearhuscarl.smack.Models.Message
import com.nearhuscarl.smack.Utilities.GET_CHANNELS_URL
import com.nearhuscarl.smack.Utilities.GET_MESSAGES_URL
import org.json.JSONException

object MessageService {
    val channels: ArrayList<Channel> = arrayListOf()
    val messages: ArrayList<Message> = arrayListOf()

    fun getChannels(complete: (Boolean) -> Unit) {
        val channelsRequest = object : JsonArrayRequest(Method.GET, GET_CHANNELS_URL, null, Response.Listener { response ->
            try {

                for (x in 0 until response.length()) {
                    val channel = response.getJSONObject(x)
                    val name = channel.getString("name")
                    val desc = channel.getString("description")
                    val id = channel.getString("_id")

                    val newChannel = Channel(name, desc, id)
                    this.channels.add(newChannel)
                }
                complete(true)

            } catch (e: JSONException) {
                Log.d("JSON", "Exception: ${e.localizedMessage}")
                complete(false)
            }
        }, Response.ErrorListener { error ->
            Log.d("ERROR", "Could not retrieve channels")
            complete(false)
        }) {
            override fun getBodyContentType(): String {
                return "application/json; charset=utf-8"
            }

            override fun getHeaders(): MutableMap<String, String> {
                val headers = HashMap<String, String>()

                headers.put("Authorization", "Bearer ${App.sharedPrefs.authToken}")
                return headers
            }
        }

        App.sharedPrefs.requestQueue.add(channelsRequest)
    }

    fun getMessages(channelId: String, complete: (Boolean) -> Unit) {

        val url = "$GET_MESSAGES_URL/$channelId"
        val messagesRequest = object : JsonArrayRequest(Method.GET, url, null, Response.Listener { response ->

            clearMessages()
            try {

                for (x in 0 until response.length()) {
                    val message = response.getJSONObject(x)
                    val messageBody = message.getString("messageBody")
                    val channelId = message.getString("channelId")
                    val id = message.getString("_id")
                    val userName = message.getString("userName")
                    val userAvatar = message.getString("userAvatar")
                    val userAvatarColor = message.getString("userAvatarColor")
                    val timeStamp = message.getString("timeStamp")

                    val newMessage = Message(messageBody, userName, channelId, userAvatar, userAvatarColor, id, timeStamp)
                    this.messages.add(newMessage)
                }
                complete(true)

            } catch (e: JSONException) {
                Log.d("JSON", "Exception: ${e.localizedMessage}")
                complete(false)
            }

        }, Response.ErrorListener { error ->
            Log.d("ERROR", "Could not retrieve messages")
            complete(false)
        }) {
            override fun getBodyContentType(): String {
                return "application/json; charset=utf-8"
            }

            override fun getHeaders(): MutableMap<String, String> {
                val headers = HashMap<String, String>()

                headers.put("Authorization", "Bearer ${App.sharedPrefs.authToken}")
                return headers
            }
        }

        App.sharedPrefs.requestQueue.add(messagesRequest)
    }

    fun clearMessages() {
        messages.clear()
    }

    fun clearChannels() {
        channels.clear()
    }
}