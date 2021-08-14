package gal.boris.compluacoge.logic;

public class Repository {

    private final CloudDB cloudDB;

    public Repository(InfoLoginReadOnly infoLoginRO) {
        this.cloudDB = new CloudDB(infoLoginRO);
    }

    public CloudDB getCloudDB() {
        return cloudDB;
    }

}
