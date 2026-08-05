package com.codeforces.app.notifications;

import android.content.Context;
import com.codeforces.app.data.repository.CodeforcesRepository;
import com.codeforces.app.data.repository.UserPreferencesRepository;
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
public final class ContestReminderManager_Factory implements Factory<ContestReminderManager> {
  private final Provider<Context> contextProvider;

  private final Provider<CodeforcesRepository> repoProvider;

  private final Provider<UserPreferencesRepository> prefsProvider;

  public ContestReminderManager_Factory(Provider<Context> contextProvider,
      Provider<CodeforcesRepository> repoProvider,
      Provider<UserPreferencesRepository> prefsProvider) {
    this.contextProvider = contextProvider;
    this.repoProvider = repoProvider;
    this.prefsProvider = prefsProvider;
  }

  @Override
  public ContestReminderManager get() {
    return newInstance(contextProvider.get(), repoProvider.get(), prefsProvider.get());
  }

  public static ContestReminderManager_Factory create(Provider<Context> contextProvider,
      Provider<CodeforcesRepository> repoProvider,
      Provider<UserPreferencesRepository> prefsProvider) {
    return new ContestReminderManager_Factory(contextProvider, repoProvider, prefsProvider);
  }

  public static ContestReminderManager newInstance(Context context, CodeforcesRepository repo,
      UserPreferencesRepository prefs) {
    return new ContestReminderManager(context, repo, prefs);
  }
}
