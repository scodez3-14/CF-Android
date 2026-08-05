package com.codeforces.app.data.repository;

import com.codeforces.app.data.api.CodeforcesApiService;
import com.codeforces.app.data.db.ContestDao;
import com.codeforces.app.data.db.ProblemDao;
import com.codeforces.app.data.db.UserDao;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata("javax.inject.Singleton")
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
public final class CodeforcesRepository_Factory implements Factory<CodeforcesRepository> {
  private final Provider<CodeforcesApiService> apiProvider;

  private final Provider<UserDao> userDaoProvider;

  private final Provider<ProblemDao> problemDaoProvider;

  private final Provider<ContestDao> contestDaoProvider;

  public CodeforcesRepository_Factory(Provider<CodeforcesApiService> apiProvider,
      Provider<UserDao> userDaoProvider, Provider<ProblemDao> problemDaoProvider,
      Provider<ContestDao> contestDaoProvider) {
    this.apiProvider = apiProvider;
    this.userDaoProvider = userDaoProvider;
    this.problemDaoProvider = problemDaoProvider;
    this.contestDaoProvider = contestDaoProvider;
  }

  @Override
  public CodeforcesRepository get() {
    return newInstance(apiProvider.get(), userDaoProvider.get(), problemDaoProvider.get(), contestDaoProvider.get());
  }

  public static CodeforcesRepository_Factory create(Provider<CodeforcesApiService> apiProvider,
      Provider<UserDao> userDaoProvider, Provider<ProblemDao> problemDaoProvider,
      Provider<ContestDao> contestDaoProvider) {
    return new CodeforcesRepository_Factory(apiProvider, userDaoProvider, problemDaoProvider, contestDaoProvider);
  }

  public static CodeforcesRepository newInstance(CodeforcesApiService api, UserDao userDao,
      ProblemDao problemDao, ContestDao contestDao) {
    return new CodeforcesRepository(api, userDao, problemDao, contestDao);
  }
}
