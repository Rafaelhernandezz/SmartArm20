package com.example.smartarm20


import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.smartarm20.databinding.ActivityMainLoginBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.auth.ktx.userProfileChangeRequest
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference


@Suppress("DEPRECATION")
class MainActivitylogin : AppCompatActivity() {

    private lateinit var binding: ActivityMainLoginBinding
    private lateinit var auth: FirebaseAuth
    private val fileResult = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        auth = Firebase.auth

        updateUI()

        binding.updateProfileAppCompatButton.setOnClickListener {
            val name = binding.nameEditText.text.toString()

            updateProfile(name)
        }

        binding.profileImageView.setOnClickListener {
            fileManager()
        }


        binding.updatePasswordTextView.setOnClickListener {
            val intent = Intent(this, UpdatePasswordActivity::class.java)
            this.startActivity(intent)
        }

        binding.deleteAccountTextView.setOnClickListener {
            val intent = Intent(this, DeleteAccountActivity::class.java)
            this.startActivity(intent)
        }

        binding.signOutImageView.setOnClickListener {
            signOut()
        }

        //iniciando el boton de nevagacion menu nav

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

    private  fun updateProfile (name : String) {

        val user = auth.currentUser

        val profileUpdates = userProfileChangeRequest {
            displayName = name
        }

        user!!.updateProfile(profileUpdates)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Se realizaron los cambios correctamente.",
                        Toast.LENGTH_SHORT).show()
                    updateUI()
                }
            }
    }

    private fun fileManager() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        startActivityForResult(intent, fileResult)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == fileResult) {
            if (resultCode == RESULT_OK && data != null) {
                val uri = data.data

                uri?.let { imageUpload(it) }

            }
        }
    }

    private fun imageUpload(mUri: Uri) {

        val user = auth.currentUser
        val folder: StorageReference = FirebaseStorage.getInstance().reference.child("Users")
        val fileName: StorageReference = folder.child("img"+user!!.uid)

        fileName.putFile(mUri).addOnSuccessListener {
            fileName.downloadUrl.addOnSuccessListener { uri ->

                val profileUpdates = userProfileChangeRequest {
                    photoUri = Uri.parse(uri.toString())
                }

                user.updateProfile(profileUpdates)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(this, "Se realizaron los cambios correctamente.",
                                Toast.LENGTH_SHORT).show()
                            updateUI()
                        }
                    }
            }
        }.addOnFailureListener {
            Log.i("TAG", "file upload error")
        }
    }



    private  fun updateUI () {
        val user = auth.currentUser

        if (user != null){

            binding.emailTextView.text = user.email

            if(user.displayName != null){
                binding.nameTextView.text = user.displayName
                binding.nameEditText.setText(user.displayName)
            }

            Glide
                .with(this)
                .load(user.photoUrl)
                .centerCrop()
                .placeholder(R.drawable.profile_photo)
                .into(binding.profileImageView)
            Glide
                .with(this)
                .load(user.photoUrl)
                .centerCrop()
                .placeholder(R.drawable.profile_photo)
                .into(binding.bgProfileImageView)
        }

    }

    private  fun signOut(){
        auth.signOut()
        val intent = Intent(this, SingInActivity::class.java)
        this.startActivity(intent)

    }
}