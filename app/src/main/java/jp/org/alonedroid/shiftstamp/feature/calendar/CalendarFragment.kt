package jp.org.alonedroid.shiftstamp.feature.calendar

import android.annotation.SuppressLint
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException
import jp.org.alonedroid.shiftstamp.MainConst
import jp.org.alonedroid.shiftstamp.MainViewModel
import jp.org.alonedroid.shiftstamp.R
import jp.org.alonedroid.shiftstamp.feature.dialog.FreeInputDialog
import jp.org.alonedroid.shiftstamp.util.ActionListener1
import jp.org.alonedroid.shiftstamp.util.CalendarInfoUtil
import jp.org.alonedroid.shiftstamp.view.MonthlyView
import java.util.*


class CalendarFragment : Fragment() {

    private lateinit var mainviewmodel: MainViewModel
    private lateinit var viewmodel: MonthlyViewModel

    override fun onAttach(context: Context?) {
        super.onAttach(context)

        mainviewmodel = ViewModelProviders.of(activity!!).get(MainViewModel::class.java)
        viewmodel = ViewModelProviders.of(this).get(MonthlyViewModel::class.java)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_calendar, container, false)

        val monthlyView = MonthlyView(requireContext())
        view.findViewById<FrameLayout>(R.id.calendar_body).addView(monthlyView)
        subscribeUi(view, monthlyView)
        return view
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun subscribeUi(view: View, monthlyView: MonthlyView) {
        monthlyView.listener = object : ActionListener1 {
            override fun call(arg1: Int) {
                viewmodel.date.value = arg1
            }
        }

        view.findViewById<FrameLayout>(R.id.calendar_frost).setOnTouchListener(View.OnTouchListener { _, _ -> return@OnTouchListener true })

        view.findViewById<TextView>(R.id.next).setOnClickListener({ viewmodel.nextMonth() })
        view.findViewById<TextView>(R.id.prev).setOnClickListener({ viewmodel.prevMonth() })

        viewmodel.month.observe(this, Observer { month ->
            view.findViewById<TextView>(R.id.headerText).text = "${viewmodel.year.value!!}/${month!! + 1}"
            monthlyView.changeMonth(viewmodel.year.value!!, month)
            Thread { viewmodel.fetchEvents() }.start()
        })

        viewmodel.monthlyEvents.observe(this, Observer { events ->
            events?.iterator()?.forEach { event ->
                val cal = Calendar.getInstance()
                cal.time = Date(event.start?.dateTime?.value ?: Date().time)

                monthlyView.setEvent(
                        cal.get(Calendar.DATE),
                        event.summary,
                        CalendarInfoUtil.convertColorIdToString(event.colorId ?: "1"))
            }
        })

        mainviewmodel.terminalDeleteListener = {
            val date = viewmodel.date.value!!

            monthlyView.clearEvent(date)
            Thread { viewmodel.deleteEvent(date) }.start()
            viewmodel.goNext()
        }

        mainviewmodel.terminalSettingListener = { changeMode(view) }

        mainviewmodel.terminalFreeInputListener = { FreeInputDialog(context!!).showInput(activity!!) }

        mainviewmodel.terminalInsertListener = { shift ->
            val month = viewmodel.month.value!!
            val date = viewmodel.date.value!!

            monthlyView.setEvent(date, shift.title, CalendarInfoUtil.convertThinColorIdToString(shift.color))
            Thread { viewmodel.replaceEvent(month, date, shift) }.start()
            viewmodel.goNext()
        }

        viewmodel.date.observe(this, Observer { date ->
            monthlyView.setFocus(date!!)
        })
    }

    private fun changeMode(view: View) {
        mainviewmodel.changeSettingMode()

        when (mainviewmodel.mode.value) {
            MainConst.MODE_SETTING -> {
                view.findViewById<FrameLayout>(R.id.calendar_frost).visibility = View.VISIBLE
            }

            else -> {
                view.findViewById<FrameLayout>(R.id.calendar_frost).visibility = View.GONE
            }
        }
    }

    override fun onStart() {
        super.onStart()

        Thread {
            try {
                viewmodel.initCalendar(context)
            } catch (e: UserRecoverableAuthIOException) {
                e.intent?.let { startActivity(it) }
            }
        }.start()
    }

    companion object {

        fun newInstance(): CalendarFragment {
            return CalendarFragment()
        }
    }
}