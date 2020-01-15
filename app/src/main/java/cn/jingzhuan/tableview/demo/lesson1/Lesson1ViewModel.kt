package cn.jingzhuan.tableview.demo.lesson1

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import cn.jingzhuan.tableview.demo.elements.*
import cn.jingzhuan.tableview.element.Column
import io.reactivex.Flowable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers

class Lesson1ViewModel : ViewModel() {

    val liveData = MutableLiveData<TitleRow>()
    private var disposable: Disposable? = null

    fun fetch(rowsCount: Int, columnsCount: Int) {
        disposable = Flowable.fromCallable { columnsCount }
            .map {

                val titleColumns = mutableListOf<Column>()
                titleColumns.add(TitleHeaderColumn())
                for (i in 0 until columnsCount) {
                    titleColumns.add(TitleColumn(i))
                }
                val titleRow = TitleRow(titleColumns)

                for (rowIndex in 0 until rowsCount) {
                    val rowColumns = mutableListOf<Column>()
                    rowColumns.add(SimpleHeaderColumn("Row${rowIndex + 1}"))
                    for (columnIndex in 0 until columnsCount) {
                        rowColumns.add(SimpleColumn("${rowIndex + 1} - ${columnIndex + 1}"))
                    }
                    titleRow.rows.add(SimpleRow(rowColumns))
                }

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