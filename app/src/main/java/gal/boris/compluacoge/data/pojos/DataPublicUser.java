package gal.boris.compluacoge.data.pojos;

import androidx.annotation.NonNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import gal.boris.compluacoge.extras.Box;

public class DataPublicUser {

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
    public static final String KEY_IS_SOCIAL_WORKER = "isSocialWorker";
    private boolean isSocialWorker;

    private DataPublicUser() {}

    public static DataPublicUser parse(@NonNull Map<String, Object> data, @NonNull Box<Boolean> dataMissing) {
        DataPublicUser user = new DataPublicUser();

        String name = "User";
        if(data.containsKey(KEY_NAME)) {
            name = ((String) data.get(KEY_NAME));
        }
        else {
            dataMissing.set(true);
        }
        user.setName(name);

        String description = "This is a description";
        if(data.containsKey(KEY_PUBLIC_DESCRIP)) {
            description = ((String) data.get(KEY_PUBLIC_DESCRIP));
        }
        else {
            dataMissing.set(true);
        }
        user.setPublicDescription(description);

        String publicID = "id_user";
        if(data.containsKey(KEY_ID)) {
            publicID = ((String) data.get(KEY_ID));
        }
        else {
            dataMissing.set(true);
        }
        user.setID(publicID);

        Long created = 0L;
        if(data.containsKey(KEY_CREATED)) {
            created = ((Long) data.get(KEY_CREATED));
        }
        else {
            dataMissing.set(true);
        }
        user.setCreated(created);

        Long lastModified = 0L;
        if(data.containsKey(KEY_LAST_MODIFIED)) {
            lastModified = ((Long) data.get(KEY_LAST_MODIFIED));
        }
        else {
            dataMissing.set(true);
        }
        user.setLastModified(lastModified);

        boolean isSocialWorker = false;
        if(data.containsKey(KEY_IS_SOCIAL_WORKER)) {
            isSocialWorker = ((Boolean) data.get(KEY_IS_SOCIAL_WORKER));
        }
        else {
            dataMissing.set(true);
        }
        user.setSocialWorker(isSocialWorker);

        return user;
    }

    public static DataPublicUser createEmpty() {
        return parse(new HashMap<>(),new Box<>());
    }

    public Map<String,Object> toMap() {
        Map<String,Object> data = new HashMap<>();
        data.put(KEY_NAME, name);
        data.put(KEY_PUBLIC_DESCRIP, publicDescription);
        data.put(KEY_ID, ID);
        data.put(KEY_CREATED, created);
        data.put(KEY_LAST_MODIFIED, lastModified);
        data.put(KEY_IS_SOCIAL_WORKER, isSocialWorker);
        return data;
    }

    // --- Getters ---

    public String getName() {
        return name;
    }

    public String getPublicDescription() {
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

    public boolean isSocialWorker() {
        return isSocialWorker;
    }

    // --- Setters ---

    private void setName(String name) {
        this.name = name;
    }

    private void setPublicDescription(String publicDescription) {
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

    private void setSocialWorker(boolean socialWorker) {
        isSocialWorker = socialWorker;
    }

    // --- Equals ---

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DataPublicUser that = (DataPublicUser) o;
        return Objects.equals(name, that.name) &&
                Objects.equals(publicDescription, that.publicDescription) &&
                Objects.equals(ID, that.ID) &&
                Objects.equals(created, that.created) &&
                Objects.equals(lastModified, that.lastModified) &&
                Objects.equals(isSocialWorker, that.isSocialWorker);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, publicDescription, ID, created, lastModified, isSocialWorker);
    }
}
