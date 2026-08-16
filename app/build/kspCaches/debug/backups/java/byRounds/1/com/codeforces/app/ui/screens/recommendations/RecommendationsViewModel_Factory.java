package com.codeforces.app.ui.screens.recommendations;

import com.codeforces.app.data.recommendation.RecommendationRepository;
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
public final class RecommendationsViewModel_Factory implements Factory<RecommendationsViewModel> {
  private final Provider<RecommendationRepository> recommendationRepoProvider;

  private final Provider<UserPreferencesRepository> prefsProvider;

  public RecommendationsViewModel_Factory(
      Provider<RecommendationRepository> recommendationRepoProvider,
      Provider<UserPreferencesRepository> prefsProvider) {
    this.recommendationRepoProvider = recommendationRepoProvider;
    this.prefsProvider = prefsProvider;
  }

  @Override
  public RecommendationsViewModel get() {
    return newInstance(recommendationRepoProvider.get(), prefsProvider.get());
  }

  public static RecommendationsViewModel_Factory create(
      Provider<RecommendationRepository> recommendationRepoProvider,
      Provider<UserPreferencesRepository> prefsProvider) {
    return new RecommendationsViewModel_Factory(recommendationRepoProvider, prefsProvider);
  }

  public static RecommendationsViewModel newInstance(RecommendationRepository recommendationRepo,
      UserPreferencesRepository prefs) {
    return new RecommendationsViewModel(recommendationRepo, prefs);
  }
}
