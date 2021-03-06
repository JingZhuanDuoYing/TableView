package cn.jingzhuan.tableview.demo

import android.annotation.SuppressLint
import android.util.Log
import cn.jingzhuan.tableview.TableViewLog

@SuppressLint("LogNotTimber")
class TableViewLogDelegate : TableViewLog.TableViewLogDelegate {

    override fun e(tag: String?, msg: String?, vararg obj: Any?) {
        Log.e(tag, msg?.format(obj) ?: "")
    }

    override fun w(tag: String?, msg: String?, vararg obj: Any?) {
        Log.w(tag, msg?.format(obj) ?: "")
    }

    override fun i(tag: String?, msg: String?, vararg obj: Any?) {
        Log.i(tag, msg?.format(obj) ?: "")
    }

    override fun d(tag: String?, msg: String?, vararg obj: Any?) {
        Log.d(tag, msg?.format(obj) ?: "")
    }

    override fun printErrStackTrace(
        tag: String?,
        tr: Throwable?,
        format: String?,
        vararg obj: Any?
    ) {
        Log.e(tag, format?.format(obj) ?: "", tr)
    }

}