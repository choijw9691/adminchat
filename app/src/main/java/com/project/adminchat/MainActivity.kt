package com.project.adminchat

import android.Manifest
import android.app.Dialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging

import com.project.adminchat.common.LoadingDialog

import com.project.adminchat.databinding.ActivityMainBinding
import com.project.adminchat.model.Gym
import com.project.adminchat.model.UserEntity
import com.project.adminchat.ui.home.MainViewModel
import com.project.adminchat.ui.util.SendMessageDialog


class MainActivity : AppCompatActivity() {
    companion object {
        var myProfile: UserEntity? = UserEntity()
    }
    private val fcmReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            // FCM에서 받은 데이터를 기반으로 DialogFragment를 띄우는 로직

            val message = intent.getStringExtra("message")?:""
            val location = intent.getStringExtra("location")?:""
            val name = intent.getStringExtra("name")?:""
            val content = intent.getStringExtra("content")?:""
            val toToken = intent.getStringExtra("toToken")?:""
            val fromToken = intent.getStringExtra("fromToken")?:""

            Log.d("JIWOUNG","ernklgerg11" +toToken+"||"+fromToken)

            SendMessageDialog("",message,location,name,content,toToken,fromToken).show(supportFragmentManager, "TAG")
        }
    }
    private lateinit var binding: ActivityMainBinding
    private val sharedViewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide();//Ocultar ActivityBar anterior
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        askNotificationPermission()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "CHANNEL_ID1",
                "CHANNEL_NAME1",
                NotificationManager.IMPORTANCE_HIGH
            )
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }

        myProfile = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getSerializableExtra("userEntity", UserEntity::class.java)
        } else {
            intent.getSerializableExtra("userEntity") as UserEntity?
        }
        Log.d("JIWOUNG", "fwenojfwejfw11: " + myProfile?.toString())

        if (myProfile?.id == "admin") {
            binding.floatingContainer.visibility = View.VISIBLE
        } else {
            binding.floatingContainer.visibility = View.GONE

        }

        val navView: BottomNavigationView = binding.navView

        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_notifications
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        binding.floatingBtn.setOnClickListener {
            showCustomDialog()
        }

        MainActivity.myProfile?.let {
            sharedViewModel.getGymByDocumentId(it.currentGym)
        }

        navView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> {
                    navController.navigate(R.id.navigation_home)
                    true
                }

                R.id.navigation_dashboard -> {
                    navController.navigate(R.id.navigation_dashboard)
                    true
                }

                R.id.navigation_notifications -> {
                    navController.navigate(R.id.navigation_notifications)
                    true
                }

                else -> false
            }
        }

        getFCMToken { token ->
            Log.d("FCM Token", token)
            if (myProfile?.token != token) {
                myProfile?.token = token
                sharedViewModel.updateToken(myProfile?.uid.toString(), token) {}
                myProfile?.let {
                    sharedViewModel.updateUserWithGym(it) { isSuccess ->
                        if (isSuccess) {
                            // 업데이트 성공
                        } else {
                            // 업데이트 실패
                        }
                    }
                }
            }

        }

    }

    private fun showCustomDialog() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_add_gym)
        val layoutParams = dialog.window?.attributes
        layoutParams?.width = WindowManager.LayoutParams.MATCH_PARENT
        layoutParams?.height = WindowManager.LayoutParams.WRAP_CONTENT
        dialog.window?.attributes = layoutParams

        val nameEditView = dialog.findViewById<EditText>(R.id.name_ev)
        val sendButton = dialog.findViewById<Button>(R.id.send_btn)
        sendButton.setOnClickListener {
            val gymName = nameEditView.text.toString()
            if (gymName.isNotEmpty()) {
                addGymToFirestore(Gym(gymName, listOf(), Timestamp.now().nanoseconds.toString()))
            }
            dialog.dismiss()
        }

        dialog.show()
    }

    fun addGymToFirestore(gym: Gym) {
        LoadingDialog.show(this)
        val db = FirebaseFirestore.getInstance()
        val gymData = hashMapOf(
            "name" to gym.name,
            "userList" to gym.userList,
            "documentId" to gym.documentId
        )

        db.collection("gyms").document(gym.documentId).set(gymData)
            .addOnSuccessListener { documentReference ->

                // 이후 작업을 수행하거나 추가된 문서 ID(gymId)를 사용할 수 있습니다.
                Toast.makeText(this, "헬스장 추가에 성공하였습니다.", Toast.LENGTH_SHORT).show()
                LoadingDialog.dismiss()
                sharedViewModel.getAllGymsFromFirestore()

            }
            .addOnFailureListener { exception ->
                // 추가 실패
                // 오류 처리를 수행하거나 사용자에게 알림을 표시할 수 있습니다.
                Log.d("JIWOUNG", "exnklefw: " + exception.message.toString())
                Toast.makeText(this, "헬스장 추가에 실패하였습니다.", Toast.LENGTH_SHORT).show()
                LoadingDialog.dismiss()

            }
    }

    fun getFCMToken(callback: (String) -> Unit) {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val token = task.result
                if (token != null) {
                    callback(token)
                }
            }
        }
    }


    // Declare the launcher at the top of your Activity/Fragment:
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { isGranted: Boolean ->
        if (isGranted) {
            // FCM SDK (and your app) can post notifications.
        } else {
            // TODO: Inform user that that your app will not show notifications.
        }
    }

    private fun askNotificationPermission() {
        // This is only necessary for API level >= 33 (TIRAMISU)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED
            ) {
                // FCM SDK (and your app) can post notifications.
            } else if (shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)) {
                // TODO: display an educational UI explaining to the user the features that will be enabled
                //       by them granting the POST_NOTIFICATION permission. This UI should provide the user
                //       "OK" and "No thanks" buttons. If the user selects "OK," directly request the permission.
                //       If the user selects "No thanks," allow the user to continue without notifications.
            } else {
                // Directly ask for the permission
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
    override fun onPause() {
        super.onPause()
        LocalBroadcastManager.getInstance(this).unregisterReceiver(fcmReceiver)
    }
    override fun onResume() {
        super.onResume()
        LocalBroadcastManager.getInstance(this).registerReceiver(fcmReceiver, IntentFilter("FCM_INTENT_FILTER"))

        val sharedPreferences = getSharedPreferences("FCM_DATA", Context.MODE_PRIVATE)
        val messages = sharedPreferences.getStringSet("messages", mutableSetOf())
        Log.d("JIWOUNG","fnklewfewf")

        messages?.forEach { messageData ->
            Log.d("JIWOUNG","fnklewfewf1")

            val parts = messageData.split("|~|")
            val timestamp = parts[0]
            val message = parts[1]
            val location = parts[2]
            val name = parts[3]
            val content = parts[4]
            val toToken = parts[5]
            val fromToken = parts[6]

            showDialog(messageData,message, location,name,content,toToken,fromToken)
        }
    }

    fun showDialog(messageKey:String,message:String, location:String,name:String,content:String,toToken:String,fromToken:String){
        SendMessageDialog(messageKey,message, location,name,content,toToken,fromToken).show(supportFragmentManager,"")
    }

}