package cn.jingzhuan.tableview.demo.lesson4

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import cn.jingzhuan.tableview.demo.elements.*
import cn.jingzhuan.tableview.element.Column
import cn.jingzhuan.tableview.element.HeaderRow
import io.reactivex.Flowable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers

class Lesson4ViewModel : ViewModel() {

    val liveData = MutableLiveData<HeaderRow<*>>()
    private var disposable: Disposable? = null

    fun fetch(rowsCount: Int, columnsCount: Int) {
        disposable = Flowable.fromCallable {
            val titleColumns = mutableListOf<Column>()
            titleColumns.add(TitleHeaderColumn())
            for (i in 1 until columnsCount) {
                val column = TitleColumn(i)
                titleColumns.add(column)
            }
            val titleRow = TitleRow(titleColumns)

            for (rowIndex in 0 until rowsCount) {
                val columns = mutableListOf<Column>()
                columns.add(SimpleHeaderColumn("Row${rowIndex + 1}"))
                for (columnIndex in 1 until columnsCount) {
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
        return SimpleColumn("${rowIndex + 1} - ${columnIndex + 1}")
    }

    override fun onCleared() {
        super.onCleared()
        disposable?.dispose()
    }

}