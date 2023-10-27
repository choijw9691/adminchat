package com.project.adminchat.ui.util

import android.app.NotificationManager
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.google.android.datatransport.cct.StringMerger
import com.google.auth.oauth2.GoogleCredentials
import com.project.adminchat.MainActivity
import com.project.adminchat.R
import com.project.adminchat.common.Constants
import com.project.adminchat.databinding.FragmentSendMessageDialogBinding
import com.project.adminchat.ui.home.MainViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import java.io.FileInputStream
import java.io.IOException
import java.util.Arrays


class SendMessageDialog(
    var messageKey: String,
    var message: String,
    var location: String,
    var myName: String,
    var yourName: String,
    var content: String,
    var toToken: String,
    var fromToken: String
) : DialogFragment(), OnClickListener {
    lateinit var binding: FragmentSendMessageDialogBinding
    private val mainViewModel: MainViewModel by activityViewModels()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentSendMessageDialogBinding.inflate(inflater, container, false)
        binding.btnSend.setOnClickListener(this)
        Log.d(
            "JIWOUNG",
            "fmklewnfklewf: " + location + "||" + myName + "||" + yourName + "||" + content + "||" + toToken + "||" + fromToken
        )
        if (message == Constants.SEND_MESSAGE.toString()) {
            binding.btnSend.text = "SEND"
            binding.contentTv.setText("")
            binding.contentTv.isEnabled = true
            binding.nameTv.text = yourName
        } else if (message == Constants.RECEIVE_MESSAGE.toString()) {
            binding.btnSend.text = "REPLY"
            binding.contentTv.isEnabled = false

            binding.contentTv.setText(content)
            val notificationManager =
                activity?.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.cancel(2)
            binding.nameTv.text = myName
        }
        binding.locationTv.text = location
        removeFromSharedPreferences(messageKey)

        return binding.root
    }

    private fun removeFromSharedPreferences(messageKey: String) {
        if (requireActivity() != null) {
            val sharedPreferences =
                requireActivity().getSharedPreferences("FCM_DATA", Context.MODE_PRIVATE)
            val messages = sharedPreferences.getStringSet("messages", mutableSetOf())
            messages?.remove(messageKey)
            sharedPreferences.edit().apply {
                putStringSet("messages", messages)
                apply()
            }
        }
    }

    fun sendFCM(
        targetToken: String,
        message: Int,
        location: String,
        myName: String,
        yourName: String,
        content: String,
        fromToken: String
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            mainViewModel.getServerKeyFromFirestore(
                onSuccess = { serverKey ->
                    Log.d("JIWOUNG", "fnelwkmursl4umrsl4m5: " + myName + "||" + yourName)
                    CoroutineScope(Dispatchers.IO).launch {
                        val client = OkHttpClient()
                        val mediaType = "application/json".toMediaType()
                        val body = """{
  "message": {
    "token": "${targetToken}",
    "notification": {
      "title": "Notification Title",
      "body": "Notification Body"
    },
        "data": {
               "message":"${message}",
                "location":"${location}",
                "myName":"${myName}",
                "yourName":"${yourName}",
                "content":"${content}",
                "toToken":"${targetToken}",
                "fromToken":"${fromToken}"
    }
  }
}""".trimIndent().toRequestBody(mediaType)
                        val aa = getAccessToken().toString()
                        Log.d("JIWOUNG", "fkwnelfkul4rsfew: " + aa)
                        val request = Request.Builder()
                            .url("https://fcm.googleapis.com/v1/projects/adminchat-9512f/messages:send")
                            .post(body)
                            .addHeader("Authorization", "Bearer ${aa}")
                            .addHeader("Content-Type", "application/json")
                            .build()
                        // Log.d("JIWOUNG", "fnelwkmursl4umrsl4m2: "+serverKey)

                        client.newCall(request).enqueue(object : Callback {
                            override fun onFailure(call: Call, e: IOException) {
                                e.printStackTrace()
                                Log.d("JIWOUNG", "fnelwkmursl4umrsl4m1")

                            }

                            override fun onResponse(call: Call, response: Response) {
                                if (response.isSuccessful) {
                                    println("FCM message sent successfully!")
                                } else {
                                    // HTTP 응답의 본문을 문자 스트림으로 읽어옵니다.
                                    val responseBody = response.body?.charStream()

                                    // 응답 본문을 문자열로 읽고 출력합니다.
                                    val responseText = responseBody?.readText()
                                    Log.d("JIWOUNG", "Response Body: $responseText")
                                }
                            }
                        })
                    }
                },
                onFailure = { exception ->
                    Log.d("JIWOUNG", "fnelwkmursl4umrsl4m4")
                }
            )

        }

    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btn_send -> {
                if (binding.btnSend.text == "SEND") {
                    if (message == Constants.SEND_MESSAGE.toString()) {
                        sendFCM(
                            toToken,
                            Constants.RECEIVE_MESSAGE,
                            location,
                            myName,
                            yourName,
                            binding.contentTv.text.toString(),
                            fromToken
                        )
                        dismiss()
                    } else {
                        dismiss()
                    }
                } else {
                    dismiss()
                    MainActivity.myProfile?.let {
                        SendMessageDialog(
                            messageKey, Constants.SEND_MESSAGE.toString(), location,
                            yourName,myName, content, fromToken, toToken
                        ).show(parentFragmentManager, "")
                    }
                }
            }
        }
    }

    @Throws(IOException::class)
    private fun getAccessToken(): String? {
        binding.dataContainer.context.assets.open("service-account.json").use { inputStream ->
            val googleCredentials = GoogleCredentials.fromStream(inputStream)
                .createScoped(listOf("https://www.googleapis.com/auth/firebase.messaging"))
            googleCredentials.refreshIfExpired()
            return googleCredentials.accessToken.tokenValue
        }
    }
}