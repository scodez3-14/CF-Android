package com.codeforces.app.ui.screens.blog;

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
public final class BlogViewModel_Factory implements Factory<BlogViewModel> {
  private final Provider<CodeforcesRepository> repoProvider;

  public BlogViewModel_Factory(Provider<CodeforcesRepository> repoProvider) {
    this.repoProvider = repoProvider;
  }

  @Override
  public BlogViewModel get() {
    return newInstance(repoProvider.get());
  }

  public static BlogViewModel_Factory create(Provider<CodeforcesRepository> repoProvider) {
    return new BlogViewModel_Factory(repoProvider);
  }

  public static BlogViewModel newInstance(CodeforcesRepository repo) {
    return new BlogViewModel(repo);
  }
}
