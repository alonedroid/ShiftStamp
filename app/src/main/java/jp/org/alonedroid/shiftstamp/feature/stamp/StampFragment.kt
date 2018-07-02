package jp.org.alonedroid.shiftstamp.feature.stamp

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.StateListDrawable
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import jp.org.alonedroid.shiftstamp.MainConst
import jp.org.alonedroid.shiftstamp.MainViewModel
import jp.org.alonedroid.shiftstamp.R
import jp.org.alonedroid.shiftstamp.data.Shift
import jp.org.alonedroid.shiftstamp.data.ShiftStamp
import jp.org.alonedroid.shiftstamp.feature.dialog.FreeInputDialog
import jp.org.alonedroid.shiftstamp.util.CalendarInfoUtil
import jp.org.alonedroid.shiftstamp.util.JsonUtil


class StampFragment : android.support.v4.app.Fragment() {

    private lateinit var shiftviewmodel: ShiftViewModel
    private lateinit var mainviewmodel: MainViewModel
    private val stampViews = HashMap<String, TextView>()

    override fun onAttach(context: Context?) {
        super.onAttach(context)

        mainviewmodel = ViewModelProviders.of(activity!!).get(MainViewModel::class.java)
        shiftviewmodel = ViewModelProviders.of(this).get(ShiftViewModel::class.java)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_stamp, container, false)

        val frame = listOf(view.findViewById<LinearLayout>(R.id.stamp_frame1)
                , view.findViewById(R.id.stamp_frame2)
                , view.findViewById(R.id.stamp_frame3))

        context?.also {
            val stamp = JsonUtil<ShiftStamp>().getData(it, ShiftStamp::class.java, R.raw.default_shift)
            stamp?.shift?.forEachIndexed { index, shift ->
                addStampView(frame[index / 6], shift)
            }
        }

        subscribeUi()

        return view
    }

    private fun addStampView(view: LinearLayout, shift: Shift) {
        val text = TextView(context)
        text.apply {
            gravity = Gravity.CENTER
            background = getStateListDrawable(shift.color)
            setTextColor(ContextCompat.getColor(context!!, R.color.white))
            setText(shift.title)
            setOnClickListener { mainviewmodel.shiftBus.postValue(shift) }
        }

        val lp = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT)
        lp.weight = 1.0f
        lp.setMargins(1, 1, 1, 1)

        view.addView(text, lp)
        stampViews.put(shift.id, text)
    }

    private fun getStateListDrawable(colorId: String): Drawable {
        val stateList = StateListDrawable()

        val pressedDrawable = GradientDrawable()
//        pressedDrawable.setStroke(1, CalendarInfoUtil.convertThickColorIdToString(colorId))
        pressedDrawable.setCornerRadius(2.0f)
        pressedDrawable.setColor(CalendarInfoUtil.convertThickColorIdToString(colorId))
        stateList.addState(intArrayOf(android.R.attr.state_pressed), pressedDrawable)

        val drawable = GradientDrawable()
//        drawable.setStroke(1, CalendarInfoUtil.convertThickColorIdToString(colorId))
        drawable.setCornerRadius(2.0f)
        drawable.setColor(CalendarInfoUtil.convertColorIdToString(colorId))
        stateList.addState(intArrayOf(), drawable)

        return stateList
    }

    private fun subscribeUi() {
        mainviewmodel.terminalModStampListener = { modShift ->
            val shift = modShift.copy(id = modShift.id.replace("mod", ""))

            shiftviewmodel.changeShiftSetting(context!!, shift)

            stampViews[shift.id]?.apply {
                background = getStateListDrawable(shift.color)
                setText(shift.title)
                setOnClickListener { mainviewmodel.shiftBus.postValue(shift) }
            }
        }

        mainviewmodel.terminalSelectStampListener = { shift ->
            FreeInputDialog(context!!).showSetting(activity!!, shift)
        }

        mainviewmodel.mode.observe(this, Observer { mode ->
            when (mode){
                MainConst.MODE_CALENDAR -> {
                    stampViews["delete"]?.visibility = View.VISIBLE
                    stampViews["free"]?.visibility = View.VISIBLE
                }

                MainConst.MODE_SETTING -> {
                    stampViews["delete"]?.visibility = View.INVISIBLE
                    stampViews["free"]?.visibility = View.INVISIBLE
                }
            }
        })
    }

    companion object {

        fun newInstance(): StampFragment {
            return StampFragment()
        }
    }
}
