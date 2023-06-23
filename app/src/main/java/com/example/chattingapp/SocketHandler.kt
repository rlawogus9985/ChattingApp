package com.example.chattingapp

import io.socket.client.IO
import io.socket.client.Socket

object SocketHandler{
    private var handlerSocket: Socket? = null

    fun get(): Socket{
        if(handlerSocket == null || !handlerSocket!!.connected()){
            // 연결 안된경우
            handlerSocket = IO.socket(BuildConfig.SOCKET_ADDRESS)
            handlerSocket!!.connect()
        }
        return handlerSocket!! // 이 시점에서 socket이 connect()안되어있을수있다.
    }


    fun disconnect(){
        handlerSocket?.disconnect()
        handlerSocket = null
    }

}