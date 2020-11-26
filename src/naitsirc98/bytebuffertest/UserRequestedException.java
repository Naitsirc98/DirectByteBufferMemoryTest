package naitsirc98.bytebuffertest;

public class UserRequestedException extends RuntimeException {

    public UserRequestedException(String message) {
        super(message);
    }

    public UserRequestedException(String message, Throwable cause) {
        super(message, cause);
    }
}
