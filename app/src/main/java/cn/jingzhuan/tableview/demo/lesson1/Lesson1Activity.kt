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
    private val viewModel by lazy { ViewModelProviders.of(this)[Lesson1ViewModel::class.java] }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(
            this,
            R.layout.activity_lesson_1
        )

        subscribe()
        viewModel.fetch(this, 30)
    }

    private fun subscribe() {
        viewModel.liveData.observe(this, Observer {
            binding.tableView.headerRow = it
            binding.tableView.notifyDataSetChanged()
        })
    }

}