package com.kazforge.jsonapi.demo.spring;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.mock.web.MockServletContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

class WebMvcIntegrationTest {
  private static final String JSON_API = "application/vnd.api+json";
  private final JsonMapper mapper = JsonMapper.builder().build();
  private AnnotationConfigWebApplicationContext context;
  private MockMvc mvc;

  @BeforeEach
  void setup() {
    context = new AnnotationConfigWebApplicationContext();
    context.setServletContext(new MockServletContext());
    context.register(WebMvcConfiguration.class);
    context.refresh();
    mvc = MockMvcBuilders.webAppContextSetup(context).build();
  }

  @AfterEach
  void close() {
    context.close();
  }

  @Test
  void readsSingleResourceAndCollection() throws Exception {
    JsonNode one =
        body(
            mvc.perform(get("/articles/42").accept(JSON_API))
                .andExpect(status().isOk())
                .andExpect(content().contentType(JSON_API))
                .andExpect(header().string("Vary", "Accept"))
                .andReturn());
    assertEquals("articles", one.at("/data/type").asString());
    assertEquals("42", one.at("/data/id").asString());
    assertEquals("Hello JSON:API", one.at("/data/attributes/title").asString());

    JsonNode many =
        body(
            mvc.perform(get("/articles").accept(JSON_API))
                .andExpect(status().isOk())
                .andExpect(content().contentType(JSON_API))
                .andReturn());
    assertTrue(many.path("data").isArray());
    assertEquals(1, many.path("data").size());
    assertEquals(one.path("data"), many.at("/data/0"));
  }

  @Test
  void bindsCreateDtoWithoutClientId() throws Exception {
    JsonNode created =
        body(
            mvc.perform(
                    post("/articles")
                        .contentType(JSON_API)
                        .accept(JSON_API)
                        .content(
                            """
            {"data":{"type":"articles","attributes":{"title":"Created without ID"}}}
            """))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/articles/42"))
                .andExpect(header().string("Vary", "Accept"))
                .andExpect(content().contentType(JSON_API))
                .andReturn());
    assertEquals("articles", created.at("/data/type").asString());
    assertEquals("42", created.at("/data/id").asString());
    assertEquals("Created without ID", created.at("/data/attributes/title").asString());
  }

  @ParameterizedTest
  @ValueSource(strings = {"*/*", "application/*", "application/vnd.api+json"})
  void negotiatesJsonApi(String accept) throws Exception {
    mvc.perform(get("/articles/42").accept(accept))
        .andExpect(status().isOk())
        .andExpect(content().contentType(JSON_API))
        .andExpect(header().string("Vary", "Accept"));
  }

  @Test
  void rejectsUnacceptableMedia() throws Exception {
    assertError(
        mvc.perform(get("/articles").accept("application/xml"))
            .andExpect(status().isNotAcceptable())
            .andReturn(),
        "406");
  }

  @ParameterizedTest
  @ValueSource(strings = {"application/json", "application/vnd.api+json; charset=UTF-8"})
  void rejectsUnsupportedContentType(String contentType) throws Exception {
    assertError(
        mvc.perform(post("/articles").contentType(contentType).accept(JSON_API).content("{}"))
            .andExpect(status().isUnsupportedMediaType())
            .andReturn(),
        "415");
  }

  @Test
  void translatesMalformedInputToSafeProtocolError() throws Exception {
    MvcResult result =
        mvc.perform(
                post("/articles")
                    .contentType(JSON_API)
                    .accept(JSON_API)
                    .content("{private-invalid-input"))
            .andExpect(status().isBadRequest())
            .andReturn();
    assertError(result, "400");
    assertFalse(result.getResponse().getContentAsString().contains("private-invalid-input"));
  }

  @Test
  void translatesInvalidQueryToProtocolError() throws Exception {
    MvcResult result =
        mvc.perform(get("/articles").queryParam("sort", "").accept(JSON_API))
            .andExpect(status().isBadRequest())
            .andReturn();
    assertError(result, "400");
    JsonNode error = body(result).at("/errors/0");
    assertEquals("INVALID_SORT_SYNTAX", error.path("code").asString());
    assertEquals("sort", error.at("/source/parameter").asString());
  }

  @Test
  void leavesOrdinaryMvcReadsAndWritesAlone() throws Exception {
    JsonNode ordinary =
        body(
            mvc.perform(get("/ordinary").queryParam("sort", "").accept("application/json"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(header().doesNotExist("Vary"))
                .andReturn());
    assertEquals("ordinary MVC", ordinary.path("message").asString());
    assertFalse(ordinary.has("data"));
    JsonNode echoed =
        body(
            mvc.perform(
                    post("/ordinary")
                        .contentType("application/json")
                        .accept("application/json")
                        .content("{\"message\":\"ordinary POST\"}"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(header().doesNotExist("Vary"))
                .andReturn());
    assertEquals("ordinary POST", echoed.path("message").asString());
    assertFalse(echoed.has("data"));
  }

  private JsonNode body(MvcResult result) {
    return mapper.readTree(result.getResponse().getContentAsByteArray());
  }

  private void assertError(MvcResult result, String status) {
    assertEquals(JSON_API, result.getResponse().getContentType());
    JsonNode error = body(result);
    assertTrue(error.path("errors").isArray());
    assertEquals(status, error.at("/errors/0/status").asString());
    assertFalse(error.has("data"));
  }
}
