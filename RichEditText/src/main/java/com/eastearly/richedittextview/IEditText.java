package com.eastearly.richedittextview;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.TextView;

/**
 * Created by dachen on 10/10/15.
 */
public interface IEditText {
    Editable getText();

    void setText(CharSequence text, TextView.BufferType type);

    void setSelection(int start, int stop);

    void setSelection(int index);

    void selectAll();

    void extendSelection(int index);

    void setEllipsize(TextUtils.TruncateAt ellipsis);

    void onInitializeAccessibilityEvent(AccessibilityEvent event);

    void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo info);

    boolean performAccessibilityAction(int action, Bundle arguments);

    /**
     * Adds a TextWatcher to the list of those whose methods are called
     * whenever this TextView's text changes.
     * <p>
     * In 1.0, the {@link TextWatcher#afterTextChanged} method was erroneously
     * not called after {@link #setText} calls.  Now, doing {@link #setText}
     * if there are any text changed listeners forces the buffer type to
     * Editable if it would not otherwise be and does call this method.
     */
    void addTextChangedListener(TextWatcher watcher);
    void removeTextChangedListener(TextWatcher watcher);
}
