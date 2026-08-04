package com.codeforces.app.ui.screens.profile;

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
public final class ProfileViewModel_Factory implements Factory<ProfileViewModel> {
  private final Provider<CodeforcesRepository> repoProvider;

  private final Provider<UserPreferencesRepository> prefsProvider;

  public ProfileViewModel_Factory(Provider<CodeforcesRepository> repoProvider,
      Provider<UserPreferencesRepository> prefsProvider) {
    this.repoProvider = repoProvider;
    this.prefsProvider = prefsProvider;
  }

  @Override
  public ProfileViewModel get() {
    return newInstance(repoProvider.get(), prefsProvider.get());
  }

  public static ProfileViewModel_Factory create(Provider<CodeforcesRepository> repoProvider,
      Provider<UserPreferencesRepository> prefsProvider) {
    return new ProfileViewModel_Factory(repoProvider, prefsProvider);
  }

  public static ProfileViewModel newInstance(CodeforcesRepository repo,
      UserPreferencesRepository prefs) {
    return new ProfileViewModel(repo, prefs);
  }
}
