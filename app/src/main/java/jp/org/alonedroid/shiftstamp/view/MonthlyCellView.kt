package jp.org.alonedroid.shiftstamp.view

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.widget.RelativeLayout
import android.widget.TextView
import jp.org.alonedroid.shiftstamp.R


class MonthlyCellView @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0
) : RelativeLayout(context) {

    private var dateView: TextView
    private var eventView: TextView

    init {
        inflate(context, R.layout.view_monthly_cell, this)

        dateView = findViewById(R.id.cell_date)
        eventView = findViewById(R.id.cell_event)
    }

    fun setDate(date: String) {
        dateView.text = date
    }

    fun setEvent(title: String, color: Int) {
        eventView.text = title
        eventView.setBackgroundColor(color)
    }

    fun focus() {
        dateView.setBackgroundColor(Color.CYAN)
    }

    fun focusOut() {
        dateView.setBackgroundColor(Color.TRANSPARENT)
    }

    fun clearEvent() {
        eventView.text = ""
        eventView.setBackgroundColor(Color.TRANSPARENT)
    }

    fun clearCell() {
        dateView.text = ""

        eventView.text = ""
        eventView.setBackgroundColor(Color.TRANSPARENT)

        setOnClickListener(null)
    }
}