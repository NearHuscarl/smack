package com.nearhuscarl.smack.Services

import com.nearhuscarl.smack.Models.Channel
import com.nearhuscarl.smack.Models.Message
import java.util.*

object MessageService {
    val channels: ArrayList<Channel> = arrayListOf()
    val messages: ArrayList<Message> = arrayListOf()

    fun clearMessages() {
        messages.clear()
    }

    fun clearChannels() {
        channels.clear()
    }
}