package gal.boris.compluacoge.logic;

import androidx.core.util.Pair;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import gal.boris.compluacoge.data.ComponentsInstitution;
import gal.boris.compluacoge.data.ComponentsUser;
import gal.boris.compluacoge.data.objects.AProcedure;
import gal.boris.compluacoge.data.objects.PrivateInstitution;
import gal.boris.compluacoge.data.objects.PrivateUser;
import gal.boris.compluacoge.data.objects.Procedure;
import gal.boris.compluacoge.data.objects.PublicInstitution;
import gal.boris.compluacoge.data.objects.PublicUser;
import gal.boris.compluacoge.data.pojos.DataAProcedure;
import gal.boris.compluacoge.data.pojos.DataPrivateInstitution;
import gal.boris.compluacoge.data.pojos.DataPrivateUser;
import gal.boris.compluacoge.extras.Box;
import gal.boris.compluacoge.extras.CombinedORBooleanLiveData;
import gal.boris.compluacoge.extras.SpeedCounter;
import gal.boris.compluacoge.logic.search.SearchIndex;
import gal.boris.compluacoge.logic.search.SearchInstCache;

public class CloudDB {

    public static final String DOC_PRIVATE_ACCOUNTS = "private_accounts";
    public static final String DOC_PUBLIC_ACCOUNTS = "public_accounts";
    public static final String DOC_PENDING_PRIV_ACCOUNTS = "pending_private_accounts";
    public static final String DOC_PENDING_PUB_ACCOUNTS = "pending_public_accounts";
    public static final String DOC_CONFIRM_SW = "confirm_social_worker";
    public static final String DOC_RANDOM = "random";
    public static final String DOC_SEARCH = "search";
    public static final String PROFILE_FILE_NAME = "profile.webp";
    public static final String BACKGROUND_FILE_NAME = "background.webp";
    private final FirebaseFirestore db;
    private ListenerRegistration docUserInfo;
    private ListenerRegistration docUserInfoPending;
    public static final String KEY_TYPE_USER = "typeUser";
    public static final String KEY_VERIFIED = "verified";

    private final InfoLoginReadOnly infoLoginRO;
    private final MutableLiveData<Boolean> connectedUserDB; //Este y los dos de abajo son mutuamente excluyentes
    private final MutableLiveData<Boolean> connectedInstitutionDB;
    private final MutableLiveData<Boolean> pendingDB;
    private final LiveData<Boolean> connectedUserDistinct;
    private final LiveData<Boolean> connectedInstitutionDistinct;
    private final LiveData<Boolean> connectedDB;
    private final LiveData<Boolean> pendingDBDistinct;

    private final ComponentsInstitution dataInstitution;
    private final ComponentsUser dataUser;
    private final SearchInstCache searchInstCache;
    private final SearchIndex searchIndex;

    private final SpeedCounter numReads;

    public CloudDB(InfoLoginReadOnly infoLoginRO) {
        this.db = FirebaseFirestore.getInstance();
        this.docUserInfo = null;
        this.docUserInfoPending = null;

        this.infoLoginRO = infoLoginRO;
        this.connectedUserDB = new MutableLiveData<>(false);
        this.connectedInstitutionDB = new MutableLiveData<>(false);
        this.pendingDB = new MutableLiveData<>(false);
        this.connectedDB = Transformations.distinctUntilChanged(new CombinedORBooleanLiveData(connectedUserDB,connectedInstitutionDB));
        this.connectedUserDistinct = Transformations.distinctUntilChanged(connectedUserDB);
        this.connectedInstitutionDistinct = Transformations.distinctUntilChanged(connectedInstitutionDB);
        this.pendingDBDistinct = Transformations.distinctUntilChanged(pendingDB);

        this.dataInstitution = new ComponentsInstitution(db);
        this.dataUser = new ComponentsUser(db);
        this.searchInstCache = new SearchInstCache(db);
        this.searchIndex = new SearchIndex();

        this.numReads = new SpeedCounter();

        download();
    }

    private LiveData<Boolean> isSignedIn() {
        return infoLoginRO.isSignedIn();
    }

    public LiveData<Boolean> isConnectedDB() {
        return connectedDB;
    }

    public LiveData<Boolean> getConnectedUserDB() {
        return connectedUserDistinct;
    }

    public LiveData<Boolean> getConnectedInstitutionDB() {
        return connectedInstitutionDistinct;
    }

    public LiveData<Boolean> getPendingDB() {
        return pendingDBDistinct;
    }

    public SearchIndex getSearchIndex() {
        return searchIndex;
    }

    public SearchInstCache getSearchInstCache() {
        return searchInstCache;
    }

    public void writeNewUser(String uid, Map<String,Object> dataPrivate, Map<String,Object> dataPublic) {
        TypeUser typeUser = TypeUser.parse((String)dataPrivate.get(CloudDB.KEY_TYPE_USER));
        if(TypeUser.INSTITUTION == typeUser) {
            dataPrivate.put(CloudDB.KEY_VERIFIED, false);
            DocumentReference docRefPublic = db.collection(DOC_PENDING_PUB_ACCOUNTS).document(uid);
            DocumentReference docRefPrivate = db.collection(DOC_PENDING_PRIV_ACCOUNTS).document(uid);
            docRefPublic.set(dataPublic, SetOptions.merge());
            docRefPrivate.set(dataPrivate, SetOptions.merge()); //importante que vaya despues de public en los pending por la Cloud Function
        } else {
            writeExistingUser(uid,dataPrivate,dataPublic);
        }
    }

    public void writeExistingUser(String uid, Map<String,Object> dataPrivate, Map<String,Object> dataPublic) {
        final DocumentReference docRefPrivate = db.collection(DOC_PRIVATE_ACCOUNTS).document(uid);
        final DocumentReference docRefPublic = db.collection(DOC_PUBLIC_ACCOUNTS).document(uid);
        docRefPrivate.set(dataPrivate, SetOptions.merge());
        docRefPublic.set(dataPublic, SetOptions.merge());
        if(dataPrivate.containsKey(DataPrivateUser.KEY_NUMBER_SOCIAL_WORKER)) {
            DocumentReference docRefSW = db.collection(DOC_CONFIRM_SW).document(uid);
            Map<String,Object> mapSW = new HashMap<>();
            mapSW.put(DataPrivateUser.KEY_NUMBER_SOCIAL_WORKER,dataPrivate.get(DataPrivateUser.KEY_NUMBER_SOCIAL_WORKER));
            mapSW.put(CloudDB.KEY_VERIFIED, false);
            mapSW.put(DataPrivateInstitution.KEY_EMAIL, dataPrivate.get(DataPrivateInstitution.KEY_EMAIL)); //me he asegurado de que lo tiene
            docRefSW.set(mapSW, SetOptions.merge());
        }
    }

    public boolean addNewProcDraft(Procedure proc) {
        PrivateInstitution privateInstitution = getDataInstitution().getValue().first;
        String uidPrivate = FirebaseAuth.getInstance().getUid();
        if(getConnectedInstitutionDB().getValue() && privateInstitution!=null &&
                privateInstitution.getID().equals(uidPrivate)) {
            List<String> fieldsToUpdate = privateInstitution.addNewProcDraft(proc);
            Map<String,Object> data = privateInstitution.toMap();
            final DocumentReference docRef = db.collection(DOC_PRIVATE_ACCOUNTS).document(uidPrivate);
            docRef.set(data, SetOptions.mergeFields(fieldsToUpdate));
            return true;
        }
        return false;
    }

    public static String autoId() {
        return FirebaseFirestore.getInstance().collection(DOC_RANDOM).document().getId();
    }

    public boolean deleteDraft(Procedure draft) {
        PrivateInstitution privateInstitution = getDataInstitution().getValue().first;
        String uidPrivate = FirebaseAuth.getInstance().getUid();
        if(getConnectedInstitutionDB().getValue() && privateInstitution!=null &&
                privateInstitution.getID().equals(uidPrivate)) {
            List<String> fieldsToUpdate = privateInstitution.deleteDraft(draft);
            Map<String,Object> data = privateInstitution.toMap();
            final DocumentReference docRef = db.collection(DOC_PRIVATE_ACCOUNTS).document(uidPrivate);
            docRef.set(data, SetOptions.mergeFields(fieldsToUpdate));
            return true;
        }
        return false;
    }

    public boolean updateNewProcDraft(Procedure newDraft, Procedure oldDraft) {
        PrivateInstitution privateInstitution = getDataInstitution().getValue().first;
        String uidPrivate = FirebaseAuth.getInstance().getUid();
        if(getConnectedInstitutionDB().getValue() && privateInstitution!=null &&
                privateInstitution.getID().equals(uidPrivate)) {
            if(newDraft.getIdProcedure().equals(oldDraft.getIdProcedure()) &&
                    newDraft.getVersionProcedure().equals(oldDraft.getVersionProcedure())) {
                List<String> fieldsToUpdate = privateInstitution.deleteDraft(oldDraft);
                fieldsToUpdate.addAll(privateInstitution.addNewProcDraft(newDraft));
                Map<String,Object> data = privateInstitution.toMap();
                final DocumentReference docRef = db.collection(DOC_PRIVATE_ACCOUNTS).document(uidPrivate);
                docRef.set(data, SetOptions.mergeFields(fieldsToUpdate));
                return true;
            }
            return false;
        }
        return false;
    }

    //todo hay algo para asegurar que no se cortan? error historico del sudoku
    public boolean publishDraft(Procedure newDraft, Procedure oldDraft) {
        Pair<PrivateInstitution,PublicInstitution> pair = getDataInstitution().getValue();
        PrivateInstitution privateInstitution = pair.first;
        PublicInstitution publicInstitution = pair.second;
        String uidPrivate = FirebaseAuth.getInstance().getUid();
        String uidPublic = privateInstitution.getID();
        boolean connected = getConnectedInstitutionDB().getValue()!=null &&
                getConnectedInstitutionDB().getValue();
        boolean permission = uidPrivate!=null && uidPublic!=null &&
                privateInstitution!=null && publicInstitution!=null &&
                uidPrivate!=null && uidPublic!=null && !"".equals(uidPublic) &&
                privateInstitution.getID().equals(uidPrivate) &&
                uidPublic.equals(publicInstitution.getID());
        if(!connected || !permission) {
            return false;
        }
        boolean oldDraftOkay = oldDraft==null ||
                (newDraft.getIdProcedure().equals(oldDraft.getIdProcedure()) &&
                newDraft.getVersionProcedure().equals(oldDraft.getVersionProcedure()));
        if(!oldDraftOkay) {
            return false;
        }

        if(oldDraft!=null) {
            List<String> fieldsToUpdate = privateInstitution.deleteDraft(oldDraft);
            Map<String,Object> dataPrivate = privateInstitution.toMap();
            final DocumentReference docRefPrivate = db.collection(DOC_PRIVATE_ACCOUNTS).document(uidPrivate);
            docRefPrivate.set(dataPrivate, SetOptions.mergeFields(fieldsToUpdate));
        }
        newDraft.setVersionProcedure(""+(1+publicInstitution.getCopyVersionsProcVisible(newDraft.getIdProcedure()).stream()
                .map(Procedure::getVersionProcedure)
                .mapToLong(Long::parseLong)
                .max().orElse(0)));
        newDraft.generateWords();
        List<String> fieldsToUpdate = publicInstitution.addNewProc(newDraft);
        Map<String,Object> dataPublic = publicInstitution.toMap();
        final DocumentReference docRefPublic = db.collection(DOC_PUBLIC_ACCOUNTS).document(uidPublic);
        docRefPublic.set(dataPublic, SetOptions.mergeFields(fieldsToUpdate));
        return true;
    }

    public boolean closeAllVisibleVersions(Procedure proc) {
        Pair<PrivateInstitution,PublicInstitution> pair = getDataInstitution().getValue();
        PrivateInstitution privateInstitution = pair.first;
        PublicInstitution publicInstitution = pair.second;
        String uidPrivate = FirebaseAuth.getInstance().getUid();
        String uidPublic = privateInstitution.getID();
        boolean connected = getConnectedInstitutionDB().getValue()!=null &&
                getConnectedInstitutionDB().getValue();
        boolean permission = uidPrivate!=null && uidPublic!=null &&
                privateInstitution!=null && publicInstitution!=null &&
                uidPrivate!=null && uidPublic!=null && !"".equals(uidPublic) &&
                privateInstitution.getID().equals(uidPrivate) &&
                uidPublic.equals(publicInstitution.getID());
        if(!connected || !permission) {
            return false;
        }

        Pair<Boolean,List<String>> changed = publicInstitution.closeVisibleVersions(proc);
        if(changed.first) {
            List<String> fieldsToUpdate = changed.second;
            Map<String,Object> dataPublic = publicInstitution.toMap();
            final DocumentReference docRefPublic = db.collection(DOC_PUBLIC_ACCOUNTS).document(uidPublic);
            docRefPublic.set(dataPublic, SetOptions.mergeFields(fieldsToUpdate));
        }
        return true;
    }

    public boolean addAProc(AProcedure aproc) {
        PrivateUser privateUser = getDataUser().getValue().first;
        String uidUser = FirebaseAuth.getInstance().getUid();
        if(getConnectedUserDB().getValue() && privateUser!=null &&
                privateUser.getID().equals(uidUser)) {
            List<String> fieldsToUpdate = privateUser.addAProc(aproc);
            Map<String,Object> data = privateUser.toMap();
            final DocumentReference docRef = db.collection(DOC_PRIVATE_ACCOUNTS).document(uidUser);
            docRef.set(data, SetOptions.mergeFields(fieldsToUpdate));
            return true;
        }
        return false;
    }

    public boolean deleteAProc(AProcedure aproc) {
        PrivateUser privateUser = getDataUser().getValue().first;
        String uidUser = FirebaseAuth.getInstance().getUid();
        if(getConnectedUserDB().getValue() && privateUser!=null &&
                privateUser.getID().equals(uidUser)) {
            List<String> fieldsToUpdate = privateUser.deleteAProc(aproc);
            Map<String,Object> data = privateUser.toMap();
            final DocumentReference docRef = db.collection(DOC_PRIVATE_ACCOUNTS).document(uidUser);
            docRef.set(data, SetOptions.mergeFields(fieldsToUpdate));
            return true;
        }
        return false;
    }

    public boolean updateAProc(AProcedure newAproc, AProcedure oldAproc) {
        PrivateUser privateUser = getDataUser().getValue().first;
        String uidPrivate = FirebaseAuth.getInstance().getUid();
        if(getConnectedUserDB().getValue() && privateUser!=null &&
                privateUser.getID().equals(uidPrivate)) {
            if(newAproc.getIdProcedure().equals(oldAproc.getIdProcedure()) &&
                    newAproc.getVersionProcedure().equals(oldAproc.getVersionProcedure())) {
                List<String> fieldsToUpdate = privateUser.deleteAProc(oldAproc);
                fieldsToUpdate.addAll(privateUser.addAProc(newAproc));
                Map<String,Object> data = privateUser.toMap();
                final DocumentReference docRef = db.collection(DOC_PRIVATE_ACCOUNTS).document(uidPrivate);
                docRef.set(data, SetOptions.mergeFields(fieldsToUpdate));
                return true;
            }
            return false;
        }
        return false;
    }

    private void download() {
        isSignedIn().observeForever(bool -> {
            String uid = FirebaseAuth.getInstance().getUid();
            if(bool && uid!=null) {
                final DocumentReference docRef = db.collection(DOC_PRIVATE_ACCOUNTS).document(uid);
                final DocumentReference docRefPending = db.collection(DOC_PENDING_PRIV_ACCOUNTS).document(uid);
                if(docUserInfoPending != null) {
                    FirebaseCrashlytics.getInstance().recordException(new IllegalStateException("Deberia haber sido eliminado"));
                    docUserInfoPending.remove();
                }
                docUserInfoPending = docRefPending.addSnapshotListener((snapshot,e) -> {
                    if(numReads.newTick(System.currentTimeMillis())) throw new IllegalStateException();
                    if(e != null || snapshot==null || !snapshot.exists()) {
                        //disconnectDB();
                        return;
                    }
                    pendingDB.setValue(true);
                });
                if(docUserInfo !=null) {
                    FirebaseCrashlytics.getInstance().recordException(new IllegalStateException("Deberia haber sido eliminado"));
                    docUserInfo.remove();
                }
                docUserInfo = docRef.addSnapshotListener((snapshot,e) -> {
                    if(numReads.newTick(System.currentTimeMillis())) throw new IllegalStateException();
                    if(e != null || snapshot==null || !snapshot.exists()) {
                        //disconnectDB();
                        return;
                    }
                    pendingDB.setValue(false);
                    Map<String, Object> data = snapshot.getData();

                    TypeUser typeUser = null;
                    if(data.containsKey(KEY_TYPE_USER)) {
                        typeUser = TypeUser.parse((String) data.get(KEY_TYPE_USER));
                    }
                    if(typeUser == null) {
                        throw new IllegalArgumentException();
                    }

                    Box<Boolean> dataMissing = new Box<>(false);
                    if(typeUser.isAKindOfUser()) {
                        DataPrivateUser dataPrivateUser = DataPrivateUser.parse(data,dataMissing);
                        Map<String, Map<String, Map<String, DataAProcedure>>> aprocedures =
                                DataAProcedure.parseMultiple(dataPrivateUser.getAproceduresMap(),dataMissing);
                        if(dataMissing.get()) {
                            dataPrivateUser.setAproceduresMap(DataAProcedure.toMapMultiple(aprocedures));
                            docRef.set(dataPrivateUser.toMap(),SetOptions.merge());
                        }
                        dataUser.update(dataPrivateUser,aprocedures);
                    }
                    else if(typeUser.equals(TypeUser.INSTITUTION)){
                        PrivateInstitution privateInstitution = new PrivateInstitution(data,dataMissing);
                        if(dataMissing.get()) {
                            docRef.set(privateInstitution.toMap(),SetOptions.merge());
                        }
                        dataInstitution.update(privateInstitution);
                    }

                    infoLoginRO.setTypeUser(typeUser);
                    setConnectedDB(typeUser);
                });
            }
            else {
                disconnectDB();
                pendingDB.setValue(false);
            }
        });

        db.collection(DOC_SEARCH).addSnapshotListener((snapshot,e) -> {
            if(e != null || snapshot==null || snapshot.isEmpty()) {
                return;
            }
            Map<String,Object> data = snapshot.getDocuments().stream()
                    .flatMap(docSnap -> docSnap.getData().entrySet().stream())
                    .collect(Collectors.toMap(Map.Entry::getKey,Map.Entry::getValue, (oldMap,newMap)->newMap));
            searchIndex.updateTables(data);
        });
    }

    public LiveData<Pair<PrivateInstitution, PublicInstitution>> getDataInstitution() {
        return dataInstitution.getResult();
    }

    public LiveData<Pair<PrivateUser, PublicUser>> getDataUser() {
        return dataUser.getResult();
    }

    private void setConnectedDB(TypeUser typeUser) {
        connectedInstitutionDB.setValue(false);
        connectedUserDB.setValue(false);

        if(typeUser.isAKindOfUser()) {
            connectedUserDB.setValue(true);
        }
        else if(typeUser.equals(TypeUser.INSTITUTION)){
            connectedInstitutionDB.setValue(true);
        }
    }

    public void disconnectDB() {
        connectedInstitutionDB.setValue(false);
        connectedUserDB.setValue(false);

        dataInstitution.clear();
        dataUser.clear();

        if(docUserInfo !=null) {
            docUserInfo.remove();
            docUserInfo = null;
        }
        if(docUserInfoPending != null) {
            docUserInfoPending.remove();
            docUserInfoPending = null;
        }
    }
}
