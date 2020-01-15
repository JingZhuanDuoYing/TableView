package cn.jingzhuan.tableview.demo.lesson6

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import cn.jingzhuan.tableview.demo.elements.*
import cn.jingzhuan.tableview.demo.lesson3.elements.ImageViewColumn
import cn.jingzhuan.tableview.element.Column
import cn.jingzhuan.tableview.element.HeaderRow
import io.reactivex.Flowable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers

class Lesson6ViewModel : ViewModel() {

    val liveData = MutableLiveData<HeaderRow<*>>()
    private var disposable: Disposable? = null

    fun fetch(stickyRowsCount: Int, rowsCount: Int, columnsCount: Int) {
        disposable = Flowable.fromCallable {
            val titleColumns = mutableListOf<Column>()
            titleColumns.add(TitleHeaderColumn())
            for (i in 0 until columnsCount) {
                titleColumns.add(TitleColumn(i))
            }
            val titleRow = TitleRow(titleColumns)
            for (i in 1 until columnsCount) {
                titleRow.setColumnVisibility(i, i % 2 == 0)
            }

            for (rowIndex in 0 until stickyRowsCount) {
                val columns = mutableListOf<Column>()
                columns.add(SimpleHeaderColumn("Sticky${rowIndex + 1}"))
                for (columnIndex in 0 until columnsCount - 1) {
                    val column = generateColumn(rowIndex, columnIndex)
                    columns.add(column)
                }
                titleRow.stickyRows.add(SimpleRow(columns))
            }

            for (rowIndex in 0 until rowsCount) {
                val columns = mutableListOf<Column>()
                columns.add(SimpleHeaderColumn("Row${rowIndex + 1}"))
                for (columnIndex in 0 until columnsCount - 1) {
                    val column = generateColumn(rowIndex, columnIndex)
                    columns.add(column)
                }
                titleRow.rows.add(SimpleRow(columns))
            }
            titleRow
        }
            .subscribeOn(Schedulers.computation())
            .subscribe({
                liveData.postValue(it)
            }, {})
    }

    private fun generateColumn(rowIndex: Int, columnIndex: Int): Column {
        return if (rowIndex == columnIndex) {
            ImageViewColumn()
        } else {
            SimpleColumn("${rowIndex + 1} - ${columnIndex + 1}")
        }
    }

    override fun onCleared() {
        super.onCleared()
        disposable?.dispose()
    }

}