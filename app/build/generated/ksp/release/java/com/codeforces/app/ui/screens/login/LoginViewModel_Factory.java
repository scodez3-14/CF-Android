package com.codeforces.app.ui.screens.login;

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
public final class LoginViewModel_Factory implements Factory<LoginViewModel> {
  private final Provider<CfSubmitter> submitterProvider;

  private final Provider<UserPreferencesRepository> prefsProvider;

  public LoginViewModel_Factory(Provider<CfSubmitter> submitterProvider,
      Provider<UserPreferencesRepository> prefsProvider) {
    this.submitterProvider = submitterProvider;
    this.prefsProvider = prefsProvider;
  }

  @Override
  public LoginViewModel get() {
    return newInstance(submitterProvider.get(), prefsProvider.get());
  }

  public static LoginViewModel_Factory create(Provider<CfSubmitter> submitterProvider,
      Provider<UserPreferencesRepository> prefsProvider) {
    return new LoginViewModel_Factory(submitterProvider, prefsProvider);
  }

  public static LoginViewModel newInstance(CfSubmitter submitter, UserPreferencesRepository prefs) {
    return new LoginViewModel(submitter, prefs);
  }
}
