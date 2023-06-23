package com.example.chattingapp.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.chattingapp.R
import com.example.chattingapp.data.UserData
import com.example.chattingapp.databinding.ItemMemberListBinding


class DialogMemberListAdapter(private val item: ArrayList<UserData>): RecyclerView.Adapter<DialogMemberListAdapter.CustomViewHolder>() {

    inner class CustomViewHolder(val binding: ItemMemberListBinding): RecyclerView.ViewHolder(binding.root){
        init{
            itemView.setOnClickListener {
                itemClickListener.onClick(it, item[adapterPosition])
            }
        }
        private val profileImage = binding.dialogProfileImage
        private val memberText = binding.dialogMemberInfo
        fun bind(data: UserData){
            Glide.with(itemView.context)
                .load(data.mainProfileUrl)
                .into(profileImage)
            memberText.text = itemView.context.getString(R.string.dialog_member_info, data.memNo,data.nickName)
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomViewHolder {
        val binding = ItemMemberListBinding.inflate(LayoutInflater.from(parent.context), parent,false)
        return CustomViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return item.size
    }

    override fun onBindViewHolder(holder: CustomViewHolder, position: Int) {
        val realItem = item[position]
        holder.bind(realItem)
    }
    interface OnItemClickListener{
        fun onClick(v: View, realItem: UserData)
    }
    fun setItemClickListener(onItemClickListener: OnItemClickListener){
        this.itemClickListener = onItemClickListener
    }
    private lateinit var itemClickListener: OnItemClickListener

}