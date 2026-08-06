package com.codeforces.app.di;

import android.content.Context;
import com.codeforces.app.data.scraper.PersistentCookieJar;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
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
public final class NetworkModule_ProvidePersistentCookieJarFactory implements Factory<PersistentCookieJar> {
  private final Provider<Context> contextProvider;

  public NetworkModule_ProvidePersistentCookieJarFactory(Provider<Context> contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public PersistentCookieJar get() {
    return providePersistentCookieJar(contextProvider.get());
  }

  public static NetworkModule_ProvidePersistentCookieJarFactory create(
      Provider<Context> contextProvider) {
    return new NetworkModule_ProvidePersistentCookieJarFactory(contextProvider);
  }

  public static PersistentCookieJar providePersistentCookieJar(Context context) {
    return Preconditions.checkNotNullFromProvides(NetworkModule.INSTANCE.providePersistentCookieJar(context));
  }
}
