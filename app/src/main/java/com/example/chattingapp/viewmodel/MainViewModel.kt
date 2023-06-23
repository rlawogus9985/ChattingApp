package com.example.chattingapp.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.chattingapp.SocketEventListener.mSocket
import org.json.JSONObject

class MainViewModel : ViewModel() {

    fun loginUser(memberId: Int) {
        val jsonObject = JSONObject().apply{
            put("cmd", "RqAuthUser")
            put("data",JSONObject().apply{
                put("memNo", memberId)
            })
        }
        if(mSocket != null && mSocket?.connected() == true){
            Log.d("로그", "보내는 json형식: $jsonObject")
            mSocket?.emit("Lobby", jsonObject)
        } else {
            Log.d("로그", "mSocket이 null이거나 disconnect임 mSocket : $mSocket, ${mSocket?.connected()}")
        }
    }
    
}