package com.wanjf.mysecretapp

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import com.wanjf.mysecretapp.fingerVerify.biometriclib.BiometricPromptManager
import com.wanjf.mysecretapp.fingerVerify.pwdVerify.VerifyWithPwd
import com.wanjf.mysecretapp.storage.DBBuilder
import com.wanjf.mysecretapp.storage.bean.SecretData
import com.wanjf.mysecretapp.supers.MyApp
import com.wanjf.mysecretapp.tools.AESCrypto
import kotlinx.android.synthetic.main.operate.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

/**
 * 新增密码分为2部分，一种是高等，一种是普通，只有正确的密码才会显示
 */
class Operate : MyApp() {
    // region 数据
    var secretData: SecretData? = null
    private var mManager: BiometricPromptManager? = null
    //endregion

    companion object {
        private var id = -1// 需要修改的id，去数据库查询

        /**
         * start at Add operate
         */
        fun start(context: Context) {
            id = -1
            val intent = Intent(context, Operate().javaClass)
            context.startActivity(intent)
        }

        /**
         * start with id, just for modify
         */
        fun start(context: Context, id: Int) {
            Operate.id = id// 方法写在这里的好处，传参不需要放在intent中
            val intent = Intent(context, Operate().javaClass)
            context.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.operate)

        try {
            mManager = BiometricPromptManager.from(this)
        } catch (e: Exception) {
            // 不显示指纹，显示密码
        }

        supportActionBar?.setDisplayHomeAsUpEnabled(true)// 显示返回按钮
        if (id != -1) {
            supportActionBar?.title = getString(R.string.modify)
        } else {
            supportActionBar?.title = resources.getString(R.string.add)
        }
        supportActionBar?.subtitle = getString(R.string.tips)
        if (id != -1) {
            GlobalScope.launch {
                secretData = DBBuilder.getDatabase().getSecret().queryById(id)
                val msg = handler.obtainMessage()
                msg.sendToTarget()
            }
        }

        id_level.isEnabled = App.getInstance().hasVerify// 如果没有验证过，那就只能是普通等级的了
    }

    override fun onRestart() {
        super.onRestart()
        invalidateOptionsMenu()
    }

    // region 函数

    private val handler: Handler = Handler(Looper.getMainLooper()) {
        when (it.what) {
            1 -> {// 操作成功
                Toast.makeText(this@Operate, getString(R.string.save_success), Toast.LENGTH_LONG)
                    .show()
                finish()
            }
            2 -> {// 操作失败
                Toast.makeText(this@Operate, getString(R.string.save_unsuccess), Toast.LENGTH_LONG)
                    .show()
            }
            else -> {// 赋值数据
                var aes = AESCrypto()
                id_title.setText(aes.decrypt(secretData!!.title))
                id_value.setText(aes.decrypt(secretData!!.value))
                setLevel(secretData!!.level!!)
            }
        }
        false
    }

    private fun setLevel(level: Int) {
        if (level == 1) {
            id_level.setSelection(0)
        } else {
            id_level.setSelection(1)
        }
    }

    private fun getLevel(): Int =
        if (id_level.selectedItem?.toString().equals(getString(R.string.normal_level))) {
            1
        } else {
            0
        }


    private fun check(): Boolean {
        if (id_title.text.toString().isEmpty()) {
            Toast.makeText(this, R.string.title, Toast.LENGTH_LONG).show()
            return false
        }
        if (id_value.text.toString().isEmpty()) {
            Toast.makeText(this, R.string.input_value, Toast.LENGTH_LONG).show()
            return false
        }
        return true
    }

    // endregion

    override fun initListeners() {
        // EditText监听
        id_value.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if (p0.toString().isNotEmpty()) {
                    if (id_clear.visibility == View.GONE) {
                        id_clear.visibility = View.VISIBLE
                    }
                } else {
                    id_clear.visibility = View.GONE
                }
            }
        })
        // Clear内容
        id_clear.setOnClickListener {
            id_value.text.clear()
        }
    }

    // region menu

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        var item = menu?.findItem(R.id.menu_pwd)// 密码
        if (App.getInstance().hasVerify) {
            item?.icon = getDrawable(R.mipmap.pwd)
        } else {
            item?.icon = getDrawable(R.mipmap.pwd_locked)
        }
        id_level.isEnabled = App.getInstance().hasVerify
        return super.onPrepareOptionsMenu(menu)
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_save -> {// 点击Save
                // TODO 保存
                if (!check()) return false
                var createTime = ""
                var sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA)
                createTime = sdf.format(Date())

                var list: List<SecretData> = ArrayList()
                var secret = SecretData(0)
                var aes = AESCrypto()
                secret.level = getLevel()
                secret.title = aes.byteToString(aes.AES256ECBEncrypt(id_title.text.toString()))
                secret.value = aes.byteToString(aes.AES256ECBEncrypt(id_value.text.toString()))

                if (id != -1) {// 修改
                    GlobalScope.launch {
                        secret.createTime = secretData!!.createTime
                        secret.modify = createTime// 如果是修改，这个就是当前修改的时间
                        (list as ArrayList).add(secret)
                        secret.id = secretData!!.id
//                        var i = DBBuilder.getDatabase().getSecret().update(secret.id, secret.level!!, secret.title!!, secret.value!!, secret.modify!!)// update死活都不行，以后再研究
                        DBBuilder.getDatabase().getSecret().insert(list)
                        var msg = handler.obtainMessage()
                        msg.what = 1
                        msg.sendToTarget()
                    }
                } else {// 新增是，两个都是一个时间
                    secret.createTime = createTime
                    secret.modify = createTime
                    (list as ArrayList).add(secret)
                    GlobalScope.launch {
                        DBBuilder.getDatabase().getSecret().insert(list)
                        var msg = handler.obtainMessage()
                        msg.what = 1
                        msg.sendToTarget()
                    }
                }
            }
            R.id.menu_pwd -> {// 验证密码，或者指纹？
                VerifyWithPwd(this, item).verify {
                    App.getInstance().hasVerify = true
                    id_level.isEnabled = App.getInstance().hasVerify
                }
            }
            R.id.menu_finger -> {// 指纹解锁
                if (mManager!!.isBiometricPromptEnable) {
                    mManager?.authenticate(object : BiometricPromptManager.OnBiometricIdentifyCallback {
                        override fun onUsePassword() {
                            VerifyWithPwd(this@Operate, item).verify {
                                App.getInstance().hasVerify = it
                            }
                        }
                        override fun onSucceeded() {
                            App.getInstance().hasVerify = true
                            id_level.isEnabled = App.getInstance().hasVerify
                        }
                        override fun onFailed() {
                        }

                        override fun onError(code: Int, reason: String?) {
                        }

                        override fun onCancel() {
                        }
                    })
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.add, menu)
        when (mManager) {
            null -> {// 没有指纹解锁功能，或者异常了，那就密码咯
                menu?.findItem(R.id.menu_finger)?.isVisible = false
                menu?.findItem(R.id.menu_pwd)?.isVisible = true
            }
        }
        return super.onCreateOptionsMenu(menu)
    }
    // endregion


}