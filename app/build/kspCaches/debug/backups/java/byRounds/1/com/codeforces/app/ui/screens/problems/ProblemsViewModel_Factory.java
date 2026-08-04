package com.codeforces.app.ui.screens.problems;

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
public final class ProblemsViewModel_Factory implements Factory<ProblemsViewModel> {
  private final Provider<CodeforcesRepository> repoProvider;

  public ProblemsViewModel_Factory(Provider<CodeforcesRepository> repoProvider) {
    this.repoProvider = repoProvider;
  }

  @Override
  public ProblemsViewModel get() {
    return newInstance(repoProvider.get());
  }

  public static ProblemsViewModel_Factory create(Provider<CodeforcesRepository> repoProvider) {
    return new ProblemsViewModel_Factory(repoProvider);
  }

  public static ProblemsViewModel newInstance(CodeforcesRepository repo) {
    return new ProblemsViewModel(repo);
  }
}
