package jp.org.alonedroid.shiftstamp.util

import android.content.Context
import android.text.TextUtils
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okio.Okio
import java.io.IOException
import java.io.InputStream


class JsonUtil<E> {

    fun getData(context: Context, cls: Class<E>, resId: Int): E? {
        val savedJson = SpUtil.JSON_DATA.getString2(context, resId.toString(), "")
        if (TextUtils.isEmpty(savedJson)) {
            return getDefaultData(context, cls, resId)
        } else {
            return convertToObject(cls, savedJson)
        }
    }

    fun saveData(context: Context, cls: Class<E>, resId: Int, obj: E?) {
        SpUtil.JSON_DATA.putString2(context, resId.toString(), convertToJson(cls, obj))
    }

    private fun getDefaultData(context: Context, cls: Class<E>, resId: Int): E? {
        var reader: InputStream? = null

        try {
            reader = context.resources.openRawResource(resId)
            val source = Okio.buffer(Okio.source(reader!!))
            val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
            return moshi.adapter(cls).fromJson(source)
        } catch (e: IOException) {
            return null
        } finally {
            reader?.close()
        }
    }

    private fun convertToObject(cls: Class<E>, json: String): E? {
        val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
        return moshi.adapter(cls).fromJson(json)
    }

    private fun convertToJson(cls: Class<E>, obj: E?): String {
        val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
        return moshi.adapter(cls).toJson(obj)
    }
}