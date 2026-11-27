package com.kazforge.jsonapi.demo;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;

class ArticleQueryHttpTest extends HttpIntegrationTest {
  private static final String SELECTION =
      "filter%5Bstatus%5D=PUBLISHED"
          + "&sort=-title"
          + "&include=author,reviewers"
          + "&fields%5Barticles%5D=title,author,reviewers"
          + "&fields%5Bpeople%5D=name";

  @Test
  void filtersSortsPagesAndAppliesSelectionOnEveryPage() {
    var first = getJson("/articles?" + SELECTION + "&page%5Bsize%5D=1");
    assertThat(ids(first.path("data"))).containsExactly(SeedData.EDITORIAL_GUIDE.toString());
    assertThat(first.at("/meta/total").asLong()).isEqualTo(2);
    assertThat(first.at("/data/0/attributes").propertyNames()).containsExactly("title");
    assertThat(first.at("/data/0/relationships/author/data/id").asString())
        .isEqualTo(SeedData.GRACE.toString());
    assertThat(ids(first.at("/data/0/relationships/reviewers/data")))
        .containsExactly(SeedData.ADA.toString());
    assertThat(ids(first.path("included")))
        .containsExactlyInAnyOrder(SeedData.ADA.toString(), SeedData.GRACE.toString());
    assertThat(first.at("/included/0/attributes").propertyNames()).containsExactly("name");
    assertThat(first.path("links").has("prev")).isFalse();
    assertThat(first.at("/links/self").asString()).isEqualTo(first.at("/links/first").asString());
    assertThat(first.at("/links/next").asString()).isEqualTo(first.at("/links/last").asString());

    var second = getJson(first.at("/links/next").asString());
    assertThat(ids(second.path("data"))).containsExactly(SeedData.API_GUIDE.toString());
    assertThat(second.at("/meta/total").asLong()).isEqualTo(2);
    assertThat(second.at("/data/0/attributes").propertyNames()).containsExactly("title");
    assertThat(ids(second.at("/data/0/relationships/reviewers/data")))
        .containsExactly(SeedData.GRACE.toString(), SeedData.URSULA.toString());
    assertThat(ids(second.path("included")))
        .containsExactlyInAnyOrder(
            SeedData.ADA.toString(), SeedData.GRACE.toString(), SeedData.URSULA.toString());
    assertThat(second.path("links").has("next")).isFalse();
    assertThat(second.at("/links/prev").asString()).isEqualTo(first.at("/links/self").asString());

    var compound = getJson("/articles?" + SELECTION);
    assertThat(ids(compound.path("data")))
        .containsExactly(SeedData.EDITORIAL_GUIDE.toString(), SeedData.API_GUIDE.toString());
    assertThat(ids(compound.path("included")))
        .containsExactlyInAnyOrder(
            SeedData.ADA.toString(), SeedData.GRACE.toString(), SeedData.URSULA.toString());

    var empty = getJson("/articles?" + SELECTION + "&page%5Bnumber%5D=3");
    assertThat(ids(empty.path("data"))).isEmpty();
    assertThat(empty.at("/meta/total").asLong()).isEqualTo(2);
    assertThat(empty.path("included").isArray()).isTrue();
    assertThat(empty.path("included").isEmpty()).isTrue();
  }

  @Test
  void breaksSortTiesByArticleIdAcrossPages() {
    String first = createArticle(draftDocument("Same title"));
    String second = createArticle(draftDocument("Same title"));
    List<String> expected = List.of(first, second).stream().sorted().toList();
    String query =
        "/articles?filter%5Bstatus%5D=DRAFT&sort=title&page%5Bsize%5D=1&page%5Bnumber%5D=";

    assertThat(ids(getJson(query + "1").path("data")))
        .containsExactly(SeedData.DRAFT_NOTES.toString());
    assertThat(ids(getJson(query + "2").path("data"))).containsExactly(expected.getFirst());
    assertThat(ids(getJson(query + "3").path("data"))).containsExactly(expected.getLast());
  }
}
