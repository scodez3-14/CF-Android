package com.codeforces.app.ui.screens.settings;

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
public final class SettingsViewModel_Factory implements Factory<SettingsViewModel> {
  private final Provider<UserPreferencesRepository> prefsProvider;

  private final Provider<CodeforcesRepository> repoProvider;

  public SettingsViewModel_Factory(Provider<UserPreferencesRepository> prefsProvider,
      Provider<CodeforcesRepository> repoProvider) {
    this.prefsProvider = prefsProvider;
    this.repoProvider = repoProvider;
  }

  @Override
  public SettingsViewModel get() {
    return newInstance(prefsProvider.get(), repoProvider.get());
  }

  public static SettingsViewModel_Factory create(Provider<UserPreferencesRepository> prefsProvider,
      Provider<CodeforcesRepository> repoProvider) {
    return new SettingsViewModel_Factory(prefsProvider, repoProvider);
  }

  public static SettingsViewModel newInstance(UserPreferencesRepository prefs,
      CodeforcesRepository repo) {
    return new SettingsViewModel(prefs, repo);
  }
}
