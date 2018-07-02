package jp.org.alonedroid.shiftstamp.feature.calendar

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.content.Context
import android.text.TextUtils
import android.util.SparseArray
import com.google.api.client.util.DateTime
import com.google.api.services.calendar.model.Event
import jp.org.alonedroid.shiftstamp.data.Shift
import jp.org.alonedroid.shiftstamp.util.CalendarInfoUtil
import jp.org.alonedroid.shiftstamp.util.SpUtil
import java.util.*


class MonthlyViewModel internal constructor() : ViewModel() {

    var year = MutableLiveData<Int>()
    var month = MutableLiveData<Int>()
    var date = MutableLiveData<Int>()

    private var maxDate = 0
    private var allEvents = SparseArray<String>()
    private var initialize = false

    private lateinit var calendarUtil: CalendarInfoUtil

    var monthlyEvents: MutableLiveData<List<Event>> = MutableLiveData()

    fun initCalendar(context: Context?) {
        setField(Calendar.getInstance())

        context?.let {
            val calId = SpUtil.CALENDAR_ID.getString(context, "")
            calendarUtil = CalendarInfoUtil(CalendarInfoUtil.getCredential(context), calId)

            if (TextUtils.isEmpty(calId)) {
                calendarUtil.chooseCalendar()
                SpUtil.CALENDAR_ID.putString(context, calendarUtil.calendarId)
            }

            initialize = true
        }
    }

    fun fetchEvents() {
        if (!initialize) return

        val targetMonth = month.value!!

        // イベント取得
        val startCal = Calendar.getInstance()
        startCal.set(year.value!!, month.value!!, 1, 0, 0, 0)

        val endCal = Calendar.getInstance()
        endCal.set(year.value!!, month.value!!, startCal.getActualMaximum(Calendar.DATE), 0, 0, 0)

        val events = calendarUtil.getEvents(DateTime(startCal.time), DateTime(endCal.time)).items

        // チェック
        if (targetMonth != month.value!!) return

        // 反映
        monthlyEvents.postValue(events)

        events.iterator().forEach { event ->
            val cal = Calendar.getInstance()
            cal.time = Date(event.start!!.dateTime!!.value)
            allEvents.put(cal.get(Calendar.DATE), event.id)
        }
    }

    fun replaceEvent(insMonth: Int, insDate: Int, shift: Shift?) {
        if (!initialize) return

        val sHour = shift?.start?.split(":")?.get(0)
        val sMin = shift?.start?.split(":")?.get(1)
        val eHour = shift?.end?.split(":")?.get(0)
        val eMin = shift?.end?.split(":")?.get(1)

        val startCal = Calendar.getInstance()
        startCal.set(year.value!!, insMonth, insDate, sHour!!.toInt(), sMin!!.toInt(), 0)

        val endCal = Calendar.getInstance()
        endCal.set(year.value!!, insMonth, insDate, eHour!!.toInt(), eMin!!.toInt(), 0)

        val event: Event
        if (allEvents[insDate] == null) {
            event = calendarUtil.insert(shift.title, shift.color, DateTime(startCal.time), DateTime(endCal.time))
        } else {
            event = calendarUtil.update(allEvents[insDate], shift.title, shift.color, DateTime(startCal.time), DateTime(endCal.time))
        }

        monthlyEvents.postValue(listOf(event))
    }

    fun goNext() {
        val nowDate = date.value!!
        if (nowDate < maxDate) {
            date.postValue(nowDate + 1)
        }
    }

    fun nextMonth() {
        month.value = month.value!! + 1
    }

    fun prevMonth() {
        month.value = month.value!! - 1
    }

    private fun setField(calendar: Calendar) {
        year.postValue(calendar.get(Calendar.YEAR))
        month.postValue(calendar.get(Calendar.MONTH))
        date.postValue(calendar.get(Calendar.DATE))

        maxDate = calendar.getActualMaximum(Calendar.DATE)
    }

    fun deleteEvent(date: Int) {
        if (!initialize) return

        allEvents[date]?.let {
            calendarUtil.delete(it)
        }
    }
}