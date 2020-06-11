package com.wanjf.mysecretapp.storage.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.wanjf.mysecretapp.storage.bean.AccountData
import com.wanjf.mysecretapp.storage.bean.SecretData
import com.wanjf.mysecretapp.storage.dao.SecretDao

@Database(entities = [SecretData::class, AccountData::class] , version = 5, exportSchema = false)
abstract class DatabaseAdapter: RoomDatabase(), SecretDao