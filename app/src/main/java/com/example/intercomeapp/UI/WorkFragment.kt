package com.example.intercomeapp.UI

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.SwitchCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.intercomeapp.R
import com.example.intercomeapp.data.Constants.APP_PREFERENCES_INTERCOM_RESPONSE
import com.example.intercomeapp.data.Constants.APP_PREFERENCES_INTERCOM_SOUND
import com.example.intercomeapp.data.Constants.INTERCOM_RESPONSE_TYPE_ALWAYS
import com.example.intercomeapp.data.Constants.INTERCOM_RESPONSE_TYPE_NEVER
import com.example.intercomeapp.data.Constants.INTERCOM_RESPONSE_TYPE_NONE
import com.example.intercomeapp.data.Constants.INTERCOM_RESPONSE_TYPE_ONCE
import com.example.intercomeapp.data.Constants.MQTT_TOPIC_CALL_STATE
import com.example.intercomeapp.databinding.FragmentWorkBinding
import com.example.intercomeapp.utils.Utils.performTimerEvent
import com.example.intercomeapp.viewmodels.MainActivityViewModel
import com.example.intercomeapp.viewmodels.MqttViewModel

@Suppress("DEPRECATION")
class WorkFragment : Fragment() {
    private lateinit var binding: FragmentWorkBinding

    private val viewModel: MainActivityViewModel by activityViewModels()
    private val mqttClient: MqttViewModel by activityViewModels()

    private lateinit var responseSwitches: ArrayList<SwitchCompat>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentWorkBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        responseSwitches = arrayListOf(binding.openNever, binding.openOnce, binding.openEverytime)

        when(viewModel.getPreference(APP_PREFERENCES_INTERCOM_RESPONSE, INTERCOM_RESPONSE_TYPE_NONE)) {
            INTERCOM_RESPONSE_TYPE_NEVER -> binding.openNever.isChecked = true
            INTERCOM_RESPONSE_TYPE_ALWAYS -> binding.openEverytime.isChecked = true
            INTERCOM_RESPONSE_TYPE_ONCE -> binding.openOnce.isChecked = true
        }
        binding.soundOnHandset.isChecked = viewModel.getPreference(APP_PREFERENCES_INTERCOM_SOUND, true)

        setupResponseSwitch(binding.openNever, INTERCOM_RESPONSE_TYPE_NEVER)
        setupResponseSwitch(binding.openOnce, INTERCOM_RESPONSE_TYPE_ONCE)
        setupResponseSwitch(binding.openEverytime, INTERCOM_RESPONSE_TYPE_ALWAYS)
        binding.soundOnHandset.setOnCheckedChangeListener {v, checked ->
            viewModel.setPreference(APP_PREFERENCES_INTERCOM_SOUND, checked)
            mqttClient.updateIntercomSound(checked)
        }

        binding.hangButton.setOnClickListener {
            if (mqttClient.intercomCallStatus.value == true && mqttClient.intercomActionStatus.value == null) {
                // mqttClient.activateResponseManually(false)
                imitateIntercomSignals(responseType = INTERCOM_RESPONSE_TYPE_NEVER)
            }
        }
        binding.openButton.setOnClickListener {
            if (mqttClient.intercomCallStatus.value == true && mqttClient.intercomActionStatus.value == null) {
                // mqttClient.activateResponseManually(true)
                imitateIntercomSignals(responseType = INTERCOM_RESPONSE_TYPE_ALWAYS)
            }
        }


        mqttClient.mqttInitialize(requireContext())
        mqttClient.mqttConnect()

        mqttClient.mqtt.observe(viewLifecycleOwner) {
            if (it != null) {
                updateConnectionStatus(binding.mqtt, it)
                updateFinalConnectionStatus(mqttStatus = it)
            }
        }
        mqttClient.connection.observe(viewLifecycleOwner) {
            if (it != null) {
                updateConnectionStatus(binding.broker, it)
                updateFinalConnectionStatus(brokerStatus = it)
            }
        }

        mqttClient.intercomCallStatus.observe(viewLifecycleOwner) {
            if (it) {
                binding.status.setBackgroundDrawable(resources.getDrawable(R.drawable.third))
                binding.status.setText("Звонок на домофон")

                performTimerEvent({
                    imitateIntercomSignals()
                }, 1000L)
            } else {
                binding.status.setBackgroundDrawable(resources.getDrawable(R.drawable.fourth))
                binding.status.setText("Ожидание")
            }

            performTimerEvent({
                updateSwitchesAndManualResponseButtons()
            }, 50L)
        }

        mqttClient.intercomActionStatus.observe(viewLifecycleOwner) {
            if (it != null) {
                if (it) {
                    if (viewModel.getPreference(APP_PREFERENCES_INTERCOM_RESPONSE, INTERCOM_RESPONSE_TYPE_NONE) == INTERCOM_RESPONSE_TYPE_ONCE) {
                        binding.openOnce.isChecked = false
                    }
                    binding.status.setBackgroundDrawable(resources.getDrawable(R.drawable.first_second))
                    binding.status.setText("Открываю")
                } else {
                    binding.status.setBackgroundDrawable(resources.getDrawable(R.drawable.first_second))
                    binding.status.setText("Сбрасываю")
                }

                performTimerEvent({
                    updateSwitchesAndManualResponseButtons()
                }, 50L)
            }
        }
    }




    private fun setupResponseSwitch(switch: SwitchCompat, responseType: String) {
        switch.setOnCheckedChangeListener { v, checked ->
            if (checked) {
                responseSwitches.forEach {
                    if (it != switch) {
                        it.isChecked = false
                    }
                }
                performTimerEvent({
                    viewModel.setPreference(APP_PREFERENCES_INTERCOM_RESPONSE, responseType)
                }, 50L)
            } else {
                viewModel.setPreference(APP_PREFERENCES_INTERCOM_RESPONSE, INTERCOM_RESPONSE_TYPE_NONE)
            }

            performTimerEvent({
                updateSwitchesAndManualResponseButtons()
                mqttClient.updateResponseType(viewModel.getPreference(APP_PREFERENCES_INTERCOM_RESPONSE, INTERCOM_RESPONSE_TYPE_NONE))
            }, 100L)
        }
    }

    private fun updateConnectionStatus(textView: TextView, status: Boolean, onSuccess: (()->(Unit))? = null) {
        if (status) {
            textView.text = "✓"
            textView.setTextColor(resources.getColor(R.color.green))
            onSuccess?.invoke()
        } else {
            textView.text = "×"
            textView.setTextColor(resources.getColor(R.color.red))
        }
    }

    private fun updateFinalConnectionStatus(mqttStatus: Boolean = mqttClient.mqtt.value!!, brokerStatus: Boolean = mqttClient.connection.value!!) {
        updateConnectionStatus(binding.contact, (mqttStatus && brokerStatus)) {
            mqttClient.updateIntercomSound(viewModel.getPreference(APP_PREFERENCES_INTERCOM_SOUND, true))
            mqttClient.updateResponseType(viewModel.getPreference(APP_PREFERENCES_INTERCOM_RESPONSE, INTERCOM_RESPONSE_TYPE_NONE))
        }
    }
    
    private fun updateSwitchesAndManualResponseButtons() {
        responseSwitches.forEach {
            it.isClickable = (!mqttClient.intercomCallStatus.value!!)
            it.isEnabled = (!mqttClient.intercomCallStatus.value!!)
        }

        if (viewModel.getPreference(APP_PREFERENCES_INTERCOM_RESPONSE, INTERCOM_RESPONSE_TYPE_NONE) == INTERCOM_RESPONSE_TYPE_NONE &&
            mqttClient.intercomCallStatus.value == true && mqttClient.intercomActionStatus.value == null) {
            binding.additionalButtons.visibility = View.VISIBLE
            binding.additionalButtons.isClickable = true
        } else {
            binding.additionalButtons.visibility = View.GONE
            binding.additionalButtons.isClickable = false
        }
    }

    /*
    Дальше идёт код (функция  imitateIntercomSignals), которого в дальнейшем будущем проекта
    не будет, поскольку эту информацию отправляло бы физическое устройство.
    */

    private fun imitateIntercomSignals(responseType: String = viewModel.getPreference(APP_PREFERENCES_INTERCOM_RESPONSE, INTERCOM_RESPONSE_TYPE_NONE)) {
        if (responseType == INTERCOM_RESPONSE_TYPE_NONE) { return }

        mqttClient.activateResponseManually(responseType != INTERCOM_RESPONSE_TYPE_NEVER)
        performTimerEvent({
            mqttClient.mqttPublish(MQTT_TOPIC_CALL_STATE, "0")
            mqttClient.intercomActionStatus.value = null
        }, 1000L)
    }
}