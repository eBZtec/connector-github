package org.kohsuke.github;

import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

public class TestGitHubCopilotSeatPagedSearchIterable<T> extends GitHubCopilotSeatPagedSearchIterable<T> {
    private final List<T> items;
    private final int totalSeats;

    public TestGitHubCopilotSeatPagedSearchIterable(List<T> items, int totalSeats) {
        super(null, null, null);
        this.items = items;
        this.totalSeats = totalSeats;
    }

    @Override
    public int getTotalSeats() {
        return totalSeats;
    }

    @Override
    public PagedIterator<T> _iterator(int pageSize) {
        final Iterator<T[]> pages = new Iterator<>() {
            private int idx = 0;

            @Override
            public boolean hasNext() {
                return idx < items.size();
            }

            @Override
            public T[] next() {
                if (!hasNext()) throw new NoSuchElementException();
                @SuppressWarnings("unchecked")
                T[] page = (T[]) new Object[] { items.get(idx++) };
                return page;
            }
        };

        return new PagedIterator<>(pages, null);
    }
}
