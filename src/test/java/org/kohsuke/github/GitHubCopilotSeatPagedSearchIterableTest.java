package org.kohsuke.github;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.util.Iterator;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SuppressWarnings({"unchecked", "rawtypes"})
class GitHubCopilotSeatPagedSearchIterableTest {

    @Test
    void testAdaptReturnsResourcesAndCachesResult() {
        GitHubCopilotSeatsSearchResult<String> result = new GitHubCopilotSeatsSearchResult<>();
        result.seats = new String[]{"X", "Y"};

        Iterator<GitHubCopilotSeatsSearchResult<String>> baseIterator = mock(Iterator.class);
        when(baseIterator.hasNext()).thenReturn(true, false);
        when(baseIterator.next()).thenReturn(result);

        GitHubCopilotSeatPagedSearchIterable<String> iterable =
                new GitHubCopilotSeatPagedSearchIterable<>(mock(GitHub.class), mock(GitHubRequest.class), (Class) GitHubCopilotSeatsSearchResult.class);

        Iterator<String[]> adapted = iterable.adapt(baseIterator);

        assertTrue(adapted.hasNext());
        String[] arr = adapted.next();
        assertArrayEquals(new String[]{"X", "Y"}, arr);
        assertFalse(adapted.hasNext());
    }

    @Test
    void testWithPageOffsetAndPageSizeFluentAPI() {
        GitHub mockRoot = mock(GitHub.class);
        GitHubRequest mockReq = mock(GitHubRequest.class);

        GitHubCopilotSeatPagedSearchIterable<String> iterable =
                new GitHubCopilotSeatPagedSearchIterable<>(mockRoot, mockReq, (Class) GitHubCopilotSeatsSearchResult.class);

        GitHubCopilotSeatPagedSearchIterable<String> result1 = iterable.withPageSize(25);
        GitHubCopilotSeatPagedSearchIterable<String> result2 = iterable.withPageOffset(3);

        assertSame(iterable, result2);
        assertNotNull(result1);
    }

    @Test
    void testGetTotalCountReturnsResulttotal_seats() throws Exception {
        GitHub github = GitHub.connectAnonymously();
        GitHubRequest request = mock(GitHubRequest.class);

        GitHubCopilotSeatPagedSearchIterable<String> iterable = new GitHubCopilotSeatPagedSearchIterable<String>(
                github,
                request,
                (Class<? extends GitHubCopilotSeatsSearchResult<String>>) (Class<?>) GitHubCopilotSeatsSearchResult.class
        );

        GitHubCopilotSeatsSearchResult<String> fakeResult = new GitHubCopilotSeatsSearchResult<>();
        fakeResult.total_seats = 42;
        iterable.result = fakeResult;

        int total = iterable.getTotalSeats();
        assertEquals(42, total, "getTotalCount deve retornar o valor de result.total_seats");
    }

    @Test
    void shouldCallIteratorWhenResultIsNull() throws MalformedURLException, NoSuchFieldException, IllegalAccessException {
        // Arrange
        GitHub mockRoot = mock(GitHub.class);
        GitHubRequest fakeRequest = GitHubRequest.newBuilder()
                .withApiUrl("https://api.github.com")
                .withUrlPath("/scim/v2/Users")
                .method("GET")
                .build();

        GitHubCopilotSeatPagedSearchIterable<GitHubCopilotSeatPageIterator> iterable =
                spy(new GitHubCopilotSeatPagedSearchIterable<>(mockRoot, fakeRequest, (Class) GitHubCopilotSeatsSearchResult.class));

        // Mock do iterator
        PagedIterator<Object> mockIterator = mock(PagedIterator.class);
        when(mockIterator.hasNext()).thenReturn(false);

        // Retorna o mock quando o método iterator() for chamado
        doReturn(mockIterator).when(iterable).iterator();

        // Garante que result é nulo
        Field resultField = GitHubCopilotSeatPagedSearchIterable.class.getDeclaredField("result");
        resultField.setAccessible(true);
        resultField.set(iterable, null);

        // Act
        iterable.populate();

        // Assert
        verify(iterable, times(1)).iterator();  // Verifica que chamou iterator()
        verify(mockIterator, times(1)).hasNext();  // Verifica que tentou iterar
    }

    @Test
    void shouldNotCallIteratorWhenResultIsNotNull() throws Exception {
        GitHub mockRoot = mock(GitHub.class);
        GitHubRequest fakeRequest = GitHubRequest.newBuilder()
                .withApiUrl("https://api.github.com")
                .withUrlPath("/scim/v2/Users")
                .method("GET")
                .build();

        GitHubCopilotSeatPagedSearchIterable<Object> iterable =
                spy(new GitHubCopilotSeatPagedSearchIterable<>(mockRoot, fakeRequest, (Class) GitHubCopilotSeatsSearchResult.class));

        PagedIterator<Object> mockIterator = mock(PagedIterator.class);
        doReturn(mockIterator).when(iterable).iterator();

        Field resultField = GitHubCopilotSeatPagedSearchIterable.class.getDeclaredField("result");
        resultField.setAccessible(true);
        resultField.set(iterable, new GitHubCopilotSeatsSearchResult<>());

        iterable.populate();
        verify(iterable, never()).iterator();
    }

}

