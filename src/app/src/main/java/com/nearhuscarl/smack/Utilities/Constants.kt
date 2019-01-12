package com.nearhuscarl.smack.Utilities

const val BASE_URL = "https://chattyk.herokuapp.com/v1"
const val SOCKET_URL = "https://chattyk.herokuapp.com"

const val REGISTER_URL = "$BASE_URL/account/register"
const val LOGIN_URL = "$BASE_URL/account/login"
const val CREATE_USER_URL = "$BASE_URL/user/add"
const val GET_USER_URL = "$BASE_URL/user/byEmail"
const val GET_CHANNELS_URL = "$BASE_URL/channel"
const val GET_MESSAGES_URL = "$BASE_URL/message/byChannel"

const val BROADCAST_USER_DATA_CHANGE = "BROADCAST_USER_DATA_CHANGE"
const val SIGN_IN_REQUEST_CODE = 1

// firebase references
const val MESSAGES_REF = "messages"
const val SOMEONE_TYPING_REF = "someoneTyping"
const val CHANNELS_REF = "channels"

const val ID_REF = "id"
const val NAME_REF = "name"
const val DESCRIPTION_REF = "description"
const val VALUE_REF = "value"

const val MESSAGE_BODY_REF = "messageBody"
const val USER_ID_REF = "userId"
const val USER_NAME_REF = "userName"
const val AVATAR_URL_REF = "avatarUrl"
const val CHANNEL_ID_REF = "channelId"
const val TIMESTAMP_REF = "timeStamp"
