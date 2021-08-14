package gal.boris.compluacoge.data.pojos;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import gal.boris.compluacoge.extras.Box;

public class DataPublicInstitution {

    public static final String KEY_NAME = "name";
    private String name;
    public static final String KEY_PUBLIC_DESCRIP = "publicDescription";
    private String publicDescription;
    public static final String KEY_ID = "ID";
    private String ID;
    public static final String KEY_CREATED = "created";
    private Long created;
    public static final String KEY_LAST_MODIFIED = "lastModified";
    private Long lastModified;

    public static final String KEY_PROC_VISIBLES = "procVisibles";
    private Map<String,Map<String,Object>> procVisibles; //id -> version -> map procedure
    private static final String KEY_ID_OPEN_QUESTIONS = "idOpenQuestions";
    private List<String> idOpenQuestions;

    public static final String KEY_STEPS_VISIBLES = "stepsVisibles";
    private Map<String,Object> stepsVisibles; //id_step -> steps with questions and answers

    public static final String KEY_PUBLIC_USERS = "publicUsers";
    private Map<String,Object> publicUsers;

    private DataPublicInstitution() {}

    public static DataPublicInstitution createEmpty() {
        return parse(new HashMap<>(),new Box<>());
    }

    public static DataPublicInstitution parse(@NonNull Map<String, Object> data, @NonNull Box<Boolean> dataMissing) {
        DataPublicInstitution institution = new DataPublicInstitution();

        String name = "Institution";
        if(data.containsKey(KEY_NAME)) {
            name = ((String) data.get(KEY_NAME));
        }
        else {
            dataMissing.set(true);
        }
        institution.setName(name);

        String publicDescription = "This is a description";
        if(data.containsKey(KEY_PUBLIC_DESCRIP)) {
            publicDescription = ((String) data.get(KEY_PUBLIC_DESCRIP));
        }
        else {
            dataMissing.set(true);
        }
        institution.setDescription(publicDescription);

        String publicID = "id_institution";
        if(data.containsKey(KEY_ID)) {
            publicID = ((String) data.get(KEY_ID));
        }
        else {
            dataMissing.set(true);
        }
        institution.setID(publicID);

        Long created = 0L;
        if(data.containsKey(KEY_CREATED)) {
            created = ((Long) data.get(KEY_CREATED));
        }
        else {
            dataMissing.set(true);
        }
        institution.setCreated(created);

        Long lastModified = 0L;
        if(data.containsKey(KEY_LAST_MODIFIED)) {
            lastModified = ((Long) data.get(KEY_LAST_MODIFIED));
        }
        else {
            dataMissing.set(true);
        }
        institution.setLastModified(lastModified);

        Map<String,Map<String,Object>> procVisibles = new HashMap<>();
        if(data.containsKey(KEY_PROC_VISIBLES)) {
            procVisibles = ((Map<String,Map<String,Object>>) data.get(KEY_PROC_VISIBLES));
        }
        else {
            dataMissing.set(true);
        }
        institution.setProcVisibles(procVisibles);

        List<String> idOpenQuestions = new ArrayList<>();
        if(data.containsKey(KEY_ID_OPEN_QUESTIONS)) {
            idOpenQuestions = ((List<String>) data.get(KEY_ID_OPEN_QUESTIONS));
        }
        else {
            dataMissing.set(true);
        }
        institution.setIdOpenQuestions(idOpenQuestions);

        Map<String,Object> stepsVisibles = new HashMap<>();
        if(data.containsKey(KEY_STEPS_VISIBLES)) {
            stepsVisibles = ((Map<String,Object>) data.get(KEY_STEPS_VISIBLES));
        }
        else {
            dataMissing.set(true);
        }
        institution.setStepsVisibles(stepsVisibles);

        Map<String,Object> publicUsers = new HashMap<>();
        if(data.containsKey(KEY_PUBLIC_USERS)) {
            publicUsers = ((Map<String,Object>) data.get(KEY_PUBLIC_USERS));
        }
        else {
            dataMissing.set(true);
        }
        institution.setPublicUsers(publicUsers);

        return institution;
    }

    public Map<String,Object> toMap() {
        Map<String,Object> data = new HashMap<>();
        data.put(KEY_NAME, name);
        data.put(KEY_PUBLIC_DESCRIP, publicDescription);
        data.put(KEY_ID, ID);
        data.put(KEY_CREATED, created);
        data.put(KEY_LAST_MODIFIED, lastModified);
        data.put(KEY_PROC_VISIBLES, procVisibles);
        data.put(KEY_ID_OPEN_QUESTIONS, idOpenQuestions);
        data.put(KEY_STEPS_VISIBLES, stepsVisibles);
        data.put(KEY_PUBLIC_USERS, publicUsers);
        return data;
    }

    // --- Getters ---

    public String getName() {
        return name;
    }

    public String getDescription() {
        return publicDescription;
    }

    public String getID() {
        return ID;
    }

    public Long getCreated() {
        return created;
    }

    public Long getLastModified() {
        return lastModified;
    }

    public Map<String, Map<String,Object>> getProcVisibles() {
        return procVisibles;
    }

    public List<String> getIdOpenQuestions() {
        return idOpenQuestions;
    }

    public Map<String, Object> getStepsVisibles() {
        return stepsVisibles;
    }

    public Map<String, Object> getPublicUsers() {
        return publicUsers;
    }

    // --- Setters ---

    private void setName(String name) {
        this.name = name;
    }

    private void setDescription(String publicDescription) {
        this.publicDescription = publicDescription;
    }

    private void setID(String ID) {
        this.ID = ID;
    }

    private void setCreated(Long created) {
        this.created = created;
    }

    private void setLastModified(Long lastModified) {
        this.lastModified = lastModified;
    }

    private void setProcVisibles(Map<String, Map<String,Object>> procVisibles) {
        this.procVisibles = procVisibles;
    }

    private void setIdOpenQuestions(List<String> idOpenQuestions) {
        this.idOpenQuestions = idOpenQuestions;
    }

    private void setStepsVisibles(Map<String, Object> stepsVisibles) {
        this.stepsVisibles = stepsVisibles;
    }

    public void setPublicUsers(Map<String, Object> publicUsers) {
        this.publicUsers = publicUsers;
    }

    // --- Equals ---

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DataPublicInstitution that = (DataPublicInstitution) o;
        return Objects.equals(name, that.name) &&
                Objects.equals(publicDescription, that.publicDescription) &&
                Objects.equals(ID, that.ID) &&
                Objects.equals(created, that.created) &&
                Objects.equals(lastModified, that.lastModified) &&
                Objects.equals(procVisibles, that.procVisibles) &&
                Objects.equals(idOpenQuestions, that.idOpenQuestions) &&
                Objects.equals(stepsVisibles, that.stepsVisibles) &&
                Objects.equals(publicUsers, that.publicUsers);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, publicDescription, ID, created, lastModified, procVisibles, idOpenQuestions, stepsVisibles, publicUsers);
    }
}