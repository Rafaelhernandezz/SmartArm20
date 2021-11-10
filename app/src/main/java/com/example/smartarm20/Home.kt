package com.example.smartarm20

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.android.synthetic.main.activity_home.*


class Home : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)


        val botonnav = findViewById<BottomNavigationView>(R.id.menunav)

        botonnav.selectedItemId = R.id.home

        botonnav.setOnNavigationItemSelectedListener(BottomNavigationView.OnNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.perfil -> {
                    startActivity(
                        Intent(
                            applicationContext, MainActivitylogin::class.java
                        )
                    )
                    overridePendingTransition(0, 0)
                    return@OnNavigationItemSelectedListener true
                }
                R.id.home -> return@OnNavigationItemSelectedListener true
                R.id.ayuda -> {
                    startActivity(
                        Intent(
                            applicationContext, Help::class.java
                        )
                    )
                    overridePendingTransition(0, 0)
                    return@OnNavigationItemSelectedListener true
                }
            }
            false
        })



        btn_vincular.setOnClickListener {
            startActivity(Intent(this,MainActivity::class.java))

        }

        btn_comand.setOnClickListener {
            startActivity(Intent(this,Main2Activity::class.java))
        }

        btn_temp.setOnClickListener {
            startActivity(Intent(this,TemperaturaActivity::class.java))
        }
        btn_pul.setOnClickListener {
            startActivity(Intent(this,Pulso::class.java))
        }
    }
}