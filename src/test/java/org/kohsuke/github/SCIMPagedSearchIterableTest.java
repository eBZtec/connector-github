package org.kohsuke.github;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.util.Iterator;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SuppressWarnings({"unchecked", "rawtypes"})
class SCIMPagedSearchIterableTest {

    @Test
    void testAdaptReturnsResourcesAndCachesResult() {
        SCIMSearchResult<String> result = new SCIMSearchResult<>();
        result.Resources = new String[]{"X", "Y"};

        Iterator<SCIMSearchResult<String>> baseIterator = mock(Iterator.class);
        when(baseIterator.hasNext()).thenReturn(true, false);
        when(baseIterator.next()).thenReturn(result);

        SCIMPagedSearchIterable<String> iterable =
                new SCIMPagedSearchIterable<>(mock(GitHub.class), mock(GitHubRequest.class), (Class) SCIMSearchResult.class);

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

        SCIMPagedSearchIterable<String> iterable =
                new SCIMPagedSearchIterable<>(mockRoot, mockReq, (Class) SCIMSearchResult.class);

        SCIMPagedSearchIterable<String> result1 = iterable.withPageSize(25);
        SCIMPagedSearchIterable<String> result2 = iterable.withPageOffset(3);

        assertSame(iterable, result2);
        assertNotNull(result1);
    }

    @Test
    void testListThrowsGHExceptionWhenMalformedUrl() throws Exception {
        GitHub github = GitHub.connectAnonymously();
        GHOrganization org = github.getOrganization("square");

        @SuppressWarnings("unchecked")
        SCIMSearchBuilder<String> builder = new SCIMSearchBuilder<String>(
                github,
                org,
                (Class<? extends SCIMSearchResult<String>>) (Class<?>) SCIMSearchResult.class) {

            @Override
            protected String getApiUrl() {
                return "/scim/v2/Users";
            }

            @Override
            public SCIMPagedSearchIterable<String> list() {
                try {
                    throw new MalformedURLException("URL malformada!");
                } catch (MalformedURLException e) {
                    throw new GHException("", e);
                }
            }
        };

        GHException thrown = assertThrows(GHException.class, builder::list);
        assertTrue(thrown.getCause() instanceof MalformedURLException);
        assertEquals("URL malformada!", thrown.getCause().getMessage());
    }

    @Test
    void testGetTotalCountReturnsResultTotalResults() throws Exception {
        GitHub github = GitHub.connectAnonymously();
        GitHubRequest request = mock(GitHubRequest.class);

        @SuppressWarnings("unchecked")
        SCIMPagedSearchIterable<String> iterable = new SCIMPagedSearchIterable<String>(
                github,
                request,
                (Class<? extends SCIMSearchResult<String>>) (Class<?>) SCIMSearchResult.class
        );

        SCIMSearchResult<String> fakeResult = new SCIMSearchResult<>();
        fakeResult.totalResults = 42;
        fakeResult.startIndex = 0;
        fakeResult.itemsPerPage = 100;
        iterable.result = fakeResult;

        int total = iterable.getTotalCount();
        assertEquals(42, total, "getTotalCount deve retornar o valor de result.totalResults");
    }

    @Test
    void testIsIncompleteWhenResultsAreComplete() {
        GitHub github = mock(GitHub.class);
        GitHubRequest request = mock(GitHubRequest.class);

        @SuppressWarnings("unchecked")
        SCIMPagedSearchIterable<String> iterable =
                new SCIMPagedSearchIterable<>(
                        github,
                        request,
                        (Class<? extends SCIMSearchResult<String>>) (Class<?>) SCIMSearchResult.class
                );

        SCIMSearchResult<String> fakeResult = new SCIMSearchResult<>();
        fakeResult.totalResults = 100;
        fakeResult.startIndex = 0;
        fakeResult.itemsPerPage = 100;
        iterable.result = fakeResult;

        boolean incomplete = iterable.isIncomplete();
        assertTrue(incomplete, "Quando totalResults <= startIndex + itemsPerPage, deve ser true");
    }

    @Test
    void testIsIncompleteWhenResultsAreNotComplete() {
        GitHub github = mock(GitHub.class);
        GitHubRequest request = mock(GitHubRequest.class);

        @SuppressWarnings("unchecked")
        SCIMPagedSearchIterable<String> iterable =
                new SCIMPagedSearchIterable<>(
                        github,
                        request,
                        (Class<? extends SCIMSearchResult<String>>) (Class<?>) SCIMSearchResult.class
                );

        SCIMSearchResult<String> fakeResult = new SCIMSearchResult<>();
        fakeResult.totalResults = 200;
        fakeResult.startIndex = 0;
        fakeResult.itemsPerPage = 100;
        iterable.result = fakeResult;

        boolean incomplete = iterable.isIncomplete();
        assertFalse(incomplete, "Quando totalResults > startIndex + itemsPerPage, deve ser false");
    }

    @Test
    void testPopulateWhenResultIsNotNull() throws Exception {
        GitHub github = mock(GitHub.class);
        GitHubRequest request = mock(GitHubRequest.class);

        @SuppressWarnings("unchecked")
        SCIMPagedSearchIterable<String> iterable =
                spy(new SCIMPagedSearchIterable<>(
                        github,
                        request,
                        (Class<? extends SCIMSearchResult<String>>) (Class<?>) SCIMSearchResult.class
                ));

        iterable.result = new SCIMSearchResult<>();
        java.lang.reflect.Method m = SCIMPagedSearchIterable.class.getDeclaredMethod("populate");
        m.setAccessible(true);
        m.invoke(iterable);

        verify(iterable, never()).iterator();
    }

    @Test
    void shouldCallIteratorWhenResultIsNull() throws MalformedURLException, NoSuchFieldException, IllegalAccessException {
        GitHub mockRoot = mock(GitHub.class);
        GitHubRequest fakeRequest = GitHubRequest.newBuilder()
                .withApiUrl("https://api.github.com")
                .withUrlPath("/scim/v2/Users")
                .method("GET")
                .build();

        SCIMPagedSearchIterable iterable =
                spy(new SCIMPagedSearchIterable<>(mockRoot, fakeRequest, (Class) SCIMSearchResult.class));

        PagedIterator<Object> mockIterator = mock(PagedIterator.class);
        when(mockIterator.hasNext()).thenReturn(false);

        doReturn(mockIterator).when(iterable).iterator();

        Field resultField = SCIMPagedSearchIterable.class.getDeclaredField("result");
        resultField.setAccessible(true);
        resultField.set(iterable, null);

        iterable.populate();
        verify(iterable, times(1)).iterator();
        verify(mockIterator, times(1)).hasNext();
    }

    @Test
    void shouldNotCallIteratorWhenResultIsNotNull() throws Exception {
        GitHub mockRoot = mock(GitHub.class);
        GitHubRequest fakeRequest = GitHubRequest.newBuilder()
                .withApiUrl("https://api.github.com")
                .withUrlPath("/scim/v2/Users")
                .method("GET")
                .build();

        SCIMPagedSearchIterable iterable =
                spy(new SCIMPagedSearchIterable<>(mockRoot, fakeRequest, (Class) SCIMSearchResult.class));

        PagedIterator<Object> mockIterator = mock(PagedIterator.class);
        doReturn(mockIterator).when(iterable).iterator();

        Field resultField = SCIMPagedSearchIterable.class.getDeclaredField("result");
        resultField.setAccessible(true);
        resultField.set(iterable, new SCIMSearchResult<>());

        iterable.populate();
        verify(iterable, never()).iterator();
    }
}
