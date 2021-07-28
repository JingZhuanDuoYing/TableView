package cn.jingzhuan.tableview.demo.lesson9

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders
import cn.jingzhuan.tableview.demo.R
import cn.jingzhuan.tableview.demo.databinding.ActivityLesson9Binding

class Lesson9Activity : AppCompatActivity() {

    private lateinit var binding: ActivityLesson9Binding
    private lateinit var viewModel: Lesson9ViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        title = "Lesson 9"
        binding = DataBindingUtil.setContentView(this, R.layout.activity_lesson_9)
        viewModel = ViewModelProviders.of(this)[Lesson9ViewModel::class.java]

        binding.tableView.updateTableSize(15, 1, 3)

        viewModel.liveData.observe(this) {
            binding.tableView.setHeaderRow(it)
            binding.tableView.notifyDataSetChanged()
        }

        viewModel.fetch(this, 100, 15)
    }

}