package com.codeforces.app.ui.screens.submissions;

import com.codeforces.app.data.repository.CodeforcesRepository;
import com.codeforces.app.data.repository.UserPreferencesRepository;
import com.codeforces.app.data.scraper.CfSubmitter;
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
public final class SubmissionsViewModel_Factory implements Factory<SubmissionsViewModel> {
  private final Provider<CodeforcesRepository> repoProvider;

  private final Provider<CfSubmitter> submitterProvider;

  private final Provider<UserPreferencesRepository> prefsProvider;

  public SubmissionsViewModel_Factory(Provider<CodeforcesRepository> repoProvider,
      Provider<CfSubmitter> submitterProvider, Provider<UserPreferencesRepository> prefsProvider) {
    this.repoProvider = repoProvider;
    this.submitterProvider = submitterProvider;
    this.prefsProvider = prefsProvider;
  }

  @Override
  public SubmissionsViewModel get() {
    return newInstance(repoProvider.get(), submitterProvider.get(), prefsProvider.get());
  }

  public static SubmissionsViewModel_Factory create(Provider<CodeforcesRepository> repoProvider,
      Provider<CfSubmitter> submitterProvider, Provider<UserPreferencesRepository> prefsProvider) {
    return new SubmissionsViewModel_Factory(repoProvider, submitterProvider, prefsProvider);
  }

  public static SubmissionsViewModel newInstance(CodeforcesRepository repo, CfSubmitter submitter,
      UserPreferencesRepository prefs) {
    return new SubmissionsViewModel(repo, submitter, prefs);
  }
}
