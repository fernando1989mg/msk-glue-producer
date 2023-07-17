package cl.tbk.msk.producer;

import org.apache.avro.AvroTypeException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

@ControllerAdvice
public class ExceptionController {

    @ExceptionHandler(AvroTypeException.class)
    @ResponseBody
    public ResponseEntity<ErrorResponse> handleAvroTypeException(AvroTypeException ex) {
        return new ResponseEntity<>(new ErrorResponse("El formato del json no es válido: " + ex.getMessage()), HttpStatus.BAD_REQUEST);
    }
    
    @ExceptionHandler(Throwable.class)
    @ResponseBody
    public ResponseEntity<ErrorResponse> throwableException(Throwable ex) {
    	ex.printStackTrace();
        return new ResponseEntity<>(new ErrorResponse("Error inesperado al enviar el mensaje: " + ex.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
