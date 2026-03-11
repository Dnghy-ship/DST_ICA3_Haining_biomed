package cn.edu.zju.bean;

import java.util.List;

/**
 * Lightweight pagination metadata wrapper.
 *
 * @param <T> the type of items on the page
 */
public class Page<T> {

    private final List<T> items;
    private final int page;        // current page (1-based)
    private final int pageSize;
    private final int totalCount;
    private final int totalPages;

    public Page(List<T> items, int page, int pageSize, int totalCount) {
        this.items = items;
        this.page = page;
        this.pageSize = pageSize;
        this.totalCount = totalCount;
        this.totalPages = (pageSize > 0 && totalCount > 0)
                ? (int) Math.ceil((double) totalCount / pageSize)
                : (totalCount > 0 ? 1 : 0);
    }

    public List<T> getItems() {
        return items;
    }

    public int getPage() {
        return page;
    }

    public int getPageSize() {
        return pageSize;
    }

    public int getTotalCount() {
        return totalCount;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public boolean isHasPrev() {
        return page > 1;
    }

    public boolean isHasNext() {
        return page < totalPages;
    }
}
