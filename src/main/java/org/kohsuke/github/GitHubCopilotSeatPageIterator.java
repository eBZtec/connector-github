package org.kohsuke.github;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Used for seats pagination information.
 * <p>
 * This class is not thread-safe. Any one instance should only be called from a single thread.
 *
 * @param <T> the type parameter
 * @author Hiroyuki Wada
 * @author Nikolas Correa
 */
public class GitHubCopilotSeatPageIterator<T extends GitHubCopilotSeatsSearchResult> implements Iterator<T> {

    private final GitHubClient client;
    private final Class<T> type;

    private T next;

    private GitHubRequest nextRequest;

    private GitHubResponse<T> finalResponse = null;

    private GitHubCopilotSeatPageIterator(GitHubClient client, Class<T> type, GitHubRequest request) {
        if (!"GET".equals(request.method())) {
            throw new IllegalStateException("Request method \"GET\" is required for page iterator.");
        }

        this.client = client;
        this.type = type;
        this.nextRequest = request;
    }

    static <T extends GitHubCopilotSeatsSearchResult> GitHubCopilotSeatPageIterator<T> create(GitHubClient client, Class<T> type, GitHubRequest request, int pageSize, int pageOffset) {

        try {
            if (pageSize > 0) {
                GitHubRequest.Builder<?> builder = request.toBuilder().with("count", pageSize);
                if (pageOffset > 0) {
                    builder.with("startIndex", pageOffset);
                }
                request = builder.build();
            }

            return new GitHubCopilotSeatPageIterator<>(client, type, request);
        } catch (MalformedURLException e) {
            throw new GHException("Unable to build GitHub SCIM API URL", e);
        }
    }

    public boolean hasNext() {
        fetch();
        return next != null;
    }

    public T next() {
        fetch();
        T result = next;
        if (result == null)
            throw new NoSuchElementException();
        next = null;
        return result;
    }

    public GitHubResponse<T> finalResponse() {
        if (hasNext()) {
            throw new GHException("Final response is not available until after iterator is done.");
        }
        return finalResponse;
    }

    private void fetch() {
        if (next != null || nextRequest == null)
            return;

        URL url = nextRequest.url();
        try {
            GitHubResponse<T> nextResponse = client.sendRequest(nextRequest,
                    (responseInfo) -> GitHubResponse.parseBody(responseInfo, type));
            next = nextResponse.body();
            nextRequest = findNextURL(nextResponse);
            if (nextRequest == null) {
                finalResponse = nextResponse;
            }
        } catch (IOException e) {
            throw new GHException("Failed to retrieve " + url, e);
        }
    }

    private GitHubRequest findNextURL(GitHubResponse<T> response) {
        String linkHeader = response.headerField("Link");
        if (linkHeader == null) return null;

        // Expressão para capturar: <https://api.github.com/...&page=N>; rel="next"
        Pattern pattern = Pattern.compile("<([^>]+)>;\\s*rel=\"next\"");
        Matcher matcher = pattern.matcher(linkHeader);
        if (matcher.find()) {
            String nextUrl = matcher.group(1);
            try {
                return GitHubRequest.newBuilder().withUrlPath(nextUrl).build();
            } catch (Exception e) {
                throw new GHException("Malformed next URL: " + nextUrl, e);
            }
        }
        return null;
    }
}
