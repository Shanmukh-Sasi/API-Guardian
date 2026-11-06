package com.project1.api_guardian.parser;

import com.project1.api_guardian.exception.InvalidSpecificationException;
import io.swagger.parser.OpenAPIParser;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.parser.core.models.ParseOptions;
import io.swagger.v3.parser.core.models.SwaggerParseResult;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class OpenApiSpecificationParser {

    public OpenAPI parseAndValidate(String definition) {
        ParseOptions options = new ParseOptions();
        options.setResolve(false);

        SwaggerParseResult result;
        try {
            result = new OpenAPIParser().readContents(definition, null, options);
        } catch (RuntimeException exception) {
            throw new InvalidSpecificationException("The uploaded file could not be parsed as an OpenAPI specification");
        }

        if (result == null || result.getOpenAPI() == null) {
            throw new InvalidSpecificationException(validationMessage(result));
        }

        if (result.getMessages() != null && !result.getMessages().isEmpty()) {
            throw new InvalidSpecificationException(validationMessage(result));
        }

        OpenAPI openAPI = result.getOpenAPI();
        if (openAPI.getOpenapi() == null || !openAPI.getOpenapi().startsWith("3.")) {
            throw new InvalidSpecificationException("Only OpenAPI 3.x specifications are supported");
        }

        if (openAPI.getInfo() == null
                || isBlank(openAPI.getInfo().getTitle())
                || isBlank(openAPI.getInfo().getVersion())) {
            throw new InvalidSpecificationException("The specification must define info.title and info.version");
        }

        return openAPI;
    }

    private String validationMessage(SwaggerParseResult result) {
        List<String> messages = result == null ? null : result.getMessages();
        if (messages == null || messages.isEmpty()) {
            return "The uploaded file is not a valid OpenAPI 3.x specification";
        }
        return "OpenAPI validation failed: " + String.join("; ", messages);
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
