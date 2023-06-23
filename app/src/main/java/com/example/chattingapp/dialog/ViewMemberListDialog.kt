package com.example.chattingapp.dialog

import android.app.Dialog
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.chattingapp.adapter.DialogMemberListAdapter
import com.example.chattingapp.data.UserData
import com.example.chattingapp.databinding.DialogMemberListLayoutBinding

class ViewMemberListDialog(private val context: AppCompatActivity) {
    private lateinit var binding: DialogMemberListLayoutBinding
    private val customDialog = Dialog(context)

    private lateinit var memberClickListener: OnMemberClickListener

    fun show(content: ArrayList<UserData>, isOwner: Boolean, hostInfo: UserData){
        binding = DialogMemberListLayoutBinding.inflate(context.layoutInflater)


        customDialog.setContentView(binding.root)

        val dialogMemberListAdapter = DialogMemberListAdapter(content)

        binding.memberListRecyclerView.apply{
            adapter = dialogMemberListAdapter
            layoutManager = LinearLayoutManager(context,LinearLayoutManager.VERTICAL, false )
        }

        dialogMemberListAdapter.setItemClickListener(object: DialogMemberListAdapter.OnItemClickListener{
            override fun onClick(v: View, realItem: UserData) {
                if(isOwner && realItem!=hostInfo){
                    kickDialog(realItem)
                    customDialog.dismiss()
                }
            }
        })

        binding.confirmButton.setOnClickListener {
            customDialog.dismiss()
        }

        customDialog.show()
    }

    fun kickDialog(kickUserInfo: UserData){
        val alertDialog = AlertDialog.Builder(context)
        alertDialog.setTitle("memNo: ${kickUserInfo.memNo}, ${kickUserInfo.nickName}님을 강퇴하시겠습니까?")
        alertDialog.setPositiveButton("강퇴하기"){dialog,_->
            memberClickListener.onClick(kickUserInfo)
            dialog.dismiss()
        }
        alertDialog.setNegativeButton("취소하기"){dialog,_->
            dialog.dismiss()
        }
        alertDialog.show()
    }


    fun setOnMemberClickListener(memberClickListener: (UserData)->Unit){
        this.memberClickListener = object: OnMemberClickListener{
            override fun onClick(kickUser: UserData) {
                memberClickListener(kickUser)
            }
        }
    }

    interface OnMemberClickListener{
        fun onClick(kickUser: UserData)
    }

}