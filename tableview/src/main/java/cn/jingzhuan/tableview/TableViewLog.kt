package cn.jingzhuan.tableview

object TableViewLog {

    interface TableViewLogDelegate {
        fun e(tag: String?, msg: String?, vararg obj: Any?)
        fun w(tag: String?, msg: String?, vararg obj: Any?)
        fun i(tag: String?, msg: String?, vararg obj: Any?)
        fun d(tag: String?, msg: String?, vararg obj: Any?)
        fun printErrStackTrace(
            tag: String?,
            tr: Throwable?,
            format: String?,
            vararg obj: Any?
        )
    }

    private var delegate: TableViewLogDelegate? = null

    fun setDelegate(delegate: TableViewLogDelegate) {
        this.delegate = delegate
    }

    fun e(tag: String?, msg: String?, vararg obj: Any?) {
        delegate?.e(tag, msg, obj)
    }

    fun w(tag: String?, msg: String?, vararg obj: Any?) {
        delegate?.w(tag, msg, obj)
    }

    fun i(tag: String?, msg: String?, vararg obj: Any?) {
        delegate?.i(tag, msg, obj)
    }

    fun d(tag: String?, msg: String?, vararg obj: Any?) {
        delegate?.d(tag, msg, obj)
    }

    fun printErrStackTrace(
        tag: String?,
        tr: Throwable?,
        format: String?,
        vararg obj: Any?
    ) {
        delegate?.printErrStackTrace(tag, tr, format, obj)
    }

}