package com.wanjf.mysecretapp.storage.bean

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_account")
data class AccountData constructor(@PrimaryKey(autoGenerate = true) var id: Int) {
    var account: String? = ""
    var pwd: String? = ""
    var createTime: String? = ""
}