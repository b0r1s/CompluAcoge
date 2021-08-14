package gal.boris.compluacoge.data.pojos;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import gal.boris.compluacoge.extras.Box;

public class DataPrivateInstitution {

    public static final String KEY_PROC_HIDDEN = "procHidden";
    private Map<String,Map<String,Object>> procHidden;//id -> version -> map procedure
    public static final String KEY_ID = "ID";
    private String ID;
    public static final String KEY_EMAIL = "email";
    private String email;

    private static final String KEY_MODERATORS = "moderators";
    private Set<String> moderators;
    private static final String KEY_ASK_FOR_MOD = "askingForBeingModerator";
    private Set<String> askingForBeingModerator;
    private static final String KEY_PROC_EXPERTS = "proceduresExperts";
    private Map<String,Set<String>> proceduresExperts;
    private static final String KEY_ASK_FOR_EXP = "askingForBeingExpert";
    private Map<String,Set<String>> askingForBeingExpert;

    public static final String KEY_STEPS_HIDDEN = "stepsHidden";
    private Map<String,Object> stepsHidden; //steps with questions and answers

    private DataPrivateInstitution() {}

    public static DataPrivateInstitution createEmpty() {
        return parse(new HashMap<>(),new Box<>());
    }

    public static DataPrivateInstitution parse(@NonNull Map<String, Object> data, @NonNull Box<Boolean> dataMissing) {
        DataPrivateInstitution institution = new DataPrivateInstitution();

        Map<String,Map<String,Object>> procHidden = new HashMap<>();
        if(data.containsKey(KEY_PROC_HIDDEN)) {
            procHidden = ((Map<String,Map<String,Object>>) data.get(KEY_PROC_HIDDEN));
        }
        else {
            dataMissing.set(true);
        }
        institution.setProcHidden(procHidden);

        String publicID = "id_institution";
        if(data.containsKey(KEY_ID)) {
            publicID = ((String) data.get(KEY_ID));
        }
        else {
            dataMissing.set(true);
        }
        institution.setID(publicID);

        String email = "email";
        if(data.containsKey(KEY_EMAIL)) {
            email = ((String) data.get(KEY_EMAIL));
        }
        else {
            dataMissing.set(true);
        }
        institution.setEmail(email);

        List<String> moderators = new ArrayList<>();
        if(data.containsKey(KEY_MODERATORS)) {
            moderators = ((List<String>) data.get(KEY_MODERATORS));
        }
        else {
            dataMissing.set(true);
        }
        institution.setModerators(new HashSet<>(moderators));

        List<String> askingForBeingModerator = new ArrayList<>();
        if(data.containsKey(KEY_ASK_FOR_MOD)) {
            askingForBeingModerator = ((List<String>) data.get(KEY_ASK_FOR_MOD));
        }
        else {
            dataMissing.set(true);
        }
        institution.setAskingForBeingModerator(new HashSet<>(askingForBeingModerator));

        Map<String,List<String>> proceduresExperts = new HashMap<>();
        if(data.containsKey(KEY_PROC_EXPERTS)) {
            proceduresExperts = ((Map<String,List<String>>) data.get(KEY_PROC_EXPERTS));
        }
        else {
            dataMissing.set(true);
        }
        Map<String,Set<String>> cpyProcExperts = new HashMap<>();
        for(Map.Entry<String,List<String>> entry : proceduresExperts.entrySet()) {
            cpyProcExperts.put(entry.getKey(),new HashSet<>(entry.getValue()));
        }
        institution.setProceduresExperts(cpyProcExperts);

        Map<String,List<String>> askingForBeingExpert = new HashMap<>();
        if(data.containsKey(KEY_ASK_FOR_EXP)) {
            askingForBeingExpert = ((Map<String,List<String>>) data.get(KEY_ASK_FOR_EXP));
        }
        else {
            dataMissing.set(true);
        }
        Map<String,Set<String>> cpyAskForExperts = new HashMap<>();
        for(Map.Entry<String,List<String>> entry : askingForBeingExpert.entrySet()) {
            cpyAskForExperts.put(entry.getKey(),new HashSet<>(entry.getValue()));
        }
        institution.setAskingForBeingExpert(cpyAskForExperts);

        Map<String,Object> stepsHidden = new HashMap<>();
        if(data.containsKey(KEY_STEPS_HIDDEN)) {
            stepsHidden = ((Map<String,Object>) data.get(KEY_STEPS_HIDDEN));
        }
        else {
            dataMissing.set(true);
        }
        institution.setStepsHidden(stepsHidden);

        return institution;
    }

    public Map<String,Object> toMap() {
        Map<String,Object> data = new HashMap<>();
        data.put(KEY_PROC_HIDDEN, procHidden);
        data.put(KEY_ID, ID);
        data.put(KEY_EMAIL, email);
        data.put(KEY_MODERATORS, new ArrayList<>(moderators));
        data.put(KEY_ASK_FOR_MOD, new ArrayList<>(askingForBeingModerator));
        Map<String,List<String>> cpyProcExperts = new HashMap<>();
        for(Map.Entry<String,Set<String>> entry : proceduresExperts.entrySet()) {
            cpyProcExperts.put(entry.getKey(),new ArrayList<>(entry.getValue()));
        }
        data.put(KEY_PROC_EXPERTS, cpyProcExperts);
        Map<String,List<String>> cpyAskForExperts = new HashMap<>();
        for(Map.Entry<String,Set<String>> entry : askingForBeingExpert.entrySet()) {
            cpyAskForExperts.put(entry.getKey(),new ArrayList<>(entry.getValue()));
        }
        data.put(KEY_ASK_FOR_EXP, cpyAskForExperts);
        data.put(KEY_STEPS_HIDDEN, stepsHidden);
        return data;
    }

    // --- Getters ---

    public Map<String, Map<String,Object>> getProcHidden() {
        return procHidden;
    }

    public String getID() {
        return ID;
    }

    public String getEmail() {
        return email;
    }

    public Set<String> getModerators() {
        return moderators;
    }

    public Set<String> getAskingForBeingModerator() {
        return askingForBeingModerator;
    }

    public Map<String, Set<String>> getProceduresExperts() {
        return proceduresExperts;
    }

    public Map<String, Set<String>> getAskingForBeingExpert() {
        return askingForBeingExpert;
    }

    public Map<String, Object> getStepsHidden() {
        return stepsHidden;
    }

    // --- Setters ---

    private void setProcHidden(Map<String, Map<String,Object>> procHidden) {
        this.procHidden = procHidden;
    }

    private void setID(String ID) {
        this.ID = ID;
    }

    private void setEmail(String email) {
        this.email = email;
    }

    private void setModerators(Set<String> moderators) {
        this.moderators = moderators;
    }

    private void setAskingForBeingModerator(Set<String> askingForBeingModerator) {
        this.askingForBeingModerator = askingForBeingModerator;
    }

    private void setProceduresExperts(Map<String, Set<String>> proceduresExperts) {
        this.proceduresExperts = proceduresExperts;
    }

    private void setAskingForBeingExpert(Map<String, Set<String>> askingForBeingExpert) {
        this.askingForBeingExpert = askingForBeingExpert;
    }

    private void setStepsHidden(Map<String, Object> stepsHidden) {
        this.stepsHidden = stepsHidden;
    }
}
