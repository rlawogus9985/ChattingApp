package com.example.chattingapp.adapter

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.chattingapp.PersonalChatScreen
import com.example.chattingapp.data.UserData
import com.example.chattingapp.data.UserInfoData
import com.example.chattingapp.databinding.ItemPersonalChatBinding

class PersonalAdapter(var hostInfo: UserData?): RecyclerView.Adapter<PersonalAdapter.CustomViewHolder>() {

    private var item: ArrayList<UserInfoData> = ArrayList()

    inner class CustomViewHolder(val binding: ItemPersonalChatBinding):
        RecyclerView.ViewHolder(binding.root), View.OnClickListener{

        init {
            itemView.setOnClickListener(this)
        }

        fun bind(data: UserData){
            binding.personListData = data
        }

        override fun onClick(view: View){
            val position = adapterPosition
            val sender = item[position].data
            navigateToDetailScreen(sender)
        }

        private fun navigateToDetailScreen(sender: UserData?){
            val intent = Intent(itemView.context, PersonalChatScreen::class.java)
            val bundle = Bundle().apply{
                putParcelable("others",sender)
                putParcelable("hostInfo",hostInfo)
            }
            intent.putExtras(bundle)
            itemView.context.startActivity(intent)
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomViewHolder {
        val binding = ItemPersonalChatBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CustomViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return item.size
    }

    override fun onBindViewHolder(holder: CustomViewHolder, position: Int) {
        val realItem = item[position]
        if(realItem.data?.memNo != hostInfo?.memNo){
            realItem.data?.let { holder.bind(it) }
        }
    }

    fun setItemList(data: ArrayList<UserInfoData>){
        item = data.filter{
            hostInfo != null && it.data?.memNo != hostInfo?.memNo
        } as ArrayList<UserInfoData>
        notifyItemRangeInserted(0,item.size-1)
    }

}