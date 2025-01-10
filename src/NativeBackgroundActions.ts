import type { TurboModule } from 'react-native';
import { TurboModuleRegistry } from 'react-native';

export interface NotificationOptions {
  taskName: string;
  taskTitle: string;
  taskDesc: string;
  taskIcon: {
    name: string;
    type: string;
    package?: string;
  };
  color?: string;
  linkingURI?: string;
  progressBar?: {
    max: number;
    value: number;
    indeterminate: boolean;
  };
  priority?: number;
  importance?: number;
  ongoing?: boolean;
  autoCancel?: boolean;
  stopOnTerminate?: boolean;
  serviceTypes?: string[];
}

export interface Spec extends TurboModule {
  /**
   * Start a background task with the given options
   */
  start(options: NotificationOptions): Promise<void>;

  /**
   * Stop the currently running background task
   */
  stop(): Promise<void>;

  /**
   * Update the notification with new options
   */
  updateNotification(options: NotificationOptions): Promise<void>;

  // Required for event emitter
  addListener(eventName: string): void;
  removeListeners(count: number): void;
}

export default TurboModuleRegistry.getEnforcing<Spec>('BackgroundActions');
