package com.nearhuscarl.smack.Services

import android.content.Context
import android.util.Log
import com.android.volley.Response
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.Volley
import com.nearhuscarl.smack.Controllers.App
import com.nearhuscarl.smack.Models.Channel
import com.nearhuscarl.smack.Utilities.GET_CHANNELS_URL
import org.json.JSONException

object MessageService {
    val channels: ArrayList<Channel> = arrayListOf()

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
}