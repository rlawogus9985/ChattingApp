package com.example.chattingapp

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.example.chattingapp.adapter.LobbyFragmentAdapter
import com.example.chattingapp.data.UserData
import com.example.chattingapp.databinding.ActivityMainLobbyBinding
import com.example.chattingapp.eventbus.SocketDisconnectedEvent
import com.example.chattingapp.viewmodel.MainLobbyViewModel
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class MainLobby : AppCompatActivity() {

    private val mainLobbyViewModel: MainLobbyViewModel by viewModels()
    private lateinit var binding: ActivityMainLobbyBinding
    private lateinit var hostInfo: UserData

    private val eventBus: EventBus = EventBus.getDefault()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainLobbyBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 툴바 활성화
        setSupportActionBar(binding.lobbyToolbar)

        supportActionBar?.apply{
            title = getString(R.string.lobby_title)
        }

        hostInfo = if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
            intent.getParcelableExtra("hostInfo", UserData::class.java)!!
        } else {
            intent.getParcelableExtra("hostInfo")!!
        }

        // fragment 에서 사용할 데이터 저장하기
        mainLobbyViewModel.setHostInfo(hostInfo)

        // host 그리기
        setBody()

        // 프래그먼트 연결
        val lobbyViewPager = binding.lobbyVieWPager
        lobbyViewPager.adapter = LobbyFragmentAdapter(this)

        observeSocket()

    } // onCreate

    private fun setBody(){
        // host 그리기
        binding.currentId.text = getString(R.string.lobby_name, hostInfo.nickName, hostInfo.memNo)
        Glide.with(this)
            .load(hostInfo.mainProfileUrl)
            .apply(RequestOptions.bitmapTransform(RoundedCorners(35)))
            .into(binding.profileImage)
    }

    private fun observeSocket(){
        SocketEventListener.ntRequestJoinParty.observe(this){
            if(it!=null){
                val ntMemNo = it.getJSONObject("data").getJSONObject("rqUserInfo").getInt("memNo")
                val ntPartyNo = it.getJSONObject("data").getJSONObject("summaryPartyInfo").getInt("partyNo")
                val alertDialog = AlertDialog.Builder(this)
                alertDialog.apply{
                    setTitle("${ntMemNo}번 님께서 ${ntPartyNo}번 방에 요청을 보냈습니다.")
                    setCancelable(false)
                    setPositiveButton("승인하기"){ dialog, _ ->
                        mainLobbyViewModel.acceptOrDeclineParty(ntPartyNo, hostInfo.memNo, ntMemNo, true)
                        dialog.dismiss()
                        mainLobbyViewModel.getSummaryPartyLists()
                    }
                    setNegativeButton("거절하기"){ dialog, _ ->
                        mainLobbyViewModel.acceptOrDeclineParty(ntPartyNo, hostInfo.memNo, ntMemNo, false)
                        dialog.dismiss()
                    }
                    show()
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        eventBus.register(this)
    }

    override fun onResume() {
        super.onResume()
        SocketEventListener.resetNtRequestJoinParty()
        mainLobbyViewModel.getSummaryPartyLists()
    } // onResume()

    override fun onStop() {
        super.onStop()
        eventBus.unregister(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("로그", "socketDisconnect 시도 ")
        SocketHandler.disconnect()
        SocketEventListener.disconnectSocket()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onSocketDisconnected(event: SocketDisconnectedEvent){
        Toast.makeText(this,getString(R.string.server_disconnected),Toast.LENGTH_SHORT).show()
        finish()
    }
}