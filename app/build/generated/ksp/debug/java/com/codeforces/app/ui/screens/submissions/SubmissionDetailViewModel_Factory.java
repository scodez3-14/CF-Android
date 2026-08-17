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
public final class SubmissionDetailViewModel_Factory implements Factory<SubmissionDetailViewModel> {
  private final Provider<CfSubmitter> submitterProvider;

  private final Provider<CodeforcesRepository> repoProvider;

  private final Provider<UserPreferencesRepository> prefsProvider;

  public SubmissionDetailViewModel_Factory(Provider<CfSubmitter> submitterProvider,
      Provider<CodeforcesRepository> repoProvider,
      Provider<UserPreferencesRepository> prefsProvider) {
    this.submitterProvider = submitterProvider;
    this.repoProvider = repoProvider;
    this.prefsProvider = prefsProvider;
  }

  @Override
  public SubmissionDetailViewModel get() {
    return newInstance(submitterProvider.get(), repoProvider.get(), prefsProvider.get());
  }

  public static SubmissionDetailViewModel_Factory create(Provider<CfSubmitter> submitterProvider,
      Provider<CodeforcesRepository> repoProvider,
      Provider<UserPreferencesRepository> prefsProvider) {
    return new SubmissionDetailViewModel_Factory(submitterProvider, repoProvider, prefsProvider);
  }

  public static SubmissionDetailViewModel newInstance(CfSubmitter submitter,
      CodeforcesRepository repo, UserPreferencesRepository prefs) {
    return new SubmissionDetailViewModel(submitter, repo, prefs);
  }
}
