/***
 Copyright (c) 2015 earlyeast , xiaodong666@gmail.com

 Licensed under the Apache License, Version 2.0 (the "License"); you may
 not use this file except in compliance with the License. You may obtain
 a copy of the License at
 http://www.apache.org/licenses/LICENSE-2.0
 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 */

package com.eastearly.richedittextview;


import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.Selection;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.BackgroundColorSpan;
import android.text.style.CharacterStyle;
import android.text.style.ForegroundColorSpan;
import android.text.style.StrikethroughSpan;
import android.text.style.StyleSpan;
import android.text.style.URLSpan;
import android.text.style.UnderlineSpan;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * Created by dachen on 9/28/15.
 */
public class RichEditText extends LinearLayout implements View.OnClickListener,IRichEditText, IEditText {

    public static final int EDIT_TOGGLE_ID = 1002;
    public static final int EDIT_TEXT_ID = 1001;
    private static final String TAG = "RichEditText";
    private final float initialAlpha = 0.7f;
    private Context _context;
    private String textContent;
    private int textColor;
    private EditText mEditText;
    private LinearLayout mHtmloptions;
    private ImageButton mImageButton;
    private SpannableStringBuilder mSS;
    private boolean mToolbarClosed = true;
    private boolean mRichEditEnabled = true;
    private final static String Tag = "RichEditTextView";

    public RichEditText(Context context) {
        super(context, null);
        setupView(context,null,0,0);
    }

    public RichEditText(Context context, AttributeSet attrs) {
        super(context, attrs, 0);
        setupView(context, attrs, 0, 0);
    }


    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public RichEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr, 0);
        setupView(context, attrs, defStyleAttr, 0);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public RichEditText(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes){
        super(context,attrs,defStyleAttr,defStyleRes);
        setupView(context,attrs,defStyleAttr,defStyleRes);

    }
    //Test to add a relativelayout programtically
    private void setupView(Context context, AttributeSet attrs, int defStyleAttr,int defStyleRes){
        _context = context;
        RelativeLayout relativeLayout = new RelativeLayout(context);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT);

        EditText editText = new EditText(context,attrs);

        editText.setId(EDIT_TEXT_ID);
        //
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.RichEditText);
        mRichEditEnabled = a.getBoolean(R.styleable.RichEditText_richEditAble,true);
        a.recycle();

        mImageButton = new ImageButton(context);
        int px = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 24, getResources().getDisplayMetrics());
        RelativeLayout.LayoutParams editToggleParams = new RelativeLayout.LayoutParams(px,px);
        mImageButton.setBackground(getResources().getDrawable(R.drawable.ic_keyboard_arrow_left_black_24dp));
        editToggleParams.addRule(RelativeLayout.ALIGN_BOTTOM, EDIT_TEXT_ID);
        editToggleParams.addRule(RelativeLayout.ALIGN_RIGHT, EDIT_TEXT_ID);
        mImageButton.setLayoutParams(editToggleParams);
        mImageButton.setId(EDIT_TOGGLE_ID);
        mImageButton.setRotation(-90);
        mImageButton.setOnClickListener(this);

        View htmlOptions = inflate(context,R.layout.htmloptions,null);

        RelativeLayout.LayoutParams htmlOptionsLayoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT);

        htmlOptionsLayoutParams.addRule(RelativeLayout.BELOW, 1001);
        htmlOptionsLayoutParams.addRule(RelativeLayout.ALIGN_RIGHT, 1001);

        htmlOptions.setLayoutParams(htmlOptionsLayoutParams);
        relativeLayout.setLayoutParams(params);
        relativeLayout.addView(editText);
        relativeLayout.addView(mImageButton);

        //htmlOptions.setVisibility(View.GONE);
        if(mRichEditEnabled) {
            relativeLayout.addView(htmlOptions);
        }
        addView(relativeLayout);

        this.mEditText = editText;
        if(mRichEditEnabled) {
            findViewById(R.id.makeBold).setOnClickListener(this);
            findViewById(R.id.makeItalic).setOnClickListener(this);
            findViewById(R.id.makeUnderline).setOnClickListener(this);
            findViewById(R.id.makeBackground).setOnClickListener(this);
            findViewById(R.id.makeForeground).setOnClickListener(this);
            findViewById(R.id.makeHyperlink).setOnClickListener(this);
            findViewById(R.id.makeStrikethrough).setOnClickListener(this);
            findViewById(R.id.makeScaleX).setOnClickListener(this);
            mHtmloptions = (LinearLayout) findViewById(R.id.rich_toolbar);
            mHtmloptions.setVisibility(View.GONE);
//            mImageButton = (ImageButton) findViewById(R.id.list_toggle);
//            mImageButton.setOnClickListener(this);
        }
        this.mEditText.setOnClickListener(this);
        setOnClickListener(this);
        mSS = new SpannableStringBuilder(mEditText.getText());

    }
    @Override
    public void onClick(View view) {

        //refresh tool bar status
        getHtmloptionToolButton();
        mSS = new SpannableStringBuilder(mEditText.getText());
        final int start = mEditText.getSelectionStart();
        final int end = mEditText.getSelectionEnd();

        int viewId = view.getId();
        if (viewId == mEditText.getId()) {
            this.refreshHtmloptionBar();
        }

        CharacterStyle span = null;

        if (viewId == R.id.makeBold) {
            if (toggleImageView((ImageView) view))
                span = new StyleSpan(Typeface.BOLD);
            else
                disableStyleSpan(start, end, Typeface.BOLD);

        } else if (viewId == R.id.makeItalic) {
            if (toggleImageView((ImageView) view))
                span = new StyleSpan(Typeface.ITALIC);
            else
                disableStyleSpan(start, end, Typeface.ITALIC);

        } else if (viewId == R.id.makeUnderline) {
            if (toggleImageView((ImageView) view))
                span = new UnderlineSpan();
            else
                disableSpan(start, end, UnderlineSpan.class);

        }
        else if(viewId == R.id.makeStrikethrough){
            if(toggleImageView((ImageView) view))
                span = new StrikethroughSpan();
            else
                disableSpan(start, end, StrikethroughSpan.class);
        }
        else if(viewId == R.id.makeScaleX && start!=end){
            AbsoluteSizeSpan[] spans = mSS.getSpans(start, end, AbsoluteSizeSpan.class);
            float textSize;
            if (spans.length > 0) {
                textSize = (float) (spans[spans.length-1].getSize() * 1.1);
            } else {
                textSize = (float) (mEditText.getTextSize() * 1.1);
            }
            for (AbsoluteSizeSpan spanItem : spans) {
                mSS.removeSpan(spanItem);
            }
            span = new AbsoluteSizeSpan((int)textSize);


        }
        else if (viewId == R.id.makeForeground && start!=end) {
            new ColorPickerDialog(_context, new ColorPickerDialog.OnColorChangedListener() {
                @Override
                public void colorChanged(int color) {
                    mSS.setSpan(new ForegroundColorSpan(color),
                            start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    mEditText.setText(mSS, TextView.BufferType.SPANNABLE);
                    //((TextView)MessageCompose.this.findViewById(R.id.makeForeground)).setTextColor(color);
                }

            }, Color.BLACK).show();

        }
        else if(viewId == R.id.makeBackground && start!=end){
            new ColorPickerDialog(_context, new ColorPickerDialog.OnColorChangedListener () {
                @Override
                public void colorChanged(int color) {
                    mSS.setSpan(new BackgroundColorSpan(color),
                            start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    mEditText.setText(mSS, TextView.BufferType.SPANNABLE);
                    //MessageCompose.this.findViewById(R.id.makeBackground).setBackgroundColor(color);
                }

            }, Color.WHITE).show();
        }
        else if (viewId == R.id.makeHyperlink && start!= end) {

            AlertDialog.Builder builder = new AlertDialog.Builder(_context);
            final EditText urlText = new EditText(_context);
            urlText.setText("http://www.");
            builder.setView(urlText)
                    .setTitle(getResources().getString(R.string.url_entry_title))
                    .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            if (urlText.getText() != null) {
                                String url = urlText.getText().toString();
                                mSS.setSpan(new URLSpan(url),
                                        start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                                mEditText.setText(mSS, TextView.BufferType.SPANNABLE);
                            }

                        }
                    })
                    .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    });
            builder.create().show();

        }
        else if( viewId == EDIT_TOGGLE_ID){
            if(!mToolbarClosed){
                mToolbarClosed = !mToolbarClosed;
                mImageButton.setRotation(-90);
                //mImageButton.setBackground(getResources().getDrawable(R.drawable.ic_keyboard_arrow_left_black_24dp));
//                AnimatorSet set = new AnimatorSet();
//                set.playTogether(
//                        ObjectAnimator.ofFloat(mHtmloptions, "translationX", mHtmloptions.getMeasuredWidth()),
//                        ObjectAnimator.ofFloat(mHtmloptions, "alpha", 1, 0)
//                );
//                set.start();

                mHtmloptions.setVisibility(View.GONE);
                //requestLayout();

            }

            else
            {
                mToolbarClosed = !mToolbarClosed;
                mHtmloptions.setVisibility(View.VISIBLE);
                //requestLayout();
                mImageButton.setRotation(90);
//                ObjectAnimator objectAnimator = new ObjectAnimator();
//                AnimatorSet set = new AnimatorSet();
//                set.playTogether(
//                        ObjectAnimator.ofFloat(mHtmloptions, "translationX", 0),
//                        ObjectAnimator.ofFloat(mHtmloptions, "alpha", 0, 1)
//                );
//                set.start();
            }
        }
        if (span != null) {

            if (start == end)
                mSS.setSpan(span, start, end, Spanned.SPAN_INCLUSIVE_INCLUSIVE);
            else
                mSS.setSpan(span, start, end, Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
            mEditText.setText(mSS, TextView.BufferType.SPANNABLE);
            mEditText.setSelection(end);
        }
    }
    private void refreshHtmloptionBar() {
        if(!mRichEditEnabled)return;
        int start = mEditText.getSelectionStart();
        int end = mEditText.getSelectionEnd();
        if (start == end && end == 0) return;
        letImageViewOff((ImageView) findViewById(R.id.makeBold));
        letImageViewOff((ImageView) findViewById(R.id.makeItalic));
        letImageViewOff((ImageView) findViewById(R.id.makeUnderline));
        Object[] spans = mSS.getSpans(start, end, Object.class);
        for (Object span : spans) {
            ImageView iv = getHtmloptionToolButton(span);
            if (iv != null)
                this.letImageViewOn(iv);
        }
    }
    private boolean imageViewOff(ImageView iv) {

        return (iv.getAlpha() - initialAlpha < 0.01);
    }

    private void letImageViewOn(ImageView iv) {
        iv.setAlpha(1.0f);

    }

    private void letImageViewOff(ImageView iv) {
        iv.setAlpha(initialAlpha);

    }
    private void getHtmloptionToolButton() {
        if(!mRichEditEnabled) return;
        int start = mEditText.getSelectionStart();
        int end = mEditText.getSelectionEnd();
        if (start == end && end == 0) return;
        letImageViewOff((ImageView) findViewById(R.id.makeBold));
        letImageViewOff((ImageView) findViewById(R.id.makeItalic));
        letImageViewOff((ImageView) findViewById(R.id.makeUnderline));
        letImageViewOff((ImageView) findViewById(R.id.makeStrikethrough));
        Object[] spans = mSS.getSpans(start, end, Object.class);
        for (Object span : spans) {
            ImageView iv = getHtmloptionToolButton(span);
            if (iv != null)
                this.letImageViewOn(iv);
        }
    }
    private ImageView getHtmloptionToolButton(Object span) {
        if (span instanceof StyleSpan) {
            switch (((StyleSpan) span).getStyle()) {
                case Typeface.BOLD:
                    return (ImageView) findViewById(R.id.makeBold);
                case Typeface.ITALIC:
                    return (ImageView) findViewById(R.id.makeItalic);
                default:
                    return null;
            }
        } else if (span instanceof UnderlineSpan) {
            return (ImageView) findViewById(R.id.makeUnderline);
        }
        else if(span instanceof StrikethroughSpan)
            return (ImageView) findViewById(R.id.makeStrikethrough);
        return null;
    }
    private boolean toggleImageView(ImageView iv) {
        if (imageViewOff(iv)) {
            letImageViewOn(iv);
            return true;
        } else {
            letImageViewOff(iv);
            return false;
        }
    }
    private void disableStyleSpan(int start, int end, int typeFace) {
        StyleSpan[] spans = mSS.getSpans(start, end, StyleSpan.class);
        for (int i = spans.length - 1; i >= 0; i--)
            if (spans[i].getStyle() == typeFace) {
                if (mSS.getSpanStart(spans[i]) <= start)
                    mSS.setSpan(spans[i], mSS.getSpanStart(spans[i]), start, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                if (mSS.getSpanEnd(spans[i]) > start) {

                    mSS.setSpan(spans[i], start, mSS.getSpanEnd(spans[i]), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
                //mSS.removeSpan(spans[i]);
            }
        mEditText.setText(mSS, TextView.BufferType.SPANNABLE);
        mEditText.setSelection(end);
    }

    private void disableSpan(int start, int end, Class<? extends CharacterStyle> clz) {
        CharacterStyle[] spans = mSS.getSpans(start, end, clz);
        for (int i = spans.length - 1; i >= 0; i--)

        {
            if (mSS.getSpanStart(spans[i]) <= start)
                mSS.setSpan(spans[i], mSS.getSpanStart(spans[i]), start, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            if (mSS.getSpanEnd(spans[i]) > start) {

                mSS.setSpan(spans[i], start, mSS.getSpanEnd(spans[i]), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
            //mSS.removeSpan(spans[i]);
        }
        mEditText.setText(mSS, TextView.BufferType.SPANNABLE);
        mEditText.setSelection(end);
    }



    public Editable getText() {
        return mEditText.getText();
    }



    public void setText(CharSequence text, TextView.BufferType type) {
        mEditText.setText(text, type);
    }

    /**
     * Convenience for {@link Selection#setSelection(Spannable, int, int)}.
     */

    public void setSelection(int start, int stop) {
        mEditText.setSelection(start, stop);
    }

    /**
     * Convenience for {@link Selection#setSelection(Spannable, int)}.
     */

    public void setSelection(int index) {
        mEditText.setSelection(index);
    }

    /**
     * Convenience for {@link Selection#selectAll}.
     */

    public void selectAll() {
        mEditText.selectAll();
    }

    /**
     * Convenience for {@link Selection#extendSelection}.
     */

    public void extendSelection(int index) {
        mEditText.extendSelection(index);
    }


    public void setEllipsize(TextUtils.TruncateAt ellipsis) {
        mEditText.setEllipsize(ellipsis);
    }


    public void onInitializeAccessibilityEvent(AccessibilityEvent event) {
        mEditText.onInitializeAccessibilityEvent(event);
    }


    public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo info) {
        mEditText.onInitializeAccessibilityNodeInfo(info);
    }


    public boolean performAccessibilityAction(int action, Bundle arguments) {
        return mEditText.performAccessibilityAction(action,arguments);

    }

    @Override
    public void addTextChangedListener(TextWatcher watcher) {
        mEditText.addTextChangedListener(watcher);
    }

    @Override
    public void removeTextChangedListener(TextWatcher watcher) {
        mEditText.removeTextChangedListener(watcher);
    }

    //implements IRichEditText
    public String toHtml() {
        if(mSS==null){
            return null;
        }
        return EmailHtmlUtil.toHtml (mSS);
    }
}
