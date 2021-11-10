package com.example.smartarm20

import android.os.Bundle
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import ingenieria.jhr.bluetoothjhr.BluetoothJhr
import kotlinx.android.synthetic.main.activity_temperatura.*
import kotlin.concurrent.thread


class TemperaturaActivity : AppCompatActivity() {

    lateinit var blue: BluetoothJhr
    var initHilo= false
    var hilo= true
    var mensaje = ""



    @RequiresApi(30)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_temperatura)

        blue = BluetoothJhr(this,MainActivity::class.java)

        termometro.tempMax(100f)
        termometro.tempMin(0f)
        termometro.timeAnimation=3000f

        thread(start= true) {
            while(!initHilo){
                Thread.sleep(500)
            }
            while(hilo){
                blue.mTx("t")
                Thread.sleep(1000)
                mensaje= blue.mRx()
                if(mensaje!=""){
                    if(hilo){
                        runOnUiThread(Runnable {
                           displayin.text=  mensaje
                            termometro.tempSet(mensaje.toFloat())

                        })
                    }else{
                        break
                    }
                    blue.mensajeReset()
                }
                Thread.sleep(1000)
            }
            print("*************************")
            print("*****termono hilo*******")
            print("*************************")
        }

    }

    override fun onResume() {
        super.onResume()
        initHilo= blue.conectaBluetooth()
    }

    override fun onPause() {
        super.onPause()
        hilo=false
        initHilo=true
        blue.exitConexion()
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        initHilo=true
        hilo=false
    }
}
