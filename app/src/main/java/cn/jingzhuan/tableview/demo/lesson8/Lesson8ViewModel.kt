package cn.jingzhuan.tableview.demo.lesson8

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import cn.jingzhuan.tableview.demo.elements.*
import cn.jingzhuan.tableview.element.Column
import cn.jingzhuan.tableview.element.HeaderRow
import io.reactivex.Flowable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlin.random.Random

class Lesson8ViewModel : ViewModel() {

    val liveData = MutableLiveData<HeaderRow<*>>()
    val liveUpdate = MutableLiveData<HeaderRow<*>>()

    val columnsCount = 100
    private val rowsCount = 100
    private val textMaxLength = 18
    private val headerRow by lazy { initHeaderRow() }
    private val keys = "abcdefghijklmnopqrstuvwxyz0123456789+-% "
    private val disposables = CompositeDisposable()

    private fun initHeaderRow(): HeaderRow<*> {
        val titleColumns = mutableListOf<Column>()
        titleColumns.add(TitleHeaderColumn())
        for (i in 1 until columnsCount) {
            titleColumns.add(TitleColumn(i))
        }
        return TitleRow(titleColumns)
    }

    fun init() {
        val disposable = Flowable
            .fromCallable {
                for (i in 0 until rowsCount) {
                    val columns = mutableListOf<Column>()
                    columns.add(SimpleHeaderColumn("$i"))
                    for (j in 1 until columnsCount) {
                        columns.add(SimpleColumn(generateText()))
                    }
                    headerRow.rows.add(SimpleRow(columns))
                }

                headerRow
            }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                liveData.value = it
            }, {

            })
        disposables.add(disposable)
    }

    fun update() {
        val disposable = Flowable
            .fromCallable {
                headerRow.rows.forEach {
                    if (it !is SimpleRow) return@forEach
                    for(i in 0 until 5) {
                        (it.columns.random() as? SimpleColumn)?.value = generateText()
                    }
//                    it.columns.forEach ForEachColumn@{ column ->
//                        if (column !is SimpleColumn) return@ForEachColumn
//                        column.value = generateText()
//                    }
                    it.forceLayout = true
                }
                headerRow
            }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                liveUpdate.value = it
            }, {

            })
        disposables.add(disposable)
    }

    private fun generateText(): String {
        val count = Random.nextInt(0, textMaxLength)
        val text = StringBuilder()
        for (i in 0 until count) {
            text.append(keys.random())
        }
        return text.toString()
    }

    override fun onCleared() {
        super.onCleared()
        disposables.clear()
    }

}