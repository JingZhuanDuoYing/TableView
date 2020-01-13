package cn.jingzhuan.tableview.demo

import android.app.Application
import cn.jingzhuan.tableview.TableViewLog
import timber.log.Timber

class TableViewDemoApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        TableViewLog.setDelegate(TableViewLogDelegate())
        Timber.plant(Timber.DebugTree())
    }

}