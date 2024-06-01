package com.example.intercomeapp.viewmodels

import android.content.Context
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.intercomeapp.data.Constants.MQTT_CLIENT_ID
import com.example.intercomeapp.data.Constants.MQTT_SERVER_PORT
import com.example.intercomeapp.data.Constants.MQTT_SERVER_URI
import com.example.intercomeapp.data.Constants.MQTT_TOPIC_CALL_STATE
import com.example.intercomeapp.data.Constants.MQTT_TOPIC_CONNECTION
import com.example.intercomeapp.data.Constants.MQTT_TOPIC_LIST
import com.example.intercomeapp.data.Constants.MQTT_TOPIC_RESPONSE_CURRENT
import com.example.intercomeapp.data.Constants.MQTT_TOPIC_RESPONSE_TYPE
import com.example.intercomeapp.data.Constants.MQTT_TOPIC_SOUND
import com.example.intercomeapp.data.Constants.MQTT_USER_NAME
import com.example.intercomeapp.data.Constants.MQTT_USER_PASSWORD
import com.example.intercomeapp.utils.Utils.performTimerEvent
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import org.eclipse.paho.client.mqttv3.IMqttActionListener
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken
import org.eclipse.paho.client.mqttv3.IMqttToken
import org.eclipse.paho.client.mqttv3.MqttCallback
import org.eclipse.paho.client.mqttv3.MqttMessage

class MqttViewModel(): ViewModel() {
    private lateinit var mqttServer: MqttRepository
    var mqtt: MutableLiveData<Boolean> = MutableLiveData(false)
    var connection: MutableLiveData<Boolean> = MutableLiveData(false)
    var intercomCallStatus: MutableLiveData<Boolean> = MutableLiveData(false)
    var intercomActionStatus: MutableLiveData<Boolean?> = MutableLiveData(null)

    fun mqttInitialize(context: Context) {
        mqttServer = MqttRepository(context, "${MQTT_SERVER_URI}:${MQTT_SERVER_PORT}", MQTT_CLIENT_ID)
    }

    fun mqttConnect() {
        mqttServer.connect(MQTT_USER_NAME, MQTT_USER_PASSWORD,
            object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken?) {
                    updateLiveData(mqtt, true)
                    MQTT_TOPIC_LIST.forEach {
                        mqttSubscribe(it)
                    }
                }
                override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                    updateLiveData(connection, false)
                    Log.d("MQTT_DEBUGGER", "MQTT connection was failed.")
                    exception?.printStackTrace()

                    try {
                        mqttConnect()
                    } catch (e: Exception) {
                        Log.d("MQTT_DEBUGGER", "MQTT connection function call failed.")
                        e.printStackTrace()
                    }
                }
            },
            object : MqttCallback {
                override fun messageArrived(topic: String?, message: MqttMessage?) {
                    if(topic == MQTT_TOPIC_CALL_STATE) {
                        when(message.toString()) {
                            "0", "false" -> updateLiveData(intercomCallStatus, false)
                            "1", "true" -> updateLiveData(intercomCallStatus, true)
                        }
                    }
                    if(topic == MQTT_TOPIC_RESPONSE_CURRENT) {
                        when(message.toString()) {
                            "0", "false" -> updateLiveData(intercomActionStatus, false)
                            "1", "true" -> updateLiveData(intercomActionStatus, true)
                        }
                    }
                }

                override fun connectionLost(cause: Throwable?) {
                    Log.d("MQTT_DEBUGGER", "MQTT connection was lost.")
                    updateLiveData(mqtt, false)
                    updateLiveData(connection, false)
                    cause?.printStackTrace()
                    try {
                        mqttConnect()
                    } catch (e: Exception) {
                        Log.d("MQTT_DEBUGGER", "MQTT connection function call failed.")
                        e.printStackTrace()
                    }
                }
                override fun deliveryComplete(token: IMqttDeliveryToken?) {}
            })
    }

    private fun <T> updateLiveData(liveData: MutableLiveData<T>, message: T) {
        MainScope().launch {
            liveData.value = message
            liveData.postValue(message)
        }
    }


    fun mqttSubscribe(topic: String) {
        if (mqttServer.isConnected()) {
            mqttServer.subscribe(topic,
                1,
                object : IMqttActionListener {
                    override fun onSuccess(asyncActionToken: IMqttToken?) {
                        if (topic == MQTT_TOPIC_CONNECTION) {
                            mqttPublish(topic, "1")
                            updateLiveData(connection, true)
                        }
                    }
                    override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                        if (topic == MQTT_TOPIC_CONNECTION) {
                            updateLiveData(connection, false)
                        }
                    }
                })
        }
    }

    fun mqttPublish(topic: String, message: String) {
        if (mqttServer.isConnected()) {
            mqttServer.publish(topic,
                message,
                1,
                false,
                object : IMqttActionListener {
                    override fun onSuccess(asyncActionToken: IMqttToken?) {}
                    override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {}
                })
        }
    }

    fun updateIntercomSound(sound: Boolean) {
        mqttPublish(MQTT_TOPIC_SOUND, sound.toString())
    }

    fun updateResponseType(response: String) {
        mqttPublish(MQTT_TOPIC_RESPONSE_TYPE, response)
    }

    fun activateResponseManually(response: Boolean) {
        mqttPublish(MQTT_TOPIC_RESPONSE_CURRENT, response.toString())
    }

    private fun mqttUnsubscribe(topic: String) {
        if (mqttServer.isConnected()) {
            mqttServer.unsubscribe( topic,
                object : IMqttActionListener {
                    override fun onSuccess(asyncActionToken: IMqttToken?) {}
                    override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {}
                })
        }
    }

    fun mqttDisconnect() {
        if (mqttServer.isConnected()) {
            mqttServer.disconnect(object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken?) {
                    updateLiveData(mqtt, false)
                    updateLiveData(connection, false)
                }
                override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {}
            })
        }
    }
}