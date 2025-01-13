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
  start(options: NotificationOptions): Promise<void>;
  stop(): Promise<void>;
  updateNotification(options: NotificationOptions): Promise<void>;
  addListener(eventName: string): void;
  removeListeners(count: number): void;
}

export default TurboModuleRegistry.getEnforcing<Spec>('BackgroundActions');
