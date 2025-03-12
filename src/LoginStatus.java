import java.io.Serializable;

public enum LoginStatus implements Serializable {
    SUCCESS,
    FAIL,
    USER_NOT_FOUND,
    INVALID_CREDENTIALS,
    ALREADY_LOGGED_IN
}
