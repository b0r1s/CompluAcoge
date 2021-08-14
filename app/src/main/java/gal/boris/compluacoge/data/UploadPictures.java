package gal.boris.compluacoge.data;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import gal.boris.compluacoge.R;
import gal.boris.compluacoge.extras.AppExecutors;
import gal.boris.compluacoge.extras.GetPicture;

public class UploadPictures {

    private int endedUpload;
    private int endedDownloadURL;
    private final Map<Uri,String> result;
    private OnCompleteListener onCompleteListener;

    private UploadPictures(int size) {
        this.endedUpload = size;
        this.endedDownloadURL = size;
        this.result = new HashMap<>();
        this.onCompleteListener = null;
    }

    public static void uploadList(String idProc, List<Uri> uris, OnCompleteListener onCompleteListener,
                                  Context context, Resources resources) {
        if(uris.size()==0) {
            onCompleteListener.complete(new HashMap<>());
            return;
        }

        UploadPictures uploadPictures = new UploadPictures(uris.size());
        uploadPictures.onCompleteListener = onCompleteListener;
        for (int i = 0; i < uris.size(); i++) {
            final int finalI = i;
            try {
                String name = GetPicture.createImageNameCloud();
                PipedInputStream stream = UploadPictures.getStreamCompression(uris.get(i), context, resources);
                StorageReference ref = FirebaseStorage.getInstance().getReference()
                        .child("images/"+ FirebaseAuth.getInstance().getUid()+"/"+idProc+"/"+name+".webp");
                UploadTask uploadTask = ref.putStream(stream);
                uploadTask.addOnCompleteListener(s -> {
                    uploadPictures.endedUpload--;
                    if(s.isSuccessful()) {
                        ref.getDownloadUrl().addOnCompleteListener(task -> {
                            uploadPictures.endedDownloadURL--;
                            if(task.isSuccessful()) {
                                uploadPictures.result.put(uris.get(finalI),task.getResult().toString());
                            }
                            uploadPictures.checkIfFinished();
                        });
                    } else {
                        uploadPictures.endedDownloadURL--;
                    }
                    uploadPictures.checkIfFinished();
                });
            } catch (IOException ioException) {
                showError(context,resources,R.string.uploading_failed);
                uploadPictures.endedUpload--;
                uploadPictures.endedDownloadURL--;
            }
        }
    }

    private void checkIfFinished() {
        if(endedUpload==0 && endedDownloadURL==0) {
            onCompleteListener.complete(result);
        }
    }

    public interface OnCompleteListener {
        void complete(Map<Uri,String> list);
    }

    public static void uploadImage(Context context, Resources resources, Uri uri, StorageReference destiny) {
        AppExecutors.getInstance().singleThread().execute(()->{
            try {
                PipedInputStream in = getStreamCompression(uri, context, resources);
                StorageMetadata metadata = new StorageMetadata.Builder().setContentType("image/webp").build();
                UploadTask uploadTask = destiny.putStream(in,metadata);
                uploadTask.addOnCompleteListener(task -> {
                    if(!task.isSuccessful()) {
                        showError(context,resources, R.string.uploading_failed);
                    }
                });
            } catch (IOException e) {
                showError(context,resources,R.string.uploading_failed);
            }
        });
    }

    public static PipedInputStream getStreamCompression(Uri uri, Context context, Resources resources) throws IOException {
        Bitmap bitmap = BitmapFactory.decodeStream(FirebaseStorage.getInstance().getApp()
                .getApplicationContext().getContentResolver().openInputStream(uri));
        PipedOutputStream out = new PipedOutputStream();
        PipedInputStream in = new PipedInputStream(out);
        AppExecutors.getInstance().cachePool().execute(()->{
            if(!bitmap.compress(Bitmap.CompressFormat.WEBP, 80, out)) {
                showError(context,resources,R.string.uploading_failed);
            }
            try {
                out.close();
            } catch (IOException e) {
                showError(context,resources,R.string.uploading_failed);
            }
        });
        return in;
    }

    private static void showError(Context context, Resources resources, Integer idString) {
        if(context!=null && resources!=null) {
            Toast.makeText(context,resources.getString(idString),Toast.LENGTH_SHORT).show();
        }
    }
}
