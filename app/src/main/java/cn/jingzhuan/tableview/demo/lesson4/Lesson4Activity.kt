package cn.jingzhuan.tableview.demo.lesson4

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import cn.jingzhuan.tableview.demo.R
import cn.jingzhuan.tableview.demo.databinding.ActivityLesson4Binding

class Lesson4Activity : AppCompatActivity() {

    private lateinit var binding: ActivityLesson4Binding
    private lateinit var viewModel: Lesson4ViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_lesson_4)
        viewModel = ViewModelProviders.of(this)[Lesson4ViewModel::class.java]

        binding.tableView.setRowsDividerEnabled(true)
        binding.tableView.setColumnsDividerEnabled(true)
        binding.tableView.updateTableSize(5, 1)
        binding.tableView.setStretchMode(true)

        subscribe()
        viewModel.fetch(100, 5)
    }

    private fun subscribe() {
        viewModel.liveData.observe(this, Observer {
            binding.tableView.setHeaderRow(it)
            binding.tableView.notifyDataSetChanged()
        })
    }

}