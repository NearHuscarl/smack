package com.nearhuscarl.smack.Models

class MessagePayload(
        val id: String,
        val messageBody: String,
        val channelId: String,
        val userName: String,
        val avatarUrl: String,
        val timeStamp: Map<String, String>)

class Message(
        val id: String,
        val messageBody: String,
        val channelId: String,
        val userName: String,
        val avatarUrl: String,
        val timeStamp: String)
