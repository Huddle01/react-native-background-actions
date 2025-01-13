#import "BackgroundActions.h"

@implementation BackgroundActions {
  UIBackgroundTaskIdentifier bgTask;
  NSTimer *timer;
}

RCT_EXPORT_MODULE()

- (std::shared_ptr<facebook::react::TurboModule>)getTurboModule:
    (const facebook::react::ObjCTurboModule::InitParams &)params
{
    return std::make_shared<facebook::react::NativeBackgroundActionsSpecJSI>(params);
}

- (NSArray<NSString *> *)supportedEvents
{
  return @[@"expiration"];
}

- (void) start
{
  [self stop];
  
  timer = [NSTimer scheduledTimerWithTimeInterval:30 repeats:YES block:^(NSTimer * _Nonnull timer) {
    [self expirationHandler];
  }];
  self->bgTask = [self _startNew];
}

- (void) stop
{
  if (bgTask != UIBackgroundTaskInvalid) {
    [[UIApplication sharedApplication] endBackgroundTask:bgTask];
    bgTask = UIBackgroundTaskInvalid;
  }
}

- (UIBackgroundTaskIdentifier)_startNew {
  UIBackgroundTaskIdentifier _bgTask = [[UIApplication sharedApplication] beginBackgroundTaskWithName:@"RNBackgroundActions" expirationHandler:^{
    [self expirationHandler];
  }];
  
  return _bgTask;
}

- (void) expirationHandler {
  [self onExpiration];
  UIBackgroundTaskIdentifier newBgTask = [self _startNew];
  
  [[UIApplication sharedApplication] endBackgroundTask: self->bgTask];
  self->bgTask = newBgTask;
}

- (void)onExpiration
{
  [self sendEventWithName:@"expiration"
                     body:@{}];
}


- (void)start:(JS::NativeBackgroundActions::NotificationOptions &)options resolve:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject { 
  [self start];
  resolve(nil);
}

- (void)stop:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject { 
  [self stop];
  [timer invalidate];
  timer = NULL;
  resolve(nil);
}

- (void)updateNotification:(JS::NativeBackgroundActions::NotificationOptions &)options resolve:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject { 
  
}

@end
