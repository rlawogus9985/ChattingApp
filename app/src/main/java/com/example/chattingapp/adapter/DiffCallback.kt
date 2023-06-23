package com.example.chattingapp.adapter

import androidx.recyclerview.widget.DiffUtil
import com.example.chattingapp.data.ChatDetailsData
import com.example.chattingapp.data.PartyItem
import com.example.chattingapp.data.UserData

class PartyListDiffCallback : DiffUtil.ItemCallback<PartyItem>() {
    override fun areItemsTheSame(oldItem: PartyItem, newItem: PartyItem): Boolean {
        return oldItem.hashCode() == newItem.hashCode()
    }

    override fun areContentsTheSame(oldItem: PartyItem, newItem: PartyItem): Boolean {
        return oldItem == newItem
    }
}

class UserDataDiffCallback : DiffUtil.ItemCallback<UserData>() {
    override fun areItemsTheSame(oldItem: UserData, newItem: UserData): Boolean {
        return oldItem.hashCode() == newItem.hashCode()
    }

    override fun areContentsTheSame(oldItem: UserData, newItem: UserData): Boolean {
        return oldItem == newItem
    }
}