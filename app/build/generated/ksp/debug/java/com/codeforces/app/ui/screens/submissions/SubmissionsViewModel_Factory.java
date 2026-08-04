package com.codeforces.app.ui.screens.submissions;

import com.codeforces.app.data.repository.CodeforcesRepository;
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

  public SubmissionsViewModel_Factory(Provider<CodeforcesRepository> repoProvider) {
    this.repoProvider = repoProvider;
  }

  @Override
  public SubmissionsViewModel get() {
    return newInstance(repoProvider.get());
  }

  public static SubmissionsViewModel_Factory create(Provider<CodeforcesRepository> repoProvider) {
    return new SubmissionsViewModel_Factory(repoProvider);
  }

  public static SubmissionsViewModel newInstance(CodeforcesRepository repo) {
    return new SubmissionsViewModel(repo);
  }
}
