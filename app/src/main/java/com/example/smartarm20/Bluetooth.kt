package com.example.smartarm20

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.AdapterView.OnItemClickListener
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.Toast
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.*

internal class Bluetooth : Activity(), OnItemClickListener {

    lateinit var blue: ingenieria.jhr.bluetoothjhr.BluetoothJhr


    var stateConexion = false
    var listAdapter: ArrayAdapter<String>? = null
    var listView: ListView? = null
    var devicesArray: Set<BluetoothDevice>? = null
    var pairedDevices: ArrayList<String>? = null
    var devices: ArrayList<BluetoothDevice?>? = null
    var filter: IntentFilter? = null
    var receiver: BroadcastReceiver? = null

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_prueba)
        init()
        if (btAdapter == null) {
            Toast.makeText(applicationContext, "No bluetooth detected", Toast.LENGTH_SHORT).show()
            finish()
        } else {
            if (!btAdapter!!.isEnabled) {
                turnOnBT()
            }
            viewDips(BluetoothJhr.mBtAdapter.bondedDevices)
            startDiscovery()
        }
    }

    private fun viewDips(dispEmparejados: MutableSet<BluetoothDevice>) {
        if(dispEmparejados.size>0){
            for (device in dispEmparejados){
                BluetoothJhr.mDevicesArrayAdapter.add(device.name+"\n"+device.address)
            }
            BluetoothJhr.listView!!.adapter = BluetoothJhr.mDevicesArrayAdapter
        }else{
            BluetoothJhr.mDevicesArrayAdapter.add("no existen dispositivos vinculados")
        }
    }

    private fun startDiscovery() {
        // TODO Auto-generated method stub
        btAdapter!!.cancelDiscovery()
        btAdapter!!.startDiscovery()
    }

    private fun turnOnBT() {
        val intent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
        startActivityForResult(intent, 1)
    }



    private fun init() {
        lateinit var blue: BluetoothJhr


        var stateConexion = false
        listView = findViewById<View>(R.id.listView) as ListView
        listView!!.onItemClickListener = this
        listAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, 0)
        listView!!.adapter = listAdapter
        btAdapter = BluetoothAdapter.getDefaultAdapter()
        pairedDevices = ArrayList()
        filter = IntentFilter(BluetoothDevice.ACTION_FOUND)
        devices = ArrayList()
        receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                val action = intent.action
                if (BluetoothDevice.ACTION_FOUND == action) {
                    val device =
                        intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
                    devices!!.add(device)
                    var s = ""
                    for (a in pairedDevices!!.indices) {
                        if (device!!.name == pairedDevices!![a]) {
                            //append
                            s = "(Paired)"
                            break
                        }
                    }
                    listAdapter!!.add(
                        """${device!!.name} $s 
${device.address}"""
                    )
                } else if (BluetoothAdapter.ACTION_DISCOVERY_STARTED == action) {
                } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED == action) {
                } else if (BluetoothAdapter.ACTION_STATE_CHANGED == action) {

                }
            }
        }
        registerReceiver(receiver, filter)
        var filter = IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_STARTED)
        registerReceiver(receiver, filter)
        filter = IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
        registerReceiver(receiver, filter)
        filter = IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED)
    }

    override fun onPause() {
        // TODO Auto-generated method stub
        super.onPause()
        unregisterReceiver(receiver)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_CANCELED) {
            Toast.makeText(
                applicationContext,
                "Bluetooth must be enabled to continue",
                Toast.LENGTH_SHORT
            ).show()
            finish()
        }
    }

    override fun onItemClick(arg0: AdapterView<*>?, arg1: View, arg2: Int, arg3: Long) {
        // TODO Auto-generated method stub
        if (btAdapter!!.isDiscovering) {
            btAdapter!!.cancelDiscovery()
        }
        if (listAdapter!!.getItem(arg2)!!.contains("(Paired)")) {
            val selectedDevice = devices!![arg2]
            val connect = ConnectThread(selectedDevice)
            connect.start()
        } else {
            Toast.makeText(applicationContext, "device is not paired", Toast.LENGTH_SHORT).show()
        }
    }

    private inner class ConnectThread(device: BluetoothDevice?) : Thread() {
        private val mmSocket: BluetoothSocket?
        private val mmDevice: BluetoothDevice?
        override fun run() {
            // Cancel discovery because it will slow down the connection
            btAdapter!!.cancelDiscovery()
            try {
                // Connect the device through the socket. This will block
                // until it succeeds or throws an exception
                mmSocket!!.connect()
                //connectedThread = new ConnectedThread(mmSocket);
            } catch (connectException: IOException) {
                // Unable to connect; close the socket and get out
                try {
                    mmSocket!!.close()
                } catch (closeException: IOException) {
                }
                return
            }

            // Do work to manage the connection (in a separate thread)
            mHandler.obtainMessage(SUCCESS_CONNECT, mmSocket).sendToTarget()
        }

        /** Will cancel an in-progress connection, and close the socket  */
        fun cancel() {
            try {
                mmSocket!!.close()
            } catch (e: IOException) {
            }
        }

        init {
            // Use a temporary object that is later assigned to mmSocket,
            // because mmSocket is final
            var tmp: BluetoothSocket? = null
            mmDevice = device

            // Get a BluetoothSocket to connect with the given BluetoothDevice
            try {
                // MY_UUID is the app's UUID string, also used by the server code
                tmp = device!!.createRfcommSocketToServiceRecord(MY_UUID)
            } catch (e: IOException) {
            }
            mmSocket = tmp
        }
    }

    internal class ConnectedThread(private val mmSocket: BluetoothSocket) : Thread() {
        private val mmInStream: InputStream?
        private val mmOutStream: OutputStream?
        var sbb = StringBuffer()
        override fun run() {
            var buffer: ByteArray // buffer store for the stream
            var bytes: Int // bytes returned from read()

            // Keep listening to the InputStream until an exception occurs
            while (true) {
                try {
                    try {
                        sleep(30)
                    } catch (e: InterruptedException) {
                        // TODO Auto-generated catch block
                        e.printStackTrace()
                    }
                    buffer = ByteArray(1024)
                    // Read from the InputStream
                    bytes = mmInStream!!.read(buffer)
                    // Send the obtained bytes to the UI activity
                    mHandler.obtainMessage(MESSAGE_READ, bytes, -1, buffer).sendToTarget()
                } catch (e: IOException) {
                    break
                }
            }
        }

        /* Call this from the main activity to send data to the remote device */
        fun write(income: String) {
            try {
                mmOutStream!!.write(income.toByteArray())
                for (i in income.toByteArray().indices) Log.v(
                    "outStream" + Integer.toString(i), Character.toString(
                        java.lang.Byte.toString(income.toByteArray()[i]).toInt().toChar()
                    )
                )
                try {
                    sleep(20)
                } catch (e: InterruptedException) {
                    // TODO Auto-generated catch block
                    e.printStackTrace()
                }
            } catch (e: IOException) {
            }
        }

        /* Call this from the main activity to shutdown the connection */
        fun cancel() {
            try {
                mmSocket.close()
            } catch (e: IOException) {
            }
        }

        init {
            var tmpIn: InputStream? = null
            var tmpOut: OutputStream? = null

            // Get the input and output streams, using temp objects because
            // member streams are final
            try {
                tmpIn = mmSocket.inputStream
                tmpOut = mmSocket.outputStream
            } catch (e: IOException) {
            }
            mmInStream = tmpIn
            mmOutStream = tmpOut
        }
    }

    companion object {
        fun disconnect() {
            if (connectedThread != null) {
                connectedThread!!.cancel()
                connectedThread = null
            }
        }

        fun gethandler(handler: Handler) { //Bluetooth handler
            mHandler = handler
        }

        var mHandler = Handler()
        var connectedThread: ConnectedThread? = null
        val MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
        const val SUCCESS_CONNECT = 0
        const val MESSAGE_READ = 1
        var btAdapter: BluetoothAdapter? = null
    }

}