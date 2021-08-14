package gal.boris.compluacoge.extras;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.os.Build;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.WindowMetrics;

import androidx.annotation.Dimension;
import androidx.annotation.NonNull;
import androidx.core.util.Pair;

public class GridSpaceCalculator {

    public static float dpToPx(@NonNull Context context, @Dimension(unit = Dimension.DP) int dp) {
        Resources r = context.getResources();
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, r.getDisplayMetrics());
    }

    public static int getNumColumns(Activity activity, int dpMinWidth) {
        Pair<Integer,Integer> pair = getHeightWidth(activity);
        int height = pair.first;
        int width = pair.second;
        float pxMinWidth = dpToPx(activity.getApplicationContext(),dpMinWidth);
        return (int) (width/pxMinWidth);
    }

    private static Pair<Integer,Integer> getHeightWidth(Activity activity) {
        int height, width;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            WindowMetrics metrics = activity.getWindowManager().getCurrentWindowMetrics();
            height = metrics.getBounds().height();
            width = metrics.getBounds().width();
        } else {
            DisplayMetrics metrics = new DisplayMetrics();
            activity.getWindowManager().getDefaultDisplay().getMetrics(metrics);
            height = metrics.heightPixels;
            width = metrics.widthPixels;
        }
        return new Pair<>(height,width);
    }

}
