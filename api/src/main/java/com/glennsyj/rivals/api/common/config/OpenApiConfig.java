package com.glennsyj.rivals.api.common.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

@OpenAPIDefinition(
    info = @Info(title = "Rivals API",
            description = "API documentation for the Rivals application",
            version = "v1"),
    servers = {
        @Server(url = "http://localhost:8080", description = "Local Server")
    }
)
@Configuration
public class OpenApiConfig {
    // Prod 환경에서는 설정 yml 파일을 통해 비활성화함
} 