package com.example.chattingapp.room.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class GroupChatEnterHistory(
    @PrimaryKey(autoGenerate = true) val uid: Long?,
    @ColumnInfo(name="party_no") val partyNo: Int,
    @ColumnInfo(name="host_no") val hostNo: Int,
    @ColumnInfo(name="enter_time") val enterTime: Long?,
    @ColumnInfo(name="exit_time") val exitTime: Long?
)
