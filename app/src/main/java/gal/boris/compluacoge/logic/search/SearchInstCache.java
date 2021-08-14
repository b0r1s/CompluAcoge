package gal.boris.compluacoge.logic.search;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;

import gal.boris.compluacoge.data.objects.PublicInstitution;
import gal.boris.compluacoge.extras.Box;
import gal.boris.compluacoge.logic.CloudDB;

public class SearchInstCache {

    //private final Map<String, ListenerRegistration> snapshotMap;
    private final Map<String, MutableLiveData<PublicInstitution>> publicInstitutionMap;
    private final FirebaseFirestore db;

    public SearchInstCache(FirebaseFirestore db) {
        this.publicInstitutionMap = new HashMap<>();
        this.db = db;
    }

    public LiveData<PublicInstitution> getInstitution(String idInst){
        if(publicInstitutionMap.containsKey(idInst)) {
            return publicInstitutionMap.get(idInst);
        }
        MutableLiveData<PublicInstitution> answer = new MutableLiveData<>(null);
        publicInstitutionMap.put(idInst, answer);
        final DocumentReference docRef = db.collection(CloudDB.DOC_PUBLIC_ACCOUNTS).document(idInst);
        docRef.addSnapshotListener((snapshot,e) -> {
            if(e != null || snapshot==null || !snapshot.exists()) {
                return;
            }
            Map<String, Object> data = snapshot.getData();
            Box<Boolean> dataMissing = new Box<>(false);
            PublicInstitution publicInstitution = new PublicInstitution(data,dataMissing);
            if(dataMissing.get()) {
                docRef.set(publicInstitution.toMap(), SetOptions.merge());
            }
            publicInstitutionMap.get(idInst).setValue(publicInstitution);
        });
        return answer;
    }
}
