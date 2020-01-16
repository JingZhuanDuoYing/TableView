package cn.jingzhuan.tableview.demo.lesson7

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import cn.jingzhuan.tableview.demo.R
import cn.jingzhuan.tableview.demo.databinding.ActivityLesson7Binding

class Lesson7Activity : AppCompatActivity() {

    private lateinit var binding: ActivityLesson7Binding
    private lateinit var viewModel: Lesson7ViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        title = "Lesson 7"
        binding = DataBindingUtil.setContentView(this, R.layout.activity_lesson_7)
        viewModel = ViewModelProviders.of(this)[Lesson7ViewModel::class.java]

        binding.tableView.setRowsDividerEnabled(true)
        binding.tableView.setColumnsDividerEnabled(true)
        binding.tableView.setDirectionLockEnabled(false)
        binding.tableView.updateTableSize(100, 1)

        subscribe()
        viewModel.fetch(3, 100, 100)
    }

    private fun subscribe() {
        viewModel.liveData.observe(this, Observer {
            binding.tableView.setHeaderRow(it)
            binding.tableView.notifyDataSetChanged()
        })
    }

}