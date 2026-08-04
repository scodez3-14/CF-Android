package com.codeforces.app.ui.screens.standings;

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
public final class StandingsViewModel_Factory implements Factory<StandingsViewModel> {
  private final Provider<CodeforcesRepository> repoProvider;

  public StandingsViewModel_Factory(Provider<CodeforcesRepository> repoProvider) {
    this.repoProvider = repoProvider;
  }

  @Override
  public StandingsViewModel get() {
    return newInstance(repoProvider.get());
  }

  public static StandingsViewModel_Factory create(Provider<CodeforcesRepository> repoProvider) {
    return new StandingsViewModel_Factory(repoProvider);
  }

  public static StandingsViewModel newInstance(CodeforcesRepository repo) {
    return new StandingsViewModel(repo);
  }
}
