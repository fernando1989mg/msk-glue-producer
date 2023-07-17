package cl.tbk.msk.producer;


import java.io.IOException;

import org.apache.avro.Schema;
import org.apache.avro.generic.GenericDatumReader;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.io.Decoder;
import org.apache.avro.io.DecoderFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.kafka.support.SendResult;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFutureCallback;

import software.amazon.awssdk.auth.credentials.WebIdentityTokenFileCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.glue.GlueClient;
import software.amazon.awssdk.services.glue.model.GetSchemaVersionRequest;
import software.amazon.awssdk.services.glue.model.GetSchemaVersionResponse;
import software.amazon.awssdk.services.glue.model.SchemaId;
import software.amazon.awssdk.services.glue.model.SchemaVersionNumber;

@Service
@PropertySource("classpath:kafka.properties")
public class KafkaProducer {
	
    @Value("${kafka.topic}")
    private String topic;
    
    @Value("${schema.version}")
    private String schemaVersion;
    
    @Value("${registry.name}")
    private String registryName;

    @Value("${schema.name}")
    private String schemaName;
    
    @Value("${aws.region}")
    private String awsRegion;

    private final KafkaTemplate<String, GenericRecord> kafkaTemplate;

    public KafkaProducer(KafkaTemplate<String, GenericRecord> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public SuccessResponse sendMessage(String message) {

    	try {
    		
    		Schema.Parser parser = new Schema.Parser();
            Schema schema = parser.parse(this.getSchemaFromRegistry());

	    	// convierte JSON string a GenericRecord para validar contra el schema
	    	Decoder decoder = DecoderFactory.get().jsonDecoder(schema, message);
	    	GenericDatumReader<GenericRecord> reader = new GenericDatumReader<>(schema);
	    	GenericRecord avroRecord = reader.read(null, decoder);

	        Message<GenericRecord> kafkaMessage = MessageBuilder
	                .withPayload(avroRecord)
	                .setHeader(KafkaHeaders.TOPIC, this.topic)
	                .build();
	
	        kafkaTemplate.send(kafkaMessage)
	                .addCallback(new ListenableFutureCallback<>() {
	                    @Override
	                    public void onSuccess(SendResult<String, GenericRecord> result) {
	                        System.out.println("Sent message=[" + result.getProducerRecord().value() + 
	                                "] with offset=["+ result.getRecordMetadata().offset() +"]");
	                    }
	
	                    @Override
	                    public void onFailure(Throwable ex) {
	                        System.out.println("Unable to send message=[" + message + "] due to : " +
	                                ex.getMessage());
	                        
	                        ex.printStackTrace();
	                    }
	                });
	        
	        return SuccessResponse.OK;
        
    	} catch (IOException e) {
    		//controlamos la excepción para que maneje el controlleradvice con un mensaje controlado
			throw new RuntimeException(e);
		}
    }
    
    private String getSchemaFromRegistry() {
    	
    	GlueClient glue = GlueClient.builder()
    		    .region(Region.of(this.awsRegion))
    		    .credentialsProvider(WebIdentityTokenFileCredentialsProvider.create())
    		    .build();

        GetSchemaVersionRequest request = GetSchemaVersionRequest.builder()
                .schemaId(
                        SchemaId.builder()
                                .registryName(this.registryName)
                                .schemaName(this.schemaName)
                                .build()
                )
                .schemaVersionNumber(
                        SchemaVersionNumber.builder()
                                .versionNumber(Long.valueOf(this.schemaVersion))
                                .build()
                )
                .build();

        GetSchemaVersionResponse response = glue.getSchemaVersion(request);

        return response.schemaDefinition();
    }
}
