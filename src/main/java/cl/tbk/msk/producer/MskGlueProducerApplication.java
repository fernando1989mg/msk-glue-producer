package cl.tbk.msk.producer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController
public class MskGlueProducerApplication {

	public static void main(String[] args) {
		SpringApplication.run(MskGlueProducerApplication.class, args);
	}
	
    @GetMapping("/health")
    public String getMessages() {
        return "200!";
    }

}
