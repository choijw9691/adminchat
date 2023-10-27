package com.project.adminchat.ui.login

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast

import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.project.adminchat.databinding.ActivityRegisterBinding

class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        getSupportActionBar()?.hide();//Ocultar ActivityBar anterior

        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.sendButton.setOnClickListener {
            if (binding.idEv.text.toString() == "" || binding.idEv.text.toString() == null) {
                Toast.makeText(this, "Please fill in your ID.", Toast.LENGTH_SHORT).show()

            } else if(binding.pwEv.text.toString().length < 6 || binding.pwEv.text.toString() == null){
                    Toast.makeText(this, "Password must be at least 6 characters long.", Toast.LENGTH_SHORT).show()
                }
            else if(binding.nicknameEv.text.toString() == null || binding.nicknameEv.text.toString() ==""){
                Toast.makeText(this, "Please fill in your nickname.", Toast.LENGTH_SHORT).show()

            }
            else {
                setAuthentication(
                    binding.idEv.text.toString(),
                    binding.pwEv.text.toString(),
                    binding.nicknameEv.text.toString()
                )
            }
        }
        binding.CommonTopBar.backButton.setOnClickListener {
            finish()
        }
    }

    fun setAuthentication(email: String, password: String, nickname: String) {
        Firebase.auth.createUserWithEmailAndPassword("$email@gmail.com", password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val data = Intent()
                    data.putExtra("email", email)
                    data.putExtra("password", password)
                    data.putExtra("nickname", nickname)

                    // Put any data you want to return in the Intent here
                    setResult(RESULT_OK, data)
                    finish()
                }
            }
            .addOnFailureListener { exception ->
                if (exception is FirebaseAuthUserCollisionException) {
                    // Handle email already in use error
                    Toast.makeText(this, "we already have an account.", Toast.LENGTH_SHORT).show()
                } else {
                    Log.d("JIWOUNG", "errorcheck1: " + exception.message)
                    Toast.makeText(this, "Sign up failed.", Toast.LENGTH_SHORT).show()
                }
            }

    }
}
