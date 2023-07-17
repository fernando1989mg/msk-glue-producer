package cl.tbk.msk.producer;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class KafkaController {
    private final KafkaProducer producer;

    KafkaController(KafkaProducer producer) {
        this.producer = producer;
    }

    @PostMapping(value = "/produce", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<SuccessResponse>  writeMessageToTopic(@RequestBody String message) {
    	return new ResponseEntity<>(this.producer.sendMessage(message), HttpStatus.CREATED);

    }
}
