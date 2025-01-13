package com.melli.hub;

import lombok.Getter;

@Getter
public class Page {
    private static final int DEFAULT_PAGE_SIZE = 10;
    private static final int DEFAULT_PAGE_NUMBER = 0;
    private static final int MINIMUM_PAGE_SIZE = 1;
    private static final int MINIMUM_PAGE_NUMBER = 0;

    private int pageNumber;
    private int pageSize;

    public Page() {
        new Page(DEFAULT_PAGE_NUMBER, DEFAULT_PAGE_SIZE);
    }

    public Page(int pageNumber, int pageSize) {
        setPageNumber(pageNumber);
        setPageSize(pageSize);
    }

    public static Page getDefaultPage() {
        return new Page(DEFAULT_PAGE_NUMBER, DEFAULT_PAGE_SIZE);
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize > MINIMUM_PAGE_SIZE ? pageSize : DEFAULT_PAGE_SIZE;
    }

    public void setPageNumber(int pageNumber) {
        this.pageNumber = pageNumber > MINIMUM_PAGE_NUMBER ? pageNumber : DEFAULT_PAGE_NUMBER;
    }
}
