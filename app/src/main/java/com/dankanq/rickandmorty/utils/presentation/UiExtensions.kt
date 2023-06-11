package com.dankanq.rickandmorty.utils.presentation

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageButton
import androidx.core.view.isVisible
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.scan

fun EditText.onSearch(callback: () -> Unit) {
    setOnEditorActionListener { _, actionId, _ ->
        if (actionId == EditorInfo.IME_ACTION_SEARCH) {
            callback.invoke()
            true
        }
        false
    }

    setOnFocusChangeListener { _, hasFocus ->
        val inputMethodManager =
            context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        if (!hasFocus) {
            inputMethodManager.hideSoftInputFromWindow(windowToken, 0)
        } else {
            inputMethodManager.showSoftInput(this, InputMethodManager.SHOW_IMPLICIT)
        }
    }
}

fun EditText.addTextChangedListener(
    searchButton: ImageButton,
    clearTextButton: ImageButton
) {
    addTextChangedListener(object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            if (text.isNotBlank()) {
                clearTextButton.isVisible = true
                searchButton.isVisible = false
            } else {
                clearTextButton.isVisible = false
                searchButton.isVisible = true
            }
        }

        override fun afterTextChanged(s: Editable?) {}
    })
}

@ExperimentalCoroutinesApi
fun <T> Flow<T>.simpleScan(count: Int): Flow<List<T?>> {
    val items = List<T?>(count) { null }
    return this.scan(items) { previous, value ->
        previous.drop(1) + value
    }
}