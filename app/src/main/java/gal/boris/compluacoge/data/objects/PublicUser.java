package gal.boris.compluacoge.data.objects;

import java.util.Objects;

import gal.boris.compluacoge.data.pojos.DataPublicUser;

public class PublicUser {

    private final DataPublicUser publicData;

    public PublicUser(DataPublicUser publicData) {
        this.publicData = publicData;
    }

    // --- Getters ---

    public String getName() {
        return publicData.getName();
    }

    public String getPublicDescription() {
        return publicData.getPublicDescription();
    }

    public Long getCreated() {
        return publicData.getCreated();
    }

    public Long getLastModified() {
        return publicData.getLastModified();
    }

    public Boolean isSocialWorker() {
        return publicData.isSocialWorker();
    }

    // --- Equals ---

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PublicUser that = (PublicUser) o;
        return Objects.equals(publicData, that.publicData);
    }

    @Override
    public int hashCode() {
        return Objects.hash(publicData);
    }
}
