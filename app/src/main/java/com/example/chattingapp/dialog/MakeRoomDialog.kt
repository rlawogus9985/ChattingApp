package com.example.chattingapp.dialog

import android.app.Dialog
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.WindowManager
import com.example.chattingapp.R
import com.example.chattingapp.data.CreatePartyContextRequest
import com.example.chattingapp.data.SummaryPartyInfo
import com.example.chattingapp.data.UserData
import com.example.chattingapp.databinding.DialogMakeRoomBinding

class MakeRoomDialog(private val context: Context) {
    private lateinit var binding: DialogMakeRoomBinding
    private val customDialog = Dialog(context)

    private lateinit var makeRoomClickListener: OnMakeRoomClickListener

    fun show(hostInfo: UserData){
        binding = DialogMakeRoomBinding.inflate(LayoutInflater.from(context))
        customDialog.setCancelable(false)

        customDialog.setContentView(binding.root)
        customDialog.window!!.setLayout(WindowManager.LayoutParams.MATCH_PARENT,
        WindowManager.LayoutParams.WRAP_CONTENT)

        binding.radioGroup.check(R.id.secretRoom)
        binding.submitButton.setOnClickListener {
            val mainPhotoUrl = binding.mainPhotoUrl.text.toString()

            val title = binding.roomTitle.text.toString()
            if(title.length < 5){
                Log.d("로그", "title 5자 미만임")
                return@setOnClickListener
            }

            val location = binding.location.text.toString()
            val maxMemberCount = binding.maxMemberCount.text.toString().toIntOrNull()

            var isSecretRoom = false
            when(binding.radioGroup.checkedRadioButtonId){
                R.id.publicRoom -> isSecretRoom = true
                R.id.secretRoom -> isSecretRoom = false
            }

            val subPhotoUrlList = binding.subPhotoUrlList.text.toString()

            val questContent = binding.questContent.text.toString()
            if(questContent.length < 30){
                Log.d("로그", "questContent 30자 이하임 ")
                return@setOnClickListener
            }

            val partyReq = CreatePartyContextRequest(
                SummaryPartyInfo(
                    hostInfo.memNo,mainPhotoUrl,title,location,maxMemberCount as Int, 1682235334,1682237334,isSecretRoom
                ),
                listOf(subPhotoUrlList),
                questContent
            )
            makeRoomClickListener.onClick(partyReq)
            customDialog.dismiss()
        }

        binding.cancelButton.setOnClickListener {
            customDialog.dismiss()
        }

        customDialog.show()

    }

    fun setOnMakeRoomClickListener(makeRoomClickListener: (CreatePartyContextRequest)->Unit){
        this.makeRoomClickListener = object: OnMakeRoomClickListener{
            override fun onClick(roomData: CreatePartyContextRequest) {
                makeRoomClickListener(roomData)
            }
        }
    }

    interface OnMakeRoomClickListener{
        fun onClick(roomData: CreatePartyContextRequest)
    }
}