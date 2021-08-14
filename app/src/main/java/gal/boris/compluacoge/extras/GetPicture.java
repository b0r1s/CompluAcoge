package gal.boris.compluacoge.extras;

import android.net.Uri;
import android.os.Environment;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import gal.boris.compluacoge.R;

public class GetPicture {
    private final Fragment fragment;

    private Integer drawable;
    private TextView titleView;
    private ImageView imageView;
    private Button editButtonView;
    private Button deleteButtonView;

    private Uri image;
    private Uri dirImageTaken;
    private final ActivityResultLauncher<String> mGetContent;
    private final ActivityResultLauncher<Uri> mTakePicture;

    private static final Map<String,Integer> names = new HashMap<>();

    //Must be created as an attribute
    public GetPicture(Fragment fragment) {
        this.fragment = fragment;
        mGetContent = fragment.registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
            if(uri!=null) {
                connect(uri,false);
            }
        });
        mTakePicture = fragment.registerForActivityResult(new ActivityResultContracts.TakePicture(), isSaved -> {
            if(dirImageTaken !=null && isSaved) {
                connect(dirImageTaken,false);
            }
        });
    }

    public Uri getUri() {
        return image;
    }

    public void init(Integer drawable, TextView titleView, ImageView imageView, Button editButtonView, Button deleteButtonView) {
        this.drawable = drawable;
        this.titleView = titleView;
        this.imageView = imageView;
        this.editButtonView = editButtonView;
        this.deleteButtonView = deleteButtonView;

        editButtonView.setOnClickListener(v -> {
            PopupMenu popupMenu = new PopupMenu(fragment.requireContext(), editButtonView);
            Map<MenuItem, Integer> map = new HashMap<>();
            map.put(popupMenu.getMenu().add(fragment.getString(R.string.take_picture)),0);
            map.put(popupMenu.getMenu().add(fragment.getString(R.string.choose_from_gallery)),1);
            popupMenu.setOnMenuItemClickListener(item -> {
                int numItem = map.get(item);
                if(numItem==0) {
                    dirImageTaken = FileProvider.getUriForFile(fragment.requireContext(),
                            fragment.requireContext().getApplicationContext().getPackageName()+".fileprovider",
                            createImageFile());
                    mTakePicture.launch(dirImageTaken);
                } else if(numItem==1) {
                    mGetContent.launch("image/*");
                }
                return true;
            });
            popupMenu.show();
        });
        deleteButtonView.setOnClickListener(v -> {
            if(image !=null) {
                image = null;
                deleteButtonView.setVisibility(View.GONE);
                if(drawable!=null) {
                    imageView.setImageResource(drawable);
                } else {
                    imageView.setImageDrawable(null);
                }
            }
        });
        deleteButtonView.setVisibility(View.GONE);
        image = null;
        if(drawable!=null) {
            imageView.setImageResource(drawable);
        } else {
            imageView.setImageDrawable(null);
        }
    }

    public void connect(Uri uri, boolean cache) {
        image = uri;
        if(cache) {
            Glide.with(fragment.requireContext()).load(image).diskCacheStrategy(DiskCacheStrategy.ALL).into(imageView);
        } else {
            Glide.with(fragment.requireContext()).load(image).into(imageView);
        }
        deleteButtonView.setVisibility(View.VISIBLE);
    }

    public File createImageFile() {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = fragment.requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        return new File(storageDir,imageFileName+".jpg");
    }

    public static String createImageNameCloud() {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        if(!names.containsKey(timeStamp)) {
            names.put(timeStamp, 1);
            return timeStamp;
        }
        int numCopies = names.get(timeStamp) + 1;
        names.put(timeStamp,numCopies);
        return timeStamp + "_" + numCopies;
    }

    public void hide() {
        titleView.setVisibility(View.GONE);
        imageView.setVisibility(View.GONE);
        editButtonView.setVisibility(View.GONE);
        deleteButtonView.setVisibility(View.GONE);
    }

    public void show() {
        titleView.setVisibility(View.VISIBLE);
        imageView.setVisibility(View.VISIBLE);
        editButtonView.setVisibility(View.VISIBLE);
        deleteButtonView.setVisibility(image ==null ? View.GONE : View.VISIBLE);
    }
}
