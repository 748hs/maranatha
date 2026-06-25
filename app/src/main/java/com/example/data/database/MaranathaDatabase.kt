package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.database.dao.AdDao
import com.example.data.database.dao.UserDao
import com.example.data.database.entities.AdEntity
import com.example.data.database.entities.UserEntity

@Database(entities = [UserEntity::class, AdEntity::class], version = 1, exportSchema = false)
abstract class MaranathaDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun adDao(): AdDao

    companion object {
        @Volatile
        private var INSTANCE: MaranathaDatabase? = null

        fun getDatabase(context: Context): MaranathaDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MaranathaDatabase::class.java,
                    "maranatha_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
