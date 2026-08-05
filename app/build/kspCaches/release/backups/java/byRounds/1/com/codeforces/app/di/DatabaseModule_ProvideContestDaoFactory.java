package com.codeforces.app.di;

import com.codeforces.app.data.db.CodeforcesDatabase;
import com.codeforces.app.data.db.ContestDao;
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
public final class DatabaseModule_ProvideContestDaoFactory implements Factory<ContestDao> {
  private final Provider<CodeforcesDatabase> dbProvider;

  public DatabaseModule_ProvideContestDaoFactory(Provider<CodeforcesDatabase> dbProvider) {
    this.dbProvider = dbProvider;
  }

  @Override
  public ContestDao get() {
    return provideContestDao(dbProvider.get());
  }

  public static DatabaseModule_ProvideContestDaoFactory create(
      Provider<CodeforcesDatabase> dbProvider) {
    return new DatabaseModule_ProvideContestDaoFactory(dbProvider);
  }

  public static ContestDao provideContestDao(CodeforcesDatabase db) {
    return Preconditions.checkNotNullFromProvides(DatabaseModule.INSTANCE.provideContestDao(db));
  }
}
