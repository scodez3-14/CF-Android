package com.codeforces.app.data.tracker;

import android.content.Context;
import com.codeforces.app.data.api.CodeforcesApiService;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata("javax.inject.Singleton")
@QualifierMetadata("dagger.hilt.android.qualifiers.ApplicationContext")
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
public final class SubmissionTracker_Factory implements Factory<SubmissionTracker> {
  private final Provider<CodeforcesApiService> apiProvider;

  private final Provider<Context> contextProvider;

  public SubmissionTracker_Factory(Provider<CodeforcesApiService> apiProvider,
      Provider<Context> contextProvider) {
    this.apiProvider = apiProvider;
    this.contextProvider = contextProvider;
  }

  @Override
  public SubmissionTracker get() {
    return newInstance(apiProvider.get(), contextProvider.get());
  }

  public static SubmissionTracker_Factory create(Provider<CodeforcesApiService> apiProvider,
      Provider<Context> contextProvider) {
    return new SubmissionTracker_Factory(apiProvider, contextProvider);
  }

  public static SubmissionTracker newInstance(CodeforcesApiService api, Context context) {
    return new SubmissionTracker(api, context);
  }
}
