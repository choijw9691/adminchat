package com.project.adminchat.ui.home

import android.util.Log
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore
import com.project.adminchat.MainActivity
import com.project.adminchat.model.Gym
import com.project.adminchat.model.UserEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class MainViewModel : ViewModel() {

    private val _gymList = MutableStateFlow<List<Gym>?>(null)
    val gymList: StateFlow<List<Gym>?> = _gymList.asStateFlow()

    private val _currentGym = MutableStateFlow<Gym?>(null)
    val currentGym: StateFlow<Gym?> = _currentGym.asStateFlow()

    fun setCurrentGym(gym: Gym) {
        _currentGym.value = gym
    }

    fun getAllGymsFromFirestore() {
        val db = FirebaseFirestore.getInstance()
        val gymsRef = db.collection("gyms")

        gymsRef.get()
            .addOnSuccessListener { querySnapshot ->
                val gymList = mutableListOf<Gym>()
                for (document in querySnapshot) {
                    if (document.exists()) {
                        val gym = document.toObject(Gym::class.java)
                        gymList.add(gym)
                    }
                }
                _gymList.value = gymList
            }
            .addOnFailureListener { exception ->
                // Handle any errors that occurred
                _gymList.value = null
            }
    }

    fun setUpdateCurrentGym(gym: Gym) {
        var userEntity = MainActivity.myProfile
        userEntity?.currentGym = gym.documentId
        MainActivity.myProfile = userEntity
        if (userEntity != null) {
            updateUserCurrentGym(userEntity.uid, gym)
            addNewUserToGym(userEntity.currentGym, userEntity)
        }
    }

    fun exitCurrentGym() {
        var userEntity = MainActivity.myProfile
        if (userEntity != null) {
            updateUserCurrentGym(userEntity.uid, Gym())
            removeUserFromGym(userEntity.currentGym, userEntity)
        }
    }

    fun removeUserFromGym(gymId: String, userToRemove: UserEntity) {
        val db = FirebaseFirestore.getInstance()
        val gymRef = db.collection("gyms").document(gymId)
        gymRef.get()
            .addOnSuccessListener { documentSnapshot ->

                if (documentSnapshot.exists()) {
                    val gym = documentSnapshot.toObject(Gym::class.java)

                    if (gym != null) {
                        // 기존 userList를 가져옴
                        val userList = gym.userList.toMutableList()
                        Log.d("fnewkf", "EWfmklwemfk11: " + userList.size)
                        Log.d("fnewkf", "EWfmklwemfk1123: " + userToRemove.nickname)

                        // 사용자를 userList에서 제거
                        userList.remove(userToRemove)
                        Log.d("fnewkf", "EWfmklwemfk111: " + userList.size)

                        // userList를 업데이트
                        val updates = hashMapOf<String, Any>(
                            "userList" to userList
                        )

                        gymRef.update(updates)
                            .addOnSuccessListener {
                                // 업데이트 성공
                                _currentGym.value = null
                                MainActivity.myProfile?.currentGym = ""
                            }
                            .addOnFailureListener { e ->
                                // 업데이트 중 발생한 오류 처리
                            }
                    }
                }
            }
            .addOnFailureListener { exception ->
                // 문서 가져오기 실패 또는 오류 처리
                Log.d("fnewkf", "EWfmklwemfk14: " + exception.message)

            }
    }

    fun updateUserCurrentGym(uid: String, gym: Gym) {
        val db = FirebaseFirestore.getInstance()
        val userRef = db.collection("users").document(uid)

        val updates = hashMapOf<String, Any?>(
            "currentGym" to gym?.documentId.toString()
        )

        userRef.update(updates)
            .addOnSuccessListener {
                // 업데이트 성공

            }
            .addOnFailureListener { e ->
                // 업데이트 중 발생한 오류 처리
            }
    }

    fun addNewUserToGym(gymId: String, newUser: UserEntity) {
        val db = FirebaseFirestore.getInstance()
        val gymRef = db.collection("gyms").document(gymId)
        gymRef.get()
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    val gym = documentSnapshot.toObject(Gym::class.java)
                    if (gym != null) {
                        // 기존 userList를 가져옴
                        val userList = gym.userList.toMutableList()

                        // 새로운 사용자를 userList에 추가
                        userList.add(newUser)

                        // userList를 업데이트
                        val updates = hashMapOf<String, Any>(
                            "userList" to userList
                        )

                        gymRef.update(updates)
                            .addOnSuccessListener {
                                // 업데이트 성공
                                _currentGym.value = Gym(gym.name, userList, gym.documentId)
                            }
                            .addOnFailureListener { e ->
                                // 업데이트 중 발생한 오류 처리
                            }
                    }
                }
            }
            .addOnFailureListener { exception ->
                // 문서 가져오기 실패 또는 오류 처리
            }
    }

    fun getGymByDocumentId(documentId: String) {
        val db = FirebaseFirestore.getInstance()
        if (!documentId.isNullOrEmpty()) {
            db.collection("gyms").document(documentId).get()
                .addOnSuccessListener { documentSnapshot ->
                    val gym = documentSnapshot.toObject(Gym::class.java)
                    if (gym != null) {
                        setCurrentGym(gym)
                    }
                }
                .addOnFailureListener { e ->
                }
        }

    }
    fun updateToken(uid: String, newToken: String, callback: (Boolean) -> Unit) {
        val db = FirebaseFirestore.getInstance()

        val userRef = db.collection("users").document(uid)

        userRef.update("token", newToken)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    callback(true)
                } else {
                    callback(false)
                }
            }
    }
    fun getServerKeyFromFirestore(onSuccess: (String?) -> Unit, onFailure: (Exception?) -> Unit) {
        val db = FirebaseFirestore.getInstance()

        db.collection("serverkey").document("serverkey")
            .get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    val serverKey = document.getString("serverkey")
                    onSuccess(serverKey)
                } else {
                    onFailure(null)
                }
            }
            .addOnFailureListener { exception ->
                onFailure(exception)
            }
    }

    fun updateUserWithGym(user: UserEntity, completion: (isSuccess: Boolean) -> Unit) {
         val db = FirebaseFirestore.getInstance()
         val userCollection = db.collection("users")
         val gymCollection = db.collection("gyms")
        userCollection.document(user.uid).set(user)
            .addOnSuccessListener {
                // 알고 있는 Gym 문서 ID로 직접 참조
                if (!user.currentGym.isNullOrEmpty()){

                    val gymDocRef = gymCollection.document(user.currentGym)

                    gymDocRef.get()
                        .addOnSuccessListener { documentSnapshot ->
                            val gym = documentSnapshot.toObject(Gym::class.java)
                            if (gym != null) {
                                val updatedUserList = gym.userList.map {
                                    if (it.id == user.id) user else it
                                }

                                gymDocRef.update("userList", updatedUserList)
                                    .addOnSuccessListener { completion(true) }
                                    .addOnFailureListener { e ->
                                        e.printStackTrace()
                                        completion(false)
                                    }
                            } else {
                                completion(false)
                            }
                        }
                        .addOnFailureListener { e ->
                            e.printStackTrace()
                            completion(false)
                        }
                }else{
                    completion(true)
                }

            }
            .addOnFailureListener { e ->
                e.printStackTrace()
                completion(false)
            }
    }
    }
