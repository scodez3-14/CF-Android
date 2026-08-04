package com.codeforces.app.ui.screens.contests;

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
public final class ContestViewModel_Factory implements Factory<ContestViewModel> {
  private final Provider<CodeforcesRepository> repoProvider;

  public ContestViewModel_Factory(Provider<CodeforcesRepository> repoProvider) {
    this.repoProvider = repoProvider;
  }

  @Override
  public ContestViewModel get() {
    return newInstance(repoProvider.get());
  }

  public static ContestViewModel_Factory create(Provider<CodeforcesRepository> repoProvider) {
    return new ContestViewModel_Factory(repoProvider);
  }

  public static ContestViewModel newInstance(CodeforcesRepository repo) {
    return new ContestViewModel(repo);
  }
}
