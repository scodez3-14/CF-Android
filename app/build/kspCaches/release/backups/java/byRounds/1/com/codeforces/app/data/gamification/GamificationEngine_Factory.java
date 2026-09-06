package com.codeforces.app.data.gamification;

import android.content.Context;
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
public final class GamificationEngine_Factory implements Factory<GamificationEngine> {
  private final Provider<Context> contextProvider;

  public GamificationEngine_Factory(Provider<Context> contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public GamificationEngine get() {
    return newInstance(contextProvider.get());
  }

  public static GamificationEngine_Factory create(Provider<Context> contextProvider) {
    return new GamificationEngine_Factory(contextProvider);
  }

  public static GamificationEngine newInstance(Context context) {
    return new GamificationEngine(context);
  }
}
