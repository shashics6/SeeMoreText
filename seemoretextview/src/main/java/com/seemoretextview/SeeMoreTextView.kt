package com.seemoretextview

import android.content.Context
import android.graphics.Color
import android.os.Parcelable
import android.text.Spannable
import android.text.SpannableString
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import androidx.appcompat.widget.AppCompatTextView

class SeeMoreTextView : AppCompatTextView {
    private var showingLine = 1
    private var showingChar = 0
    private var isCharEnable = false
    private var showMore = "Show more"
    private var showLess = "Show less"
    private val dot = "..."
    private val MAGIC_NUMBER = 5
    private var showMoreTextColor = Color.RED
    private var showLessTextColor = Color.RED
    private var mainText: String? = null
    private var isAlreadySet = false

    constructor(context: Context?) : super(context!!) {}
    constructor(context: Context?, attrs: AttributeSet?) : super(
        context!!, attrs
    ) {
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        mainText = text.toString()
    }

    override fun onSaveInstanceState(): Parcelable? {
        return super.onSaveInstanceState()
    }

    override fun onRestoreInstanceState(state: Parcelable) {
        super.onRestoreInstanceState(state)
    }

    private fun addShowMore() {
        val vto = viewTreeObserver
        vto.addOnGlobalLayoutListener(object : OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                val text = text.toString()
                if (!isAlreadySet) {
                    mainText = getText().toString()
                    isAlreadySet = true
                }
                var showingText = ""
                if (isCharEnable) {
                    if (showingChar >= text.length) {
                        try {
                            throw Exception("Character count cannot be exceed total line count")
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                    var newText = text.substring(0, showingChar)
                    newText += dot + showMore
                    isCollapse = true
                    setText(newText)
                    Log.d(TAG, "Text: $newText")
                } else {
                    if (showingLine >= lineCount) {
                        try {
                            throw Exception("Line Number cannot be exceed total line count")
                        } catch (e: Exception) {
                            e.printStackTrace()
                            Log.e(TAG, "Error: " + e.message)
                        }
                        viewTreeObserver.removeOnGlobalLayoutListener(this)
                        return
                    }
                    var start = 0
                    var end: Int
                    for (i in 0 until showingLine) {
                        end = layout.getLineEnd(i)
                        showingText += text.substring(start, end)
                        start = end
                    }
                    var newText = showingText.substring(
                        0,
                        showingText.length - (dot.length + showMore.length + MAGIC_NUMBER)
                    )
                    Log.d(TAG, "Text: $newText")
                    Log.d(TAG, "Text: $showingText")
                    newText += dot + showMore
                    isCollapse = true
                    setText(newText)
                }
                setShowMoreColoringAndClickable()
                viewTreeObserver.removeOnGlobalLayoutListener(this)
            }
        })
    }

    private fun setShowMoreColoringAndClickable() {
        val spannableString = SpannableString(text)
        Log.d(TAG, "Text: $text")
        spannableString.setSpan(
            object : ClickableSpan() {
                override fun updateDrawState(ds: TextPaint) {
                    ds.isUnderlineText = false
                }

                override fun onClick(view: View) {
                    maxLines = Int.MAX_VALUE
                    text = mainText
                    isCollapse = false
                    showLessButton()
                    Log.d(TAG, "Item clicked: $mainText")
                }
            },
            text.length - (dot.length + showMore.length),
            text.length, 0
        )
        spannableString.setSpan(
            ForegroundColorSpan(showMoreTextColor),
            text.length - (dot.length + showMore.length),
            text.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        movementMethod = LinkMovementMethod.getInstance()
        setText(spannableString, BufferType.SPANNABLE)
    }

    private fun showLessButton() {
        val text = text.toString() + dot + showLess
        val spannableString = SpannableString(text)
        spannableString.setSpan(
            object : ClickableSpan() {
                override fun updateDrawState(ds: TextPaint) {
                    ds.isUnderlineText = false
                }

                override fun onClick(view: View) {
                    maxLines = showingLine
                    addShowMore()
                    Log.d(TAG, "Item clicked: ")
                }
            },
            text.length - (dot.length + showLess.length),
            text.length, 0
        )
        spannableString.setSpan(
            ForegroundColorSpan(showLessTextColor),
            text.length - (dot.length + showLess.length),
            text.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        movementMethod = LinkMovementMethod.getInstance()
        setText(spannableString, BufferType.SPANNABLE)
    }

    /**
     * User can add minimum line number to show collapse text
     *
     * @param lineNumber int
     */
    fun setShowingLine(lineNumber: Int) {
        if (lineNumber == 0) {
            try {
                throw Exception("Line Number cannot be 0")
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return
        }
        isCharEnable = false
        showingLine = lineNumber
        maxLines = showingLine
        if (isCollapse) {
            addShowMore()
        } else {
            maxLines = Int.MAX_VALUE
            showLessButton()
        }
    }

    /**
     * User can limit character limit of text
     *
     * @param character int
     */
    fun setShowingChar(character: Int) {
        if (character == 0) {
            try {
                throw Exception("Character length cannot be 0")
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return
        }
        isCharEnable = true
        showingChar = character
        if (isCollapse) {
            addShowMore()
        } else {
            maxLines = Int.MAX_VALUE
            showLessButton()
        }
    }

    /**
     * User can add their own  show more text
     *
     * @param text String
     */
    fun addShowMoreText(text: String) {
        showMore = text
    }

    /**
     * User can add their own show less text
     *
     * @param text String
     */
    fun addShowLessText(text: String) {
        showLess = text
    }

    /**
     * User Can add show more text color
     *
     * @param color Integer
     */
    fun setShowMoreColor(color: Int) {
        showMoreTextColor = color
    }

    /**
     * User can add show less text color
     *
     * @param color Integer
     */
    fun setShowLessTextColor(color: Int) {
        showLessTextColor = color
    }

    companion object {
        private val TAG = SeeMoreTextView::class.java.name
        var isCollapse = true
    }
}