package gal.boris.compluacoge.data;

import androidx.core.util.Pair;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.SetOptions;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import gal.boris.compluacoge.data.objects.AProcedure;
import gal.boris.compluacoge.data.objects.PrivateUser;
import gal.boris.compluacoge.data.objects.Procedure;
import gal.boris.compluacoge.data.objects.PublicInstitution;
import gal.boris.compluacoge.data.objects.PublicUser;
import gal.boris.compluacoge.data.pojos.DataAProcedure;
import gal.boris.compluacoge.data.pojos.DataPrivateUser;
import gal.boris.compluacoge.data.pojos.DataPublicUser;
import gal.boris.compluacoge.extras.Box;
import gal.boris.compluacoge.logic.CloudDB;

public class ComponentsUser {

    private DataPublicUser dataPublicUser;
    private DataPrivateUser dataPrivateUser;
    private Map<String,Map<String,Map<String,DataAProcedure>>> dataAproceduresMap; //idInst -> idProcedure -> versionProcedure(s) -> dataAProcedure

    private ListenerRegistration listenerPublicUser;
    private String idPublicUser;

    private final FirebaseFirestore db;
    private final Map<String, ListenerRegistration> connections; //idInstitution //connections and dataAProcedureMap have all the institutions
    private final Map<String, PublicInstitution> publicInstitutions; //idInstitution
    private final Map<String,Map<String,Map<String,AProcedure>>> aproceduresMap; //idInst -> idProc -> versionProc -> aProc
    private final MutableLiveData<Pair<PrivateUser, PublicUser>> result;

    public ComponentsUser(FirebaseFirestore db) {
        this.dataAproceduresMap = new HashMap<>();

        this.db = db;
        this.connections = new HashMap<>();
        this.publicInstitutions = new HashMap<>();

        this.aproceduresMap = new HashMap<>();
        PublicUser publicUser = new PublicUser(DataPublicUser.createEmpty());
        PrivateUser privateUser = new PrivateUser(DataPrivateUser.createEmpty(), new HashMap<>());
        this.result = new MutableLiveData<>(new Pair<>(privateUser,publicUser));

        clear();
    }

    public void clear() {
        this.dataPublicUser = DataPublicUser.createEmpty();
        this.dataPrivateUser = DataPrivateUser.createEmpty();
        this.dataAproceduresMap.clear();

        if(this.listenerPublicUser != null) {
            this.listenerPublicUser.remove();
            this.listenerPublicUser = null;
        }
        this.idPublicUser = "";

        disconnectInstitutions(new HashSet<>(connections.keySet())); //Hace updateResult
    }

    public void update(DataPrivateUser dataPrivateUser, Map<String,Map<String,Map<String,DataAProcedure>>> newDataAproceduresMap) {
        this.dataPrivateUser = dataPrivateUser;
        this.dataAproceduresMap = newDataAproceduresMap;

        //Conseguimos instsToAdd e instsToRemove
        Set<String> newInsts = new HashSet<>(newDataAproceduresMap.keySet());
        Set<String> oldInsts = new HashSet<>(connections.keySet());
        Set<String> instsToAdd = new HashSet<>(newInsts);
        instsToAdd.removeAll(oldInsts);
        Set<String> instsToRemove = new HashSet<>(oldInsts);
        instsToRemove.removeAll(newInsts);

        disconnectInstitutions(instsToRemove); //Hace updateResult
        connectPublicUser();
        connectInstitutions(instsToAdd);
    }

    private void connectPublicUser() {
        if(!idPublicUser.equals(dataPrivateUser.getID())) {
            if(listenerPublicUser != null) {
                listenerPublicUser.remove();
            }
            idPublicUser = dataPrivateUser.getID();
            final DocumentReference docRef = db.collection(CloudDB.DOC_PUBLIC_ACCOUNTS).document(idPublicUser);
            listenerPublicUser = docRef.addSnapshotListener((snapshot,e) -> {
                if(e != null || snapshot==null || !snapshot.exists()) {
                    dataPublicUser = DataPublicUser.createEmpty();
                    updatePublicUser();
                    return;
                }
                Map<String, Object> data = snapshot.getData();
                Box<Boolean> dataMissing = new Box<>(false);
                dataPublicUser = DataPublicUser.parse(data,dataMissing);
                if(dataMissing.get()) {
                    docRef.set(dataPublicUser.toMap(), SetOptions.merge());
                }
                updatePublicUser();
            });
        }
    }

    /*
    private static Map<String,Set<Pair<String,String>>> getInstToIdVersionProcs(Map<String,Map<String,Map<String,DataAProcedure>>> map) {
        Map<String,Set<Pair<String,String>>> result = new HashMap<>();
        for(Map.Entry<String,Map<String,Map<String,DataAProcedure>>> entryInst : map.entrySet()) {
            String idInst = entryInst.getKey();
            Set<Pair<String,String>> set = new HashSet<>();
            for(Map.Entry<String,Map<String,DataAProcedure>> entryIdProc : entryInst.getValue().entrySet()) {
                for(Map.Entry<String,DataAProcedure> entryVersionProc : entryIdProc.getValue().entrySet()) {
                    set.add(new Pair<>(entryIdProc.getKey(),entryVersionProc.getKey()));
                }
            }
            result.put(idInst,set);
        }
        return result;
    }
     */

    private void connectInstitutions(Set<String> instsToAdd) {
        for(String idInst : instsToAdd) {
            if(connections.get(idInst) != null) { //No deberia ocurrir
                FirebaseCrashlytics.getInstance().recordException(new IllegalStateException("Habia conexion con institucion nueva"));
                connections.get(idInst).remove();
                connections.remove(idInst);
            }
            final DocumentReference docRef = db.collection(CloudDB.DOC_PUBLIC_ACCOUNTS).document(idInst);
            connections.put(idInst,docRef.addSnapshotListener((snapshot,e) -> {
                if(e != null || snapshot==null || !snapshot.exists()) {
                    publicInstitutions.remove(idInst);
                    updateInstitution(Collections.singletonList(idInst));
                    return;
                }
                Map<String, Object> data = snapshot.getData();
                Box<Boolean> dataMissing = new Box<>(false);
                PublicInstitution publicInstitution = new PublicInstitution(data,dataMissing);
                if(dataMissing.get()) {
                    docRef.set(publicInstitution.toMap(), SetOptions.merge());
                }
                publicInstitutions.put(idInst,publicInstitution);
                updateInstitution(Collections.singletonList(idInst));
            }));
        }
    }

    private void disconnectInstitutions(Set<String> instsToRemove) {
        for(String idInst : instsToRemove) {
            connections.get(idInst).remove();
            connections.remove(idInst);
            publicInstitutions.remove(idInst);
            aproceduresMap.remove(idInst);
        }
        updateResult();
    }

    private void updatePublicUser() {
        updateInstitution(connections.keySet());
    }

    private void updateInstitution(Collection<String> idInstitutionUpdatedList) {
        for(String idInstitutionUpdated : idInstitutionUpdatedList) {
            aproceduresMap.remove(idInstitutionUpdated);
            if(publicInstitutions.containsKey(idInstitutionUpdated)) { //Downloaded
                PublicInstitution publicInst = publicInstitutions.get(idInstitutionUpdated);
                for(Map.Entry<String,Map<String,DataAProcedure>> entryIdProc : dataAproceduresMap.get(idInstitutionUpdated).entrySet()) {
                    for(Map.Entry<String,DataAProcedure> entryVersionProc: entryIdProc.getValue().entrySet()) {
                        DataAProcedure dap = entryVersionProc.getValue();
                        Procedure proc = publicInst.getProcVisible(entryIdProc.getKey(),entryVersionProc.getKey());
                        if(dap!=null && proc!=null) {
                            if(!aproceduresMap.containsKey(idInstitutionUpdated)) {
                                aproceduresMap.put(idInstitutionUpdated,new HashMap<>());
                            }
                            Map<String, Map<String, AProcedure>> instAProcMap = aproceduresMap.get(idInstitutionUpdated);
                            if(!instAProcMap.containsKey(entryIdProc.getKey())) {
                                instAProcMap.put(entryIdProc.getKey(),new HashMap<>());
                            }
                            instAProcMap.get(entryIdProc.getKey()).put(entryVersionProc.getKey(),new AProcedure(dap,proc,publicInst));
                        }
                    }
                }
            }
        }
        updateResult();
    }

    public LiveData<Pair<PrivateUser,PublicUser>> getResult() {
        return result;
    }

    private void updateResult() {
        PrivateUser privateUser = new PrivateUser(dataPrivateUser, aproceduresMap);
        PublicUser publicUser = new PublicUser(dataPublicUser);
        this.result.setValue(new Pair<>(privateUser,publicUser));
    }

}
