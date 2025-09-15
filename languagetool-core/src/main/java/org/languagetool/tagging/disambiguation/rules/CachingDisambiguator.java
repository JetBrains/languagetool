package org.languagetool.tagging.disambiguation.rules;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.google.common.collect.MapMaker;
import org.jspecify.annotations.NonNull;
import org.languagetool.AnalyzedSentence;
import org.languagetool.tagging.disambiguation.AbstractDisambiguator;
import org.languagetool.tagging.disambiguation.Disambiguator;

import java.io.IOException;

public class CachingDisambiguator extends AbstractDisambiguator {

  private static final Cache<@NonNull String, AnalyzedSentence> CACHE = Caffeine.newBuilder()
    .maximumSize(1024)
    .softValues()
    .build();


  private final Disambiguator disambiguator;
  public CachingDisambiguator(Disambiguator disambiguator) {
    this.disambiguator = disambiguator;
  }

  @Override
  public AnalyzedSentence disambiguate(AnalyzedSentence input) throws IOException {
    try {
      return CACHE.get(input.getText(), key -> {
        try {
          return disambiguator.disambiguate(input);
        } catch (IOException e) {
          throw new RuntimeException(e);
        }
      });
    } catch (RuntimeException e) {
      if (e.getCause() instanceof IOException io) throw io;
      throw e;
    }
  }
}
