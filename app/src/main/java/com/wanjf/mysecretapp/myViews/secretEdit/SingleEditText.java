package com.wanjf.mysecretapp.myViews.secretEdit;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.PasswordTransformationMethod;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;

import androidx.appcompat.widget.AppCompatEditText;

/**
 * 可以延时
 */
public class SingleEditText extends AppCompatEditText implements View.OnKeyListener {
    private SingleEditText nextEdit;
    private SingleEditText lastEdit;

    public SingleEditText(Context context) {
        super(context);
        init();
    }

    public SingleEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public SingleEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_DEL && event.getAction() == KeyEvent.ACTION_DOWN) {// 按下去
            if (getText().toString().length() > 0) {
                setText("");
                return true;
            }
            if (lastEdit != null) {
                lastEdit.setEnabled(true);
                lastEdit.requestFocus();
                lastEdit.setText("");
                setEnabled(false);
            }
        }
        return false;
    }

    private void init() {
        setOnKeyListener(this);
        setTransformationMethod(passwordTransformationMethod);
        addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().length() == 1) {
                    if (nextEdit != null) {
                        nextEdit.setEnabled(true);
                        nextEdit.requestFocus();
                        setEnabled(false);
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    public void setNextEdit(SingleEditText nextEdit) {
        this.nextEdit = nextEdit;
        nextEdit.setLastEdit(this);
    }

    private void setLastEdit(SingleEditText lastEdit) {
        this.lastEdit = lastEdit;
    }

    private PasswordTransformationMethod passwordTransformationMethod = new PasswordTransformationMethod() {
        @Override
        public CharSequence getTransformation(CharSequence source, View view) {
            return new PasswordCharSequence(source);
        }
    };

    static class PasswordCharSequence implements CharSequence {
        private CharSequence mSource;

        public PasswordCharSequence(CharSequence source) {
            mSource = source;
        }

        @Override
        public int length() {
            return mSource.length();
        }

        @Override
        public char charAt(int index) {
            return '*';//●
        }

        @Override
        public CharSequence subSequence(int start, int end) {
            return mSource.subSequence(start, end);
        }
    }
}
