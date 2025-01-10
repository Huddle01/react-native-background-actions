import type { TurboModule } from 'react-native';
import { TurboModuleRegistry, NativeModules } from 'react-native';

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

const isNewArchEnabled = global?.nativeFabricUIManager === 'Fabric';

// Get the appropriate native module based on architecture
const getNativeModule = (): Spec => {
  if (isNewArchEnabled) {
    return TurboModuleRegistry.getEnforcing<Spec>('BackgroundActions');
  }

  const RNBackgroundActions = NativeModules.RNBackgroundActions;

  if (!RNBackgroundActions) {
    throw new Error(
      "react-native-background-actions didn't link properly. Make sure autolink is enabled or try to manually add native module"
    );
  }

  return RNBackgroundActions;
};

// Create a proxy that lazily gets the native module and properly handles method calls
const module = new Proxy<Spec>({} as Spec, {
  get(_target: Spec, prop: string | symbol) {
    const nativeModule = getNativeModule();

    if (typeof prop === 'string') {
      const value = nativeModule[prop as keyof Spec];

      if (typeof value === 'function') {
        // Bind the function to the native module to preserve context
        return value.bind(nativeModule);
      }

      return value;
    }

    return undefined;
  },
});

export default module;
