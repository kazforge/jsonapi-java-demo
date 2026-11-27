package com.kazforge.jsonapi.demo.controller;

import static java.nio.charset.StandardCharsets.UTF_8;

import com.kazforge.jsonapi.core.model.Link;
import com.kazforge.jsonapi.core.model.Links;
import com.kazforge.jsonapi.core.model.Meta;
import com.kazforge.jsonapi.demo.dto.ArticleResource;
import com.kazforge.jsonapi.spring.webmvc.document.JsonApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.web.util.UriUtils;

/** Builds the article collection envelope: pagination links and total metadata. */
@Component
public class ArticleCollectionAssembler {
  private static final String PAGE_NUMBER = "page[number]";
  private static final String PAGE_SIZE = "page[size]";

  public JsonApiResponse<List<ArticleResource>> assemble(
      Page<ArticleResource> page, HttpServletRequest request) {
    int number = page.getNumber() + 1;
    int lastPage = Math.max(1, page.getTotalPages());
    int size = page.getSize();

    Map<String, Link> links = new LinkedHashMap<>();
    links.put("self", pageLink(request, number, size));
    links.put("first", pageLink(request, 1, size));
    links.put("last", pageLink(request, lastPage, size));
    if (number > 1) {
      links.put("prev", pageLink(request, Math.min(number - 1, lastPage), size));
    }
    if (page.hasNext()) {
      links.put("next", pageLink(request, number + 1, size));
    }
    return JsonApiResponse.of(page.getContent())
        .withMeta(Meta.of(Map.of("total", page.getTotalElements())))
        .withLinks(Links.ofLinks(links));
  }

  private static Link pageLink(HttpServletRequest request, int number, int size) {
    var uri = UriComponentsBuilder.fromPath(request.getRequestURI());
    request
        .getParameterMap()
        .forEach(
            (name, values) -> {
              if (!PAGE_NUMBER.equals(name) && !PAGE_SIZE.equals(name)) {
                for (String value : values) {
                  uri.queryParam(UriUtils.encode(name, UTF_8), UriUtils.encode(value, UTF_8));
                }
              }
            });
    uri.queryParam("page%5Bnumber%5D", number).queryParam("page%5Bsize%5D", size);
    return new Link.StringLink(uri.build(true).toUriString());
  }
}
