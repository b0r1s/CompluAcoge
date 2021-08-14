package gal.boris.compluacoge.extras;

import android.net.Uri;
import android.os.Environment;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import gal.boris.compluacoge.R;
import gal.boris.compluacoge.ui.adapters.ImagesAdapter;

public class GetListPictures {
    private final Fragment fragment;
    private final boolean editable;

    private RecyclerView recyclerView;
    private Button editButtonView;
    private ImagesAdapter adapterImages;

    private List<Uri> listImages; //dir and isLocal
    private Uri dirImageTaken;
    private final ActivityResultLauncher<String> mGetContent;
    private final ActivityResultLauncher<Uri> mTakePicture;

    //Must be created as an attribute
    public GetListPictures(Fragment fragment, boolean editable) {
        this.fragment = fragment;
        this.editable = editable;
        mGetContent = fragment.registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
            if(uri!=null) {
                connect(uri);
            }
        });
        mTakePicture = fragment.registerForActivityResult(new ActivityResultContracts.TakePicture(), isSaved -> {
            if(dirImageTaken !=null && isSaved) {
                connect(dirImageTaken);
            }
        });
    }

    public List<Uri> getListUri() {
        return listImages;
    }

    public void init(RecyclerView recyclerView, Button editButtonView) {
        this.recyclerView = recyclerView;
        this.editButtonView = editButtonView;
        this.listImages = new ArrayList<>();

        if(editButtonView != null) {
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
        }

        recyclerView.setHasFixedSize(true);
        recyclerView.setOverScrollMode(View.OVER_SCROLL_IF_CONTENT_SCROLLS);
        recyclerView.setLayoutManager(new LinearLayoutManager(fragment.requireActivity(),LinearLayoutManager.HORIZONTAL,false));
        adapterImages = new ImagesAdapter(listImages,fragment,recyclerView,editable);
        recyclerView.setAdapter(adapterImages);
        recyclerView.setVisibility(View.GONE);
    }

    public void changeList(List<Uri> list) {
        adapterImages.changeList(list);
        recyclerView.setVisibility(list.isEmpty() ? View.GONE : View.VISIBLE);
    }

    public void connect(Uri uri) {
        adapterImages.addUri(uri);
        recyclerView.setVisibility(View.VISIBLE);
    }

    private File createImageFile() {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = fragment.requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        return new File(storageDir,imageFileName+".jpg");
    }

}
