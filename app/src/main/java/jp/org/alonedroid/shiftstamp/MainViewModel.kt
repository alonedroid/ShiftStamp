package jp.org.alonedroid.shiftstamp

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import jp.org.alonedroid.shiftstamp.data.Shift


class MainViewModel internal constructor() : ViewModel() {

    val loading: MutableLiveData<Boolean> = MutableLiveData()
    var mode: MutableLiveData<String> = MutableLiveData()

    var terminalDeleteListener: () -> Unit = {}
    var terminalSettingListener: () -> Unit = {}
    var terminalFreeInputListener: () -> Unit = {}
    var terminalModStampListener: (shift: Shift) -> Unit = {}
    var terminalInsertListener: (shift: Shift) -> Unit = {}
    var terminalSelectStampListener: (shift: Shift) -> Unit = {}

    val shiftBus: MutableLiveData<Shift> = MutableLiveData()

    init {
        shiftBus.observeForever({ shift -> shiftTerminal(shift) })
    }

    fun shiftTerminal(shift: Shift?) {
        if (shift == null) return

        when {
            "delete".equals(shift.id) -> {
                terminalDeleteListener()
            }

            "setting".equals(shift.id) -> {
                terminalSettingListener()
            }

            "free".equals(shift.id) -> {
                terminalFreeInputListener()
            }

            shift.id.startsWith("mod") -> {
                terminalModStampListener(shift)
            }

            MainConst.MODE_SETTING.equals(mode.value) -> {
                terminalSelectStampListener(shift)
            }

            else -> {
                terminalInsertListener(shift)
            }
        }
    }

    fun changeSettingMode() {
        when (mode.value) {
            MainConst.MODE_SETTING -> {
                mode.value = MainConst.MODE_CALENDAR
            }

            else -> {
                mode.value = MainConst.MODE_SETTING
            }
        }
    }
}