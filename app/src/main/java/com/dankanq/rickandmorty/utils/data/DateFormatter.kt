package com.dankanq.rickandmorty.utils.data

import com.dankanq.rickandmorty.utils.data.Constants.EMPTY_STRING
import java.text.SimpleDateFormat
import java.util.*

fun formatDateString(dateString: String): String {
    val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.ENGLISH)
    val outputFormat = SimpleDateFormat("d MMMM yyyy", Locale.ENGLISH)
    val date = inputFormat.parse(dateString)
    return if (date != null) {
        outputFormat.format(date)
    } else {
        EMPTY_STRING
    }
}