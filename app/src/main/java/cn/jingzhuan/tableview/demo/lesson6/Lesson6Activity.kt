package cn.jingzhuan.tableview.demo.lesson6

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import cn.jingzhuan.tableview.demo.R
import cn.jingzhuan.tableview.demo.databinding.ActivityLesson6Binding

class Lesson6Activity: AppCompatActivity() {

    private lateinit var binding: ActivityLesson6Binding
    private lateinit var viewModel: Lesson6ViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_lesson_6)
        viewModel = ViewModelProviders.of(this)[Lesson6ViewModel::class.java]

        binding.tableView.setRowsDividerEnabled(true)
        binding.tableView.setColumnsDividerEnabled(true)
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