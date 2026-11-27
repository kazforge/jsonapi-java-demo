package com.kazforge.jsonapi.demo.config;

import com.kazforge.jsonapi.demo.dto.ArticleResource;
import com.kazforge.jsonapi.representation.IncludePolicy;
import com.kazforge.jsonapi.representation.RelationshipAllowance;
import com.kazforge.jsonapi.representation.RepresentationPolicy;
import java.util.stream.Collectors;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class JsonApiConfiguration {
  @Bean
  RepresentationPolicy representationPolicy() {
    var allowances =
        ArticleIncludes.ALLOWED.stream()
            .map(path -> RelationshipAllowance.of(ArticleResource.TYPE, path.dotted()))
            .collect(Collectors.toUnmodifiableSet());
    return RepresentationPolicy.defaults().withIncludePolicy(IncludePolicy.allowing(allowances));
  }
}
