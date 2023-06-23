package com.example.chattingapp.room.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.chattingapp.room.dao.GroupChatEnterDao
import com.example.chattingapp.room.entity.GroupChatEnterHistory

@Database(entities = [GroupChatEnterHistory::class], version = 3)
abstract class AppDatabase : RoomDatabase(){
    abstract fun groupChatEnterDao(): GroupChatEnterDao
}