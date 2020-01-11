package com.nagihong.tableview.demo

import android.app.Application
import com.nagihong.tableview.TableViewLog

class TableViewDemoApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        TableViewLog.setDelegate(TableViewLogDelegate())
    }

}