package com.wanjf.mysecretapp.storage.dao

import androidx.room.*
import com.wanjf.mysecretapp.storage.bean.AccountData

// 本人自己用，只看只有一个密码，没有，那就自己调试找回密码
@Dao
interface Account {
    @Query("select * from user_account")
    fun getAccount(): List<AccountData?>?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(users: List<AccountData?>?)

    @Query("update user_account set pwd = :pwd where id = :id")
    fun modify(pwd: String, id: Int)
}