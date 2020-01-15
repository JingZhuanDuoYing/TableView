package cn.jingzhuan.tableview.demo.lesson3

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import cn.jingzhuan.tableview.demo.elements.*
import cn.jingzhuan.tableview.demo.lesson3.elements.DataBindingTextViewColumn
import cn.jingzhuan.tableview.demo.lesson3.elements.ImageViewColumn
import cn.jingzhuan.tableview.demo.lesson3.elements.TextViewColumn
import cn.jingzhuan.tableview.element.Column
import cn.jingzhuan.tableview.element.HeaderRow
import io.reactivex.Flowable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers

class Lesson3ViewModel : ViewModel() {

    val liveData = MutableLiveData<HeaderRow<*>>()
    private var disposable: Disposable? = null

    fun fetch(rowsCount: Int, columnsCount: Int) {
        disposable = Flowable.fromCallable {
            val titleColumns = mutableListOf<Column>()
            titleColumns.add(TitleHeaderColumn())
            for (i in 0 until columnsCount) {
                titleColumns.add(TitleColumn(i))
            }
            val titleRow = TitleRow(titleColumns)

            for (rowIndex in 0 until rowsCount) {
                val columns = mutableListOf<Column>()
                columns.add(SimpleHeaderColumn("Row${rowIndex + 1}"))
                for (columnIndex in 0 until columnsCount) {
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
        return if (rowIndex == 1 && columnIndex == 1) {
            ImageViewColumn()
        } else if (rowIndex == 2 && columnIndex == 2) {
            TextViewColumn()
        } else if (rowIndex == 3 && columnIndex == 3) {
            DataBindingTextViewColumn()
        } else {
            SimpleColumn("${rowIndex + 1} - ${columnIndex + 1}")
        }
    }

    override fun onCleared() {
        super.onCleared()
        disposable?.dispose()
    }

}