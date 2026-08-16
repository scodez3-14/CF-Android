package com.codeforces.app.ui.screens.home;

import com.codeforces.app.data.recommendation.RecommendationRepository;
import com.codeforces.app.data.repository.CodeforcesRepository;
import com.codeforces.app.data.repository.UserPreferencesRepository;
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
public final class HomeViewModel_Factory implements Factory<HomeViewModel> {
  private final Provider<CodeforcesRepository> repoProvider;

  private final Provider<RecommendationRepository> recommendationRepoProvider;

  private final Provider<UserPreferencesRepository> prefsProvider;

  public HomeViewModel_Factory(Provider<CodeforcesRepository> repoProvider,
      Provider<RecommendationRepository> recommendationRepoProvider,
      Provider<UserPreferencesRepository> prefsProvider) {
    this.repoProvider = repoProvider;
    this.recommendationRepoProvider = recommendationRepoProvider;
    this.prefsProvider = prefsProvider;
  }

  @Override
  public HomeViewModel get() {
    return newInstance(repoProvider.get(), recommendationRepoProvider.get(), prefsProvider.get());
  }

  public static HomeViewModel_Factory create(Provider<CodeforcesRepository> repoProvider,
      Provider<RecommendationRepository> recommendationRepoProvider,
      Provider<UserPreferencesRepository> prefsProvider) {
    return new HomeViewModel_Factory(repoProvider, recommendationRepoProvider, prefsProvider);
  }

  public static HomeViewModel newInstance(CodeforcesRepository repo,
      RecommendationRepository recommendationRepo, UserPreferencesRepository prefs) {
    return new HomeViewModel(repo, recommendationRepo, prefs);
  }
}
