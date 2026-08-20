package com.mediwise.common.util;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

/**
 * Centralises Pageable construction with safe defaults and max-page-size guards.
 */
public final class PaginationUtil {

    public static final int DEFAULT_PAGE = 0;
    public static final int DEFAULT_SIZE = 10;
    public static final int MAX_SIZE = 100;

    private PaginationUtil() {}

    /**
     * Build a Pageable with bounds-checking.
     *
     * @param page      zero-based page index (clamped to >= 0)
     * @param size      page size (clamped to [1, MAX_SIZE])
     * @param sortBy    field name to sort by
     * @param direction "asc" or "desc" (case-insensitive)
     */
    public static Pageable of(int page, int size, String sortBy, String direction) {
        int safePage = Math.max(0, page);
        int safeSize = Math.min(Math.max(1, size), MAX_SIZE);
        Sort sort = "asc".equalsIgnoreCase(direction)
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        return PageRequest.of(safePage, safeSize, sort);
    }

    /**
     * Build a Pageable with default sort (no explicit field).
     */
    public static Pageable of(int page, int size) {
        int safePage = Math.max(0, page);
        int safeSize = Math.min(Math.max(1, size), MAX_SIZE);
        return PageRequest.of(safePage, safeSize);
    }

    /**
     * Default pageable — page 0, size 10, unsorted.
     */
    public static Pageable defaultPageable() {
        return PageRequest.of(DEFAULT_PAGE, DEFAULT_SIZE);
    }
}
