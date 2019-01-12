package com.nearhuscarl.smack.Utilities

const val BASE_URL = "https://chattyk.herokuapp.com/v1"

const val REGISTER_URL = "$BASE_URL/account/register"
const val LOGIN_URL = "$BASE_URL/account/login"
const val CREATE_USER_URL = "$BASE_URL/user/add"
const val GET_USER_URL = "$BASE_URL/user/byEmail"

// request codes
const val BROADCAST_USER_DATA_CHANGE = "BROADCAST_USER_DATA_CHANGE"
const val SIGN_IN_REQUEST_CODE = 1
const val GALLERY_PICK = 2

// firebase references
const val MESSAGES_REF = "messages"
const val SOMEONE_TYPING_REF = "someoneTyping"
const val CHANNELS_REF = "channels"
const val MESSAGE_IMAGE_REF = "messageImages"

const val ID_REF = "id"
const val TYPE_REF = "type"
const val NAME_REF = "name"
const val DESCRIPTION_REF = "description"
const val VALUE_REF = "value"

const val MESSAGE_BODY_REF = "messageBody"
const val USER_ID_REF = "userId"
const val USER_NAME_REF = "userName"
const val AVATAR_URL_REF = "avatarUrl"
const val CHANNEL_ID_REF = "channelId"
const val TIMESTAMP_REF = "timeStamp"
