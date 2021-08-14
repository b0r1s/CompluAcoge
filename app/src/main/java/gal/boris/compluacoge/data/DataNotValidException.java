package gal.boris.compluacoge.data;

public class DataNotValidException extends Exception {

    public DataNotValidException() {
        super();
    }

    public DataNotValidException(String message) {
        super(message);
    }

    public DataNotValidException(String message, Throwable cause) {
        super(message, cause);
    }

    public DataNotValidException(Throwable cause) {
        super(cause);
    }
}
