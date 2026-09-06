package com.codeforces.app.ui.screens.submissions;

import com.codeforces.app.data.api.CodeforcesApiService;
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

  private final Provider<CodeforcesApiService> apiProvider;

  private final Provider<UserPreferencesRepository> prefsProvider;

  public SubmissionDetailViewModel_Factory(Provider<CfSubmitter> submitterProvider,
      Provider<CodeforcesApiService> apiProvider,
      Provider<UserPreferencesRepository> prefsProvider) {
    this.submitterProvider = submitterProvider;
    this.apiProvider = apiProvider;
    this.prefsProvider = prefsProvider;
  }

  @Override
  public SubmissionDetailViewModel get() {
    return newInstance(submitterProvider.get(), apiProvider.get(), prefsProvider.get());
  }

  public static SubmissionDetailViewModel_Factory create(Provider<CfSubmitter> submitterProvider,
      Provider<CodeforcesApiService> apiProvider,
      Provider<UserPreferencesRepository> prefsProvider) {
    return new SubmissionDetailViewModel_Factory(submitterProvider, apiProvider, prefsProvider);
  }

  public static SubmissionDetailViewModel newInstance(CfSubmitter submitter,
      CodeforcesApiService api, UserPreferencesRepository prefs) {
    return new SubmissionDetailViewModel(submitter, api, prefs);
  }
}
