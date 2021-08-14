package gal.boris.compluacoge.data.pojos;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import gal.boris.compluacoge.extras.Box;

public class DataPrivateUser  {

    public static final String KEY_ID = "ID";
    private String ID;
    public static final String KEY_EMAIL = "email";
    private String email;
    public static final String KEY_PRIVATE_KEYWORDS = "privateKeywords";
    private List<String> privateKeywords;
    public static final String KEY_APROCEDURES = "aproceduresMap";
    private Map<String, Map<String, Map<String, Object>>> aproceduresMap; //idProc -> version -> map aprocedure
    public static final String KEY_NUMBER_SOCIAL_WORKER = "numberSocialWorker";
    private String numberSocialWorker;
    private static final String KEY_MODERATOR_INST = "moderatorOfInstitutions";
    private Set<String> moderatorOfInstitutions;
    private static final String KEY_EXPERT_PROC = "expertInProc";
    private Set<String> expertInProc;

    private DataPrivateUser() {}

    public static DataPrivateUser parse(@NonNull Map<String, Object> data, @NonNull Box<Boolean> dataMissing) {
        DataPrivateUser user = new DataPrivateUser();

        String publicID = "id_user";
        if(data.containsKey(KEY_ID)) {
            publicID = ((String) data.get(KEY_ID));
        }
        else {
            dataMissing.set(true);
        }
        user.setID(publicID);

        String email = "email";
        if(data.containsKey(KEY_EMAIL)) {
            email = ((String) data.get(KEY_EMAIL));
        }
        else {
            dataMissing.set(true);
        }
        user.setEmail(email);

        List<String> privateKeywords = new ArrayList<>();
        if(data.containsKey(KEY_PRIVATE_KEYWORDS)) {
            privateKeywords = ((List<String>) data.get(KEY_PRIVATE_KEYWORDS));
        }
        else {
            dataMissing.set(true);
        }
        user.setPrivateKeywords(privateKeywords);

        Map<String, Map<String, Map<String, Object>>> aproceduresMap = new HashMap<>();
        if(data.containsKey(KEY_APROCEDURES)) {
            aproceduresMap = ((Map<String, Map<String, Map<String, Object>>>) data.get(KEY_APROCEDURES));
        }
        else {
            dataMissing.set(true);
        }
        user.setAproceduresMap(aproceduresMap);

        String numberSocialWorker = "";
        if(data.containsKey(KEY_NUMBER_SOCIAL_WORKER)) {
            numberSocialWorker = ((String) data.get(KEY_NUMBER_SOCIAL_WORKER));
        }
        else {
            dataMissing.set(true);
        }
        user.setNumberSocialWorker(numberSocialWorker);

        List<String> moderatorOfInstitutions = new ArrayList<>();
        if(data.containsKey(KEY_MODERATOR_INST)) {
            moderatorOfInstitutions = ((List<String>) data.get(KEY_MODERATOR_INST));
        }
        else {
            dataMissing.set(true);
        }
        user.setModeratorOfInstitutions(new HashSet<>(moderatorOfInstitutions));

        List<String> expertInProc = new ArrayList<>();
        if(data.containsKey(KEY_EXPERT_PROC)) {
            expertInProc = ((List<String>) data.get(KEY_EXPERT_PROC));
        }
        else {
            dataMissing.set(true);
        }
        user.setExpertInProc(new HashSet<>(expertInProc));

        return user;
    }

    public static DataPrivateUser createEmpty() {
        return parse(new HashMap<>(),new Box<>());
    }

    public Map<String,Object> toMap() {
        Map<String,Object> data = new HashMap<>();
        data.put(KEY_ID, ID);
        data.put(KEY_EMAIL, email);
        data.put(KEY_PRIVATE_KEYWORDS, privateKeywords);
        data.put(KEY_APROCEDURES, aproceduresMap);
        data.put(KEY_NUMBER_SOCIAL_WORKER, numberSocialWorker);
        data.put(KEY_MODERATOR_INST, new ArrayList<>(moderatorOfInstitutions));
        data.put(KEY_EXPERT_PROC, new ArrayList<>(expertInProc));
        return data;
    }

    // --- Getters ---

    public String getID() {
        return ID;
    }

    public String getEmail() {
        return email;
    }

    public List<String> getPrivateKeywords() {
        return privateKeywords;
    }

    public Map<String, Map<String, Map<String, Object>>> getAproceduresMap() {
        return aproceduresMap;
    }

    public String getNumberSocialWorker() {
        return numberSocialWorker;
    }

    public Set<String> getModeratorOfInstitutions() {
        return moderatorOfInstitutions;
    }

    public Set<String> getExpertInProc() {
        return expertInProc;
    }

    // --- Setters ---

    private void setID(String ID) {
        this.ID = ID;
    }

    private void setEmail(String email) {
        this.email = email;
    }

    private void setPrivateKeywords(List<String> privateKeywords) {
        this.privateKeywords = privateKeywords;
    }

    public void setAproceduresMap(Map<String, Map<String, Map<String, Object>>> aproceduresMap) {
        this.aproceduresMap = aproceduresMap;
    }

    private void setNumberSocialWorker(String numberSocialWorker) {
        this.numberSocialWorker = numberSocialWorker;
    }

    private void setModeratorOfInstitutions(Set<String> moderatorOfInstitutions) {
        this.moderatorOfInstitutions = moderatorOfInstitutions;
    }

    private void setExpertInProc(Set<String> expertInProc) {
        this.expertInProc = expertInProc;
    }

}
