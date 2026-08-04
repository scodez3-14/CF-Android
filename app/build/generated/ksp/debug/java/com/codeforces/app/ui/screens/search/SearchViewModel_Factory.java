package com.codeforces.app.ui.screens.search;

import com.codeforces.app.data.repository.CodeforcesRepository;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata
@QualifierMetadata
@DaggerGenerated
@Generated(
    value = "dagger.internal.codegen.ComponentProcessor",
    comments = "https://dagger.dev"
)
@SuppressWarnings({
    "unchecked",
    "rawtypes",
    "KotlinInternal",
    "KotlinInternalInJava",
    "cast"
})
public final class SearchViewModel_Factory implements Factory<SearchViewModel> {
  private final Provider<CodeforcesRepository> repoProvider;

  public SearchViewModel_Factory(Provider<CodeforcesRepository> repoProvider) {
    this.repoProvider = repoProvider;
  }

  @Override
  public SearchViewModel get() {
    return newInstance(repoProvider.get());
  }

  public static SearchViewModel_Factory create(Provider<CodeforcesRepository> repoProvider) {
    return new SearchViewModel_Factory(repoProvider);
  }

  public static SearchViewModel newInstance(CodeforcesRepository repo) {
    return new SearchViewModel(repo);
  }
}
