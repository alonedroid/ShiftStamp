package jp.org.alonedroid.shiftstamp.data


data class ShiftStamp(
        val shift: List<Shift>
)

data class Shift(
        val id: String,
        val title: String,
        val start: String,
        val end: String,
        val color: String
)