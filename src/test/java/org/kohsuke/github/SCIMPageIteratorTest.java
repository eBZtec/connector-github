package org.kohsuke.github;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.NoSuchElementException;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SuppressWarnings({"unchecked", "rawtypes"})
class SCIMPageIteratorTest {

    @Test
    void testCreateNormalAndWithOffsets() throws Exception {
        GitHubClient mockClient = mock(GitHubClient.class);
        GitHubRequest mockRequest = mock(GitHubRequest.class);
        GitHubRequest.Builder mockBuilder = mock(GitHubRequest.Builder.class);

        when(mockRequest.method()).thenReturn("GET");
        when(mockRequest.toBuilder()).thenReturn(mockBuilder);
        when(mockBuilder.with(anyString(), anyInt())).thenReturn(mockBuilder);
        when(mockBuilder.build()).thenReturn(mockRequest);

        SCIMPageIterator<SCIMSearchResult> iterator =
                SCIMPageIterator.create(mockClient, SCIMSearchResult.class, mockRequest, 10, 5);

        assertNotNull(iterator);
        verify(mockBuilder).with("count", 10);
        verify(mockBuilder).with("startIndex", 5);
        verify(mockBuilder).build();
    }

    @Test
    void testCreateWithoutPageOffset() throws Exception {
        GitHubClient mockClient = mock(GitHubClient.class);
        GitHubRequest mockRequest = mock(GitHubRequest.class);
        GitHubRequest.Builder mockBuilder = mock(GitHubRequest.Builder.class);

        when(mockRequest.method()).thenReturn("GET");
        when(mockRequest.toBuilder()).thenReturn(mockBuilder);
        when(mockBuilder.with(anyString(), anyInt())).thenReturn(mockBuilder);
        when(mockBuilder.build()).thenReturn(mockRequest);

        SCIMPageIterator<SCIMSearchResult> iterator =
                SCIMPageIterator.create(mockClient, SCIMSearchResult.class, mockRequest, 10, 0);

        assertNotNull(iterator);
        verify(mockBuilder).with("count", 10);
        verify(mockBuilder, never()).with(eq("startIndex"), anyInt());
    }

    @Test
    void testThrowsGHExceptionWhenMalformedURL() throws Exception {
        GitHubClient mockClient = mock(GitHubClient.class);
        GitHubRequest mockRequest = mock(GitHubRequest.class);
        GitHubRequest.Builder mockBuilder = mock(GitHubRequest.Builder.class);

        when(mockRequest.method()).thenReturn("GET");
        when(mockRequest.toBuilder()).thenReturn(mockBuilder);
        when(mockBuilder.with(anyString(), anyInt())).thenReturn(mockBuilder);
        when(mockBuilder.build()).thenThrow(new MalformedURLException("Bad URL"));

        GHException ex = assertThrows(GHException.class, () ->
                SCIMPageIterator.create(mockClient, SCIMSearchResult.class, mockRequest, 10, 1));

        assertTrue(ex.getMessage().contains("Unable to build GitHub SCIM API URL"));
    }

    @Test
    void testThrowsIllegalStateWhenNotGET() {
        GitHubClient mockClient = mock(GitHubClient.class);
        GitHubRequest mockRequest = mock(GitHubRequest.class);
        when(mockRequest.method()).thenReturn("POST");

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> new SCIMPageIterator<>(mockClient, SCIMSearchResult.class, mockRequest));

        assertTrue(ex.getMessage().contains("Request method \"GET\" is required"));
    }

    @Test
    void shouldIterateThroughPages() throws Exception {
        GitHubClient mockClient = mock(GitHubClient.class);
        GitHubRequest mockRequest = mock(GitHubRequest.class);

        when(mockRequest.method()).thenReturn("GET");

        GitHubRequest fakeRequest = GitHubRequest.newBuilder()
                .withApiUrl("https://api.github.com")
                .withUrlPath("/scim/v2/Users")
                .set("startIndex", 0)
                .build();

        GitHubResponse.ResponseInfo fakeInfo = mock(GitHubResponse.ResponseInfo.class);
        when(fakeInfo.request()).thenReturn(fakeRequest);
        when(fakeInfo.statusCode()).thenReturn(200);

        SCIMSearchResult result1 = new SCIMSearchResult() {{
            startIndex = 0;
            itemsPerPage = 100;
            totalResults = 150;
        }};

        SCIMSearchResult result2 = new SCIMSearchResult() {{
            startIndex = 100;
            itemsPerPage = 100;
            totalResults = 150;
        }};

        GitHubResponse<SCIMSearchResult> response1 = new GitHubResponse<>(fakeInfo, result1);
        GitHubResponse<SCIMSearchResult> response2 = new GitHubResponse<>(fakeInfo, result2);

        when(mockClient.sendRequest(any(GitHubRequest.class), any()))
                .thenReturn((GitHubResponse) response1)
                .thenReturn((GitHubResponse) response2);

        SCIMPageIterator<SCIMSearchResult> iterator =
                new SCIMPageIterator<>(mockClient, SCIMSearchResult.class, mockRequest);

        assertTrue(iterator.hasNext(), "Iterator should have first page");
        assertEquals(result1, iterator.next(), "First result should match");
        assertTrue(iterator.hasNext(), "Iterator should have second page");
        assertEquals(result2, iterator.next(), "Second result should match");
        assertFalse(iterator.hasNext(), "Iterator should have no more pages");
    }

    @Test
    void shouldThrowWhenNoMoreElements() throws IOException {
        GitHubClient mockClient = mock(GitHubClient.class);
        GitHubRequest mockRequest = mock(GitHubRequest.class);

        when(mockRequest.method()).thenReturn("GET");

        GitHubRequest fakeRequest = GitHubRequest.newBuilder()
                .withApiUrl("https://api.github.com")
                .withUrlPath("/scim/v2/Users")
                .set("startIndex", 0)
                .build();

        GitHubResponse.ResponseInfo fakeInfo = mock(GitHubResponse.ResponseInfo.class);
        when(fakeInfo.request()).thenReturn(fakeRequest);
        when(fakeInfo.statusCode()).thenReturn(200);

        SCIMSearchResult result1 = new SCIMSearchResult();
        result1.startIndex = 0;
        result1.itemsPerPage = 100;
        result1.totalResults = 50;

        GitHubResponse<SCIMSearchResult> response1 = new GitHubResponse<>(fakeInfo, result1);

        when(mockClient.sendRequest(any(GitHubRequest.class), any()))
                .thenReturn((GitHubResponse) response1);

        SCIMPageIterator<SCIMSearchResult> iterator =
                new SCIMPageIterator<>(mockClient, SCIMSearchResult.class, fakeRequest);

        assertTrue(iterator.hasNext());
        assertEquals(result1, iterator.next());
        assertFalse(iterator.hasNext());
        assertThrows(NoSuchElementException.class, iterator::next);
    }

    private static Stream<TestCase> provideTestCases() {
        return Stream.of(
                new TestCase(100, 100, true),   // ainda há próxima página → deve lançar GHException
                new TestCase(50, 100, false)    // sem próxima página → deve retornar finalResponse
        );
    }

    @ParameterizedTest
    @MethodSource("provideTestCases")
    void shouldHandleFinalResponseBehaviorBasedOnPagination(TestCase testCase) throws IOException {
        // Arrange
        GitHubClient mockClient = mock(GitHubClient.class);
        GitHubRequest fakeRequest = GitHubRequest.newBuilder()
                .withApiUrl("https://api.github.com")
                .withUrlPath("/scim/v2/Users")
                .set("startIndex", 0)
                .build();

        GitHubResponse.ResponseInfo fakeInfo = mock(GitHubResponse.ResponseInfo.class);
        when(fakeInfo.request()).thenReturn(fakeRequest);
        when(fakeInfo.statusCode()).thenReturn(200);

        SCIMSearchResult result = new SCIMSearchResult();
        result.startIndex = 0;
        result.itemsPerPage = testCase.itemsPerPage;
        result.totalResults = testCase.totalResults;

        GitHubResponse<SCIMSearchResult> response = new GitHubResponse<>(fakeInfo, result);
        when(mockClient.sendRequest(any(GitHubRequest.class), any()))
                .thenReturn((GitHubResponse) response);

        SCIMPageIterator<SCIMSearchResult> iterator =
                new SCIMPageIterator<>(mockClient, SCIMSearchResult.class, fakeRequest);

        assertTrue(iterator.hasNext());
        iterator.next();

        // Act + Assert
        if (testCase.shouldThrow) {
            assertTrue(iterator.hasNext());
            assertThrows(GHException.class, iterator::finalResponse);
        } else {
            assertFalse(iterator.hasNext());
            GitHubResponse<SCIMSearchResult> finalResp = iterator.finalResponse();
            assertNotNull(finalResp);
            assertEquals(response, finalResp, "Final response should be the last one retrieved");
        }
    }

    private static class TestCase {
        final int totalResults;
        final int itemsPerPage;
        final boolean shouldThrow;

        TestCase(int totalResults, int itemsPerPage, boolean shouldThrow) {
            this.totalResults = totalResults;
            this.itemsPerPage = itemsPerPage;
            this.shouldThrow = shouldThrow;
        }

        @Override
        public String toString() {
            return shouldThrow
                    ? "Throws GHException (still has next)"
                    : "Returns finalResponse (iteration complete)";
        }
    }
}
