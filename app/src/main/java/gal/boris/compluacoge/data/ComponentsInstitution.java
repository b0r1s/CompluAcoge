package gal.boris.compluacoge.data;

import androidx.core.util.Pair;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.SetOptions;

import java.util.Map;

import gal.boris.compluacoge.data.objects.PrivateInstitution;
import gal.boris.compluacoge.data.objects.PublicInstitution;
import gal.boris.compluacoge.extras.Box;
import gal.boris.compluacoge.logic.CloudDB;

public class ComponentsInstitution {

    private PrivateInstitution privateInst;

    private final FirebaseFirestore db;
    private ListenerRegistration listenerPublicInst;
    private String idPublicInst;

    private PublicInstitution publicInst;
    private MutableLiveData<Pair<PrivateInstitution, PublicInstitution>> result;

    public ComponentsInstitution(FirebaseFirestore db) {
        this.result = new MutableLiveData<>(new Pair<>(privateInst,publicInst));
        this.db = db;
        clear();
    }

    public void clear() {
        this.privateInst = PrivateInstitution.createEmpty();
        this.publicInst = PublicInstitution.createEmpty();
        if(this.listenerPublicInst != null) {
            this.listenerPublicInst.remove();
            this.listenerPublicInst = null;
        }
        this.idPublicInst = "";
        updateResult();
    }

    public void update(PrivateInstitution privateInstitution) {
        this.privateInst = privateInstitution;
        connectPublicUser();
        updateResult();
    }

    private void connectPublicUser() {
        if(!idPublicInst.equals(privateInst.getID())) {
            if(listenerPublicInst != null) {
                listenerPublicInst.remove();
            }
            idPublicInst = privateInst.getID();
            final DocumentReference docRef = db.collection(CloudDB.DOC_PUBLIC_ACCOUNTS).document(idPublicInst);
            listenerPublicInst = docRef.addSnapshotListener((snapshot,e) -> {
                if(e != null || snapshot==null || !snapshot.exists()) {
                    publicInst = PublicInstitution.createEmpty();
                    updateResult();
                    return;
                }
                Map<String, Object> data = snapshot.getData();
                Box<Boolean> dataMissing = new Box<>(false);
                publicInst = new PublicInstitution(data,dataMissing);
                if(dataMissing.get()) {
                    docRef.set(publicInst.toMap(), SetOptions.merge());
                }
                updateResult();

            });
        }
    }

    private void updateResult() {
        this.result.setValue(new Pair<>(privateInst,publicInst));
    }

    public LiveData<Pair<PrivateInstitution, PublicInstitution>> getResult() {
        return result;
    }
}
