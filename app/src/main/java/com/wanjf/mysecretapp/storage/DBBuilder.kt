package com.wanjf.mysecretapp.storage

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import com.wanjf.mysecretapp.storage.database.DatabaseAdapter

class DBBuilder {
    companion object {
        private var db: RoomDatabase? = null
        private const val DATABASE_NAME = "db-secret"

        // 不需要返回值，无关风雨月
        fun <T : RoomDatabase?> build(context: Context, database: Class<T>) {
            synchronized(DBBuilder::class) {
                if (db == null) {
                    db = Room.databaseBuilder(context, database, DATABASE_NAME)
                        .fallbackToDestructiveMigration()
                        .build()
                }
            }
        }


        fun getDatabase(): DatabaseAdapter {
            return db!! as DatabaseAdapter
        }

    }


}