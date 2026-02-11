package org.kohsuke.github;

import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

public class TestSCIMPagedSearchIterable<T> extends SCIMPagedSearchIterable<T> {
    private final List<T> items;
    private final int totalCount;

    public TestSCIMPagedSearchIterable(List<T> items, int totalCount) {
        super(null, null, null);
        this.items = items;
        this.totalCount = totalCount;
    }

    @Override
    public int getTotalCount() {
        return totalCount;
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
