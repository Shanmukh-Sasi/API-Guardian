package com.project1.api_guardian.comparison;

import com.project1.api_guardian.dto.ComparisonChangeResponse;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.parameters.Parameter;
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
public class EndpointComparisonService {

    public List<ComparisonChangeResponse> compare(OpenAPI baseline, OpenAPI target) {
        List<ComparisonChangeResponse> changes = new ArrayList<>();
        Set<String> paths = new TreeSet<>();

        if (baseline.getPaths() != null) {
            paths.addAll(baseline.getPaths().keySet());
        }
        if (target.getPaths() != null) {
            paths.addAll(target.getPaths().keySet());
        }

        for (String path : paths) {
            PathItem beforePath = baseline.getPaths() == null ? null : baseline.getPaths().get(path);
            PathItem afterPath = target.getPaths() == null ? null : target.getPaths().get(path);

            if (beforePath == null) {
                changes.add(change("ENDPOINT_ADDED", path, null, null, null, path, false));
                continue;
            }
            if (afterPath == null) {
                changes.add(change("ENDPOINT_REMOVED", path, null, null, path, null, true));
                continue;
            }

            compareOperations(path, beforePath, afterPath, baseline, target, changes);
        }
        return changes;
    }

    private void compareOperations(
            String path,
            PathItem beforePath,
            PathItem afterPath,
            OpenAPI baseline,
            OpenAPI target,
            List<ComparisonChangeResponse> changes
    ) {
        Map<String, Operation> beforeOperations = operations(beforePath);
        Map<String, Operation> afterOperations = operations(afterPath);
        Set<String> methods = new TreeSet<>();
        methods.addAll(beforeOperations.keySet());
        methods.addAll(afterOperations.keySet());

        for (String method : methods) {
            Operation before = beforeOperations.get(method);
            Operation after = afterOperations.get(method);
            if (before == null) {
                changes.add(change("HTTP_METHOD_ADDED", path, method, null, null, method, false));
            } else if (after == null) {
                changes.add(change("HTTP_METHOD_REMOVED", path, method, null, method, null, true));
            } else {
                compareParameters(path, method, beforePath, afterPath, before, after, baseline, target, changes);
            }
        }
    }

    private void compareParameters(
            String path,
            String method,
            PathItem beforePath,
            PathItem afterPath,
            Operation beforeOperation,
            Operation afterOperation,
            OpenAPI baseline,
            OpenAPI target,
            List<ComparisonChangeResponse> changes
    ) {
        Map<String, Parameter> beforeParameters = parameters(
                beforePath.getParameters(), beforeOperation.getParameters(), baseline
        );
        Map<String, Parameter> afterParameters = parameters(
                afterPath.getParameters(), afterOperation.getParameters(), target
        );

        Set<String> parameterKeys = new TreeSet<>();
        parameterKeys.addAll(beforeParameters.keySet());
        parameterKeys.addAll(afterParameters.keySet());

        for (String key : parameterKeys) {
            Parameter before = beforeParameters.get(key);
            Parameter after = afterParameters.get(key);
            if (before == null) {
                changes.add(change(
                        "REQUEST_PARAMETER_ADDED", path, method, parameterLabel(after), null,
                        parameterType(after, target), Boolean.TRUE.equals(after.getRequired())
                ));
            } else if (after == null) {
                changes.add(change(
                        "REQUEST_PARAMETER_REMOVED", path, method, parameterLabel(before),
                        parameterType(before, baseline), null, Boolean.TRUE.equals(before.getRequired())
                ));
            } else if (!parameterType(before, baseline).equals(parameterType(after, target))) {
                changes.add(change(
                        "REQUEST_PARAMETER_TYPE_CHANGED", path, method, parameterLabel(after),
                        parameterType(before, baseline), parameterType(after, target), true
                ));
            }
        }
    }

    private Map<String, Parameter> parameters(
            List<Parameter> pathParameters,
            List<Parameter> operationParameters,
            OpenAPI openAPI
    ) {
        Map<String, Parameter> result = new LinkedHashMap<>();
        addParameters(result, pathParameters, openAPI);
        // An operation-level parameter with the same (location, name) overrides the path-level one.
        addParameters(result, operationParameters, openAPI);
        return result;
    }

    private void addParameters(Map<String, Parameter> destination, List<Parameter> parameters, OpenAPI openAPI) {
        if (parameters == null) {
            return;
        }
        for (Parameter original : parameters) {
            Parameter parameter = resolveParameter(original, openAPI);
            if (parameter == null || parameter.getName() == null || parameter.getIn() == null) {
                continue;
            }
            destination.put(parameter.getIn() + ":" + parameter.getName(), parameter);
        }
    }

    private Parameter resolveParameter(Parameter parameter, OpenAPI openAPI) {
        if (parameter == null || parameter.get$ref() == null) {
            return parameter;
        }
        String prefix = "#/components/parameters/";
        if (!parameter.get$ref().startsWith(prefix) || openAPI.getComponents() == null
                || openAPI.getComponents().getParameters() == null) {
            return parameter;
        }
        String componentName = parameter.get$ref().substring(prefix.length()).replace("~1", "/").replace("~0", "~");
        return openAPI.getComponents().getParameters().getOrDefault(componentName, parameter);
    }

    private Map<String, Operation> operations(PathItem pathItem) {
        Map<String, Operation> result = new LinkedHashMap<>();
        addOperation(result, "GET", pathItem.getGet());
        addOperation(result, "PUT", pathItem.getPut());
        addOperation(result, "POST", pathItem.getPost());
        addOperation(result, "DELETE", pathItem.getDelete());
        addOperation(result, "OPTIONS", pathItem.getOptions());
        addOperation(result, "HEAD", pathItem.getHead());
        addOperation(result, "PATCH", pathItem.getPatch());
        addOperation(result, "TRACE", pathItem.getTrace());
        return result;
    }

    private void addOperation(Map<String, Operation> destination, String method, Operation operation) {
        if (operation != null) {
            destination.put(method, operation);
        }
    }

    private String parameterLabel(Parameter parameter) {
        return parameter.getName() + " (" + parameter.getIn() + ")";
    }

    private String parameterType(Parameter parameter, OpenAPI openAPI) {
        Schema<?> schema = parameter.getSchema();
        Set<String> visitedReferences = new HashSet<>();
        while (schema != null && schema.get$ref() != null
                && schema.get$ref().startsWith("#/components/schemas/")
                && openAPI.getComponents() != null
                && openAPI.getComponents().getSchemas() != null) {
            String reference = schema.get$ref();
            if (!visitedReferences.add(reference)) {
                break;
            }
            String componentName = reference.substring("#/components/schemas/".length())
                    .replace("~1", "/")
                    .replace("~0", "~");
            Schema<?> component = openAPI.getComponents().getSchemas().get(componentName);
            if (component == null) {
                break;
            }
            schema = component;
        }
        if (schema == null || schema.getType() == null) {
            return "unknown";
        }
        String type = schema.getType();
        if ("array".equals(type) && schema.getItems() != null) {
            type += " of " + parameterSchemaType(schema.getItems());
        }
        return schema.getFormat() == null ? type : type + " (" + schema.getFormat() + ")";
    }

    private String parameterSchemaType(Schema<?> schema) {
        if (schema == null || schema.getType() == null) {
            return "unknown";
        }
        String type = schema.getType();
        if ("array".equals(type) && schema.getItems() != null) {
            type += " of " + parameterSchemaType(schema.getItems());
        }
        return schema.getFormat() == null ? type : type + " (" + schema.getFormat() + ")";
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
