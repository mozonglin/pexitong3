package com.example.pexitong2.dto.race;

import java.util.List;

/**
 * 管理端分页查询成绩列表的响应体
 */
public class RaceResultPageResponse {

    private List<RaceResultResponse> list;
    private long total;
    private int page;
    private int pageSize;
    private int totalPages;

    public RaceResultPageResponse() {}

    public RaceResultPageResponse(List<RaceResultResponse> list, long total, int page, int pageSize) {
        this.list       = list;
        this.total      = total;
        this.page       = page;
        this.pageSize   = pageSize;
        this.totalPages = (int) Math.ceil((double) total / pageSize);
    }

    public List<RaceResultResponse> getList() { return list; }
    public void setList(List<RaceResultResponse> list) { this.list = list; }

    public long getTotal() { return total; }
    public void setTotal(long total) { this.total = total; }

    public int getPage() { return page; }
    public void setPage(int page) { this.page = page; }

    public int getPageSize() { return pageSize; }
    public void setPageSize(int pageSize) { this.pageSize = pageSize; }

    public int getTotalPages() { return totalPages; }
    public void setTotalPages(int totalPages) { this.totalPages = totalPages; }
}
