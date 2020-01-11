package cn.jingzhuan.tableview.demo

import android.app.Application
import cn.jingzhuan.tableview.TableViewLog

class TableViewDemoApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        TableViewLog.setDelegate(TableViewLogDelegate())
    }

}