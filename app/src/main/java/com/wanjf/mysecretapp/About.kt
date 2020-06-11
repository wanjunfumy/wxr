package com.wanjf.mysecretapp

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import com.wanjf.mysecretapp.supers.MyApp

class About: MyApp() {
    companion object {

        fun start(context: Context) {
            val intent = Intent(context, About().javaClass)
            context.startActivity(intent)
        }
    }

    override fun onCreate(b: Bundle?) {
        super.onCreate(b)
        setContentView(R.layout.about)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)// 显示返回按钮
        supportActionBar?.title = getString(R.string.about)
    }

    override fun initListeners() {

    }

}