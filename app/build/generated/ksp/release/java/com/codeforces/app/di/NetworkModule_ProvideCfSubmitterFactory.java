package com.codeforces.app.di;

import android.content.Context;
import com.codeforces.app.data.scraper.CfSubmitter;
import com.codeforces.app.data.scraper.PersistentCookieJar;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;
import okhttp3.OkHttpClient;

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
public final class NetworkModule_ProvideCfSubmitterFactory implements Factory<CfSubmitter> {
  private final Provider<Context> contextProvider;

  private final Provider<OkHttpClient> okHttpClientProvider;

  private final Provider<PersistentCookieJar> cookieJarProvider;

  public NetworkModule_ProvideCfSubmitterFactory(Provider<Context> contextProvider,
      Provider<OkHttpClient> okHttpClientProvider,
      Provider<PersistentCookieJar> cookieJarProvider) {
    this.contextProvider = contextProvider;
    this.okHttpClientProvider = okHttpClientProvider;
    this.cookieJarProvider = cookieJarProvider;
  }

  @Override
  public CfSubmitter get() {
    return provideCfSubmitter(contextProvider.get(), okHttpClientProvider.get(), cookieJarProvider.get());
  }

  public static NetworkModule_ProvideCfSubmitterFactory create(Provider<Context> contextProvider,
      Provider<OkHttpClient> okHttpClientProvider,
      Provider<PersistentCookieJar> cookieJarProvider) {
    return new NetworkModule_ProvideCfSubmitterFactory(contextProvider, okHttpClientProvider, cookieJarProvider);
  }

  public static CfSubmitter provideCfSubmitter(Context context, OkHttpClient okHttpClient,
      PersistentCookieJar cookieJar) {
    return Preconditions.checkNotNullFromProvides(NetworkModule.INSTANCE.provideCfSubmitter(context, okHttpClient, cookieJar));
  }
}
