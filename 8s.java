<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
    <version>2.1.0</version>
</dependency>

  package com.example.accesscontrol.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI apiInfo() {
        return new OpenAPI()
                .info(new Info()
                        .title("Door Access Control API")
                        .description("APIs for validating card access to doors")
                        .version("v1.0"));
    }
}

File: src/main/java/com/example/accesscontrol/controller/AccessController.java

Add Swagger annotations:

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

@RestController
@RequestMapping("/api/v1/access")
public class AccessController {

    private final AccessService accessService;

    public AccessController(AccessService accessService) {
        this.accessService = accessService;
    }

    @Operation(summary = "Validate card access to a door")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Access validated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request")
    })
    @PostMapping("/validate")
    public AccessResponse validate(@RequestBody AccessRequest request) {
        return accessService.validateAccess(request);
    }
}
///
