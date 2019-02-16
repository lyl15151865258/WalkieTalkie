package jp.co.shiratsuki.walkietalkie.widget.spinner;

import android.text.Spannable;
import android.text.SpannableString;

public class SimpleSpinnerTextFormatter implements SpinnerTextFormatter {

    @Override
    public Spannable format(String text) {
        return new SpannableString(text);
    }
}
