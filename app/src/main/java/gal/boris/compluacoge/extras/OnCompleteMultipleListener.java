package gal.boris.compluacoge.extras;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

public class OnCompleteMultipleListener implements OnCompleteListener<Void> {

    private int haveToEnd;
    private int haveToSuccess;
    private final Runnable success;
    private final Runnable failed;

    public OnCompleteMultipleListener(int total, Runnable success, Runnable failed) {
        this.haveToEnd = total;
        this.haveToSuccess = total;
        this.success = success;
        this.failed = failed;
        if(total==0) {
            success.run();
        }
    }

    @Override
    public void onComplete(@NonNull Task task) {
        haveToEnd--;
        if(task.isSuccessful()) {
            haveToSuccess--;
        }
        if(haveToEnd==0) {
            if(haveToSuccess==0) {
                success.run();
            } else {
                failed.run();
            }
        }
    }
}
