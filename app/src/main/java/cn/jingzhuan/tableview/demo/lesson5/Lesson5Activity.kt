package cn.jingzhuan.tableview.demo.lesson5

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import cn.jingzhuan.tableview.demo.R
import cn.jingzhuan.tableview.demo.databinding.ActivityLesson5Binding

class Lesson5Activity : AppCompatActivity() {

    private lateinit var binding: ActivityLesson5Binding
    private lateinit var viewModel: Lesson5ViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        title = "Lesson 5"
        binding = DataBindingUtil.setContentView(this, R.layout.activity_lesson_5)
        viewModel = ViewModelProviders.of(this)[Lesson5ViewModel::class.java]

        binding.tableView.setRowsDividerEnabled(true)
        binding.tableView.setColumnsDividerEnabled(true)
        binding.tableView.updateTableSize(100, 1)

        subscribe()
        viewModel.fetch(100, 100)
    }

    private fun subscribe() {
        viewModel.liveData.observe(this, Observer {
            binding.tableView.setHeaderRow(it)
            binding.tableView.notifyDataSetChanged()
        })
    }

}