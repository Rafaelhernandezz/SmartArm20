package com.example.smartarm20

import android.annotation.SuppressLint
import android.bluetooth.BluetoothSocket
import android.content.Intent
import android.content.pm.ActivityInfo
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.util.Log
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Toast
import android.widget.ToggleButton
import androidx.appcompat.app.AppCompatActivity
import com.jjoe64.graphview.GraphView
import com.jjoe64.graphview.GraphView.GraphViewData
import com.jjoe64.graphview.GraphView.LegendAlign
import com.jjoe64.graphview.GraphViewSeries
import com.jjoe64.graphview.GraphViewSeries.GraphViewStyle
import com.jjoe64.graphview.LineGraphView
import ingenieria.jhr.bluetoothjhr.BluetoothJhr

class Pulso :  AppCompatActivity(),View.OnClickListener {

    lateinit var blue: BluetoothJhr


    var stateConexion = false

    override fun onBackPressed() {
        // TODO Auto-generated method stub
        if (Bluetooth.connectedThread != null) {
            Bluetooth.connectedThread!!.write("Q")
        } //Stop streaming
        super.onBackPressed()
    }

    //Button init
    var bXminus: Button? = null
    var bXplus: Button? = null
    var tbLock: ToggleButton? = null
    var tbScroll: ToggleButton? = null
    var tbStream: ToggleButton? = null
    var bConnect: Button? = null
    var bDisconnect: Button? = null
    var mHandler: Handler = object : Handler() {
        @SuppressLint("HandlerLeak")
        override fun handleMessage(msg: Message) {
            // TODO Auto-generated method stub
            super.handleMessage(msg)
            when (msg.what) {
                Bluetooth.SUCCESS_CONNECT -> {
                    Bluetooth.ConnectedThread(msg.obj as BluetoothSocket)
                    Toast.makeText(applicationContext, "Connected!", Toast.LENGTH_SHORT).show()
                    val s = "successfully connected"
                    Bluetooth.connectedThread!!.start()
                }
                Bluetooth.MESSAGE_READ -> {
                    val readBuf = msg.obj as ByteArray
                    var strIncom = String(readBuf, 0, 50) // create string from bytes array
                    Log.d("strIncom", strIncom)
                    if (strIncom.indexOf('.') == 2 && strIncom.indexOf('s') == 0) {
                        strIncom = strIncom.replace("s", "")
                        if (isFloatNumber(strIncom)) {
                            Series?.appendData(
                                GraphViewData(
                                    graph2LastXValue,
                                    strIncom.toDouble()
                                ), AutoScrollX
                            )

                            //X-axis control
                            if (graph2LastXValue >= Xview && Lock == true) {
                                Series?.resetData(arrayOf())
                                graph2LastXValue = 0.0
                            } else graph2LastXValue += 0.1
                            if (Lock == true) graphView!!.setViewPort(
                                0.0,
                                Xview.toDouble()
                            ) else graphView?.setViewPort(
                                graph2LastXValue - Xview, Xview.toDouble()
                            )

                            //refresh
                            GraphView?.removeView(graphView)
                            GraphView?.addView(graphView)
                        }
                    }
                }
            }
        }

        fun isFloatNumber(num: String): Boolean {
            //Log.d("checkfloatNum", num);
            try {
                num.toDouble()
            } catch (nfe: NumberFormatException) {
                return false
            }
            return true
        }
    }

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        requestWindowFeature(Window.FEATURE_NO_TITLE) //Hide title
        this.window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        ) //Hide Status bar
        setContentView(R.layout.activity_pulso)
        //set background color
        val background = findViewById<View>(R.id.bg) as LinearLayout
        background.setBackgroundColor(Color.WHITE)
        init()
        ButtonInit()

        blue = BluetoothJhr(this,MainActivity::class.java)
    }

    fun init() {
        Bluetooth.gethandler(mHandler)

        //init graphview
        GraphView = findViewById<View>(R.id.Graph) as LinearLayout
        // init example series data-------------------
        Series = GraphViewSeries(
            "Signal",
            GraphViewStyle(Color.YELLOW, 2), arrayOf(GraphViewData(0.0, 0.0))
        )
        graphView = LineGraphView(
            this // context
            , "Graph" // heading
        )
        (graphView as LineGraphView).setViewPort(0.0, Xview.toDouble())
        (graphView as LineGraphView).setScrollable(true)
        (graphView as LineGraphView).setScalable(true)
        (graphView as LineGraphView).setShowLegend(true)
        (graphView as LineGraphView).setLegendAlign(LegendAlign.BOTTOM)
        (graphView as LineGraphView).setManualYAxis(true)
        (graphView as LineGraphView).setManualYAxisBounds(50.0, 0.0)
        (graphView as LineGraphView).addSeries(Series) // data
        GraphView?.addView(graphView)
    }

    fun ButtonInit() {




        bConnect = findViewById<View>(R.id.bConnect) as Button
        bConnect!!.setOnClickListener(this)
        bDisconnect = findViewById<View>(R.id.bDisconnect) as Button
        bDisconnect!!.setOnClickListener(this)
        //X-axis control button
        bXminus = findViewById<View>(R.id.bXminus) as Button
        bXminus!!.setOnClickListener(this)
        bXplus = findViewById<View>(R.id.bXplus) as Button
        bXplus!!.setOnClickListener(this)
        //
        tbLock = findViewById<View>(R.id.tbLock) as ToggleButton
        tbLock!!.setOnClickListener(this)
        tbScroll = findViewById<View>(R.id.tbScroll) as ToggleButton
        tbScroll!!.setOnClickListener(this)
        tbStream = findViewById<View>(R.id.tbStream) as ToggleButton
        tbStream?.setOnClickListener(this)
        //init toggleButton
        Lock = true
        AutoScrollX = true
        Stream = true
    }

    override fun onClick(v: View) {
        // TODO Auto-generated method stub
        when (v.id) {
            R.id.bConnect -> startActivity(Intent("android.intent.action.MAIN"))
            R.id.bDisconnect -> Bluetooth.disconnect()
            R.id.bXminus -> if (Xview > 1) Xview--
            R.id.bXplus -> if (Xview < 30) Xview++
            R.id.tbLock -> if (tbLock!!.isChecked) {
                Lock = true
            } else {
                Lock = false
            }
            R.id.tbScroll -> if (tbScroll!!.isChecked) {
                AutoScrollX = true
            } else {
                AutoScrollX = false
            }
            R.id.tbStream -> if (tbStream?.isChecked == true) {
              blue.mTx("E")

            } else blue.mTx("Q")
        }
    }

    companion object {
        //toggle Button
        var Lock //whether lock the x-axis to 0-5
                = false
        var AutoScrollX //auto scroll to the last x value
                = false
        var Stream //Start or stop streaming
                = false

        //GraphView init
        var GraphView: LinearLayout? = null
        var graphView: GraphView? = null
        var Series: GraphViewSeries? = null

        //graph value
        private var graph2LastXValue = 0.0
        private var Xview = 10
    }

    override fun onResume() {

        super.onResume()
        if (!stateConexion){
            blue.conectaBluetooth()
            stateConexion = true
        }

        //blue.mTx("a")
    }

    override fun onDestroy() {

        super.onDestroy()
        blue.exitConexion()
    }
}