package com.example.smartarm20

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.smartarm20.databinding.ActivityCheckEmailBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.auth.ktx.userProfileChangeRequest
import com.google.firebase.ktx.Firebase

class CheckEmailActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var binding: ActivityCheckEmailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityCheckEmailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        auth = Firebase.auth

        val user = auth.currentUser

        binding.veficateEmailAppCompatButton.setOnClickListener {
            val profileUpdates = userProfileChangeRequest {  }

            user!!.updateProfile(profileUpdates).addOnCompleteListener { task ->
                if (task.isSuccessful){
                    if (user.isEmailVerified){
                        reload()
                    } else{
                        Toast.makeText(this, "Por favor verifique su correo.", Toast.LENGTH_SHORT).show()
                    }
                }

            }
        }
        binding.signOutImageView.setOnClickListener {
            SignOut()
        }


    }

    public override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null) and update UI accordingly.
        val currentUser = auth.currentUser
        if(currentUser != null){
            if (currentUser.isEmailVerified){
                reload()
            }else {
                SendEmailVerification()
            }

        }
    }


    private fun SendEmailVerification (){
        val user = auth.currentUser
        user!!.sendEmailVerification().addOnCompleteListener(this){ task ->
            if (task.isSuccessful){
                Toast.makeText(this, "Se envio un correo de verficacion.", Toast.LENGTH_SHORT).show()
            }

        }
    }

    private fun reload() {
        val intent = Intent (this, SingInActivity::class.java)
        this.startActivity(intent)
    }

    //funcio  para cerrar sesion
    private fun SignOut (){
        Firebase.auth.signOut()
        val intent = Intent(this, SingInActivity::class.java)
        startActivity(intent)
    }

}