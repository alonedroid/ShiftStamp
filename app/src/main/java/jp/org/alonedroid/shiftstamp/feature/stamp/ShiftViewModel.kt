package jp.org.alonedroid.shiftstamp.feature.stamp

import android.arch.lifecycle.ViewModel
import android.content.Context
import jp.org.alonedroid.shiftstamp.R
import jp.org.alonedroid.shiftstamp.data.Shift
import jp.org.alonedroid.shiftstamp.data.ShiftStamp
import jp.org.alonedroid.shiftstamp.util.JsonUtil

class ShiftViewModel internal constructor() : ViewModel() {

    fun changeShiftSetting(context: Context, newShift: Shift) {
        val stamp = JsonUtil<ShiftStamp>().getData(context, ShiftStamp::class.java, R.raw.default_shift)

        stamp?.shift?.let {
            var modIndex = -1

            it.forEachIndexed { index, shift ->
                if (shift.id == newShift.id) {
                    modIndex = index
                }
            }

            if (modIndex >= 0) {
                (it as ArrayList).set(modIndex, newShift)
            }

            JsonUtil<ShiftStamp>().saveData(context, ShiftStamp::class.java, R.raw.default_shift, stamp)
        }
    }
}