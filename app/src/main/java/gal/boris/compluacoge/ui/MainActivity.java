package gal.boris.compluacoge.ui;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;
import androidx.core.provider.FontRequest;
import androidx.core.view.WindowCompat;
import androidx.emoji.text.EmojiCompat;
import androidx.emoji.text.FontRequestEmojiCompatConfig;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.PreferenceManager;

import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;

import java.util.Locale;

import gal.boris.compluacoge.R;
import gal.boris.compluacoge.logic.AccountManager;
import gal.boris.compluacoge.logic.MyViewModel;

public class MainActivity extends AppCompatActivity {

    private MyViewModel viewModel;
    private AccountManager accountManager;

    @Override
    protected void attachBaseContext(Context newBase) {
        Context context = checkLanguage(newBase);
        checkDarkMode(newBase);
        super.attachBaseContext(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        viewModel = new ViewModelProvider(this).get(MyViewModel.class);
        accountManager = viewModel.getAccountManager();
        accountManager.initialize();

        saveDarkMode();
        checkBackground();
        checkKeepScreenOn();
        initializeEmojis();
        checkRemoteConfig();
    }

    @Override
    protected void onStart() {
        super.onStart();
        accountManager.signInSilently();
    }

    public AccountManager getAccountManager() {
        return accountManager;
    }

    public Context checkLanguage(Context context) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        Configuration config = new Configuration(context.getResources().getConfiguration());
        String language = sharedPref.getString(context.getResources().getString(R.string.sp_key_language),context.getResources().getString(R.string.language_code));
        Locale locale = new Locale(language);
        Locale.setDefault(locale);
        config.setLocale(locale);
        return context.createConfigurationContext(config);
    }

    private void checkDarkMode(Context context) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        int defaultMode = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q ? AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM : AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY;
        int darkMode = sharedPref.getInt(context.getString(R.string.sp_dark_mode_value),defaultMode);
        AppCompatDelegate.setDefaultNightMode(darkMode);
    }

    private void saveDarkMode() { //es el unico que escribe en flags el darkMode
        Configuration configuration = getResources().getConfiguration();
        int currentNightMode = configuration.uiMode & Configuration.UI_MODE_NIGHT_MASK;
        viewModel.getFlags().setDarkModeOn(currentNightMode == Configuration.UI_MODE_NIGHT_YES);
    }

    private void checkRemoteConfig() {
        FirebaseRemoteConfig mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
        FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
                .setMinimumFetchIntervalInSeconds(3600)
                .build();
        mFirebaseRemoteConfig.setConfigSettingsAsync(configSettings);
        mFirebaseRemoteConfig.setDefaultsAsync(R.xml.remote_config_defaults);
        mFirebaseRemoteConfig.fetchAndActivate().addOnSuccessListener(this, aBoolean -> {
            //Change in Flags the new values
        });
    }

    public void checkBackground() {
        //Uso algo muy similar en ModesFragment
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        boolean blackBetterThanGray = sharedPref.getBoolean(getString(R.string.sp_black_better_than_gray),true);
        viewModel.getFlags().setBlackNotGray(blackBetterThanGray);

        int blackOrGrayID = blackBetterThanGray ? R.color.black : R.color.google_dark;
        int colorID = viewModel.getFlags().getDarkModeOn().getValue() ? blackOrGrayID : R.color.white;

        int color = ContextCompat.getColor(getApplicationContext(),colorID);
        getWindow().setBackgroundDrawable(new ColorDrawable(color));

        //La status bar no puede estar con texto negro sobre fondo blanco en api 21 y 22, asi que la pintamos de  texto blanco sobre fondo negro
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.M && colorID==R.color.whiteOpt87) {
            getWindow().setStatusBarColor(ContextCompat.getColor(getApplicationContext(),blackOrGrayID));
        }
        //Lo mismo para nav bar
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.O_MR1 && colorID==R.color.whiteOpt87) {
            getWindow().setNavigationBarColor(ContextCompat.getColor(getApplicationContext(),blackOrGrayID));
        }

        //Lo ponemos a pantalla completa (pintando detras de statusBar y navBar)
        WindowCompat.setDecorFitsSystemWindows(getWindow(),false);
    }

    private void checkKeepScreenOn() {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        boolean keepScreenOn = sharedPref.getBoolean(getString(R.string.sp_keep_screen_on),true);
        if(keepScreenOn) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
    }

    private void initializeEmojis() {
        FontRequest fontRequest = new FontRequest(
                "com.google.android.gms.fonts",
                "com.google.android.gms",
                "Noto Color Emoji Compat",
                R.array.com_google_android_gms_fonts_certs);
        EmojiCompat.Config config = new FontRequestEmojiCompatConfig(this, fontRequest);
        config.setReplaceAll(true)
                .setEmojiSpanIndicatorEnabled(false)
                .setEmojiSpanIndicatorColor(Color.GREEN);

        EmojiCompat.init(config);
    }

}