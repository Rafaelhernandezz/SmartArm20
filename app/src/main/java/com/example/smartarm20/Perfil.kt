package com.example.smartarm20


import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView

class Perfil : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_perfil)

        val botonnav = findViewById<BottomNavigationView>(R.id.menunav)

        botonnav.selectedItemId = R.id.perfil

        botonnav.setOnNavigationItemSelectedListener(BottomNavigationView.OnNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.perfil -> return@OnNavigationItemSelectedListener true
                R.id.home -> {
                    startActivity(
                        Intent(
                            applicationContext, Home::class.java
                        )
                    )
                    overridePendingTransition(0, 0)
                    return@OnNavigationItemSelectedListener true
                }
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

    }
}