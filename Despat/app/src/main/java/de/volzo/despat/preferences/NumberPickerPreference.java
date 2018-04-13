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

public class NumberPickerPreference extends DialogPreference {

    // enable or disable the 'circular behavior'
    public static final boolean WRAP_SELECTOR_WHEEL = true;

    private NumberPicker picker;

    private int minValue = 0;
    private int maxValue = 100;
    private int stepSize = 1;
    private int defaultValue = minValue;
    private int currentValue;

    public NumberPickerPreference(Context context) {
        super(context);
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

        NumberPicker.Formatter formatter = new NumberPicker.Formatter() {
            @Override
            public String format(int value) {
                return Integer.toString(value);
            }
        };
        picker.setFormatter(formatter);

        int totalValues = maxValue-minValue/stepSize;
        String availableValues[] = new String[totalValues];
        for (int i=0; i<totalValues; i++) {
            availableValues[i] = Integer.toString(minValue + i * stepSize);
        }
        picker.setDisplayedValues(availableValues);

        FrameLayout dialogView = new FrameLayout(getContext());
        dialogView.addView(picker);

        return dialogView;
    }

    @Override
    protected void onSetInitialValue(boolean restorePersistedValue, Object defaultValue) {
        if (restorePersistedValue) { // Restore existing state
            currentValue = this.getPersistedInt((int) defaultValue);
        } else { // Set default state
            currentValue = (Integer) defaultValue;
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
        return a.getInteger(index, defaultValue);
    }

    public void setMinValue(int value) {
        this.minValue = value;
    }

    public void setMaxValue(int value) {
        this.maxValue = value;
    }

    public void setStepSize(int value) {
        this.stepSize = value;
    }

    public void setDefaultValue(int value) {
        this.defaultValue = value;
    }

    public void setValue(int value) {
        this.currentValue = value;
        persistInt(this.currentValue);
    }

    public int getValue() {
        return this.currentValue;
    }
}