package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [GrammarItemEntity::class], version = 1, exportSchema = false)
abstract class GrammarDatabase : RoomDatabase() {
    abstract fun grammarDao(): GrammarDao

    companion object {
        @Volatile
        private var INSTANCE: GrammarDatabase? = null

        fun getInstance(context: Context): GrammarDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    GrammarDatabase::class.java,
                    "grammar_craft.db"
                ).build().also { INSTANCE = it }
            }
        }
    }
}
