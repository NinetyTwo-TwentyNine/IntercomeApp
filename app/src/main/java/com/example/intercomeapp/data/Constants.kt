package com.example.intercomeapp.data

object Constants {
    const val APP_MIN_PASSWORD_LENGTH = 8

    const val APP_TOAST_WEAK_CONNECTION = "Looks like there are some problems with connection..."
    const val APP_TOAST_NOT_SIGNED_IN = "You aren't signed in yet."
    const val APP_TOAST_LOGIN_FAILED = "Failed to log in"
    const val APP_TOAST_LOGIN_SUCCESS = "Logged in successfully"
    const val APP_TOAST_SIGNUP_FAILED = "Failed to sign up"
    const val APP_TOAST_SIGNUP_SUCCESS = "Registered successfully"
    const val APP_TOAST_PASSWORD_TOO_SHORT = "Your password should be at least $APP_MIN_PASSWORD_LENGTH characters long."
    const val APP_TOAST_PASSWORDS_DONT_MATCH = "Your passwords don't match. Please confirm your password."

    const val APP_PREFERENCES = "APP_PREFERENCES"
    const val APP_PREFERENCES_STAY = "APP_PREFERENCES_STAY_BOOL"
    const val APP_PREFERENCES_INTERCOM_SOUND = "APP_PREFERENCES_INTERCOM_SOUND_BOOL_MAIL"
    const val APP_PREFERENCES_INTERCOM_RESPONSE = "APP_PREFERENCES_INTERCOM_RESPONSE_MAIL"

    const val INTERCOM_RESPONSE_TYPE_NONE = "none"
    const val INTERCOM_RESPONSE_TYPE_NEVER = "never"
    const val INTERCOM_RESPONSE_TYPE_ONCE = "once"
    const val INTERCOM_RESPONSE_TYPE_ALWAYS = "always"


    const val MQTT_SERVER_URI = "tcp://wqtt.ru"
    const val MQTT_SERVER_PORT = "5031"

    const val MQTT_CLIENT_ID = "xdtcgfvgyjuhijokcfghjkj202"

    const val MQTT_USER_NAME = "123"
    const val MQTT_USER_PASSWORD = "123"

    private const val MQTT_TOPIC_MAIN = "intercom"

    const val MQTT_TOPIC_CONNECTION = "$MQTT_TOPIC_MAIN/connection"
    const val MQTT_TOPIC_RESPONSE_TYPE = "$MQTT_TOPIC_MAIN/response/type"
    const val MQTT_TOPIC_RESPONSE_CURRENT = "$MQTT_TOPIC_MAIN/response/current"
    const val MQTT_TOPIC_CALL_STATE = "$MQTT_TOPIC_MAIN/call/current"
    const val MQTT_TOPIC_SOUND = "$MQTT_TOPIC_MAIN/sound"

    val MQTT_TOPIC_LIST = listOf(MQTT_TOPIC_CONNECTION, MQTT_TOPIC_RESPONSE_CURRENT, MQTT_TOPIC_CALL_STATE)
}
