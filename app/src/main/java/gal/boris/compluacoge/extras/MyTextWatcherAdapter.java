package gal.boris.compluacoge.extras;

import android.text.Editable;
import android.text.TextWatcher;

import androidx.annotation.NonNull;

public class MyTextWatcherAdapter implements TextWatcher {

    @Override
    public void beforeTextChanged(@NonNull CharSequence s, int start, int count, int after) {}

    @Override
    public void onTextChanged(@NonNull CharSequence s, int start, int before, int count) {}

    @Override
    public void afterTextChanged(@NonNull Editable s) {}
}
