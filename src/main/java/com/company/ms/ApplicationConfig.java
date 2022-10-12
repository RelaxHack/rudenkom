package com.company.ms;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@PropertySource("file:${app.home}/config/config.properties")
@Configuration
public class ApplicationConfig {
}