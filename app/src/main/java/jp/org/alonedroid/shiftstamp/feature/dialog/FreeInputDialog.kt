package jp.org.alonedroid.shiftstamp.feature.dialog

import android.annotation.SuppressLint
import android.app.TimePickerDialog
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.content.DialogInterface
import android.support.v4.app.FragmentActivity
import android.support.v7.app.AlertDialog
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import jp.org.alonedroid.shiftstamp.MainViewModel
import jp.org.alonedroid.shiftstamp.R
import jp.org.alonedroid.shiftstamp.data.Shift
import jp.org.alonedroid.shiftstamp.util.CalendarInfoUtil


class FreeInputDialog constructor(context: Context) : AlertDialog(context) {

    private lateinit var viewmodel: MainViewModel
    private lateinit var dialog: AlertDialog

    private lateinit var titleView: TextView
    private lateinit var startView: TextView
    private lateinit var endView: TextView

    private lateinit var selectedColor: String


    private fun show(activity: FragmentActivity, view: View) {
        viewmodel = ViewModelProviders.of(activity).get(MainViewModel::class.java)
        dialog = Builder(context)
                .setView(view)
                .setCancelable(false)
                .create()

        dialog.show()
    }

    fun showInput(activity: FragmentActivity) {
        show(activity, inflateInputView())
    }

    fun showSetting(activity: FragmentActivity, shift: Shift) {
        show(activity, inflateSettingView(shift))
    }

    @SuppressLint("InflateParams")
    private fun inflateView(id: String, defShift: Shift? = null): View {
        val view = layoutInflater.inflate(R.layout.dialog_free_input, null, false)

        // findViewById
        titleView = view.findViewById(R.id.free_input_title)
        startView = view.findViewById(R.id.free_input_start)
        endView = view.findViewById(R.id.free_input_end)
        val colorView = view.findViewById<FrameLayout>(R.id.free_input_color)

        // 初期値セット
        titleView.text = defShift?.title
        startView.text = defShift?.start ?: "00:00"
        endView.text = defShift?.end ?: "00:00"
        selectedColor = defShift?.color ?: "1"
        colorView.setBackgroundColor(CalendarInfoUtil.convertColorIdToString(defShift?.color ?: "1"))

        // アクションセット
        startView.setOnClickListener({
            TimePickerDialog(context, TimePickerDialog.OnTimeSetListener { _, hour, min ->
                startView.text = String.format("%02d:%02d", hour, min)
                endView.text = String.format("%02d:%02d", hour + 9, min)
            }, 0, 0, true).show()
        })

        view.findViewById<TextView>(R.id.free_input_end).setOnClickListener({
            TimePickerDialog(context, TimePickerDialog.OnTimeSetListener { _, hour, min ->
                endView.text = String.format("%02d:%02d", hour, min)
            }, 0, 0, true).show()
        })

        view.findViewById<TextView>(R.id.free_input_cancel).setOnClickListener({
            dialog.dismiss()
        })

        colorView.setOnClickListener {
            ColorPickerDialog(context).showPicker(DialogInterface.OnClickListener { _, i ->
                selectedColor = i.toString()
                colorView.setBackgroundColor(CalendarInfoUtil.convertColorIdToString(selectedColor))
            })
        }

        view.findViewById<TextView>(R.id.free_input_save).setOnClickListener({
            val shift = Shift(
                    id + defShift?.id,
                    titleView.text.toString(),
                    startView.text.toString(),
                    endView.text.toString(),
                    selectedColor)

            viewmodel.shiftBus.postValue(shift)
            dialog.dismiss()
        })

        return view
    }

    private fun inflateInputView(): View {
        return inflateView("stamp0")
    }

    private fun inflateSettingView(defShift: Shift): View {
        return inflateView("mod", defShift)
    }
}