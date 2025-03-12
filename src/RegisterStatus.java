import java.io.Serializable;

public enum RegisterStatus implements Serializable {
    SUCCESS,
    FAIL,
    USERNAME_ALREADY_EXISTED,
    LOGIN_FAIL
}
