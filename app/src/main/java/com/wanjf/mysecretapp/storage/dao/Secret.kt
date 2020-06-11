package com.wanjf.mysecretapp.storage.dao

import androidx.room.*
import com.wanjf.mysecretapp.storage.bean.SecretData

@Dao
interface Secret {

    @Query("SELECT * FROM secret")
    fun getAll(): List<SecretData?>?

    // 请求等级为1的
    @Query("SELECT * FROM secret where level = 1")
    fun getNormalSecret(): List<SecretData?>?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(users: List<SecretData>)


    @Delete
    fun delete(data: SecretData)

    @Delete
    fun delete(args: List<SecretData?>?)

    @Query("SELECT * FROM secret WHERE id = :id")
    fun queryById(id: Int): SecretData

    @Query("UPDATE secret SET level = :level, title = :title, value = :value, modify = :modify WHERE id = :id")
    fun update(id: Int, level: Int?, title: String?, value: String?, modify: String?): Int

    // 告诉我，为什么更新失败？
    @Update(entity = SecretData::class, onConflict = OnConflictStrategy.REPLACE)
    fun update(data: SecretData): Int
}