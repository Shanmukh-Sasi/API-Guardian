package com.project1.api_guardian.comparison;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import com.project1.api_guardian.dto.ComparisonChangeResponse;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

@Component
public class SchemaComparisonService {

    private static final String SCHEMA_REF_PREFIX = "#/components/schemas/";

    private final ObjectMapper objectMapper;

    public SchemaComparisonService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public List<ComparisonChangeResponse> compare(OpenAPI baseline, OpenAPI target) {
        List<ComparisonChangeResponse> changes = new ArrayList<>();
        if (baseline.getPaths() == null || target.getPaths() == null) {
            return changes;
        }

        Set<String> paths = new TreeSet<>(baseline.getPaths().keySet());
        paths.retainAll(target.getPaths().keySet());
        for (String path : paths) {
            PathItem beforePath = baseline.getPaths().get(path);
            PathItem afterPath = target.getPaths().get(path);
            Map<String, Operation> beforeOperations = operations(beforePath);
            Map<String, Operation> afterOperations = operations(afterPath);

            Set<String> methods = new TreeSet<>(beforeOperations.keySet());
            methods.retainAll(afterOperations.keySet());
            for (String method : methods) {
                Operation before = beforeOperations.get(method);
                Operation after = afterOperations.get(method);
                compareRequestSchemas(path, method, before, after, baseline, target, changes);
                compareResponseSchemas(path, method, before, after, baseline, target, changes);
            }
        }
        return changes;
    }

    private void compareRequestSchemas(
            String path,
            String method,
            Operation before,
            Operation after,
            OpenAPI baseline,
            OpenAPI target,
            List<ComparisonChangeResponse> changes
    ) {
        Map<String, JsonNode> beforeSchemas = requestSchemas(before, baseline);
        Map<String, JsonNode> afterSchemas = requestSchemas(after, target);
        compareSchemaGroups(
                path, method, "REQUEST_FIELD", "requestBody", beforeSchemas, afterSchemas,
                baseline, target, changes
        );
    }

    private void compareResponseSchemas(
            String path,
            String method,
            Operation before,
            Operation after,
            OpenAPI baseline,
            OpenAPI target,
            List<ComparisonChangeResponse> changes
    ) {
        Map<String, JsonNode> beforeSchemas = responseSchemas(before, baseline);
        Map<String, JsonNode> afterSchemas = responseSchemas(after, target);
        compareSchemaGroups(
                path, method, "RESPONSE_FIELD", "response", beforeSchemas, afterSchemas,
                baseline, target, changes
        );
    }

    private void compareSchemaGroups(
            String path,
            String method,
            String changePrefix,
            String context,
            Map<String, JsonNode> beforeSchemas,
            Map<String, JsonNode> afterSchemas,
            OpenAPI baseline,
            OpenAPI target,
            List<ComparisonChangeResponse> changes
    ) {
        Set<String> groups = new TreeSet<>(beforeSchemas.keySet());
        groups.addAll(afterSchemas.keySet());
        for (String group : groups) {
            compareFields(
                    path,
                    method,
                    changePrefix,
                    context + "[" + group + "]",
                    beforeSchemas.get(group),
                    afterSchemas.get(group),
                    baseline,
                    target,
                    new HashSet<>(),
                    new HashSet<>(),
                    changes
            );
        }
    }

    private void compareFields(
            String path,
            String method,
            String changePrefix,
            String context,
            JsonNode beforeSchema,
            JsonNode afterSchema,
            OpenAPI baseline,
            OpenAPI target,
            Set<String> beforeReferences,
            Set<String> afterReferences,
            List<ComparisonChangeResponse> changes
    ) {
        beforeSchema = resolveLocalReference(beforeSchema, baseline, beforeReferences);
        afterSchema = resolveLocalReference(afterSchema, target, afterReferences);
        JsonNode beforeProperties = beforeSchema == null ? null : beforeSchema.get("properties");
        JsonNode afterProperties = afterSchema == null ? null : afterSchema.get("properties");
        Set<String> propertyNames = propertyNames(beforeProperties);
        propertyNames.addAll(propertyNames(afterProperties));

        for (String propertyName : propertyNames) {
            JsonNode beforeProperty = beforeProperties == null ? null : beforeProperties.get(propertyName);
            JsonNode afterProperty = afterProperties == null ? null : afterProperties.get(propertyName);
            String fieldPath = context + "." + propertyName;

            if (beforeProperty == null) {
                changes.add(change(
                        changePrefix + "_ADDED", path, method, fieldPath, null,
                        typeDescription(afterProperty),
                        changePrefix.equals("REQUEST_FIELD") && isRequired(afterSchema, propertyName)
                ));
                continue;
            }
            if (afterProperty == null) {
                changes.add(change(
                        changePrefix + "_REMOVED", path, method, fieldPath,
                        typeDescription(beforeProperty), null, true
                ));
                continue;
            }

            beforeProperty = resolveLocalReference(beforeProperty, baseline, new HashSet<>(beforeReferences));
            afterProperty = resolveLocalReference(afterProperty, target, new HashSet<>(afterReferences));
            String beforeType = typeDescription(beforeProperty);
            String afterType = typeDescription(afterProperty);
            if (!beforeType.equals(afterType)) {
                changes.add(change(
                        changePrefix + "_TYPE_CHANGED", path, method, fieldPath,
                        beforeType, afterType, true
                ));
            }

            compareFields(
                    path,
                    method,
                    changePrefix,
                    fieldPath,
                    beforeProperty,
                    afterProperty,
                    baseline,
                    target,
                    new HashSet<>(beforeReferences),
                    new HashSet<>(afterReferences),
                    changes
            );
        }
    }

    private Set<String> propertyNames(JsonNode properties) {
        Set<String> names = new TreeSet<>();
        if (properties != null && properties.isObject()) {
            names.addAll(properties.propertyNames());
        }
        return names;
    }

    private JsonNode resolveLocalReference(JsonNode schema, OpenAPI openAPI, Set<String> references) {
        if (schema == null || !schema.isObject()) {
            return schema;
        }
        JsonNode referenceNode = schema.get("$ref");
        if (referenceNode == null || !referenceNode.isString()) {
            return schema;
        }

        String reference = referenceNode.asText();
        if (!reference.startsWith(SCHEMA_REF_PREFIX)
                || openAPI.getComponents() == null
                || openAPI.getComponents().getSchemas() == null) {
            return schema;
        }

        String componentName = reference.substring(SCHEMA_REF_PREFIX.length())
                .replace("~1", "/")
                .replace("~0", "~");
        if (!references.add(componentName)) {
            return schema;
        }

        Schema<?> component = openAPI.getComponents().getSchemas().get(componentName);
        return component == null ? schema : resolveLocalReference(objectMapper.valueToTree(component), openAPI, references);
    }

    private boolean isRequired(JsonNode schema, String propertyName) {
        JsonNode required = schema == null ? null : schema.get("required");
        if (required == null || !required.isArray()) {
            return false;
        }
        for (JsonNode requiredName : required) {
            if (propertyName.equals(requiredName.asString())) {
                return true;
            }
        }
        return false;
    }

    private String typeDescription(JsonNode schema) {
        if (schema == null) {
            return "unknown";
        }
        String type = schema.path("type").asString("");
        String format = schema.path("format").asString("");
        if (type.isBlank()) {
            JsonNode reference = schema.get("$ref");
            return reference == null ? "unknown" : reference.asString("unknown");
        }
        if ("array".equals(type) && schema.has("items")) {
            type = "array of " + typeDescription(schema.path("items"));
        }
        return format.isBlank() ? type : type + " (" + format + ")";
    }

    private Map<String, JsonNode> requestSchemas(Operation operation, OpenAPI openAPI) {
        Map<String, JsonNode> schemas = new LinkedHashMap<>();
        if (operation.getRequestBody() == null || operation.getRequestBody().getContent() == null) {
            return schemas;
        }
        addContentSchemas(schemas, operation.getRequestBody().getContent(), "request");
        return schemas;
    }

    private Map<String, JsonNode> responseSchemas(Operation operation, OpenAPI openAPI) {
        Map<String, JsonNode> schemas = new LinkedHashMap<>();
        if (operation.getResponses() == null) {
            return schemas;
        }
        operation.getResponses().forEach((status, response) -> {
            if (response != null && response.getContent() != null) {
                addContentSchemas(schemas, response.getContent(), status);
            }
        });
        return schemas;
    }

    private void addContentSchemas(
            Map<String, JsonNode> destination,
            Content content,
            String groupPrefix
    ) {
        content.forEach((mediaTypeName, mediaType) -> {
            if (mediaType != null && mediaType.getSchema() != null) {
                JsonNode schema = objectMapper.valueToTree(mediaType.getSchema());
                destination.put(groupPrefix + " " + mediaTypeName, schema);
            }
        });
    }

    private Map<String, Operation> operations(PathItem pathItem) {
        Map<String, Operation> operations = new LinkedHashMap<>();
        addOperation(operations, "GET", pathItem.getGet());
        addOperation(operations, "PUT", pathItem.getPut());
        addOperation(operations, "POST", pathItem.getPost());
        addOperation(operations, "DELETE", pathItem.getDelete());
        addOperation(operations, "OPTIONS", pathItem.getOptions());
        addOperation(operations, "HEAD", pathItem.getHead());
        addOperation(operations, "PATCH", pathItem.getPatch());
        addOperation(operations, "TRACE", pathItem.getTrace());
        return operations;
    }

    private void addOperation(Map<String, Operation> operations, String method, Operation operation) {
        if (operation != null) {
            operations.put(method, operation);
        }
    }

    private ComparisonChangeResponse change(
            String type,
            String path,
            String method,
            String element,
            String oldValue,
            String newValue,
            boolean breaking
    ) {
        return new ComparisonChangeResponse(type, path, method, element, oldValue, newValue, breaking);
    }
}
