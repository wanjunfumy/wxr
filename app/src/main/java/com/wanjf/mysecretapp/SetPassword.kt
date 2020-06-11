package com.wanjf.mysecretapp

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import com.wanjf.mysecretapp.storage.DBBuilder
import com.wanjf.mysecretapp.storage.bean.AccountData
import com.wanjf.mysecretapp.supers.MyApp
import com.wanjf.mysecretapp.tools.AESCrypto
import kotlinx.android.synthetic.main.set_password.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class SetPassword : MyApp() {

    companion object {
        private var isModify = false
        fun start(context: Context) {
            isModify = false
            val i = Intent(context, SetPassword().javaClass)
            context.startActivity(i)
        }

        fun start(context: Context, isModify: Boolean) {
            Companion.isModify = isModify
            val i = Intent(context, SetPassword().javaClass)
            context.startActivity(i)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.set_password)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)// 显示返回按钮
        if (isModify) {
            supportActionBar?.title = getString(R.string.modify_pwd_title)
            supportActionBar?.subtitle = getString(R.string.modify_wtf)
        } else {
            supportActionBar?.title = getString(R.string.setting_pwd)
            supportActionBar?.subtitle = getString(R.string.set_pwd_tip)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            Menu.FIRST + 1 -> {// 保存
                if (check()) {// 合格了。就保存密码
                    val account = AccountData(0)
                    val aesCrypto = AESCrypto()
                    val list = ArrayList<AccountData>()
                    var sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA)

                    account.account = getString(R.string.yes_is_me)
                    account.createTime = sdf.format(Date())

                    account.pwd = aesCrypto.byteToString(aesCrypto.AES256ECBEncrypt(id_pwd.text.toString()))
                    list.add(account)
                    GlobalScope.launch {
                        if (isModify) {
                            DBBuilder.getDatabase().getAccount().modify(account.pwd!!, App.getInstance().getAccount()!!.id)
                        } else {
                            DBBuilder.getDatabase().getAccount().insert(list)
                        }

                        // 不管了，就算是设置成功
                        App.getInstance().account = listOf(account)
                        App.getInstance().hasVerify = true

                        val msg = handler.obtainMessage()
                        msg.what = 1
                        msg.sendToTarget()
                    }

                }
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private val handler = Handler(Looper.getMainLooper()) {
        when {
            it.what == 0 -> {// 失败
                Toast.makeText(this, getString(R.string.opps_unsuccess), Toast.LENGTH_LONG).show()
            }
            it.what > 0 -> {// 插入成功了
                Toast.makeText(this, getString(R.string.save_success), Toast.LENGTH_LONG).show()
                finish()// 退出页面
            }
        }
        false
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menu?.add(
            Menu.NONE,
            Menu.FIRST + 1,
            0,
            getString(R.string.save)
        )
        return super.onCreateOptionsMenu(menu)
    }

    override fun initListeners() {}

    private fun check() : Boolean {
        if (id_pwd.text.toString().isEmpty() ||
            id_pwd_confirm.text.toString().isEmpty()) {
            Toast.makeText(this, getString(R.string.what_are_you_thinking_about), Toast.LENGTH_LONG).show()
            return false
        }
        if (id_pwd.text.toString().compareTo(id_pwd_confirm.text.toString()) != 0) {
            AlertDialog.Builder(this)
                .setTitle(R.string.delete_tips)
                .setMessage(getString(R.string.pwd_nu_equals, id_pwd.text.toString(), id_pwd_confirm.text.toString()))
                .setPositiveButton(getString(R.string.ok), null)
                .show()
            return false
        }
        if (isModify) {
            val pwd = App.getInstance().getPwd()
            if (id_pwd_old.text.toString().compareTo(pwd) != 0) {
                Toast.makeText(this, getString(R.string.incorrect_pwd), Toast.LENGTH_LONG).show()
                return false
            }
        }
        return true
    }
}
