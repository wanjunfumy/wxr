package com.wanjf.mysecretapp.storage.dao

interface SecretDao {
    fun getSecret(): Secret
    fun getAccount(): Account
}