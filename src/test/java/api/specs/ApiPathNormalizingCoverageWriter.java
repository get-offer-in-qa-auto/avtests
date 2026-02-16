package api.specs;

import com.github.viclovsky.swagger.coverage.CoverageOutputWriter;
import com.github.viclovsky.swagger.coverage.FileSystemOutputWriter;
import io.swagger.models.Path;
import io.swagger.models.Swagger;
import io.swagger.v3.oas.models.OpenAPI;

import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Ensures coverage paths include /api/v1 prefix to match OpenAPI spec.
 * Rest Assured's getUserDefinedPath() may return paths without base path in some configs.
 */
public class ApiPathNormalizingCoverageWriter implements CoverageOutputWriter {

    private static final String API_PREFIX = "/api/v1";

    private final FileSystemOutputWriter delegate;

    public ApiPathNormalizingCoverageWriter() {
        this.delegate = new FileSystemOutputWriter(Paths.get("swagger-coverage-output"));
    }

    @Override
    public void write(Swagger swagger) {
        Swagger normalized = normalizePaths(swagger);
        delegate.write(normalized);
    }

    @Override
    public void write(OpenAPI openAPI) {
        delegate.write(openAPI);
    }

    private Swagger normalizePaths(Swagger swagger) {
        Map<String, Path> paths = swagger.getPaths();
        if (paths == null || paths.isEmpty()) {
            return swagger;
        }

        Map<String, Path> normalizedPaths = new LinkedHashMap<>();
        for (Map.Entry<String, Path> e : paths.entrySet()) {
            normalizedPaths.put(normalizePath(e.getKey()), e.getValue());
        }

        return new Swagger()
                .scheme(swagger.getSchemes() != null && !swagger.getSchemes().isEmpty() ? swagger.getSchemes().get(0) : null)
                .host(swagger.getHost())
                .consumes(swagger.getConsumes())
                .produces(swagger.getProduces())
                .paths(normalizedPaths);
    }

    private String normalizePath(String path) {
        if (path == null || path.startsWith(API_PREFIX)) {
            return path;
        }
        return API_PREFIX + (path.startsWith("/") ? path : "/" + path);
    }
}
