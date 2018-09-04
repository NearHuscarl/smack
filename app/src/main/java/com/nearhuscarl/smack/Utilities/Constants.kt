package com.nearhuscarl.smack.Utilities

//const val BASE_URL = "http://10.0.2.2/v1"
const val BASE_URL = "https://chattyk.herokuapp.com/v1"
const val SOCKET_URL = "https://chattyk.herokuapp.com"

const val REGISTER_URL = "$BASE_URL/account/register"
const val LOGIN_URL = "$BASE_URL/account/login"
const val CREATE_USER_URL = "$BASE_URL/user/add"
const val GET_USER_URL = "$BASE_URL/user/byEmail"
const val GET_CHANNELS_URL = "$BASE_URL/channel"
const val GET_MESSAGES_URL = "$BASE_URL/message/byChannel"

const val BROADCAST_USER_DATA_CHANGE = "BROADCAST_USER_DATA_CHANGE"