package com.project.adminchat.ui.login

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.project.adminchat.MainActivity
import com.project.adminchat.common.LoadingDialog
import com.project.adminchat.databinding.ActivityLoginBinding
import com.project.adminchat.model.UserEntity

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var db: FirebaseFirestore
    private val myActivityResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val data: Intent? = result.data
                // Handle the result here
                var email = data?.getStringExtra("email")
                var name = data?.getStringExtra("nickname")
                var password = data?.getStringExtra("password")
                setUser(email.toString(), name.toString(), password.toString())
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        getSupportActionBar()?.hide();//Ocultar ActivityBar anterior

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        db = FirebaseFirestore.getInstance()

        binding.registerButton.setOnClickListener {
            var intent = Intent(this, RegisterActivity::class.java)
            myActivityResultLauncher.launch(intent)
        }
        binding.loginButton.setOnClickListener {
            getAuthentication(
                binding.idEv.text.toString(),
                binding.pwEv.text.toString()
            )
        }

        val currentUser = Firebase.auth.currentUser
        if (currentUser != null) {
            getUser(currentUser.uid)
        } else {
            // User is not signed in, show login screen
        }

    }

    fun getAuthentication(email: String, password: String) {
        Firebase.auth.signInWithEmailAndPassword("$email@gmail.com", password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // User is signed in, you can now use the user ID token to access Firestore
                    Toast.makeText(applicationContext, "로그인에 성공했습니다.", Toast.LENGTH_SHORT).show()
                    task.result.user?.let { getUser(it.uid) }
                } else {
                    Toast.makeText(applicationContext, "아이디 또는 비밀번호가 잘못됐습니다.", Toast.LENGTH_SHORT)
                        .show()
                }
            }
    }

    fun setUser(email: String, nickname: String, password: String) {
        LoadingDialog.show(this)

        Firebase.auth.signInWithEmailAndPassword("$email@gmail.com", password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // User is signed in, you can now use the user ID token to access Firestore
                    var uid = task.result.user?.uid
                    if (uid != null) {
                        db.collection("users").document(uid)
                            .set(UserEntity(email,nickname,uid))
                            .addOnSuccessListener { documentReference ->
                                startMainActivity(
                                    UserEntity(email,nickname,uid)
                                )
                                LoadingDialog.dismiss()
                            }
                            .addOnFailureListener { e ->
                                LoadingDialog.dismiss()
                            }
                    }
                } else {

                }
            }
    }

    fun startMainActivity(userEntity: UserEntity) {
        var intent = Intent(this, MainActivity::class.java)
        intent.putExtra("userEntity", userEntity)
        startActivity(intent)
        finish()
    }

    fun getUser(uid: String) {
        LoadingDialog.show(this)
        val userRef = db.collection("users").document(uid)
        userRef.get()
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    val user = documentSnapshot.toObject(UserEntity::class.java)
                    if (user != null) {
                        startMainActivity(
                            user
                        )
                    }
                } else {
                    // Document does not exist
                }
                LoadingDialog.dismiss()
            }
            .addOnFailureListener { exception ->
                // Handle any errors that occurred
                LoadingDialog.dismiss()
            }
    }
}
