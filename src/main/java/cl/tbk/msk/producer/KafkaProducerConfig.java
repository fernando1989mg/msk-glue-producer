package cl.tbk.msk.producer;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.apache.avro.generic.GenericRecord;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

import com.amazonaws.services.schemaregistry.serializers.GlueSchemaRegistryKafkaSerializer;
import com.amazonaws.services.schemaregistry.utils.AWSSchemaRegistryConstants;

import software.amazon.awssdk.services.glue.model.Compatibility;
import software.amazon.awssdk.services.glue.model.DataFormat;

@Configuration
@PropertySource("classpath:kafka.properties")
public class KafkaProducerConfig {
    @Value("${kafka.bootstrap.servers}")
    private String bootstrapServersConfig;

    @Value("${sasl.username}")
    private String saslUsername;

    @Value("${sasl.password}")
    private String saslPassword;
    
    @Value("${aws.region}")
    private String awsRegion;

    @Value("${registry.name}")
    private String registryName;

    @Value("${schema.name}")
    private String schemaName;
   
	
    @Bean
    public ProducerFactory<String, GenericRecord> producerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, this.bootstrapServersConfig);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, GlueSchemaRegistryKafkaSerializer.class);
        
        // Configuración para SASL/SCRAM icon SSL
        props.put("security.protocol", "SASL_SSL");
        props.put("sasl.mechanism", "SCRAM-SHA-512");
        props.put("sasl.jaas.config", String.format("org.apache.kafka.common.security.scram.ScramLoginModule required username=\"%s\" password=\"%s\";", this.saslUsername, this.saslPassword));
        File truststoreFile = new File(getClass().getClassLoader().getResource("kafka.client.truststore.jks").getFile());
        props.put("ssl.truststore.location", truststoreFile.getAbsolutePath());
        //configProps.put("ssl.truststore.password", "truststore-password");
        
        props.put(AWSSchemaRegistryConstants.SCHEMA_AUTO_REGISTRATION_SETTING, true);
        props.put(AWSSchemaRegistryConstants.AWS_REGION, this.awsRegion);
        props.put(AWSSchemaRegistryConstants.REGISTRY_NAME, this.registryName);
        props.put(AWSSchemaRegistryConstants.COMPATIBILITY_SETTING, Compatibility.FULL);
        props.put(AWSSchemaRegistryConstants.DATA_FORMAT, DataFormat.AVRO.name());
        props.put(AWSSchemaRegistryConstants.SCHEMA_NAME, this.schemaName);

        return new DefaultKafkaProducerFactory<>(props);
    }

    @Bean
    public KafkaTemplate<String, GenericRecord> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }
  
}

