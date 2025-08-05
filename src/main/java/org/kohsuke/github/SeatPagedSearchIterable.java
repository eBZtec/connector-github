package org.kohsuke.github;

import java.util.Iterator;

/**
 * {@link PagedIterable} especializada para resultados com paginação de assentos (seats).
 *
 * @param <T> tipo do recurso listado (por exemplo, Seat)
 */
public class SeatPagedSearchIterable<T> extends PagedIterable<T> {
    private final transient GitHub root;
    private final GitHubRequest request;
    private final Class<? extends SeatSearchResult<T>> receiverType;

    private SeatSearchResult<T> result;
    private int pageOffset;

    public SeatPagedSearchIterable(GitHub root, GitHubRequest request, Class<? extends SeatSearchResult<T>> receiverType) {
        this.root = root;
        this.request = request;
        this.receiverType = receiverType;
    }

    @Override
    public SeatPagedSearchIterable<T> withPageSize(int size) {
        return (SeatPagedSearchIterable<T>) super.withPageSize(size);
    }

    public SeatPagedSearchIterable<T> withPageOffset(int pageOffset) {
        this.pageOffset = pageOffset;
        return this;
    }

    public int getTotalSeats() {
        populate();
        return result.total_seats;
    }

    public boolean isIncomplete() {
        populate();
        return result.seats.length == 0;
    }

    private void populate() {
        if (result == null)
            iterator().hasNext();  // dispara a carga inicial
    }

    @Override
    public PagedIterator<T> _iterator(int pageSize) {
        final Iterator<T[]> adapter = adapt(
                SeatPageIterator.create(root.getClient(), receiverType, request, pageSize, pageOffset));
        return new PagedIterator<>(adapter, null);
    }

    protected Iterator<T[]> adapt(final Iterator<? extends SeatSearchResult<T>> base) {
        return new Iterator<T[]>() {
            public boolean hasNext() {
                return base.hasNext();
            }

            public T[] next() {
                SeatSearchResult<T> v = base.next();
                if (result == null)
                    result = v;
                return v.seats;
            }
        };
    }
}
