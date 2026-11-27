package com.kazforge.jsonapi.demo.controller;

import com.kazforge.jsonapi.core.model.DocumentData;
import com.kazforge.jsonapi.core.model.JsonApiDocument;
import com.kazforge.jsonapi.core.model.ResourceObject;
import com.kazforge.jsonapi.representation.RepresentationSelection;
import com.kazforge.jsonapi.spring.webmvc.document.JsonApiDocumentResponseWriter;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

/**
 * Writes the related-author resource when the article has no author.
 *
 * <p>Spring 0.7.0 has no typed nullable related-resource response: a Java {@code null} writes no
 * body, so this low-level document is confined to the empty case. Field selection does not apply to
 * null data; an explicit empty {@code include} still produces an empty included array.
 */
@Component
public class EmptyArticleAuthorResponse {
  private final JsonApiDocumentResponseWriter documentWriter;

  public EmptyArticleAuthorResponse(JsonApiDocumentResponseWriter documentWriter) {
    this.documentWriter = documentWriter;
  }

  public void write(RepresentationSelection selection, HttpServletResponse response)
      throws IOException {
    List<ResourceObject> included = selection.includeRequested() ? List.of() : null;
    var document =
        new JsonApiDocument(
            DocumentData.NullData.INSTANCE, null, null, null, null, included, Map.of());
    documentWriter.write(document, HttpServletResponse.SC_OK, response);
  }
}
