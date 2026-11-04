package madstodolist.service;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class EquipoServiceException extends RuntimeException {
    public EquipoServiceException(String message) {
        super(message);
    }
}
