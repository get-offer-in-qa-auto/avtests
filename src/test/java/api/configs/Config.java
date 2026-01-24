package api.configs;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.regex.Pattern;

public class Config {
    private static final Config INSTANCE = new Config();
    private final Properties properties = new Properties();

    private Config() {
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("config.properties")) {
            if (input == null) {
                throw new RuntimeException("config.properties not found in resources");
            }
            properties.load(input);
        } catch (IOException e) {
            throw new RuntimeException("Fail to load config.properties", e);
        }
    }

    public static String getProperty(String key) {
        return INSTANCE.properties.getProperty(key);
    }

    /**
     * Gets the UI base URL that should be used by browser containers.
     * If uiBaseUrlForBrowsers is set in config, it will be used.
     * Otherwise, if uiBaseUrl contains a 192.168.x.x IP address, it will be converted
     * to use host.docker.internal for Docker Desktop compatibility.
     * 
     * @return URL that browser containers can access
     */
    public static String getUiBaseUrlForBrowsers() {
        // Check if explicit URL for browsers is configured
        String explicitUrl = getProperty("uiBaseUrlForBrowsers");
        if (explicitUrl != null && !explicitUrl.trim().isEmpty()) {
            return explicitUrl;
        }

        String uiBaseUrl = getProperty("uiBaseUrl");
        if (uiBaseUrl == null) {
            return null;
        }

        // Check if URL contains 192.168.x.x IP address
        Pattern ipPattern = Pattern.compile("(https?://)(192\\.168\\.[0-9.]+)(:[0-9]+)");
        java.util.regex.Matcher matcher = ipPattern.matcher(uiBaseUrl);
        if (matcher.find()) {
            String protocol = matcher.group(1);
            String port = matcher.group(3);
            // Convert to host.docker.internal for Docker Desktop
            return protocol + "host.docker.internal" + port;
        }

        // Return original URL if no conversion needed
        return uiBaseUrl;
    }
}
