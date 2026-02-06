package org.kohsuke.github;

import java.util.Iterator;

/**
 * {@link PagedIterable} enhanced to report search result specific information.
 *
 * @param <T> the type parameter
 * @author Hiroyuki Wada
 * @author Nikolas Correa
 */
public class GitHubCopilotSeatPagedSearchIterable<T> extends PagedIterable<T> {
    private final GitHub root;

    private final GitHubRequest request;

    private final Class<? extends GitHubCopilotSeatsSearchResult<T>> receiverType;

    GitHubCopilotSeatsSearchResult<T> result;

    private int pageOffset;

    public GitHubCopilotSeatPagedSearchIterable(GitHub root, GitHubRequest request, Class<? extends GitHubCopilotSeatsSearchResult<T>> receiverType) {
        this.root = root;
        this.request = request;
        this.receiverType = receiverType;
    }

    @Override
    public GitHubCopilotSeatPagedSearchIterable<T> withPageSize(int size) {
        return (GitHubCopilotSeatPagedSearchIterable<T>) super.withPageSize(size);
    }

    public GitHubCopilotSeatPagedSearchIterable<T> withPageOffset(int pageOffset) {
        this.pageOffset = pageOffset;
        return this;
    }

    public int getTotalSeats() {
        populate();
        return result.total_seats;
    }

    public void populate() {
        if (result == null)
            iterator().hasNext();  // dispara a carga inicial
    }

    @Override
    public PagedIterator<T> _iterator(int pageSize) {
        final Iterator<T[]> adapter = adapt(
                GitHubCopilotSeatPageIterator.create(root.getClient(), receiverType, request, pageSize, pageOffset));
        return new PagedIterator<>(adapter, null);
    }

    public Iterator<T[]> adapt(final Iterator<? extends GitHubCopilotSeatsSearchResult<T>> base) {
        return new Iterator<T[]>() {
            public boolean hasNext() {
                return base.hasNext();
            }

            public T[] next() {
                GitHubCopilotSeatsSearchResult<T> v = base.next();
                if (result == null)
                    result = v;
                return v.seats;
            }
        };
    }
}
