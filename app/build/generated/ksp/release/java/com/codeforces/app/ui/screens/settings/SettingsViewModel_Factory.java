package com.codeforces.app.ui.screens.settings;

import com.codeforces.app.data.repository.CodeforcesRepository;
import com.codeforces.app.data.repository.UserPreferencesRepository;
import com.codeforces.app.data.scraper.CfSubmitter;
import com.codeforces.app.data.update.UpdateChecker;
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

  private final Provider<CfSubmitter> submitterProvider;

  private final Provider<UpdateChecker> updateCheckerProvider;

  public SettingsViewModel_Factory(Provider<UserPreferencesRepository> prefsProvider,
      Provider<CodeforcesRepository> repoProvider, Provider<CfSubmitter> submitterProvider,
      Provider<UpdateChecker> updateCheckerProvider) {
    this.prefsProvider = prefsProvider;
    this.repoProvider = repoProvider;
    this.submitterProvider = submitterProvider;
    this.updateCheckerProvider = updateCheckerProvider;
  }

  @Override
  public SettingsViewModel get() {
    return newInstance(prefsProvider.get(), repoProvider.get(), submitterProvider.get(), updateCheckerProvider.get());
  }

  public static SettingsViewModel_Factory create(Provider<UserPreferencesRepository> prefsProvider,
      Provider<CodeforcesRepository> repoProvider, Provider<CfSubmitter> submitterProvider,
      Provider<UpdateChecker> updateCheckerProvider) {
    return new SettingsViewModel_Factory(prefsProvider, repoProvider, submitterProvider, updateCheckerProvider);
  }

  public static SettingsViewModel newInstance(UserPreferencesRepository prefs,
      CodeforcesRepository repo, CfSubmitter submitter, UpdateChecker updateChecker) {
    return new SettingsViewModel(prefs, repo, submitter, updateChecker);
  }
}
