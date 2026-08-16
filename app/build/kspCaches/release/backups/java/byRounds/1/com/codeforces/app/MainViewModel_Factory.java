package com.codeforces.app;

import com.codeforces.app.data.repository.UserPreferencesRepository;
import com.codeforces.app.data.update.UpdateChecker;
import com.codeforces.app.notifications.ContestReminderManager;
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
public final class MainViewModel_Factory implements Factory<MainViewModel> {
  private final Provider<UserPreferencesRepository> prefsProvider;

  private final Provider<ContestReminderManager> reminderManagerProvider;

  private final Provider<UpdateChecker> updateCheckerProvider;

  public MainViewModel_Factory(Provider<UserPreferencesRepository> prefsProvider,
      Provider<ContestReminderManager> reminderManagerProvider,
      Provider<UpdateChecker> updateCheckerProvider) {
    this.prefsProvider = prefsProvider;
    this.reminderManagerProvider = reminderManagerProvider;
    this.updateCheckerProvider = updateCheckerProvider;
  }

  @Override
  public MainViewModel get() {
    return newInstance(prefsProvider.get(), reminderManagerProvider.get(), updateCheckerProvider.get());
  }

  public static MainViewModel_Factory create(Provider<UserPreferencesRepository> prefsProvider,
      Provider<ContestReminderManager> reminderManagerProvider,
      Provider<UpdateChecker> updateCheckerProvider) {
    return new MainViewModel_Factory(prefsProvider, reminderManagerProvider, updateCheckerProvider);
  }

  public static MainViewModel newInstance(UserPreferencesRepository prefs,
      ContestReminderManager reminderManager, UpdateChecker updateChecker) {
    return new MainViewModel(prefs, reminderManager, updateChecker);
  }
}
