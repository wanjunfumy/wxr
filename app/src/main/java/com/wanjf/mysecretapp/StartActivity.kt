package com.wanjf.mysecretapp

import android.content.Intent
import android.os.Bundle
import com.wanjf.mysecretapp.supers.MyApp

class StartActivity : MyApp() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.start_activity)
    }

    override fun onStart() {
        super.onStart()
        val i = Intent(this, MainActivity().javaClass)
        startActivity(i)
        finish()
    }

    override fun initListeners() {
    }
}