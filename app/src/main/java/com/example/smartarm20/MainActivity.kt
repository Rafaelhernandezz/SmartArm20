package com.example.smartarm20

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)






        BluetoothJhr.parameters(this,listBluetooth,Pulso::class.java,this,MainActivity::class.java)

        BluetoothJhr.onBluetooth()

        listBluetooth.setOnItemClickListener { parent, view, position, id ->

            BluetoothJhr.bluetoothSeleccion(position)

            true

        }


    }




}