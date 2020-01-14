package cn.jingzhuan.tableview.demo.lesson1

import android.content.Context
import android.text.SpannableString
import android.text.SpannableStringBuilder
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

    fun fetch(context: Context, columns: Int) {
        disposable = Flowable.fromCallable { columns }
            .map {
                val titleRow = constructTitleRow(it)
                val data = constructRowData(it)
                val dataRows = constructRows(data)
                titleRow.rows.addAll(dataRows)
                titleRow.measureAndLayoutInBackground(context)
                titleRow
            }
            .subscribeOn(Schedulers.io())
            .subscribe({
                liveData.postValue(it)
            }, {

            })
    }

    private fun constructTitleRow(columnsCount: Int): TitleRow {
        val titleColumns = mutableListOf<Column>()
        titleColumns.add(TitleHeaderColumn())
        for (i in 0 until columnsCount) {
            titleColumns.add(TitleColumn(i))
        }
        return TitleRow(titleColumns)
    }

    private fun constructRowData(columnsCount: Int): List<RowData> {
        val rows = mutableListOf<RowData>()
        for (i in 0 until 30) {
            val columns = mutableListOf<ColumnData>()
            val row = RowData(title = "Row${i + 1}", columns = columns)
            for (j in 0 until columnsCount) {
                columns.add(ColumnData(SpannableStringBuilder("${i + 1} - ${j + 1}")))
            }
            rows.add(row)
        }
        return rows
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

    override fun onCleared() {
        super.onCleared()
        disposable?.dispose()
    }

}