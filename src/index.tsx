import { Platform, AppRegistry, NativeEventEmitter } from 'react-native';
import type { EmitterSubscription } from 'react-native';
import EventEmitter from 'eventemitter3';
import { type NotificationOptions } from './NativeBackgroundActions';
import { default as RNBackgroundActions } from './NativeBackgroundActions';

export const Priority = {
  MIN: -2,
  LOW: -1,
  DEFAULT: 0,
  HIGH: 1,
  MAX: 2,
} as const;

export const Importance = {
  DEFAULT: 3,
  MAX: 5,
  HIGH: 4,
  LOW: 2,
  MIN: 1,
  NONE: 0,
} as const;

export type TaskIcon = {
  name: string;
  type: string;
  package?: string;
};

export type ProgressBar = {
  max: number;
  value: number;
  indeterminate?: boolean;
};

export interface NotificationUpdateOptions
  extends Partial<NotificationOptions> {}

class BackgroundServer extends EventEmitter {
  private _runnedTasks: number;
  private _stopTask: (arg?: any) => void;
  private _isRunning: boolean;
  private _currentOptions?: NotificationOptions;
  private _eventEmitter: NativeEventEmitter;
  private _subscription?: EmitterSubscription;

  constructor() {
    super();
    this._runnedTasks = 0;
    this._stopTask = () => {};
    this._isRunning = false;
    this._eventEmitter = new NativeEventEmitter(RNBackgroundActions);
    this._addListeners();
  }

  private _addListeners(): void {
    this._subscription = this._eventEmitter.addListener('expiration', () => {
      this.emit('expiration');
    });
  }

  /**
   * **ANDROID ONLY**
   * Updates the task notification.
   * @param taskData - The notification update options
   * @throws {Error} If no background action is running
   */
  public async updateNotification(
    taskData: NotificationUpdateOptions
  ): Promise<void> {
    if (Platform.OS !== 'android') return;

    if (!this.isRunning()) {
      throw new Error(
        'A BackgroundAction must be running before updating the notification'
      );
    }

    if (!this._currentOptions) {
      throw new Error('No current options found');
    }

    this._currentOptions = this._normalizeOptions({
      ...this._currentOptions,
      ...taskData,
    });

    await RNBackgroundActions.updateNotification(this._currentOptions);
  }

  /**
   * Returns if the current background task is running.
   * @returns {boolean} True if the task is running, false otherwise
   */
  public isRunning(): boolean {
    return this._isRunning;
  }

  /**
   * Starts a background task
   * @param task - The task to run in the background
   * @param options - The background task options
   */
  public async start<T>(
    task: (taskData?: T) => Promise<void>,
    options: NotificationOptions & { parameters?: T }
  ): Promise<void> {
    this._runnedTasks++;
    this._currentOptions = this._normalizeOptions(options);
    const finalTask = this._generateTask(task, options.parameters);

    if (Platform.OS === 'android') {
      AppRegistry.registerHeadlessTask(
        this._currentOptions.taskName,
        () => finalTask
      );

      await RNBackgroundActions.start(this._currentOptions);
      this._isRunning = true;
    } else {
      await RNBackgroundActions.start(this._currentOptions);
      this._isRunning = true;
      finalTask();
    }
  }

  private _generateTask<T>(
    task: (taskData?: T) => Promise<void>,
    parameters?: T
  ): () => Promise<void> {
    return async () => {
      await new Promise<void>((resolve) => {
        this._stopTask = resolve;
        task(parameters).then(() => this.stop());
      });
    };
  }

  private _normalizeOptions(options: NotificationOptions): NotificationOptions {
    return {
      taskName: `${options.taskName}${this._runnedTasks}`,
      taskTitle: options.taskTitle,
      taskDesc: options.taskDesc,
      taskIcon: { ...options.taskIcon },
      color: options.color || '#ffffff',
      linkingURI: options.linkingURI,
      progressBar: options.progressBar,
      priority: options.priority ?? Priority.DEFAULT,
      importance: options.importance ?? Importance.DEFAULT,
      ongoing: options.ongoing ?? true,
      autoCancel: options.autoCancel ?? false,
      stopOnTerminate: options.stopOnTerminate ?? false,
      serviceTypes: options.serviceTypes,
    };
  }

  /**
   * Stops the background task
   */
  public async stop(): Promise<void> {
    this._stopTask();
    await RNBackgroundActions.stop();
    this._isRunning = false;
  }

  /**
   * Cleanup method to be called when component unmounts
   */
  public cleanup(): void {
    this._subscription?.remove();
  }
}

// Create singleton instance
const backgroundServer = new BackgroundServer();
export default backgroundServer;
