package org.kohsuke.github;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class SCIMSearchBuilderTest {

    private GitHub gitHub;
    private Requester requester;
    private SCIMSearchBuilder<String> builder;

    @BeforeEach
    void setUp() throws Exception {
        gitHub = mock(GitHub.class);
        requester = mock(Requester.class);
        GHOrganization org = mock(GHOrganization.class);

        when(gitHub.createRequest()).thenReturn(requester);
        when(requester.withUrlPath(anyString())).thenReturn(requester);
        when(requester.with(anyString(), Collections.singleton(anyString()))).thenReturn(requester);

        builder = new SCIMSearchBuilder<String>(
                gitHub,
                org,
                (Class<? extends SCIMSearchResult<String>>) (Class<?>) SCIMSearchResult.class) {

            @Override
            protected String getApiUrl() {
                return "/scim/v2/Users";
            }

        };
    }

    @Test
    void escape_shouldEscapeDoubleQuotes() throws Exception {
        Method escapeMethod = SCIMSearchBuilder.class
                .getDeclaredMethod("escape", String.class);

        escapeMethod.setAccessible(true);

        String input = "a\"b";
        String escaped = (String) escapeMethod.invoke(builder, input);

        assertEquals("a\\\"b", escaped);
    }

}