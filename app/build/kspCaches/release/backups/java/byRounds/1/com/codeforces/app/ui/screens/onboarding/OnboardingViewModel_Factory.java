package com.codeforces.app.ui.screens.onboarding;

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
public final class OnboardingViewModel_Factory implements Factory<OnboardingViewModel> {
  private final Provider<UserPreferencesRepository> prefsProvider;

  public OnboardingViewModel_Factory(Provider<UserPreferencesRepository> prefsProvider) {
    this.prefsProvider = prefsProvider;
  }

  @Override
  public OnboardingViewModel get() {
    return newInstance(prefsProvider.get());
  }

  public static OnboardingViewModel_Factory create(
      Provider<UserPreferencesRepository> prefsProvider) {
    return new OnboardingViewModel_Factory(prefsProvider);
  }

  public static OnboardingViewModel newInstance(UserPreferencesRepository prefs) {
    return new OnboardingViewModel(prefs);
  }
}
