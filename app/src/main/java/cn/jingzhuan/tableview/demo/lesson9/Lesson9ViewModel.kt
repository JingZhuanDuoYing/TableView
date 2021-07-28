package cn.jingzhuan.tableview.demo.lesson9

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import cn.jingzhuan.tableview.demo.elements.*
import cn.jingzhuan.tableview.element.Column
import io.reactivex.Flowable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers

class Lesson9ViewModel : ViewModel() {

    val liveData = MutableLiveData<TitleRow>()
    private var disposable: Disposable? = null

    fun fetch(context: Context, rowsCount: Int, columnsCount: Int) {
        disposable = Flowable.fromCallable { columnsCount }
            .map {

                val titleColumns = mutableListOf<Column>()
                titleColumns.add(TitleHeaderColumn())
                for (i in 0 until columnsCount) {
                    val titleColumn = TitleColumn(i)
                    when (i) {
                        0 -> titleColumn.weight = 1
                        1 -> titleColumn.weight = 1
                        2 -> titleColumn.weight = 0
                    }
                    titleColumn.rightMargin = 10
                    titleColumns.add(titleColumn)
                }
                val titleRow = TitleRow(titleColumns)

                for (rowIndex in 0 until rowsCount) {
                    val rowColumns = mutableListOf<Column>()
                    rowColumns.add(SimpleHeaderColumn("Row${rowIndex + 1}"))
                    for (columnIndex in 0 until columnsCount) {
                        if(columnIndex == 1) {
                            val column = Lesson9ViewColumn()
                            rowColumns.add(column)
                        } else {
                            val column = SimpleColumn("${rowIndex + 1} - ${columnIndex + 1}")
                            column.leftMargin = 40
                            column.rightMargin = 10
                            rowColumns.add(column)
                        }
                    }
                    titleRow.rows.add(SimpleRow(rowColumns))
                }

//                titleRow.preMeasureAllRows(context)
                titleRow
            }
            .subscribeOn(Schedulers.computation())
            .subscribe({
                liveData.postValue(it)
            }, {

            })
    }

    override fun onCleared() {
        super.onCleared()
        disposable?.dispose()
    }

}