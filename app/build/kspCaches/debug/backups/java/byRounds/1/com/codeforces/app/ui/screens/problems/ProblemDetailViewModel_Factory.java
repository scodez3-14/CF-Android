package com.codeforces.app.ui.screens.problems;

import com.codeforces.app.data.api.CodeforcesApiService;
import com.codeforces.app.data.repository.UserPreferencesRepository;
import com.codeforces.app.data.scraper.CfSubmitter;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;
import okhttp3.OkHttpClient;

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
public final class ProblemDetailViewModel_Factory implements Factory<ProblemDetailViewModel> {
  private final Provider<OkHttpClient> okHttpClientProvider;

  private final Provider<UserPreferencesRepository> prefsProvider;

  private final Provider<CfSubmitter> submitterProvider;

  private final Provider<CodeforcesApiService> apiProvider;

  public ProblemDetailViewModel_Factory(Provider<OkHttpClient> okHttpClientProvider,
      Provider<UserPreferencesRepository> prefsProvider, Provider<CfSubmitter> submitterProvider,
      Provider<CodeforcesApiService> apiProvider) {
    this.okHttpClientProvider = okHttpClientProvider;
    this.prefsProvider = prefsProvider;
    this.submitterProvider = submitterProvider;
    this.apiProvider = apiProvider;
  }

  @Override
  public ProblemDetailViewModel get() {
    return newInstance(okHttpClientProvider.get(), prefsProvider.get(), submitterProvider.get(), apiProvider.get());
  }

  public static ProblemDetailViewModel_Factory create(Provider<OkHttpClient> okHttpClientProvider,
      Provider<UserPreferencesRepository> prefsProvider, Provider<CfSubmitter> submitterProvider,
      Provider<CodeforcesApiService> apiProvider) {
    return new ProblemDetailViewModel_Factory(okHttpClientProvider, prefsProvider, submitterProvider, apiProvider);
  }

  public static ProblemDetailViewModel newInstance(OkHttpClient okHttpClient,
      UserPreferencesRepository prefs, CfSubmitter submitter, CodeforcesApiService api) {
    return new ProblemDetailViewModel(okHttpClient, prefs, submitter, api);
  }
}
