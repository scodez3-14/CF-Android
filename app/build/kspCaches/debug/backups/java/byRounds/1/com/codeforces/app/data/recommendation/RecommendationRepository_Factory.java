package com.codeforces.app.data.recommendation;

import com.codeforces.app.data.db.ProblemDao;
import com.codeforces.app.data.repository.CodeforcesRepository;
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
public final class RecommendationRepository_Factory implements Factory<RecommendationRepository> {
  private final Provider<CodeforcesRepository> repoProvider;

  private final Provider<ProblemDao> problemDaoProvider;

  public RecommendationRepository_Factory(Provider<CodeforcesRepository> repoProvider,
      Provider<ProblemDao> problemDaoProvider) {
    this.repoProvider = repoProvider;
    this.problemDaoProvider = problemDaoProvider;
  }

  @Override
  public RecommendationRepository get() {
    return newInstance(repoProvider.get(), problemDaoProvider.get());
  }

  public static RecommendationRepository_Factory create(Provider<CodeforcesRepository> repoProvider,
      Provider<ProblemDao> problemDaoProvider) {
    return new RecommendationRepository_Factory(repoProvider, problemDaoProvider);
  }

  public static RecommendationRepository newInstance(CodeforcesRepository repo,
      ProblemDao problemDao) {
    return new RecommendationRepository(repo, problemDao);
  }
}
