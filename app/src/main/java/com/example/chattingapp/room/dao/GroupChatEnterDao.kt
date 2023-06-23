package com.example.chattingapp.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.chattingapp.room.entity.GroupChatEnterHistory

@Dao
interface GroupChatEnterDao {

    @Query("SELECT enter_time FROM GroupChatEnterHistory WHERE party_no=:partyNo AND host_no=:hostNo " +
            "AND uid = (SELECT MAX(uid) FROM GroupChatEnterHistory WHERE party_no=:partyNo AND host_no=:hostNo )")
    suspend fun getEnterTime(partyNo: Int, hostNo: Int): Long?

    @Query("SELECT exit_time FROM GroupChatEnterHistory WHERE party_no=:partyNo AND host_no=:hostNo " +
            "AND uid = (SELECT MAX(uid) FROM GroupChatEnterHistory WHERE party_no=:partyNo AND host_no=:hostNo )")
    suspend fun getExitTime(partyNo: Int, hostNo: Int): Long?

    @Query("SELECT MAX(uid) FROM GroupChatEnterHistory WHERE party_no=:partyNo AND host_no=:hostNo")
    suspend fun getPrimaryKey(partyNo: Int, hostNo: Int): Long

    @Insert
    suspend fun insertHistory(history: GroupChatEnterHistory): Long

    @Update
    suspend fun updateHistory(history: GroupChatEnterHistory)
}