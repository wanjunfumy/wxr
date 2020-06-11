package com.wanjf.mysecretapp.supers

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.wanjf.mysecretapp.R
import com.wanjf.mysecretapp.tools.StatusBarUtil

abstract class MyApp: AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        StatusBarUtil.setStatusBarColor(this, R.color.colorPrimary)
    }

    override fun onStart() {
        super.onStart()
        initListeners()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finish()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    abstract fun initListeners()
}