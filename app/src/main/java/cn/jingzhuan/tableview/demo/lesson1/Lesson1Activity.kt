package cn.jingzhuan.tableview.demo.lesson1

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import cn.jingzhuan.tableview.demo.R
import cn.jingzhuan.tableview.demo.databinding.ActivityLesson1Binding

class Lesson1Activity : AppCompatActivity() {

    private lateinit var binding: ActivityLesson1Binding
    private lateinit var viewModel: Lesson1ViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        title = "Lesson 1"
        binding = DataBindingUtil.setContentView(
            this,
            R.layout.activity_lesson_1
        )
        viewModel = ViewModelProviders.of(this)[Lesson1ViewModel::class.java]

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