package com.codeforces.app;

import android.app.Activity;
import android.app.Service;
import android.view.View;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.ViewModel;
import com.codeforces.app.data.api.CodeforcesApiService;
import com.codeforces.app.data.db.CodeforcesDatabase;
import com.codeforces.app.data.db.ContestDao;
import com.codeforces.app.data.db.ProblemDao;
import com.codeforces.app.data.db.UserDao;
import com.codeforces.app.data.repository.CodeforcesRepository;
import com.codeforces.app.data.repository.UserPreferencesRepository;
import com.codeforces.app.data.scraper.CfSubmitter;
import com.codeforces.app.data.scraper.PersistentCookieJar;
import com.codeforces.app.di.DatabaseModule_ProvideContestDaoFactory;
import com.codeforces.app.di.DatabaseModule_ProvideDatabaseFactory;
import com.codeforces.app.di.DatabaseModule_ProvideProblemDaoFactory;
import com.codeforces.app.di.DatabaseModule_ProvideUserDaoFactory;
import com.codeforces.app.di.NetworkModule_ProvideApiServiceFactory;
import com.codeforces.app.di.NetworkModule_ProvideCfSubmitterFactory;
import com.codeforces.app.di.NetworkModule_ProvideOkHttpClientFactory;
import com.codeforces.app.di.NetworkModule_ProvidePersistentCookieJarFactory;
import com.codeforces.app.di.NetworkModule_ProvideRetrofitFactory;
import com.codeforces.app.notifications.ContestReminderBootReceiver;
import com.codeforces.app.notifications.ContestReminderBootReceiver_MembersInjector;
import com.codeforces.app.notifications.ContestReminderManager;
import com.codeforces.app.ui.screens.blog.BlogViewModel;
import com.codeforces.app.ui.screens.blog.BlogViewModel_HiltModules;
import com.codeforces.app.ui.screens.contests.ContestViewModel;
import com.codeforces.app.ui.screens.contests.ContestViewModel_HiltModules;
import com.codeforces.app.ui.screens.home.HomeViewModel;
import com.codeforces.app.ui.screens.home.HomeViewModel_HiltModules;
import com.codeforces.app.ui.screens.leaderboard.LeaderboardViewModel;
import com.codeforces.app.ui.screens.leaderboard.LeaderboardViewModel_HiltModules;
import com.codeforces.app.ui.screens.login.LoginViewModel;
import com.codeforces.app.ui.screens.login.LoginViewModel_HiltModules;
import com.codeforces.app.ui.screens.login.WebLoginViewModel;
import com.codeforces.app.ui.screens.login.WebLoginViewModel_HiltModules;
import com.codeforces.app.ui.screens.onboarding.OnboardingViewModel;
import com.codeforces.app.ui.screens.onboarding.OnboardingViewModel_HiltModules;
import com.codeforces.app.ui.screens.problems.ProblemDetailViewModel;
import com.codeforces.app.ui.screens.problems.ProblemDetailViewModel_HiltModules;
import com.codeforces.app.ui.screens.problems.ProblemsViewModel;
import com.codeforces.app.ui.screens.problems.ProblemsViewModel_HiltModules;
import com.codeforces.app.ui.screens.profile.ProfileViewModel;
import com.codeforces.app.ui.screens.profile.ProfileViewModel_HiltModules;
import com.codeforces.app.ui.screens.search.SearchViewModel;
import com.codeforces.app.ui.screens.search.SearchViewModel_HiltModules;
import com.codeforces.app.ui.screens.settings.SettingsViewModel;
import com.codeforces.app.ui.screens.settings.SettingsViewModel_HiltModules;
import com.codeforces.app.ui.screens.standings.StandingsViewModel;
import com.codeforces.app.ui.screens.standings.StandingsViewModel_HiltModules;
import com.codeforces.app.ui.screens.submissions.SubmissionsViewModel;
import com.codeforces.app.ui.screens.submissions.SubmissionsViewModel_HiltModules;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import dagger.hilt.android.ActivityRetainedLifecycle;
import dagger.hilt.android.ViewModelLifecycle;
import dagger.hilt.android.internal.builders.ActivityComponentBuilder;
import dagger.hilt.android.internal.builders.ActivityRetainedComponentBuilder;
import dagger.hilt.android.internal.builders.FragmentComponentBuilder;
import dagger.hilt.android.internal.builders.ServiceComponentBuilder;
import dagger.hilt.android.internal.builders.ViewComponentBuilder;
import dagger.hilt.android.internal.builders.ViewModelComponentBuilder;
import dagger.hilt.android.internal.builders.ViewWithFragmentComponentBuilder;
import dagger.hilt.android.internal.lifecycle.DefaultViewModelFactories;
import dagger.hilt.android.internal.lifecycle.DefaultViewModelFactories_InternalFactoryFactory_Factory;
import dagger.hilt.android.internal.managers.ActivityRetainedComponentManager_LifecycleModule_ProvideActivityRetainedLifecycleFactory;
import dagger.hilt.android.internal.managers.SavedStateHandleHolder;
import dagger.hilt.android.internal.modules.ApplicationContextModule;
import dagger.hilt.android.internal.modules.ApplicationContextModule_ProvideContextFactory;
import dagger.internal.DaggerGenerated;
import dagger.internal.DoubleCheck;
import dagger.internal.IdentifierNameString;
import dagger.internal.KeepFieldType;
import dagger.internal.LazyClassKeyMap;
import dagger.internal.MapBuilder;
import dagger.internal.Preconditions;
import dagger.internal.Provider;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.Generated;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;

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
public final class DaggerCodeforcesApp_HiltComponents_SingletonC {
  private DaggerCodeforcesApp_HiltComponents_SingletonC() {
  }

  public static Builder builder() {
    return new Builder();
  }

  public static final class Builder {
    private ApplicationContextModule applicationContextModule;

    private Builder() {
    }

    public Builder applicationContextModule(ApplicationContextModule applicationContextModule) {
      this.applicationContextModule = Preconditions.checkNotNull(applicationContextModule);
      return this;
    }

    public CodeforcesApp_HiltComponents.SingletonC build() {
      Preconditions.checkBuilderRequirement(applicationContextModule, ApplicationContextModule.class);
      return new SingletonCImpl(applicationContextModule);
    }
  }

  private static final class ActivityRetainedCBuilder implements CodeforcesApp_HiltComponents.ActivityRetainedC.Builder {
    private final SingletonCImpl singletonCImpl;

    private SavedStateHandleHolder savedStateHandleHolder;

    private ActivityRetainedCBuilder(SingletonCImpl singletonCImpl) {
      this.singletonCImpl = singletonCImpl;
    }

    @Override
    public ActivityRetainedCBuilder savedStateHandleHolder(
        SavedStateHandleHolder savedStateHandleHolder) {
      this.savedStateHandleHolder = Preconditions.checkNotNull(savedStateHandleHolder);
      return this;
    }

    @Override
    public CodeforcesApp_HiltComponents.ActivityRetainedC build() {
      Preconditions.checkBuilderRequirement(savedStateHandleHolder, SavedStateHandleHolder.class);
      return new ActivityRetainedCImpl(singletonCImpl, savedStateHandleHolder);
    }
  }

  private static final class ActivityCBuilder implements CodeforcesApp_HiltComponents.ActivityC.Builder {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private Activity activity;

    private ActivityCBuilder(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
    }

    @Override
    public ActivityCBuilder activity(Activity activity) {
      this.activity = Preconditions.checkNotNull(activity);
      return this;
    }

    @Override
    public CodeforcesApp_HiltComponents.ActivityC build() {
      Preconditions.checkBuilderRequirement(activity, Activity.class);
      return new ActivityCImpl(singletonCImpl, activityRetainedCImpl, activity);
    }
  }

  private static final class FragmentCBuilder implements CodeforcesApp_HiltComponents.FragmentC.Builder {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private Fragment fragment;

    private FragmentCBuilder(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, ActivityCImpl activityCImpl) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;
    }

    @Override
    public FragmentCBuilder fragment(Fragment fragment) {
      this.fragment = Preconditions.checkNotNull(fragment);
      return this;
    }

    @Override
    public CodeforcesApp_HiltComponents.FragmentC build() {
      Preconditions.checkBuilderRequirement(fragment, Fragment.class);
      return new FragmentCImpl(singletonCImpl, activityRetainedCImpl, activityCImpl, fragment);
    }
  }

  private static final class ViewWithFragmentCBuilder implements CodeforcesApp_HiltComponents.ViewWithFragmentC.Builder {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private final FragmentCImpl fragmentCImpl;

    private View view;

    private ViewWithFragmentCBuilder(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, ActivityCImpl activityCImpl,
        FragmentCImpl fragmentCImpl) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;
      this.fragmentCImpl = fragmentCImpl;
    }

    @Override
    public ViewWithFragmentCBuilder view(View view) {
      this.view = Preconditions.checkNotNull(view);
      return this;
    }

    @Override
    public CodeforcesApp_HiltComponents.ViewWithFragmentC build() {
      Preconditions.checkBuilderRequirement(view, View.class);
      return new ViewWithFragmentCImpl(singletonCImpl, activityRetainedCImpl, activityCImpl, fragmentCImpl, view);
    }
  }

  private static final class ViewCBuilder implements CodeforcesApp_HiltComponents.ViewC.Builder {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private View view;

    private ViewCBuilder(SingletonCImpl singletonCImpl, ActivityRetainedCImpl activityRetainedCImpl,
        ActivityCImpl activityCImpl) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;
    }

    @Override
    public ViewCBuilder view(View view) {
      this.view = Preconditions.checkNotNull(view);
      return this;
    }

    @Override
    public CodeforcesApp_HiltComponents.ViewC build() {
      Preconditions.checkBuilderRequirement(view, View.class);
      return new ViewCImpl(singletonCImpl, activityRetainedCImpl, activityCImpl, view);
    }
  }

  private static final class ViewModelCBuilder implements CodeforcesApp_HiltComponents.ViewModelC.Builder {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private SavedStateHandle savedStateHandle;

    private ViewModelLifecycle viewModelLifecycle;

    private ViewModelCBuilder(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
    }

    @Override
    public ViewModelCBuilder savedStateHandle(SavedStateHandle handle) {
      this.savedStateHandle = Preconditions.checkNotNull(handle);
      return this;
    }

    @Override
    public ViewModelCBuilder viewModelLifecycle(ViewModelLifecycle viewModelLifecycle) {
      this.viewModelLifecycle = Preconditions.checkNotNull(viewModelLifecycle);
      return this;
    }

    @Override
    public CodeforcesApp_HiltComponents.ViewModelC build() {
      Preconditions.checkBuilderRequirement(savedStateHandle, SavedStateHandle.class);
      Preconditions.checkBuilderRequirement(viewModelLifecycle, ViewModelLifecycle.class);
      return new ViewModelCImpl(singletonCImpl, activityRetainedCImpl, savedStateHandle, viewModelLifecycle);
    }
  }

  private static final class ServiceCBuilder implements CodeforcesApp_HiltComponents.ServiceC.Builder {
    private final SingletonCImpl singletonCImpl;

    private Service service;

    private ServiceCBuilder(SingletonCImpl singletonCImpl) {
      this.singletonCImpl = singletonCImpl;
    }

    @Override
    public ServiceCBuilder service(Service service) {
      this.service = Preconditions.checkNotNull(service);
      return this;
    }

    @Override
    public CodeforcesApp_HiltComponents.ServiceC build() {
      Preconditions.checkBuilderRequirement(service, Service.class);
      return new ServiceCImpl(singletonCImpl, service);
    }
  }

  private static final class ViewWithFragmentCImpl extends CodeforcesApp_HiltComponents.ViewWithFragmentC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private final FragmentCImpl fragmentCImpl;

    private final ViewWithFragmentCImpl viewWithFragmentCImpl = this;

    private ViewWithFragmentCImpl(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, ActivityCImpl activityCImpl,
        FragmentCImpl fragmentCImpl, View viewParam) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;
      this.fragmentCImpl = fragmentCImpl;


    }
  }

  private static final class FragmentCImpl extends CodeforcesApp_HiltComponents.FragmentC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private final FragmentCImpl fragmentCImpl = this;

    private FragmentCImpl(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, ActivityCImpl activityCImpl,
        Fragment fragmentParam) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;


    }

    @Override
    public DefaultViewModelFactories.InternalFactoryFactory getHiltInternalFactoryFactory() {
      return activityCImpl.getHiltInternalFactoryFactory();
    }

    @Override
    public ViewWithFragmentComponentBuilder viewWithFragmentComponentBuilder() {
      return new ViewWithFragmentCBuilder(singletonCImpl, activityRetainedCImpl, activityCImpl, fragmentCImpl);
    }
  }

  private static final class ViewCImpl extends CodeforcesApp_HiltComponents.ViewC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private final ViewCImpl viewCImpl = this;

    private ViewCImpl(SingletonCImpl singletonCImpl, ActivityRetainedCImpl activityRetainedCImpl,
        ActivityCImpl activityCImpl, View viewParam) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;


    }
  }

  private static final class ActivityCImpl extends CodeforcesApp_HiltComponents.ActivityC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl = this;

    private ActivityCImpl(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, Activity activityParam) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;


    }

    @Override
    public void injectMainActivity(MainActivity mainActivity) {
    }

    @Override
    public DefaultViewModelFactories.InternalFactoryFactory getHiltInternalFactoryFactory() {
      return DefaultViewModelFactories_InternalFactoryFactory_Factory.newInstance(getViewModelKeys(), new ViewModelCBuilder(singletonCImpl, activityRetainedCImpl));
    }

    @Override
    public Map<Class<?>, Boolean> getViewModelKeys() {
      return LazyClassKeyMap.<Boolean>of(MapBuilder.<String, Boolean>newMapBuilder(15).put(LazyClassKeyProvider.com_codeforces_app_ui_screens_blog_BlogViewModel, BlogViewModel_HiltModules.KeyModule.provide()).put(LazyClassKeyProvider.com_codeforces_app_ui_screens_contests_ContestViewModel, ContestViewModel_HiltModules.KeyModule.provide()).put(LazyClassKeyProvider.com_codeforces_app_ui_screens_home_HomeViewModel, HomeViewModel_HiltModules.KeyModule.provide()).put(LazyClassKeyProvider.com_codeforces_app_ui_screens_leaderboard_LeaderboardViewModel, LeaderboardViewModel_HiltModules.KeyModule.provide()).put(LazyClassKeyProvider.com_codeforces_app_ui_screens_login_LoginViewModel, LoginViewModel_HiltModules.KeyModule.provide()).put(LazyClassKeyProvider.com_codeforces_app_MainViewModel, MainViewModel_HiltModules.KeyModule.provide()).put(LazyClassKeyProvider.com_codeforces_app_ui_screens_onboarding_OnboardingViewModel, OnboardingViewModel_HiltModules.KeyModule.provide()).put(LazyClassKeyProvider.com_codeforces_app_ui_screens_problems_ProblemDetailViewModel, ProblemDetailViewModel_HiltModules.KeyModule.provide()).put(LazyClassKeyProvider.com_codeforces_app_ui_screens_problems_ProblemsViewModel, ProblemsViewModel_HiltModules.KeyModule.provide()).put(LazyClassKeyProvider.com_codeforces_app_ui_screens_profile_ProfileViewModel, ProfileViewModel_HiltModules.KeyModule.provide()).put(LazyClassKeyProvider.com_codeforces_app_ui_screens_search_SearchViewModel, SearchViewModel_HiltModules.KeyModule.provide()).put(LazyClassKeyProvider.com_codeforces_app_ui_screens_settings_SettingsViewModel, SettingsViewModel_HiltModules.KeyModule.provide()).put(LazyClassKeyProvider.com_codeforces_app_ui_screens_standings_StandingsViewModel, StandingsViewModel_HiltModules.KeyModule.provide()).put(LazyClassKeyProvider.com_codeforces_app_ui_screens_submissions_SubmissionsViewModel, SubmissionsViewModel_HiltModules.KeyModule.provide()).put(LazyClassKeyProvider.com_codeforces_app_ui_screens_login_WebLoginViewModel, WebLoginViewModel_HiltModules.KeyModule.provide()).build());
    }

    @Override
    public ViewModelComponentBuilder getViewModelComponentBuilder() {
      return new ViewModelCBuilder(singletonCImpl, activityRetainedCImpl);
    }

    @Override
    public FragmentComponentBuilder fragmentComponentBuilder() {
      return new FragmentCBuilder(singletonCImpl, activityRetainedCImpl, activityCImpl);
    }

    @Override
    public ViewComponentBuilder viewComponentBuilder() {
      return new ViewCBuilder(singletonCImpl, activityRetainedCImpl, activityCImpl);
    }

    @IdentifierNameString
    private static final class LazyClassKeyProvider {
      static String com_codeforces_app_ui_screens_problems_ProblemDetailViewModel = "com.codeforces.app.ui.screens.problems.ProblemDetailViewModel";

      static String com_codeforces_app_ui_screens_blog_BlogViewModel = "com.codeforces.app.ui.screens.blog.BlogViewModel";

      static String com_codeforces_app_ui_screens_login_WebLoginViewModel = "com.codeforces.app.ui.screens.login.WebLoginViewModel";

      static String com_codeforces_app_ui_screens_settings_SettingsViewModel = "com.codeforces.app.ui.screens.settings.SettingsViewModel";

      static String com_codeforces_app_ui_screens_problems_ProblemsViewModel = "com.codeforces.app.ui.screens.problems.ProblemsViewModel";

      static String com_codeforces_app_ui_screens_standings_StandingsViewModel = "com.codeforces.app.ui.screens.standings.StandingsViewModel";

      static String com_codeforces_app_ui_screens_login_LoginViewModel = "com.codeforces.app.ui.screens.login.LoginViewModel";

      static String com_codeforces_app_ui_screens_contests_ContestViewModel = "com.codeforces.app.ui.screens.contests.ContestViewModel";

      static String com_codeforces_app_MainViewModel = "com.codeforces.app.MainViewModel";

      static String com_codeforces_app_ui_screens_home_HomeViewModel = "com.codeforces.app.ui.screens.home.HomeViewModel";

      static String com_codeforces_app_ui_screens_leaderboard_LeaderboardViewModel = "com.codeforces.app.ui.screens.leaderboard.LeaderboardViewModel";

      static String com_codeforces_app_ui_screens_profile_ProfileViewModel = "com.codeforces.app.ui.screens.profile.ProfileViewModel";

      static String com_codeforces_app_ui_screens_onboarding_OnboardingViewModel = "com.codeforces.app.ui.screens.onboarding.OnboardingViewModel";

      static String com_codeforces_app_ui_screens_submissions_SubmissionsViewModel = "com.codeforces.app.ui.screens.submissions.SubmissionsViewModel";

      static String com_codeforces_app_ui_screens_search_SearchViewModel = "com.codeforces.app.ui.screens.search.SearchViewModel";

      @KeepFieldType
      ProblemDetailViewModel com_codeforces_app_ui_screens_problems_ProblemDetailViewModel2;

      @KeepFieldType
      BlogViewModel com_codeforces_app_ui_screens_blog_BlogViewModel2;

      @KeepFieldType
      WebLoginViewModel com_codeforces_app_ui_screens_login_WebLoginViewModel2;

      @KeepFieldType
      SettingsViewModel com_codeforces_app_ui_screens_settings_SettingsViewModel2;

      @KeepFieldType
      ProblemsViewModel com_codeforces_app_ui_screens_problems_ProblemsViewModel2;

      @KeepFieldType
      StandingsViewModel com_codeforces_app_ui_screens_standings_StandingsViewModel2;

      @KeepFieldType
      LoginViewModel com_codeforces_app_ui_screens_login_LoginViewModel2;

      @KeepFieldType
      ContestViewModel com_codeforces_app_ui_screens_contests_ContestViewModel2;

      @KeepFieldType
      MainViewModel com_codeforces_app_MainViewModel2;

      @KeepFieldType
      HomeViewModel com_codeforces_app_ui_screens_home_HomeViewModel2;

      @KeepFieldType
      LeaderboardViewModel com_codeforces_app_ui_screens_leaderboard_LeaderboardViewModel2;

      @KeepFieldType
      ProfileViewModel com_codeforces_app_ui_screens_profile_ProfileViewModel2;

      @KeepFieldType
      OnboardingViewModel com_codeforces_app_ui_screens_onboarding_OnboardingViewModel2;

      @KeepFieldType
      SubmissionsViewModel com_codeforces_app_ui_screens_submissions_SubmissionsViewModel2;

      @KeepFieldType
      SearchViewModel com_codeforces_app_ui_screens_search_SearchViewModel2;
    }
  }

  private static final class ViewModelCImpl extends CodeforcesApp_HiltComponents.ViewModelC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ViewModelCImpl viewModelCImpl = this;

    private Provider<BlogViewModel> blogViewModelProvider;

    private Provider<ContestViewModel> contestViewModelProvider;

    private Provider<HomeViewModel> homeViewModelProvider;

    private Provider<LeaderboardViewModel> leaderboardViewModelProvider;

    private Provider<LoginViewModel> loginViewModelProvider;

    private Provider<MainViewModel> mainViewModelProvider;

    private Provider<OnboardingViewModel> onboardingViewModelProvider;

    private Provider<ProblemDetailViewModel> problemDetailViewModelProvider;

    private Provider<ProblemsViewModel> problemsViewModelProvider;

    private Provider<ProfileViewModel> profileViewModelProvider;

    private Provider<SearchViewModel> searchViewModelProvider;

    private Provider<SettingsViewModel> settingsViewModelProvider;

    private Provider<StandingsViewModel> standingsViewModelProvider;

    private Provider<SubmissionsViewModel> submissionsViewModelProvider;

    private Provider<WebLoginViewModel> webLoginViewModelProvider;

    private ViewModelCImpl(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, SavedStateHandle savedStateHandleParam,
        ViewModelLifecycle viewModelLifecycleParam) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;

      initialize(savedStateHandleParam, viewModelLifecycleParam);

    }

    @SuppressWarnings("unchecked")
    private void initialize(final SavedStateHandle savedStateHandleParam,
        final ViewModelLifecycle viewModelLifecycleParam) {
      this.blogViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 0);
      this.contestViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 1);
      this.homeViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 2);
      this.leaderboardViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 3);
      this.loginViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 4);
      this.mainViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 5);
      this.onboardingViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 6);
      this.problemDetailViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 7);
      this.problemsViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 8);
      this.profileViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 9);
      this.searchViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 10);
      this.settingsViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 11);
      this.standingsViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 12);
      this.submissionsViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 13);
      this.webLoginViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 14);
    }

    @Override
    public Map<Class<?>, javax.inject.Provider<ViewModel>> getHiltViewModelMap() {
      return LazyClassKeyMap.<javax.inject.Provider<ViewModel>>of(MapBuilder.<String, javax.inject.Provider<ViewModel>>newMapBuilder(15).put(LazyClassKeyProvider.com_codeforces_app_ui_screens_blog_BlogViewModel, ((Provider) blogViewModelProvider)).put(LazyClassKeyProvider.com_codeforces_app_ui_screens_contests_ContestViewModel, ((Provider) contestViewModelProvider)).put(LazyClassKeyProvider.com_codeforces_app_ui_screens_home_HomeViewModel, ((Provider) homeViewModelProvider)).put(LazyClassKeyProvider.com_codeforces_app_ui_screens_leaderboard_LeaderboardViewModel, ((Provider) leaderboardViewModelProvider)).put(LazyClassKeyProvider.com_codeforces_app_ui_screens_login_LoginViewModel, ((Provider) loginViewModelProvider)).put(LazyClassKeyProvider.com_codeforces_app_MainViewModel, ((Provider) mainViewModelProvider)).put(LazyClassKeyProvider.com_codeforces_app_ui_screens_onboarding_OnboardingViewModel, ((Provider) onboardingViewModelProvider)).put(LazyClassKeyProvider.com_codeforces_app_ui_screens_problems_ProblemDetailViewModel, ((Provider) problemDetailViewModelProvider)).put(LazyClassKeyProvider.com_codeforces_app_ui_screens_problems_ProblemsViewModel, ((Provider) problemsViewModelProvider)).put(LazyClassKeyProvider.com_codeforces_app_ui_screens_profile_ProfileViewModel, ((Provider) profileViewModelProvider)).put(LazyClassKeyProvider.com_codeforces_app_ui_screens_search_SearchViewModel, ((Provider) searchViewModelProvider)).put(LazyClassKeyProvider.com_codeforces_app_ui_screens_settings_SettingsViewModel, ((Provider) settingsViewModelProvider)).put(LazyClassKeyProvider.com_codeforces_app_ui_screens_standings_StandingsViewModel, ((Provider) standingsViewModelProvider)).put(LazyClassKeyProvider.com_codeforces_app_ui_screens_submissions_SubmissionsViewModel, ((Provider) submissionsViewModelProvider)).put(LazyClassKeyProvider.com_codeforces_app_ui_screens_login_WebLoginViewModel, ((Provider) webLoginViewModelProvider)).build());
    }

    @Override
    public Map<Class<?>, Object> getHiltViewModelAssistedMap() {
      return Collections.<Class<?>, Object>emptyMap();
    }

    @IdentifierNameString
    private static final class LazyClassKeyProvider {
      static String com_codeforces_app_ui_screens_problems_ProblemsViewModel = "com.codeforces.app.ui.screens.problems.ProblemsViewModel";

      static String com_codeforces_app_ui_screens_contests_ContestViewModel = "com.codeforces.app.ui.screens.contests.ContestViewModel";

      static String com_codeforces_app_ui_screens_search_SearchViewModel = "com.codeforces.app.ui.screens.search.SearchViewModel";

      static String com_codeforces_app_ui_screens_onboarding_OnboardingViewModel = "com.codeforces.app.ui.screens.onboarding.OnboardingViewModel";

      static String com_codeforces_app_ui_screens_login_WebLoginViewModel = "com.codeforces.app.ui.screens.login.WebLoginViewModel";

      static String com_codeforces_app_ui_screens_leaderboard_LeaderboardViewModel = "com.codeforces.app.ui.screens.leaderboard.LeaderboardViewModel";

      static String com_codeforces_app_ui_screens_login_LoginViewModel = "com.codeforces.app.ui.screens.login.LoginViewModel";

      static String com_codeforces_app_ui_screens_blog_BlogViewModel = "com.codeforces.app.ui.screens.blog.BlogViewModel";

      static String com_codeforces_app_ui_screens_submissions_SubmissionsViewModel = "com.codeforces.app.ui.screens.submissions.SubmissionsViewModel";

      static String com_codeforces_app_ui_screens_problems_ProblemDetailViewModel = "com.codeforces.app.ui.screens.problems.ProblemDetailViewModel";

      static String com_codeforces_app_MainViewModel = "com.codeforces.app.MainViewModel";

      static String com_codeforces_app_ui_screens_settings_SettingsViewModel = "com.codeforces.app.ui.screens.settings.SettingsViewModel";

      static String com_codeforces_app_ui_screens_standings_StandingsViewModel = "com.codeforces.app.ui.screens.standings.StandingsViewModel";

      static String com_codeforces_app_ui_screens_profile_ProfileViewModel = "com.codeforces.app.ui.screens.profile.ProfileViewModel";

      static String com_codeforces_app_ui_screens_home_HomeViewModel = "com.codeforces.app.ui.screens.home.HomeViewModel";

      @KeepFieldType
      ProblemsViewModel com_codeforces_app_ui_screens_problems_ProblemsViewModel2;

      @KeepFieldType
      ContestViewModel com_codeforces_app_ui_screens_contests_ContestViewModel2;

      @KeepFieldType
      SearchViewModel com_codeforces_app_ui_screens_search_SearchViewModel2;

      @KeepFieldType
      OnboardingViewModel com_codeforces_app_ui_screens_onboarding_OnboardingViewModel2;

      @KeepFieldType
      WebLoginViewModel com_codeforces_app_ui_screens_login_WebLoginViewModel2;

      @KeepFieldType
      LeaderboardViewModel com_codeforces_app_ui_screens_leaderboard_LeaderboardViewModel2;

      @KeepFieldType
      LoginViewModel com_codeforces_app_ui_screens_login_LoginViewModel2;

      @KeepFieldType
      BlogViewModel com_codeforces_app_ui_screens_blog_BlogViewModel2;

      @KeepFieldType
      SubmissionsViewModel com_codeforces_app_ui_screens_submissions_SubmissionsViewModel2;

      @KeepFieldType
      ProblemDetailViewModel com_codeforces_app_ui_screens_problems_ProblemDetailViewModel2;

      @KeepFieldType
      MainViewModel com_codeforces_app_MainViewModel2;

      @KeepFieldType
      SettingsViewModel com_codeforces_app_ui_screens_settings_SettingsViewModel2;

      @KeepFieldType
      StandingsViewModel com_codeforces_app_ui_screens_standings_StandingsViewModel2;

      @KeepFieldType
      ProfileViewModel com_codeforces_app_ui_screens_profile_ProfileViewModel2;

      @KeepFieldType
      HomeViewModel com_codeforces_app_ui_screens_home_HomeViewModel2;
    }

    private static final class SwitchingProvider<T> implements Provider<T> {
      private final SingletonCImpl singletonCImpl;

      private final ActivityRetainedCImpl activityRetainedCImpl;

      private final ViewModelCImpl viewModelCImpl;

      private final int id;

      SwitchingProvider(SingletonCImpl singletonCImpl, ActivityRetainedCImpl activityRetainedCImpl,
          ViewModelCImpl viewModelCImpl, int id) {
        this.singletonCImpl = singletonCImpl;
        this.activityRetainedCImpl = activityRetainedCImpl;
        this.viewModelCImpl = viewModelCImpl;
        this.id = id;
      }

      @SuppressWarnings("unchecked")
      @Override
      public T get() {
        switch (id) {
          case 0: // com.codeforces.app.ui.screens.blog.BlogViewModel 
          return (T) new BlogViewModel(singletonCImpl.codeforcesRepositoryProvider.get());

          case 1: // com.codeforces.app.ui.screens.contests.ContestViewModel 
          return (T) new ContestViewModel(singletonCImpl.codeforcesRepositoryProvider.get(), singletonCImpl.provideOkHttpClientProvider.get());

          case 2: // com.codeforces.app.ui.screens.home.HomeViewModel 
          return (T) new HomeViewModel(singletonCImpl.codeforcesRepositoryProvider.get(), singletonCImpl.userPreferencesRepositoryProvider.get());

          case 3: // com.codeforces.app.ui.screens.leaderboard.LeaderboardViewModel 
          return (T) new LeaderboardViewModel(singletonCImpl.codeforcesRepositoryProvider.get());

          case 4: // com.codeforces.app.ui.screens.login.LoginViewModel 
          return (T) new LoginViewModel(singletonCImpl.provideCfSubmitterProvider.get(), singletonCImpl.userPreferencesRepositoryProvider.get());

          case 5: // com.codeforces.app.MainViewModel 
          return (T) new MainViewModel(singletonCImpl.userPreferencesRepositoryProvider.get(), singletonCImpl.contestReminderManagerProvider.get());

          case 6: // com.codeforces.app.ui.screens.onboarding.OnboardingViewModel 
          return (T) new OnboardingViewModel(singletonCImpl.userPreferencesRepositoryProvider.get());

          case 7: // com.codeforces.app.ui.screens.problems.ProblemDetailViewModel 
          return (T) new ProblemDetailViewModel(singletonCImpl.provideOkHttpClientProvider.get(), singletonCImpl.userPreferencesRepositoryProvider.get(), singletonCImpl.provideCfSubmitterProvider.get(), singletonCImpl.provideApiServiceProvider.get());

          case 8: // com.codeforces.app.ui.screens.problems.ProblemsViewModel 
          return (T) new ProblemsViewModel(singletonCImpl.codeforcesRepositoryProvider.get(), singletonCImpl.userPreferencesRepositoryProvider.get());

          case 9: // com.codeforces.app.ui.screens.profile.ProfileViewModel 
          return (T) new ProfileViewModel(singletonCImpl.codeforcesRepositoryProvider.get(), singletonCImpl.userPreferencesRepositoryProvider.get());

          case 10: // com.codeforces.app.ui.screens.search.SearchViewModel 
          return (T) new SearchViewModel(singletonCImpl.codeforcesRepositoryProvider.get());

          case 11: // com.codeforces.app.ui.screens.settings.SettingsViewModel 
          return (T) new SettingsViewModel(singletonCImpl.userPreferencesRepositoryProvider.get(), singletonCImpl.codeforcesRepositoryProvider.get(), singletonCImpl.provideCfSubmitterProvider.get());

          case 12: // com.codeforces.app.ui.screens.standings.StandingsViewModel 
          return (T) new StandingsViewModel(singletonCImpl.codeforcesRepositoryProvider.get());

          case 13: // com.codeforces.app.ui.screens.submissions.SubmissionsViewModel 
          return (T) new SubmissionsViewModel(singletonCImpl.codeforcesRepositoryProvider.get());

          case 14: // com.codeforces.app.ui.screens.login.WebLoginViewModel 
          return (T) new WebLoginViewModel(singletonCImpl.provideCfSubmitterProvider.get(), singletonCImpl.userPreferencesRepositoryProvider.get());

          default: throw new AssertionError(id);
        }
      }
    }
  }

  private static final class ActivityRetainedCImpl extends CodeforcesApp_HiltComponents.ActivityRetainedC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl = this;

    private Provider<ActivityRetainedLifecycle> provideActivityRetainedLifecycleProvider;

    private ActivityRetainedCImpl(SingletonCImpl singletonCImpl,
        SavedStateHandleHolder savedStateHandleHolderParam) {
      this.singletonCImpl = singletonCImpl;

      initialize(savedStateHandleHolderParam);

    }

    @SuppressWarnings("unchecked")
    private void initialize(final SavedStateHandleHolder savedStateHandleHolderParam) {
      this.provideActivityRetainedLifecycleProvider = DoubleCheck.provider(new SwitchingProvider<ActivityRetainedLifecycle>(singletonCImpl, activityRetainedCImpl, 0));
    }

    @Override
    public ActivityComponentBuilder activityComponentBuilder() {
      return new ActivityCBuilder(singletonCImpl, activityRetainedCImpl);
    }

    @Override
    public ActivityRetainedLifecycle getActivityRetainedLifecycle() {
      return provideActivityRetainedLifecycleProvider.get();
    }

    private static final class SwitchingProvider<T> implements Provider<T> {
      private final SingletonCImpl singletonCImpl;

      private final ActivityRetainedCImpl activityRetainedCImpl;

      private final int id;

      SwitchingProvider(SingletonCImpl singletonCImpl, ActivityRetainedCImpl activityRetainedCImpl,
          int id) {
        this.singletonCImpl = singletonCImpl;
        this.activityRetainedCImpl = activityRetainedCImpl;
        this.id = id;
      }

      @SuppressWarnings("unchecked")
      @Override
      public T get() {
        switch (id) {
          case 0: // dagger.hilt.android.ActivityRetainedLifecycle 
          return (T) ActivityRetainedComponentManager_LifecycleModule_ProvideActivityRetainedLifecycleFactory.provideActivityRetainedLifecycle();

          default: throw new AssertionError(id);
        }
      }
    }
  }

  private static final class ServiceCImpl extends CodeforcesApp_HiltComponents.ServiceC {
    private final SingletonCImpl singletonCImpl;

    private final ServiceCImpl serviceCImpl = this;

    private ServiceCImpl(SingletonCImpl singletonCImpl, Service serviceParam) {
      this.singletonCImpl = singletonCImpl;


    }
  }

  private static final class SingletonCImpl extends CodeforcesApp_HiltComponents.SingletonC {
    private final ApplicationContextModule applicationContextModule;

    private final SingletonCImpl singletonCImpl = this;

    private Provider<OkHttpClient> provideOkHttpClientProvider;

    private Provider<Retrofit> provideRetrofitProvider;

    private Provider<CodeforcesApiService> provideApiServiceProvider;

    private Provider<CodeforcesDatabase> provideDatabaseProvider;

    private Provider<CodeforcesRepository> codeforcesRepositoryProvider;

    private Provider<UserPreferencesRepository> userPreferencesRepositoryProvider;

    private Provider<ContestReminderManager> contestReminderManagerProvider;

    private Provider<PersistentCookieJar> providePersistentCookieJarProvider;

    private Provider<CfSubmitter> provideCfSubmitterProvider;

    private SingletonCImpl(ApplicationContextModule applicationContextModuleParam) {
      this.applicationContextModule = applicationContextModuleParam;
      initialize(applicationContextModuleParam);

    }

    private UserDao userDao() {
      return DatabaseModule_ProvideUserDaoFactory.provideUserDao(provideDatabaseProvider.get());
    }

    private ProblemDao problemDao() {
      return DatabaseModule_ProvideProblemDaoFactory.provideProblemDao(provideDatabaseProvider.get());
    }

    private ContestDao contestDao() {
      return DatabaseModule_ProvideContestDaoFactory.provideContestDao(provideDatabaseProvider.get());
    }

    @SuppressWarnings("unchecked")
    private void initialize(final ApplicationContextModule applicationContextModuleParam) {
      this.provideOkHttpClientProvider = DoubleCheck.provider(new SwitchingProvider<OkHttpClient>(singletonCImpl, 4));
      this.provideRetrofitProvider = DoubleCheck.provider(new SwitchingProvider<Retrofit>(singletonCImpl, 3));
      this.provideApiServiceProvider = DoubleCheck.provider(new SwitchingProvider<CodeforcesApiService>(singletonCImpl, 2));
      this.provideDatabaseProvider = DoubleCheck.provider(new SwitchingProvider<CodeforcesDatabase>(singletonCImpl, 5));
      this.codeforcesRepositoryProvider = DoubleCheck.provider(new SwitchingProvider<CodeforcesRepository>(singletonCImpl, 1));
      this.userPreferencesRepositoryProvider = DoubleCheck.provider(new SwitchingProvider<UserPreferencesRepository>(singletonCImpl, 6));
      this.contestReminderManagerProvider = DoubleCheck.provider(new SwitchingProvider<ContestReminderManager>(singletonCImpl, 0));
      this.providePersistentCookieJarProvider = DoubleCheck.provider(new SwitchingProvider<PersistentCookieJar>(singletonCImpl, 8));
      this.provideCfSubmitterProvider = DoubleCheck.provider(new SwitchingProvider<CfSubmitter>(singletonCImpl, 7));
    }

    @Override
    public void injectCodeforcesApp(CodeforcesApp codeforcesApp) {
    }

    @Override
    public void injectContestReminderBootReceiver(
        ContestReminderBootReceiver contestReminderBootReceiver) {
      injectContestReminderBootReceiver2(contestReminderBootReceiver);
    }

    @Override
    public Set<Boolean> getDisableFragmentGetContextFix() {
      return Collections.<Boolean>emptySet();
    }

    @Override
    public ActivityRetainedComponentBuilder retainedComponentBuilder() {
      return new ActivityRetainedCBuilder(singletonCImpl);
    }

    @Override
    public ServiceComponentBuilder serviceComponentBuilder() {
      return new ServiceCBuilder(singletonCImpl);
    }

    @CanIgnoreReturnValue
    private ContestReminderBootReceiver injectContestReminderBootReceiver2(
        ContestReminderBootReceiver instance) {
      ContestReminderBootReceiver_MembersInjector.injectManager(instance, contestReminderManagerProvider.get());
      return instance;
    }

    private static final class SwitchingProvider<T> implements Provider<T> {
      private final SingletonCImpl singletonCImpl;

      private final int id;

      SwitchingProvider(SingletonCImpl singletonCImpl, int id) {
        this.singletonCImpl = singletonCImpl;
        this.id = id;
      }

      @SuppressWarnings("unchecked")
      @Override
      public T get() {
        switch (id) {
          case 0: // com.codeforces.app.notifications.ContestReminderManager 
          return (T) new ContestReminderManager(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule), singletonCImpl.codeforcesRepositoryProvider.get(), singletonCImpl.userPreferencesRepositoryProvider.get());

          case 1: // com.codeforces.app.data.repository.CodeforcesRepository 
          return (T) new CodeforcesRepository(singletonCImpl.provideApiServiceProvider.get(), singletonCImpl.userDao(), singletonCImpl.problemDao(), singletonCImpl.contestDao());

          case 2: // com.codeforces.app.data.api.CodeforcesApiService 
          return (T) NetworkModule_ProvideApiServiceFactory.provideApiService(singletonCImpl.provideRetrofitProvider.get());

          case 3: // retrofit2.Retrofit 
          return (T) NetworkModule_ProvideRetrofitFactory.provideRetrofit(singletonCImpl.provideOkHttpClientProvider.get());

          case 4: // okhttp3.OkHttpClient 
          return (T) NetworkModule_ProvideOkHttpClientFactory.provideOkHttpClient();

          case 5: // com.codeforces.app.data.db.CodeforcesDatabase 
          return (T) DatabaseModule_ProvideDatabaseFactory.provideDatabase(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule));

          case 6: // com.codeforces.app.data.repository.UserPreferencesRepository 
          return (T) new UserPreferencesRepository(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule));

          case 7: // com.codeforces.app.data.scraper.CfSubmitter 
          return (T) NetworkModule_ProvideCfSubmitterFactory.provideCfSubmitter(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule), singletonCImpl.provideOkHttpClientProvider.get(), singletonCImpl.providePersistentCookieJarProvider.get());

          case 8: // com.codeforces.app.data.scraper.PersistentCookieJar 
          return (T) NetworkModule_ProvidePersistentCookieJarFactory.providePersistentCookieJar(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule));

          default: throw new AssertionError(id);
        }
      }
    }
  }
}
