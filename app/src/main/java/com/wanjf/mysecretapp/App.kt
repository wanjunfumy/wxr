package com.wanjf.mysecretapp

import android.app.Application
import androidx.annotation.Nullable
import com.wanjf.mysecretapp.storage.DBBuilder
import com.wanjf.mysecretapp.storage.bean.AccountData
import com.wanjf.mysecretapp.storage.database.DatabaseAdapter
import com.wanjf.mysecretapp.tools.AESCrypto

class App : Application() {
    var account: List<AccountData?>? = null

    var hasVerify = false// 是否认证过，不管是账号，还是指纹

    companion object {
        private var instance: App? = null
        fun getInstance(): App {
            return instance!!
        }
    }

    @Nullable
    fun getAccount(): AccountData? {
        if (account != null && account?.size!! > 0) {
            return account?.get(0)
        }
        return null
    }

    /**
     * 获取密码
     */
    fun getPwd(): String {
        val account = getAccount()
        val aes = AESCrypto()
        return aes.decrypt(account?.pwd)
    }

    /**
     * 只要有账号密码，就可以是高级
     */
    fun isHighLevel(): Boolean {
        if (account == null || account?.size!! == 0) {
            return false
        }
        return true
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        DBBuilder.build(this, DatabaseAdapter::class.java)
    }
}