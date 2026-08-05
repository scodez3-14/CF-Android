package com.codeforces.app.di;

import com.codeforces.app.data.db.CodeforcesDatabase;
import com.codeforces.app.data.db.ProblemDao;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
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
public final class DatabaseModule_ProvideProblemDaoFactory implements Factory<ProblemDao> {
  private final Provider<CodeforcesDatabase> dbProvider;

  public DatabaseModule_ProvideProblemDaoFactory(Provider<CodeforcesDatabase> dbProvider) {
    this.dbProvider = dbProvider;
  }

  @Override
  public ProblemDao get() {
    return provideProblemDao(dbProvider.get());
  }

  public static DatabaseModule_ProvideProblemDaoFactory create(
      Provider<CodeforcesDatabase> dbProvider) {
    return new DatabaseModule_ProvideProblemDaoFactory(dbProvider);
  }

  public static ProblemDao provideProblemDao(CodeforcesDatabase db) {
    return Preconditions.checkNotNullFromProvides(DatabaseModule.INSTANCE.provideProblemDao(db));
  }
}
