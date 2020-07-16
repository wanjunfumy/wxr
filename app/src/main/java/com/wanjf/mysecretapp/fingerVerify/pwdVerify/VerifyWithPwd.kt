package com.wanjf.mysecretapp.fingerVerify.pwdVerify

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import com.wanjf.mysecretapp.App
import com.wanjf.mysecretapp.R
import com.wanjf.mysecretapp.SetPassword
import com.wanjf.mysecretapp.tools.AESCrypto
import com.wanjf.wxr.views.secretEdit.SecretEditText

class VerifyWithPwd {
    private constructor()
    var verify: AlertDialog? = null

    var context: Context? = null
    var item: MenuItem? = null

    constructor(context: Context, item: MenuItem) {
        this.context = context
        this.item = item
    }

    fun verify(query: (b: Boolean) -> Unit): VerifyWithPwd {

        if (App.getInstance().hasVerify) {
            item?.icon = context?.getDrawable(R.mipmap.pwd_locked)
            App.getInstance().hasVerify = false
            query(false)
            return this
        }

        // 数据库验证一下，是不是有账号密码
        val accountData = App.getInstance().getAccount()

        // 如果有，就弹出来。如果没有，就设置一个
        if (accountData != null) {
            var aes = AESCrypto()
            var myPwd = aes.decrypt(accountData.pwd)
            val view: View = LayoutInflater.from(context).inflate(R.layout.pwd_verify, null)
            view.findViewById<SecretEditText>(R.id.id_pwd_verify).setOnSecretPasswordListener { password, complete ->
                if (complete) {
                    // 去数据库验证
                    if (myPwd.compareTo(password) == 0) {
                        Toast.makeText(context, context?.getString(R.string.verify_success), Toast.LENGTH_LONG).show()
                        // 成功，就请求一下更多数据，就是更高级的
                        verify?.dismiss()
                        App.getInstance().hasVerify = true
                        item?.icon = context?.getDrawable(R.mipmap.pwd)
                        // 查询
                        query(true)
                    } else {
                        Toast.makeText(context, context?.getString(R.string.verify_unsuccess), Toast.LENGTH_LONG).show()
                    }
                }
            }

            verify = AlertDialog.Builder(context)
                .setView(view)
                .setTitle(context?.getString(R.string.pwd_verify))
                .setNegativeButton(R.string.cancel, null).create()
            verify!!.show()
        } else {
            SetPassword.start(context!!)
        }

        return this
    }
}