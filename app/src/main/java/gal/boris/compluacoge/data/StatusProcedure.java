package gal.boris.compluacoge.data;

public enum StatusProcedure {

    OPEN(0), FINISHED(1), OLD_WITHOUT_FINISH(2);

    private final int integer;

    StatusProcedure(int integer) {
        this.integer = integer;
    }

    public int getInteger() {
        return integer;
    }

    public boolean isOpen() {
        return this.equals(OPEN);
    }


}
