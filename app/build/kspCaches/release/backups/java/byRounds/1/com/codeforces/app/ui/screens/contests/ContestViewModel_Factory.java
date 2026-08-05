package com.codeforces.app.ui.screens.contests;

import com.codeforces.app.data.repository.CodeforcesRepository;
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
public final class ContestViewModel_Factory implements Factory<ContestViewModel> {
  private final Provider<CodeforcesRepository> repoProvider;

  private final Provider<OkHttpClient> okHttpClientProvider;

  public ContestViewModel_Factory(Provider<CodeforcesRepository> repoProvider,
      Provider<OkHttpClient> okHttpClientProvider) {
    this.repoProvider = repoProvider;
    this.okHttpClientProvider = okHttpClientProvider;
  }

  @Override
  public ContestViewModel get() {
    return newInstance(repoProvider.get(), okHttpClientProvider.get());
  }

  public static ContestViewModel_Factory create(Provider<CodeforcesRepository> repoProvider,
      Provider<OkHttpClient> okHttpClientProvider) {
    return new ContestViewModel_Factory(repoProvider, okHttpClientProvider);
  }

  public static ContestViewModel newInstance(CodeforcesRepository repo, OkHttpClient okHttpClient) {
    return new ContestViewModel(repo, okHttpClient);
  }
}
