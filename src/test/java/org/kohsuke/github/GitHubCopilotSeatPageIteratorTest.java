package org.kohsuke.github;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SuppressWarnings({"unchecked", "rawtypes"})
class GitHubCopilotSeatPageIteratorTest {

    GitHubRequest.Builder<?> fakeRequest = GitHubRequest.newBuilder()
            .withApiUrl("https://api.github.com")
            .withUrlPath("/scim/v2/Users");

    @Test
    void testCreateNormalAndWithOffsets() throws Exception {
        GitHubClient mockClient = mock(GitHubClient.class);
        GitHubRequest mockRequest = mock(GitHubRequest.class);
        GitHubRequest.Builder mockBuilder = mock(GitHubRequest.Builder.class);

        when(mockRequest.method()).thenReturn("GET");
        when(mockRequest.toBuilder()).thenReturn(mockBuilder);
        when(mockBuilder.with(anyString(), anyInt())).thenReturn(mockBuilder);
        when(mockBuilder.build()).thenReturn(mockRequest);

        GitHubCopilotSeatPageIterator<GitHubCopilotSeatsSearchResult> iterator =
                GitHubCopilotSeatPageIterator.create(mockClient, GitHubCopilotSeatsSearchResult.class, mockRequest, 10, 5);

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

        GitHubCopilotSeatPageIterator<GitHubCopilotSeatsSearchResult> iterator =
                GitHubCopilotSeatPageIterator.create(mockClient, GitHubCopilotSeatsSearchResult.class, mockRequest, 10, 0);

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
                GitHubCopilotSeatPageIterator.create(mockClient, GitHubCopilotSeatsSearchResult.class, mockRequest, 10, 1));

        assertTrue(ex.getMessage().contains("Unable to build GitHub SCIM API URL"));
    }

    @Test
    void testThrowsIllegalStateWhenNotGET() {
        GitHubClient mockClient = mock(GitHubClient.class);
        GitHubRequest mockRequest = mock(GitHubRequest.class);
        when(mockRequest.method()).thenReturn("POST");

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> new GitHubCopilotSeatPageIterator<>(mockClient, GitHubCopilotSeatsSearchResult.class, mockRequest));

        assertTrue(ex.getMessage().contains("Request method \"GET\" is required"));
    }

    @Test
    void shouldThrowWhenNoMoreElements() throws IOException {
        GitHubClient mockClient = mock(GitHubClient.class);
        GitHubRequest mockRequest = mock(GitHubRequest.class);

        when(mockRequest.method()).thenReturn("GET");

        GitHubRequest request = fakeRequest
                .set("total_seats", 100)
                .build();

        GitHubResponse.ResponseInfo fakeInfo = mock(GitHubResponse.ResponseInfo.class);
        when(fakeInfo.request()).thenReturn(request);
        when(fakeInfo.statusCode()).thenReturn(200);

        GitHubCopilotSeatsSearchResult result1 = new GitHubCopilotSeatsSearchResult();
        result1.total_seats = 100;

        GitHubResponse<GitHubCopilotSeatsSearchResult> response1 = new GitHubResponse<>(fakeInfo, result1);

        when(mockClient.sendRequest(any(GitHubRequest.class), any()))
                .thenReturn((GitHubResponse) response1);

        GitHubCopilotSeatPageIterator<GitHubCopilotSeatsSearchResult> iterator =
                new GitHubCopilotSeatPageIterator<>(mockClient, GitHubCopilotSeatsSearchResult.class, request);

        assertTrue(iterator.hasNext());
        assertEquals(result1, iterator.next());
        assertFalse(iterator.hasNext());
        assertThrows(NoSuchElementException.class, iterator::next);
    }

    @Test
    void shouldThrowExceptionWhenHasNextIsTrue() throws IOException {
        GitHubClient mockClient = mock(GitHubClient.class);
        GitHubRequest request = fakeRequest.build();

        GitHubResponse.ResponseInfo fakeInfo = mock(GitHubResponse.ResponseInfo.class);
        when(fakeInfo.request()).thenReturn(request);
        when(fakeInfo.statusCode()).thenReturn(200);

        GitHubCopilotSeatPageIterator<GitHubCopilotSeatsSearchResult> iterator =
                spy(new GitHubCopilotSeatPageIterator<>(mockClient, GitHubCopilotSeatsSearchResult.class, request));

        doReturn(true).when(iterator).hasNext();
        assertThrows(GHException.class, iterator::finalResponse);
    }

    @Test
    void shouldReturnFinalResponseWhenNoNextPage() throws IOException, NoSuchFieldException, IllegalAccessException {
        GitHubClient mockClient = mock(GitHubClient.class);
        GitHubRequest request = fakeRequest.build();

        GitHubResponse.ResponseInfo fakeInfo = mock(GitHubResponse.ResponseInfo.class);
        when(fakeInfo.request()).thenReturn(request);
        when(fakeInfo.statusCode()).thenReturn(200);

        GitHubCopilotSeatsSearchResult result = new GitHubCopilotSeatsSearchResult();
        GitHubResponse<GitHubCopilotSeatsSearchResult> response = new GitHubResponse<>(fakeInfo, result);

        GitHubCopilotSeatPageIterator<GitHubCopilotSeatsSearchResult> iterator =
                spy(new GitHubCopilotSeatPageIterator<>(mockClient, GitHubCopilotSeatsSearchResult.class, request));

        doReturn(false).when(iterator).hasNext();

        Field field = GitHubCopilotSeatPageIterator.class.getDeclaredField("finalResponse");
        field.setAccessible(true);
        field.set(iterator, response);

        GitHubResponse<GitHubCopilotSeatsSearchResult> finalResp = iterator.finalResponse();

        assertNotNull(finalResp);
        assertEquals(response, finalResp);
    }

    @Test
    void shouldCallSendRequestInsideFetch() throws Exception {
        GitHubClient mockClient = mock(GitHubClient.class);
        GitHubRequest request = fakeRequest.method("GET").build();

        GitHubResponse.ResponseInfo mockInfo = mock(GitHubResponse.ResponseInfo.class);
        when(mockInfo.request()).thenReturn(request);
        when(mockInfo.statusCode()).thenReturn(200);

        GitHubCopilotSeatsSearchResult mockBody = new GitHubCopilotSeatsSearchResult();
        GitHubResponse<GitHubCopilotSeatsSearchResult> fakeResponse =
                new GitHubResponse<>(mockInfo, mockBody);

        when(mockClient.sendRequest(any(GitHubRequest.class), any()))
                .thenReturn((GitHubResponse) fakeResponse);

        GitHubCopilotSeatPageIterator<GitHubCopilotSeatsSearchResult> iterator =
                new GitHubCopilotSeatPageIterator<>(mockClient, GitHubCopilotSeatsSearchResult.class, request);

        Field nextField = GitHubCopilotSeatPageIterator.class.getDeclaredField("next");
        nextField.setAccessible(true);
        nextField.set(iterator, null);

        Field nextReqField = GitHubCopilotSeatPageIterator.class.getDeclaredField("nextRequest");
        nextReqField.setAccessible(true);
        nextReqField.set(iterator, request);

        Method fetchMethod = GitHubCopilotSeatPageIterator.class.getDeclaredMethod("fetch");
        fetchMethod.setAccessible(true);

        fetchMethod.invoke(iterator);
        verify(mockClient, times(1)).sendRequest(eq(request), any());
    }

    @Test
    void shouldReturnNextRequestWhenLinkHeaderHasNextRel() throws MalformedURLException {
        GitHubResponse mockResponse = mock(GitHubResponse.class);
        when(mockResponse.headerField("Link")).thenReturn(
                "<https://api.github.com/scim/v2/Users?page=2>; rel=\"next\", <https://api.github.com/scim/v2/Users?page=5>; rel=\"last\""
        );

        GitHubRequest request = fakeRequest.method("GET").build();
        GitHubCopilotSeatPageIterator<GitHubCopilotSeatsSearchResult> iterator =
                new GitHubCopilotSeatPageIterator<>(mock(GitHubClient.class),
                        GitHubCopilotSeatsSearchResult.class, request);

        GitHubRequest nextReq = iterator.findNextURL(mockResponse);

        assertNotNull(nextReq);
        assertTrue(nextReq.url().toString().contains("page=2"));
    }

    @Test
    void shouldReturnNullWhenNoLinkHeader() throws MalformedURLException {
        GitHubResponse mockResponse = mock(GitHubResponse.class);
        when(mockResponse.headerField("Link")).thenReturn(null);

        GitHubRequest request = fakeRequest.method("GET").build();
        GitHubCopilotSeatPageIterator iterator =
                new GitHubCopilotSeatPageIterator<>(mock(GitHubClient.class), GitHubCopilotSeatsSearchResult.class, request);

        assertNull(iterator.findNextURL(mockResponse));
    }

    @Test
    void shouldThrowExceptionWhenNextUrlIsMalformed() throws MalformedURLException {
        GitHubResponse mockResponse = mock(GitHubResponse.class);
        when(mockResponse.headerField("Link")).thenReturn("<:invalid_url>; rel=\"next\"");

        GitHubRequest request = fakeRequest.method("GET").build();
        GitHubCopilotSeatPageIterator iterator =
                new GitHubCopilotSeatPageIterator<>(mock(GitHubClient.class), GitHubCopilotSeatsSearchResult.class, request);

        assertThrows(GHException.class, () -> iterator.findNextURL(mockResponse));
    }

    @Test
    void shouldReturnNullWhenLinkHeaderIsNull() throws MalformedURLException {
        GitHubResponse mockResponse = mock(GitHubResponse.class);
        when(mockResponse.headerField("Link")).thenReturn(null);

        GitHubRequest request = fakeRequest.method("GET").build();
        GitHubCopilotSeatPageIterator<GitHubCopilotSeatsSearchResult> iterator =
                new GitHubCopilotSeatPageIterator<>(
                        mock(GitHubClient.class),
                        GitHubCopilotSeatsSearchResult.class,
                        request);

        GitHubRequest result = iterator.findNextURL(mockResponse);
        assertNull(result, "Deveria retornar null quando o header Link está ausente");
    }

    @Test
    void shouldReturnNullWhenLinkHeaderDoesNotContainNextRel() throws MalformedURLException {
        GitHubResponse mockResponse = mock(GitHubResponse.class);
        when(mockResponse.headerField("Link")).thenReturn(
                "<https://api.github.com/scim/v2/Users?page=5>; rel=\"last\"" // sem "next"
        );

        GitHubRequest request = fakeRequest.method("GET").build();
        GitHubCopilotSeatPageIterator<GitHubCopilotSeatsSearchResult> iterator =
                new GitHubCopilotSeatPageIterator<>(
                        mock(GitHubClient.class),
                        GitHubCopilotSeatsSearchResult.class,
                        request);

        GitHubRequest result = iterator.findNextURL(mockResponse);
        assertNull(result, "Deveria retornar null quando não há rel=\"next\" no header Link");
    }
}
