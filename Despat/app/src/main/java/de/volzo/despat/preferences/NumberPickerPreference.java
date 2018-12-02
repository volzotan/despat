package de.volzo.despat.preferences;

import android.content.Context;
import android.content.res.TypedArray;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.NumberPicker;

import java.util.List;

public class NumberPickerPreference extends DialogPreference {

    // enable or disable the 'circular behavior'
    public static final boolean WRAP_SELECTOR_WHEEL = true;

    private NumberPicker picker;

    private int minValue = -1;
    private int maxValue = 100;
    private float factor = 1;
    private int defaultValue;
    private int currentValue;

    public NumberPickerPreference(Context context) {
        super(context);

        defaultValue = minValue;
        currentValue = convertToDisplay(this.getPersistedInt(minValue));
    }

    public NumberPickerPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public NumberPickerPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected View onCreateDialogView() {
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.gravity = Gravity.CENTER;

        picker = new NumberPicker(getContext());
        picker.setLayoutParams(layoutParams);

//        NumberPicker.Formatter formatter = new NumberPicker.Formatter() {
//            @Override
//            public String.format(Config.LOCALE, (int value) {
//                return Integer.toString(value / stepSize);
//            }
//        };
//        picker.setFormatter(formatter);

        picker.setMinValue(minValue);
        picker.setMaxValue(maxValue);

        FrameLayout dialogView = new FrameLayout(getContext());
        dialogView.addView(picker);

        return dialogView;
    }

    @Override
    protected void onSetInitialValue(boolean restorePersistedValue, Object defaultValue) {
        if (restorePersistedValue) { // Restore existing state
            if (defaultValue != null) {
                currentValue = convertToDisplay(this.getPersistedInt((int) (defaultValue)));
            } else {
                currentValue = convertToDisplay(this.getPersistedInt(this.defaultValue));
            }

            if (currentValue < this.minValue) {
                setValue(this.minValue);
            }

            if (currentValue > this.maxValue) {
                setValue(this.maxValue);
            }

        } else { // Set default state

            if (defaultValue instanceof Float) {
                currentValue = Math.round((float) defaultValue);
            } else {
                currentValue = (int) defaultValue;
            }

            persistInt(currentValue);
        }
    }

    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);
        picker.setWrapSelectorWheel(WRAP_SELECTOR_WHEEL);
        picker.setValue(getValue());
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        if (positiveResult) {
            picker.clearFocus();
            int newValue = picker.getValue();
            if (callChangeListener(newValue)) {
                setValue(newValue);
            }
        }
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        return a.getInteger(index, convertToSave(defaultValue));
    }

    public int convertToDisplay(int value) {
        return (int) (value / factor);
    }

    public int convertToSave(int value) {
        return (int) (value * factor);
    }

    public void setMinValue(int value) {
        this.minValue = value;
    }

    public void setMaxValue(int value) {
        this.maxValue = value;
    }

    public void setFactor(float value) {
        this.factor = value;
    }

    public void setDefaultValue(int value) {
        this.defaultValue = value;
    }

    public void setValue(int value) {
        this.currentValue = value;
        persistInt(convertToSave(this.currentValue));
    }

    public int getValue() {
        return this.currentValue;
    }
}