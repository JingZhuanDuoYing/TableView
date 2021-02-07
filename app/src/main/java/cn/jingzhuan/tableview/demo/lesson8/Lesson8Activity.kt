package cn.jingzhuan.tableview.demo.lesson8

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders
import cn.jingzhuan.tableview.demo.R
import cn.jingzhuan.tableview.demo.databinding.ActivityLesson8Binding

class Lesson8Activity : AppCompatActivity() {

    private lateinit var binding: ActivityLesson8Binding
    private lateinit var viewModel: Lesson8ViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        title = "Lesson 8"
        viewModel = ViewModelProviders.of(this)[Lesson8ViewModel::class.java]
        binding = DataBindingUtil.setContentView(this, R.layout.activity_lesson_8)

        binding.tableView.setRowsDividerEnabled(true)
        binding.tableView.setColumnsDividerEnabled(true)
        binding.tableView.setDirectionLockEnabled(true)
        binding.tableView.updateTableSize(viewModel.columnsCount, 1)
        binding.tableView.setCoroutineEnabled(true)

        viewModel.liveData.observe(this) {
            binding.tableView.setHeaderRow(it)
            binding.tableView.notifyDataSetChanged()
            binding.tableView.postDelayed(intervalRunnable, 5000)
        }

        viewModel.liveUpdate.observe(this) {
            binding.tableView.notifyDataSetChanged()
        }

        viewModel.init()

    }

    private val intervalRunnable = object : Runnable {

        override fun run() {
            viewModel.update()
            binding.tableView.postDelayed(this, 5000)
        }

    }

}