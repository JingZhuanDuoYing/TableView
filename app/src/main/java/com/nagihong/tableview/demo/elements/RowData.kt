package com.nagihong.tableview.demo.elements

import java.util.*

class RowData(
    val id: String = UUID.randomUUID().toString(),
    val title: CharSequence,
    val columns: List<ColumnData>
)