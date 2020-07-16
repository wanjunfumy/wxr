package com.wanjf.wxr.views.secretEdit;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.wanjf.wxr.R;

public class SecretEditText extends ConstraintLayout {
    private SingleEditText input_one;
    private SingleEditText input_two;
    private SingleEditText input_three;
    private SingleEditText input_four;
    private SingleEditText input_five;
    private SingleEditText input_six;

    private OnSecretPasswordListener onSecretPasswordListener;

    public OnSecretPasswordListener getOnSecretPasswordListener() {
        return onSecretPasswordListener;
    }

    public void setOnSecretPasswordListener(OnSecretPasswordListener onSecretPasswordListene) {
        onSecretPasswordListener = onSecretPasswordListene;
    }

    public SecretEditText(Context context) {
        super(context);
        init();
    }

    public SecretEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public SecretEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }


    private void init() {
        inflate(getContext(), R.layout.secret_edit_text, this);
        input_one = findViewById(R.id.input_one);
        input_two = findViewById(R.id.input_two);
        input_three = findViewById(R.id.input_three);
        input_four = findViewById(R.id.input_four);
        input_five = findViewById(R.id.input_five);
        input_six = findViewById(R.id.input_six);

        input_one.setNextEdit(input_two);
        input_two.setNextEdit(input_three);
        input_three.setNextEdit(input_four);
        input_four.setNextEdit(input_five);
        input_five.setNextEdit(input_six);
        input_six.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() == 1) {
                    if (getOnSecretPasswordListener() != null) {
                        String currentString = getValue();
                        getOnSecretPasswordListener().onPasswordChange(currentString, currentString.length() == 6);
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    private String getValue() {
        return input_one.getText().toString() +
                input_two.getText().toString() +
                input_three.getText().toString() +
                input_four.getText().toString() +
                input_five.getText().toString() +
                input_six.getText().toString();
    }

}
