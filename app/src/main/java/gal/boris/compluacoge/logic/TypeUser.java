package gal.boris.compluacoge.logic;

public enum TypeUser {
    USER("user"), INSTITUTION("institution"), SOCIAL_WORKER("social_worker");

    private final String name;

    TypeUser(String name) {
        this.name = name;
    }

    public boolean isAKindOfUser() {
        return this == USER || this == SOCIAL_WORKER;
    }

    public static TypeUser parse(String type) {
        if(USER.toString().equals(type)) {
            return USER;
        } else if(INSTITUTION.toString().equals(type)) {
            return INSTITUTION;
        } else if(SOCIAL_WORKER.toString().equals(type)) {
            return SOCIAL_WORKER;
        }
        return null;
    }

    @Override
    public String toString() {
        return name;
    }
}
