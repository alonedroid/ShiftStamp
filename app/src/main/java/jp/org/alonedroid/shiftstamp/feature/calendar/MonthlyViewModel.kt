package jp.org.alonedroid.shiftstamp.feature.calendar

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.content.Context
import android.text.TextUtils
import android.util.SparseArray
import com.google.api.services.calendar.model.Event
import jp.org.alonedroid.shiftstamp.data.Shift
import jp.org.alonedroid.shiftstamp.util.CalendarInfoUtil
import jp.org.alonedroid.shiftstamp.util.MyCalendar
import jp.org.alonedroid.shiftstamp.util.SpUtil


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
        observeField()
        setField(MyCalendar())

        context?.let {
            val calId = SpUtil.CALENDAR_ID.getString(context, "")
            calendarUtil = CalendarInfoUtil(CalendarInfoUtil.getCredential(context), calId)

            if (TextUtils.isEmpty(calId)) {
                calendarUtil.chooseCalendar()
                SpUtil.CALENDAR_ID.putString(context, calendarUtil.calendarId)
                month.postValue(month.value)
            }

            initialize = true
        }
    }

    fun fetchEvents() {
        if (!initialize) return

        val targetMonth = month.value!!

        // イベント取得
        val startCal = MyCalendar(year.value!!, month.value!!, 1, 0, 0, 0)
        val endCal = MyCalendar(year.value!!, month.value!!, startCal.maxDate, 23, 59, 59)

        val events = calendarUtil.getEvents(startCal.datetime, endCal.datetime).items

        // チェック
        if (targetMonth != month.value!!) return

        // 反映
        monthlyEvents.postValue(events)

        allEvents = SparseArray()
        events.iterator().forEach { event ->
            allEvents.put(MyCalendar(event.start!!.dateTime!!.value).date, event.id)
        }
    }

    fun replaceEvent(insMonth: Int, insDate: Int, shift: Shift?) {
        if (!initialize) return

        val sHour = shift?.start?.split(":")?.get(0)
        val sMin = shift?.start?.split(":")?.get(1)
        val eHour = shift?.end?.split(":")?.get(0)
        val eMin = shift?.end?.split(":")?.get(1)

        val staCal = MyCalendar(year.value!!, insMonth, insDate, sHour!!.toInt(), sMin!!.toInt(), 0)
        val endCal = MyCalendar(year.value!!, insMonth, insDate, eHour!!.toInt(), eMin!!.toInt(), 0)

        val event: Event
        if (allEvents[insDate] == null) {
            event = calendarUtil.insert(shift.title, shift.color, staCal.datetime, endCal.datetime)
        } else {
            event = calendarUtil.update(allEvents[insDate], shift.title, shift.color, staCal.datetime, endCal.datetime)
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
        date.value = 1
    }

    fun prevMonth() {
        month.value = month.value!! - 1
        date.value = 1
    }

    private fun observeField() {
        month.observeForever({
            maxDate = MyCalendar().getMaxDate(year.value!!, it!!)
        })
    }

    private fun setField(cal: MyCalendar) {
        year.postValue(cal.year)
        month.postValue(cal.month)
        date.postValue(cal.date)

        maxDate = cal.maxDate
    }

    fun deleteEvent(date: Int) {
        if (!initialize) return

        allEvents[date]?.let {
            calendarUtil.delete(it)
        }
    }
}