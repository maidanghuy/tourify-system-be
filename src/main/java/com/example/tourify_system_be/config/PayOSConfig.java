package com.example.tourify_system_be.config;


import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import vn.payos.PayOS;

@Configuration
@EnableConfigurationProperties(PayOSProperties.class)
public class PayOSConfig {

    @Bean
    public PayOS payOS(PayOSProperties props) {
        return new PayOS(props.getClientId(), props.getApiKey(), props.getChecksumKey());
    }
}
