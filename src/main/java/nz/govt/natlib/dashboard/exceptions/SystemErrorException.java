package nz.govt.natlib.dashboard.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR, reason = "Failed to process")  // 500
public class SystemErrorException extends RuntimeException {
    public SystemErrorException() {
        super();
    }

    public SystemErrorException(String msg) {
        super(msg);
    }
}