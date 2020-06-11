package com.wanjf.mysecretapp.storage.bean

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey

@Entity(tableName = "secret")
data class SecretData constructor(@PrimaryKey(autoGenerate = true) var id: Int) {

    var level: Int? = 0// 1，普通级，0，高级
    var title: String? = ""
    var value: String? = ""
    var createTime: String? = ""
    var modify: String? = ""

    @Ignore
    var canDelete: Boolean = false
}