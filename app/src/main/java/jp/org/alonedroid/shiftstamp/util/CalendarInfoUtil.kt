package jp.org.alonedroid.shiftstamp.util

import android.content.Context
import android.graphics.Color
import android.text.TextUtils
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.client.util.DateTime
import com.google.api.client.util.ExponentialBackOff
import com.google.api.services.calendar.CalendarScopes
import com.google.api.services.calendar.model.*
import com.google.api.services.calendar.model.Calendar
import java.io.IOException


class CalendarInfoUtil(_credential: GoogleAccountCredential, _calendarId: String = "") {

    var calendarId = ""
    private var client: com.google.api.services.calendar.Calendar

    init {
        calendarId = _calendarId

        val transport = AndroidHttp.newCompatibleTransport()
        val jsonFactory = JacksonFactory.getDefaultInstance()
        client = com.google.api.services.calendar.Calendar.Builder(transport, jsonFactory, _credential)
                .setApplicationName("Google Calendar API Android Quickstart")
                .build()
    }

    companion object {

        private val CALENDAR_NAME = "シフたん"
        private val SCOPES = mutableListOf(CalendarScopes.CALENDAR)

        fun getCredential(context: Context): GoogleAccountCredential {
            val credential = GoogleAccountCredential.usingOAuth2(context, SCOPES)
                    .setBackOff(ExponentialBackOff())
            credential.selectedAccountName = SpUtil.ACCOUNT_NAME.getString(context, "")

            return credential
        }

        fun convertColorIdToString(colorId: String): Int {
            val colorStrs = mapOf(
                    "1" to "#A4BDFC",
                    "2" to "#7AE7BF",
                    "3" to "#DBADFF",
                    "4" to "#FF887C",
                    "5" to "#FBD75B",
                    "6" to "#FFB878",
                    "7" to "#46D6DB",
                    "8" to "#aeaeae",
                    "9" to "#5484ED",
                    "10" to "#51B749",
                    "11" to "#DC2127")

            return Color.parseColor(colorStrs[colorId] ?: "#A4BDFC")
        }

        fun convertThinColorIdToString(colorId: String): Int {
            val colorStrs = mapOf(
                    "1" to "#e6edfe",
                    "2" to "#edfcf6",
                    "3" to "#f1e0ff",
                    "4" to "#ffe5e2",
                    "5" to "#fef7e0",
                    "6" to "#ffeede",
                    "7" to "#e2f9f9",
                    "8" to "#E1E1E1",
                    "9" to "#eef3fd",
                    "10" to "#e8f6e7",
                    "11" to "#fceeef")

            return Color.parseColor(colorStrs[colorId] ?: "#e6edfe")
        }

        fun convertThickColorIdToString(colorId: String): Int {
            val colorStrs = mapOf(
                    "1" to "#073fcd",
                    "2" to "#21b881",
                    "3" to "#7400cf",
                    "4" to "#d11300",
                    "5" to "#c99d05",
                    "6" to "#cd6100",
                    "7" to "#21a6ab",
                    "8" to "#6a6a6a",
                    "9" to "#154dc6",
                    "10" to "#41933a",
                    "11" to "#be1d22")

            return Color.parseColor(colorStrs[colorId] ?: "#021239")
        }
    }

    fun chooseCalendar() {
        val feed = client.calendarList().list().execute()
        val items = feed["items"] as ArrayList<CalendarListEntry>

        for (entry: CalendarListEntry in items) {
            if (CALENDAR_NAME.equals(entry.summary)) {
                calendarId = entry.id
            }
        }

        if (TextUtils.isEmpty(calendarId)) {
            calendarId = createCalendar()
        }
    }

    /**
     * 選択されたGoogleアカウントに対して、新規にカレンダーを追加する。
     *
     * @return 作成したカレンダーのID
     * @throws IOException
     */
    @Throws(IOException::class)
    private fun createCalendar(): String {
        // 新規にカレンダーを作成する
        val calendar = Calendar()
        // カレンダーにタイトルを設定する
        calendar.summary = CALENDAR_NAME
        // カレンダーにタイムゾーンを設定する
        calendar.timeZone = "Asia/Tokyo"

        // 作成したカレンダーをGoogleカレンダーに追加する
        val createdCalendar = client.calendars().insert(calendar).execute()
        val calendarId = createdCalendar.id

        // カレンダー一覧から新規に作成したカレンダーのエントリを取得する
        val calendarListEntry = client.calendarList().get(calendarId).execute()

        // カレンダーのデフォルトの背景色を設定する
        calendarListEntry.backgroundColor = "#66b7ec"

        // カレンダーのデフォルトの背景色をGoogleカレンダーに反映させる
        client.calendarList()
                .update(calendarListEntry.id, calendarListEntry)
                .setColorRgbFormat(true)
                .execute()

        // 新規に作成したカレンダーのIDを返却する
        return calendarId
    }

    fun getEvents(start: DateTime, end: DateTime): Events {
        val calendar = client.events().list(calendarId)

        calendar.apply {
            timeMin = start
            timeMax = end
            timeZone = "Asia/Tokyo"
            orderBy = "startTime"
            singleEvents = true
        }

        return calendar.execute()
    }

    /**
     * カレンダーイベント新規作成
     */
    fun insert(title: String, color: String, start: DateTime, end: DateTime): Event {
        val event = Event()

        event.summary = title
        event.colorId = color

        event.start = EventDateTime().setDateTime(start)
        event.end = EventDateTime().setDateTime(end)

        return client.events().insert(calendarId, event).execute()
    }

    /**
     * カレンダーイベント更新
     */
    fun update(eventId: String, title: String, color: String, start: DateTime, end: DateTime): Event {
        val event = Event()

        event.summary = title
        event.colorId = color

        event.start = EventDateTime().setDateTime(start)
        event.end = EventDateTime().setDateTime(end)

        return client.events().update(calendarId, eventId, event).execute()
    }

    /**
     * カレンダーイベント削除
     */
    fun delete(eventId: String) {
        try {
            client.events().delete(calendarId, eventId).execute()
        } catch (e: IOException) {
        }
    }
}