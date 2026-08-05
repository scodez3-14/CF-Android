package com.codeforces.app.ui.screens.problems;

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

  public ProblemDetailViewModel_Factory(Provider<OkHttpClient> okHttpClientProvider) {
    this.okHttpClientProvider = okHttpClientProvider;
  }

  @Override
  public ProblemDetailViewModel get() {
    return newInstance(okHttpClientProvider.get());
  }

  public static ProblemDetailViewModel_Factory create(Provider<OkHttpClient> okHttpClientProvider) {
    return new ProblemDetailViewModel_Factory(okHttpClientProvider);
  }

  public static ProblemDetailViewModel newInstance(OkHttpClient okHttpClient) {
    return new ProblemDetailViewModel(okHttpClient);
  }
}
