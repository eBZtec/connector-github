package jp.openstandia.connector.github;

import org.identityconnectors.common.security.GuardedString;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GitHubEMUConfigurationTest {

    @Test
    void validate_doesNothing_whenRequiredFieldsAreSet() {
        GitHubEMUConfiguration config = new GitHubEMUConfiguration();

        config.setEnterpriseSlug("my-enterprise");
        config.setAccessToken(new GuardedString("ghp_xxxxxxxx".toCharArray()));
        config.setEndpointURL("https://api.github.com");

        assertDoesNotThrow(config::validate);
    }

    @Test
    void validate_doesNothing_evenWithMinimalFields() {
        GitHubEMUConfiguration config = new GitHubEMUConfiguration();

        config.setEnterpriseSlug("test-enterprise");
        config.setAccessToken(new GuardedString("token".toCharArray()));

        assertDoesNotThrow(config::validate);
    }
}