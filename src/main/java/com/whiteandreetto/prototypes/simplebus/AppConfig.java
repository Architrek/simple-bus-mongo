package com.whiteandreetto.prototypes.simplebus;


import com.whiteandreetto.prototypes.simplebus.cfg.MongoDBConfiguration;
import com.whiteandreetto.prototypes.simplebus.cfg.PlumbingConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.integration.annotation.IntegrationComponentScan;
import org.springframework.integration.config.EnableIntegration;

/**
 * Created by anda on 06/08/14.
 */
@Configuration
@EnableIntegration
@IntegrationComponentScan
@ComponentScan({"com.whiteandreetto.prototypes.simplebus", "com.whiteandreetto.prototypes.simplebus.dbevents"})
@Import({MongoDBConfiguration.class, PlumbingConfiguration.class})
public class AppConfig {

}