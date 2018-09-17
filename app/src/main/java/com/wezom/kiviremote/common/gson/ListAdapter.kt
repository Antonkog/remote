package com.wezom.kiviremote.common.gson

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import java.lang.reflect.Type


class ListAdapter : JsonSerializer<List<*>> {
    override fun serialize(src: List<*>?, typeOfSrc: Type?, context: JsonSerializationContext?): JsonElement? {
        src?.takeIf { it.isNotEmpty() }?.let {
            val jsonArray = JsonArray()
            it.forEach {
                val element = context?.serialize(it)
                jsonArray.add(element)
            }

            return jsonArray
        } ?: return null
    }
}