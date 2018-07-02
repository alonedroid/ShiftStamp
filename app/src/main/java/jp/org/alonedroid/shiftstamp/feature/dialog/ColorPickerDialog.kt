package jp.org.alonedroid.shiftstamp.feature.dialog

import android.content.Context
import android.content.DialogInterface
import android.graphics.Color
import android.support.v7.app.AlertDialog
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import jp.org.alonedroid.shiftstamp.util.CalendarInfoUtil


class ColorPickerDialog constructor(context: Context) : AlertDialog(context) {

    lateinit var dialog: AlertDialog

    fun showPicker(listener: DialogInterface.OnClickListener) {
        val adapter = ColorListAdapter(context, (1..11).map { i -> i.toString() }.toList())

        dialog = AlertDialog.Builder(context)
                .setAdapter(adapter, { dialogInterface, i ->
                    listener.onClick(dialogInterface, i + 1)
                    dialog.dismiss()
                })
                .setCancelable(false)
                .create()

        dialog.show()
    }

    class ColorListAdapter constructor(context: Context, colors: List<String>) : ArrayAdapter<String>(context, android.R.layout.simple_list_item_1, colors) {

        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            val view = super.getView(position, convertView, parent) as TextView
            view.setBackgroundColor(CalendarInfoUtil.convertColorIdToString(getItem(position)))
            view.setTextColor(Color.TRANSPARENT);
            return view
        }
    }
}