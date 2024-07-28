package com.qworks.workflow.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Getter
@Setter
public class ApiResponse<T> {

    private Metadata metadata;

    private List<T> data;

    public ApiResponse(long totalCount, List<T> data) {
        this.metadata = Metadata.builder()
                .totalCount(totalCount)
                .build();
        this.data = data;
    }

    public ApiResponse(long totalCount, int pageSize, int currentPage, long totalPages, List<T> data) {
        this.metadata = Metadata.builder()
                .totalCount(totalCount)
                .pageSize((long) pageSize)
                .currentPage((long) currentPage)
                .totalPages(totalPages)
                .build();
        this.data = data;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @SuperBuilder(toBuilder = true)
    public static class Metadata {

        private Long totalCount;

        private Long pageSize;

        private Long currentPage;

        private Long totalPages;
    }
}
