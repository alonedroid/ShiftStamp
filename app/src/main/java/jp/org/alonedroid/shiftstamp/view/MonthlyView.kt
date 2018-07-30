package jp.org.alonedroid.shiftstamp.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import android.widget.LinearLayout
import jp.org.alonedroid.shiftstamp.R
import jp.org.alonedroid.shiftstamp.util.ActionListener1
import jp.org.alonedroid.shiftstamp.util.MyCalendar


class MonthlyView @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    var focusedIndex = -1
    var firstDayIndex = 0
    val views: Array<MonthlyCellView?> = arrayOfNulls(43)
    var listener: ActionListener1? = null

    init {
        inflate(context, R.layout.view_monthly, this)
        createView()
    }

    private fun createView() {
        val inflater = LayoutInflater.from(context)

        val cal = MyCalendar()
        firstDayIndex = cal.getFirstDay()
        val endDate = cal.maxDate
        val viewRoot = findViewById<LinearLayout>(R.id.view_root)

        for (idxRow in 0..5) {
            val root: LinearLayout = inflater.inflate(R.layout.view_monthly_row, viewRoot) as LinearLayout
            val row = root.getChildAt(root.childCount - 1) as LinearLayout
            for (idxCol in 0..6) {
                val cellIndex = (idxRow * 7) + idxCol + 1
                views[cellIndex] = row.getChildAt(idxCol) as MonthlyCellView

                if (cellIndex < firstDayIndex) continue

                val date = cellIndex - firstDayIndex
                if (date < 1 || date > endDate) continue

                views[cellIndex]?.setDate((date).toString())
                views[cellIndex]?.setOnClickListener({
                    listener?.call(date)
                    setFocus(date)
                })
            }
        }
    }

    fun setFocus(date: Int) {
        val cellIndex = date + firstDayIndex
        views[cellIndex]?.let {
            if (focusedIndex != -1) {
                views[focusedIndex]?.focusOut()
            }
            it.focus()
            focusedIndex = cellIndex
        }
    }

    fun clearEvent(date: Int) {
        views[date + firstDayIndex]?.clearEvent()
    }

    fun setEvent(date: Int, event: String, color: Int) {
        views[date + firstDayIndex]?.setEvent(event, color)
    }

    fun changeMonth(year: Int, month: Int) {
        val cal = MyCalendar(year, month, 1, 0, 0, 0)
        firstDayIndex = cal.getFirstDay()
        val endDate = cal.maxDate

        for (idxRow in 0..5) {
            for (idxCol in 0..6) {
                val cellIndex = (idxRow * 7) + idxCol + 1
                views[cellIndex]?.clearCell()

                if (cellIndex < firstDayIndex) continue

                val date = cellIndex - firstDayIndex
                if (date < 1 || date > endDate) continue

                views[cellIndex]?.setDate((date).toString())
                views[cellIndex]?.setOnClickListener({
                    listener?.call(date)
                    setFocus(date)
                })
            }
        }
    }
}