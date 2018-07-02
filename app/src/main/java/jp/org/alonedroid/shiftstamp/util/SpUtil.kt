package jp.org.alonedroid.shiftstamp.util

import android.content.Context
import android.content.SharedPreferences


enum class SpUtil constructor(private val mSpFilePrefix: String, private val mTypeClass: Class<*>) {
    CALENDAR_ID("shiftstamp.", String::class.java),
    JSON_DATA("shiftstamp.", String::class.java),
    ACCOUNT_NAME("shiftstamp", String::class.java);

    private val key: String
        get() = name.toLowerCase()

    private fun getSp(context: Context): SharedPreferences {
        return context.getSharedPreferences(mSpFilePrefix + context.packageName, Context.MODE_PRIVATE)
    }

    fun getLong(context: Context, def: Long): Long {
        if (mTypeClass != Long::class.java) throw IllegalArgumentException()
        return getSp(context).getLong(key, def)
    }

    fun putLong(context: Context, `val`: Long) {
        if (mTypeClass != Long::class.java) throw IllegalArgumentException()
        getSp(context).edit().putLong(key, `val`).apply()
    }

    fun getBoolean(context: Context, def: Boolean): Boolean {
        if (mTypeClass != Boolean::class.java) throw IllegalArgumentException()
        return getSp(context).getBoolean(key, def)
    }

    fun putBoolean(context: Context, `val`: Boolean) {
        if (mTypeClass != Boolean::class.java) throw IllegalArgumentException()
        getSp(context).edit().putBoolean(key, `val`).apply()
    }

    fun getString2(context: Context, key: String, def: String): String {
        if (mTypeClass != String::class.java) throw IllegalArgumentException()
        return getSp(context).getString(key + key, def)
    }

    fun putString2(context: Context, key: String, `val`: String) {
        if (mTypeClass != String::class.java) throw IllegalArgumentException()
        getSp(context).edit().putString(key + key, `val`).apply()
    }

    fun getString(context: Context, def: String): String {
        if (mTypeClass != String::class.java) throw IllegalArgumentException()
        return getSp(context).getString(key, def)
    }

    fun putString(context: Context, `val`: String) {
        if (mTypeClass != String::class.java) throw IllegalArgumentException()
        getSp(context).edit().putString(key, `val`).apply()
    }

    fun getInt(context: Context, def: Int): Int {
        if (mTypeClass != Int::class.java) throw IllegalArgumentException()
        return getSp(context).getInt(key, def)
    }

    fun putInt(context: Context, `val`: Int) {
        if (mTypeClass != Int::class.java) throw IllegalArgumentException()
        getSp(context).edit().putInt(key, `val`).apply()
    }

    fun getInt(context: Context, key: String, def: Int): Int {
        if (mTypeClass != Int::class.java) throw IllegalArgumentException()
        return getSp(context).getInt(key + key, def)
    }

    fun putInt(context: Context, key: String, `val`: Int) {
        if (mTypeClass != Int::class.java) throw IllegalArgumentException()
        getSp(context).edit().putInt(key + key, `val`).apply()
    }

    fun hasValue(context: Context): Boolean {
        return getSp(context).contains(key)
    }
}
