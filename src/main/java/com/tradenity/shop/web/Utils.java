package com.tradenity.shop.web;

import com.tradenity.sdk.model.PageRequest;

/**
 * Created by joseph
 * on 3/21/16.
 */
public class Utils {
    public static PageRequest mapPageable(org.springframework.data.domain.Pageable pageable){
        return new PageRequest(pageable.getPageNumber(), pageable.getPageSize(), pageable.getOffset());
    }
}
