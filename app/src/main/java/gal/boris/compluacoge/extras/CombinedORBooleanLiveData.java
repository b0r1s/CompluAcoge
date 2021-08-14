package gal.boris.compluacoge.extras;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;

public class CombinedORBooleanLiveData extends MediatorLiveData<Boolean> {
    private boolean[] listBooleans;

    @SafeVarargs
    public CombinedORBooleanLiveData(LiveData<Boolean>... liveDatas) {
        setValue(false);
        listBooleans = new boolean[liveDatas.length];
        for (int i = 0; i < liveDatas.length; i++) {
            final int pos = i;
            addSource(liveDatas[i], bool -> {
                listBooleans[pos] = bool;
                updateResult();
            });
        }
    }

    private void updateResult() {
        boolean result = false;
        for(boolean bool : listBooleans) {
            result = result || bool;
        }
        setValue(result);
    }
}