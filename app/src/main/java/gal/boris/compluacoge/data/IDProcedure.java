package gal.boris.compluacoge.data;

public interface IDProcedure {

    String getIdProcedure();

    String getVersionProcedure();

    default boolean idEquals(IDProcedure other) {
        return this.getIdProcedure().equals(other.getIdProcedure()) &&
                this.getVersionProcedure().equals(other.getVersionProcedure());
    }

}
