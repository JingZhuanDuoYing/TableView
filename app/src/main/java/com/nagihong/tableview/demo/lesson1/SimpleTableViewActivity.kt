package com.nagihong.tableview.demo.lesson1

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.nagihong.tableview.demo.R
import com.nagihong.tableview.demo.databinding.ActivitySimpleTableViewBinding
import com.nagihong.tableview.demo.lesson1.elements.*
import com.nagihong.tableview.element.Column

class SimpleTableViewActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySimpleTableViewBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this,
            R.layout.activity_simple_table_view
        )
        val rows = mutableListOf<RowData>()
        val columnsCount = 30

        for (i in 0 until 100) {
            val columns = mutableListOf<ColumnData>()
            val row = RowData(title = "Row$i", columns = columns)
            for (j in 0 until 30) {
                val value = if (i == 0) "Column ${j + 1}" else "$i - ${j + 1}"
                columns.add(ColumnData(value))
            }
            rows.add(row)
        }

        val titleColumns = mutableListOf<Column>()
        titleColumns.add(TitleHeaderColumn())
        for(i in 0 until columnsCount) {
            titleColumns.add(TitleColumn(i))
        }
        val titleRow = TitleRow(titleColumns)

        val tableRows = constructRows(rows)
        val dataRows = tableRows.subList(1, tableRows.size).toMutableList()

        binding.tableView.columnsLayoutManager.updateTableSize(titleRow.columns.size, 1)
        binding.tableView.columnsLayoutManager.measureAndLayoutInBackground(this, titleRow)
        tableRows.forEach {
            binding.tableView.columnsLayoutManager.measureAndLayoutInBackground(this, it)
        }

        binding.tableView.adapter.setTitleRow(titleRow)
        binding.tableView.adapter.setRows(dataRows)
        binding.tableView.adapter.notifyDataSetChanged()
    }

    private fun constructRows(rows: List<RowData>): List<SimpleRow> {
        return rows.map { constructRow(it) }
    }

    private fun constructRow(row: RowData): SimpleRow {
        val tableColumns = mutableListOf<Column>()
        tableColumns.add(SimpleHeaderColumn(row))
        tableColumns.addAll(row.columns.map { columnData -> SimpleColumn(columnData) })
        return SimpleRow(row, tableColumns)
    }

}