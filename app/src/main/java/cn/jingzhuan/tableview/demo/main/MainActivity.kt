package cn.jingzhuan.tableview.demo.main

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import cn.jingzhuan.tableview.demo.R
import cn.jingzhuan.tableview.demo.databinding.ActivityMainBinding
import cn.jingzhuan.tableview.demo.lesson1.Lesson1Activity
import cn.jingzhuan.tableview.demo.lesson2.Lesson2Activity
import cn.jingzhuan.tableview.demo.lesson3.Lesson3Activity
import cn.jingzhuan.tableview.demo.lesson4.Lesson4Activity
import cn.jingzhuan.tableview.demo.lesson5.Lesson5Activity
import cn.jingzhuan.tableview.demo.lesson6.Lesson6Activity
import cn.jingzhuan.tableview.demo.lesson7.Lesson7Activity

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        val adapter = MainAdapter()
        binding.recyclerView.adapter = adapter
        adapter.data.add("Lesson 1 - simple demo")
        adapter.data.add("Lesson 2 - custom drawable column")
        adapter.data.add("Lesson 3 - custom view column")
        adapter.data.add("Lesson 4 - stretch mode")
        adapter.data.add("Lesson 5 - column visibility")
        adapter.data.add("Lesson 6 - sticky rows")
        adapter.data.add("Lesson 7 - direction lock")
        adapter.onItemClick = {
            when (it) {
                0 -> startActivity(Intent(this, Lesson1Activity::class.java))
                1 -> startActivity(Intent(this, Lesson2Activity::class.java))
                2 -> startActivity(Intent(this, Lesson3Activity::class.java))
                3 -> startActivity(Intent(this, Lesson4Activity::class.java))
                4 -> startActivity(Intent(this, Lesson5Activity::class.java))
                5 -> startActivity(Intent(this, Lesson6Activity::class.java))
                6 -> startActivity(Intent(this, Lesson7Activity::class.java))
            }
        }
        adapter.notifyDataSetChanged()
    }

}