package com.codeforces.app.notifications;

import dagger.MembersInjector;
import dagger.internal.DaggerGenerated;
import dagger.internal.InjectedFieldSignature;
import dagger.internal.QualifierMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

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
public final class ContestReminderBootReceiver_MembersInjector implements MembersInjector<ContestReminderBootReceiver> {
  private final Provider<ContestReminderManager> managerProvider;

  public ContestReminderBootReceiver_MembersInjector(
      Provider<ContestReminderManager> managerProvider) {
    this.managerProvider = managerProvider;
  }

  public static MembersInjector<ContestReminderBootReceiver> create(
      Provider<ContestReminderManager> managerProvider) {
    return new ContestReminderBootReceiver_MembersInjector(managerProvider);
  }

  @Override
  public void injectMembers(ContestReminderBootReceiver instance) {
    injectManager(instance, managerProvider.get());
  }

  @InjectedFieldSignature("com.codeforces.app.notifications.ContestReminderBootReceiver.manager")
  public static void injectManager(ContestReminderBootReceiver instance,
      ContestReminderManager manager) {
    instance.manager = manager;
  }
}
