package com.wanjf.mysecretapp

import android.app.AlertDialog.*
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.*
import android.widget.CheckBox
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.wanjf.mysecretapp.fingerVerify.biometriclib.BiometricPromptManager
import com.wanjf.mysecretapp.fingerVerify.pwdVerify.VerifyWithPwd
import com.wanjf.mysecretapp.storage.DBBuilder
import com.wanjf.mysecretapp.storage.bean.SecretData
import com.wanjf.mysecretapp.supers.MyApp
import com.wanjf.mysecretapp.tools.AESCrypto
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.data_item.view.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.system.exitProcess


/**
 * 只要是6位密码，我都对，服不服？我会给密码计算出一个等级，然后我存储的数据也有等级。你输入的密码都对，你服不服。只有输入正确的密码，才能看到真正完全的存储。你服不服
 * 当然也有指纹解锁
 */
class MainActivity : MyApp() {

    //region views
    // oops, kotlin, no views in activity
    //endregion

    //region 数据
    private var datas: List<SecretData?>? = null
    private var mManager: BiometricPromptManager? = null
    //endregion

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        id_secret_data.layoutManager = LinearLayoutManager(this)
        try {
            mManager = BiometricPromptManager.from(this)
        } catch (e: Exception) {
            // 不显示指纹，显示密码
        }
    }

    private fun query() {
        GlobalScope.launch {
            datas = if (App.getInstance().hasVerify) {
                DBBuilder.getDatabase().getSecret().getAll()
            } else {
                DBBuilder.getDatabase().getSecret().getNormalSecret()
            }

            val msg = handler.obtainMessage()
            msg.what = 0
            msg.sendToTarget()
        }

    }

    override fun onResume() {
        super.onResume()
        GlobalScope.launch {
            App.getInstance().account = DBBuilder.getDatabase().getAccount().getAccount()
        }
        query()
    }

    override fun onRestart() {
        super.onRestart()
        invalidateOptionsMenu()
    }

    private val handler: Handler = Handler(Looper.getMainLooper()) {
        when (it.what) {
            0 -> {
                if (id_secret_data.adapter == null) {
                    id_secret_data.adapter = DataAdapter(datas, this)
                    (id_secret_data.adapter as DataAdapter).itemClick =
                        object : OnItemClickListener {
                            override fun onItemClick(view: View, sData: SecretData, position: Int) {
                                Operate.start(this@MainActivity, sData.id)
                                cancelDelete()
                                (id_secret_data.adapter as DataAdapter).resetCanDelete()
                            }
                        }
                } else {
                    (id_secret_data.adapter as DataAdapter).setData(datas)
                }

            }
            1 -> {// 删除
                Toast.makeText(this, getString(R.string.delete_success), Toast.LENGTH_LONG).show()
                cancelDelete()
                // 去除选择，只要重新请求一次就行
                query()

            }
        }
        false
    }

    // region menu

    /**
     * 控制解锁的类型
     */
    private fun controlVerify(menu: Menu?) {
        when (mManager) {
            null -> {// 没有指纹解锁功能，或者异常了，那就密码咯
                menu?.findItem(R.id.finger)?.isVisible = false
                menu?.findItem(R.id.pwd)?.isVisible = true
            }
            else -> {
                menu?.findItem(R.id.find_password)?.isVisible = false
                menu?.findItem(R.id.modify_password)?.isVisible = false
            }
        }
    }

    /**
     * 重新更新下状态
     */
    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        var item = menu?.findItem(R.id.pwd)// 密码
        if (App.getInstance().hasVerify) {
            item?.icon = getDrawable(R.mipmap.pwd)
        } else {
            item?.icon = getDrawable(R.mipmap.pwd_locked)
        }
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_item, menu)
        controlVerify(menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_add -> {// 添加一个
                Operate.start(this@MainActivity)
            }
            R.id.action_delete -> {// 显示所有的数据，选择性删除
                if (datas?.size == 0) {
                    Toast.makeText(this, getString(R.string.no_datas_opps), Toast.LENGTH_LONG).show()
                    return false
                }
                if (id_secret_data.adapter == null) {
                    id_delete_area.visibility = View.GONE
                    return false
                }
                (id_secret_data.adapter as DataAdapter).setDataAllCheck(true)
                showDelete()
            }
            R.id.action_exit -> {// 退出程序
                exitProcess(0)
            }
            R.id.action_mine -> {// 关于
                About.start(this@MainActivity)
            }
            R.id.finger -> {// 指纹验证，没有什么密码，只有指纹解锁
                if (mManager!!.isBiometricPromptEnable) {
                    mManager?.authenticate(object : BiometricPromptManager.OnBiometricIdentifyCallback {
                        override fun onUsePassword() {
                            VerifyWithPwd(this@MainActivity, item).verify {
                                query()
                            }
                        }
                        override fun onSucceeded() {
                            Toast.makeText(this@MainActivity, "onSucceeded", Toast.LENGTH_SHORT)
                                .show()
                            App.getInstance().hasVerify = true
                            query()
                        }
                        override fun onFailed() {
                            Toast.makeText(this@MainActivity, "onFailed", Toast.LENGTH_SHORT).show()
                        }

                        override fun onError(code: Int, reason: String?) {
                            Toast.makeText(this@MainActivity, "onError", Toast.LENGTH_SHORT).show()
                        }

                        override fun onCancel() {
                            Toast.makeText(this@MainActivity, "onCancel", Toast.LENGTH_SHORT).show()
                        }
                    })
                }
            }
            R.id.pwd -> {// 弹出密码验证
                VerifyWithPwd(this, item).verify {
                    query()
                }
            }
            R.id.find_password -> {// 找回密码
                FindPassword.start(this)
            }
            R.id.modify_password -> {// 修改密码
                SetPassword.start(this, true)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    // endregion

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        when (keyCode) {
            KeyEvent.KEYCODE_BACK -> {
                if (id_delete_area.visibility == View.VISIBLE) {
                    cancelDelete()
                    return false
                } else {
                    exitProcess(0)
                }
            }
        }
        return super.onKeyDown(keyCode, event)
    }

    /**
     * 显示删除按钮，一定要用这个，因为有点逻辑
     */
    private fun showDelete() {
        id_delete_area.visibility = View.VISIBLE
        changeDelete()
    }

    private fun cancelDelete() {
        id_delete_area.visibility = View.GONE
        (id_secret_data.adapter as DataAdapter).setDataAllCheck(false)
    }

    fun changeDelete() {
        if (id_secret_data.adapter != null &&
            id_delete_all.visibility == View.VISIBLE
        ) {
            val num = (id_secret_data.adapter as DataAdapter).getChoiceNum()
            id_delete_all.text = getString(R.string.delete_p, num.toString())
        }
    }

    override fun initListeners() {
        // 删除数据
        id_delete_all.setOnClickListener {
            Builder(this).setTitle(getString(R.string.delete_tips))
                .setMessage(getString(R.string.sure_delete)).setPositiveButton(
                    getString(
                        R.string.sure
                    )
                ) { _, _ ->
                    GlobalScope.launch {
                        val list: List<SecretData?>? =
                            (id_secret_data.adapter as DataAdapter).getChoiceItem()
                        DBBuilder.getDatabase().getSecret().delete(list)
                        val msg = handler.obtainMessage()
                        msg.what = 1
                        msg.sendToTarget()
                    }
                }.setNegativeButton(getString(R.string.cancel), null).show()
        }

        // 取消
        id_delete_cancel.setOnClickListener {
            cancelDelete()
            (id_secret_data.adapter as DataAdapter).resetCanDelete()
        }
    }


    interface OnItemClickListener {
        fun onItemClick(view: View, sData: SecretData, position: Int)
    }

    //region 内部类

    class DataAdapter() :
        RecyclerView.Adapter<ViewHolder>() {
        private var datas: List<SecretData?>? = null
        private var context: Context? = null
        private var canDelete = false
        private var aes = AESCrypto()

        constructor(datas: List<SecretData?>?, context: Context) : this() {
            this.datas = datas
            this.context = context
            filterAndSort()
        }

        private val c1: Comparator<SecretData?> = Comparator { o1, o2 ->
            var sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA)
            var long1: Long = sdf.parse(o1!!.modify!!)!!.time
            var long2: Long = sdf.parse(o2!!.modify!!)!!.time
            if (long2 == long1) {
                0
            } else {
                var i = (long2 - long1).toInt()
                if (i > 0) {
                    1
                } else {
                    -1
                }
            }
        }

        /**
         * 是否显示删除按钮
         */
        fun setDataAllCheck(b: Boolean) {
            canDelete = b
            notifyDataSetChanged()
        }

        /**
         * 复位
         */
        fun resetCanDelete() {
            this.datas?.forEach {
                it!!.canDelete = false
            }
        }

        fun getChoiceNum(): Int {
            var i = 0
            this.datas?.forEach {
                if (it!!.canDelete) i++
            }
            return i
        }

        fun getChoiceItem(): List<SecretData?>? {
            val list: List<SecretData?>? = ArrayList()
            this.datas?.forEach {
                if (it!!.canDelete)
                    (list as ArrayList).add(it)
            }
            return list
        }

        fun setData(data: List<SecretData?>?) {
            this.datas = data
            filterAndSort()
            notifyDataSetChanged()
        }

        private fun filterAndSort() {
            (this.datas as MutableList).apply { sortWith(c1) }
        }

        var itemClick: OnItemClickListener? = null

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view: View = LayoutInflater.from(context).inflate(R.layout.data_item, parent, false)
            return ViewHolder(view)
        }

        override fun getItemCount(): Int = datas!!.size

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {

            holder.idName.text = aes.decrypt(datas!![position]!!.title)
            holder.idContent.text = aes.decrypt(datas!![position]!!.value)

            holder.idTime.text = datas!![position]!!.modify
            holder.itemView.setOnClickListener {
                itemClick?.onItemClick(holder.itemView, datas!![position]!!, position)
            }
            if (canDelete)
                holder.idDelete.visibility = View.VISIBLE
            else
                holder.idDelete.visibility = View.GONE

            holder.idDelete.isChecked = datas!![position]!!.canDelete

            holder.idDelete.tag = datas!![position]!!
            holder.idDelete.setOnCheckedChangeListener { button, b ->
                val data: SecretData = button.tag as SecretData
                data.canDelete = b
                (context as MainActivity).changeDelete()
            }
        }
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var idName: TextView = itemView.id_name
        var idTime: TextView = itemView.id_time
        var idContent: TextView = itemView.id_content
        var idDelete: CheckBox = itemView.id_delete
    }

    //endregion
}

