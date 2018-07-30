package jp.org.alonedroid.shiftstamp.util


import com.google.api.client.util.DateTime
import java.util.*

class MyCalendar {

    constructor()

    constructor(datetime: Long?) {
        calendar.time = Date(datetime ?: Date().time)
    }

    constructor(year: Int, month: Int, date: Int, hour: Int, minute: Int, second: Int) {
        calendar.set(year, month, date, hour, minute, second)
    }

    private val calendar = Calendar.getInstance()

    val year
        get() = calendar.get(Calendar.YEAR)

    val month
        get() = calendar.get(Calendar.MONTH)

    val date
        get() = calendar.get(Calendar.DATE)

    val maxDate
        get() = calendar.getActualMaximum(Calendar.DATE)

    val datetime: DateTime
        get() = DateTime(calendar.time)

    fun getMaxDate(year: Int, month: Int): Int {
        val cal = Calendar.getInstance()
        cal.set(year, month, 1, 0, 0, 0)
        return cal.getActualMaximum(Calendar.DATE)
    }

    fun getFirstDay(): Int {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.YEAR, year)
        calendar.set(Calendar.MONTH, month)
        calendar.set(Calendar.DATE, 1)
        return calendar.get(Calendar.DAY_OF_WEEK) - 1
    }
}
