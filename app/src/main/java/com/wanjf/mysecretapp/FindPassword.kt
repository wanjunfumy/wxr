package com.wanjf.mysecretapp

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.wanjf.mysecretapp.supers.MyApp

class FindPassword : MyApp() {

    companion object {
        fun start(context: Context) {
            var i = Intent(context, FindPassword().javaClass)
            context.startActivity(i)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.find_password)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)// 显示返回按钮
        supportActionBar?.title = getString(R.string.find_pwd_title)
        supportActionBar?.subtitle = "哦豁！密码忘记了吧？"
    }

    override fun initListeners() {
    }
}