package com.codeforces.app.ui.screens.leaderboard;

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
public final class LeaderboardViewModel_Factory implements Factory<LeaderboardViewModel> {
  private final Provider<CodeforcesRepository> repoProvider;

  public LeaderboardViewModel_Factory(Provider<CodeforcesRepository> repoProvider) {
    this.repoProvider = repoProvider;
  }

  @Override
  public LeaderboardViewModel get() {
    return newInstance(repoProvider.get());
  }

  public static LeaderboardViewModel_Factory create(Provider<CodeforcesRepository> repoProvider) {
    return new LeaderboardViewModel_Factory(repoProvider);
  }

  public static LeaderboardViewModel newInstance(CodeforcesRepository repo) {
    return new LeaderboardViewModel(repo);
  }
}
