package cn.jingzhuan.tableview.demo.lesson2

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import cn.jingzhuan.tableview.demo.R
import cn.jingzhuan.tableview.demo.databinding.ActivityLesson2Binding

class Lesson2Activity : AppCompatActivity() {

    private lateinit var binding: ActivityLesson2Binding
    private lateinit var viewModel: Lesson2ViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        title = "Lesson 2"
        binding = DataBindingUtil.setContentView(this, R.layout.activity_lesson_2)
        viewModel = ViewModelProviders.of(this)[Lesson2ViewModel::class.java]

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