package com.rag.backend.config.properties;


import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "orange.assistance")
public class OrangeAssistanceProperties {

    private List<String> allowedHosts;
    private int timeout;
    private String userAgent;
}