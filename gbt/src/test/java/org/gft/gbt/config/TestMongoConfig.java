package org.gft.gbt.config;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoClientDatabaseFactory;
import org.springframework.data.mongodb.core.convert.MappingMongoConverter;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;

import static org.mockito.Mockito.mock;

/**
 * Configuración de prueba para MongoDB.
 * Proporciona beans mockeados para las pruebas unitarias.
 */
@TestConfiguration
@Profile("test")
public class TestMongoConfig {

    @Bean
    @Primary
    public MongoTemplate mongoTemplate() {
        return mock(MongoTemplate.class);
    }

    @Bean
    @Primary
    public MongoDatabaseFactory mongoDbFactory() {
        return mock(MongoDatabaseFactory.class);
    }

    @Bean
    @Primary
    public MappingMongoConverter mappingMongoConverter() {
        return mock(MappingMongoConverter.class);
    }

    @Bean
    @Primary
    public MongoCustomConversions customConversions() {
        return mock(MongoCustomConversions.class);
    }
}
