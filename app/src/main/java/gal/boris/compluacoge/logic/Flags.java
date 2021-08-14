package gal.boris.compluacoge.logic;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.preference.PreferenceManager;

public class Flags {

    private MutableLiveData<Boolean> showTutorial;
    private MutableLiveData<Boolean> darkModeOn;
    private MutableLiveData<Boolean> blackNotGray;

    public Flags(Context context) {
        this.showTutorial = new MutableLiveData<>(); //Initialized later?? TODO
        this.darkModeOn = new MutableLiveData<>(); //Initialized later on MainActivity
        this.blackNotGray = new MutableLiveData<>(); //Initialized later on MainActivity

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        //For app preferences
    }

    public LiveData<Boolean> getShowTutorial() {
        return showTutorial;
    }

    public LiveData<Boolean> getDarkModeOn() {
        return darkModeOn;
    }

    public LiveData<Boolean> getBlackNotGray() {
        return blackNotGray;
    }

    public void setShowTutorial(boolean showTutorial) {
        this.showTutorial.setValue(showTutorial);
    }

    public void setDarkModeOn(boolean darkModeOn) {
        this.darkModeOn.setValue(darkModeOn);
    }

    public void setBlackNotGray(boolean blackNotGray) {
        this.blackNotGray.setValue(blackNotGray);
    }

}
