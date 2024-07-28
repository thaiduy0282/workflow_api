package com.qworks.workflow.helper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.HashMap;
import java.util.Map;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class LinkHelper {

    public static Map<String, String> createLinks(String workflowId) {
        UriComponentsBuilder baseUri = ServletUriComponentsBuilder.fromCurrentRequestUri();

        Map<String, String> links = new HashMap<>();
        links.put("self", baseUri.path("/{id}").buildAndExpand(workflowId).toUriString());

        return links;
    }

}
