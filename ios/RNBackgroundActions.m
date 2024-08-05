@import UIKit;
#import "RNBackgroundActions.h"

@implementation RNBackgroundActions {
  UIBackgroundTaskIdentifier bgTask;
  NSTimer *timer;
}

RCT_EXPORT_MODULE()
- (NSArray<NSString *> *)supportedEvents
{
  return @[@"expiration"];
}

- (void) _start
{
  [self _stop];
  
  timer = [NSTimer scheduledTimerWithTimeInterval:30 repeats:YES block:^(NSTimer * _Nonnull timer) {
    [self expirationHandler];
  }];
  self->bgTask = [self _startNew];
}

- (void) _stop
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

RCT_EXPORT_METHOD(start:(NSDictionary *)options
                  resolver:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject)
{
  [self _start];
  resolve(nil);
}

RCT_EXPORT_METHOD(stop:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject)
{
  [self _stop];
  [timer invalidate];
  timer = NULL;
  resolve(nil);
}

@end
