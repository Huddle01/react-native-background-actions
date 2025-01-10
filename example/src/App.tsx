// MinimalBackgroundExample.tsx
import React, { useState, useEffect } from 'react';
import { View, Button, Text } from 'react-native';
import BackgroundService from 'react-native-background-actions';

export const MinimalBackgroundExample = () => {
  const [isRunning, setIsRunning] = useState(false);

  useEffect(() => {
    return () => {
      // Cleanup on unmount
      if (BackgroundService.isRunning()) {
        handleStop();
      }
    };
  }, []);

  const task = async () => {
    // Example background task
    await new Promise((resolve) => setTimeout(resolve, 5000));
  };

  const handleStart = async () => {
    try {
      const options = {
        taskName: 'Huddle01',
        taskTitle: 'Huddle01',
        taskDesc: 'You are in a meeting now',
        taskIcon: {
          name: 'ic_launcher_round',
          type: 'mipmap',
        },
        color: '#246BFD',
        linkingURI: 'huddle01://huddle01.com',
        stopOnTerminate: true,
        type: ['mediaPlayback'],
      };

      await BackgroundService.start(task, options);
      setIsRunning(true);
    } catch (error) {
      console.error('Failed to start task:', error);
    }
  };

  const handleStop = async () => {
    try {
      await BackgroundService.stop();
      setIsRunning(false);
    } catch (error) {
      console.error('Failed to stop task:', error);
    }
  };

  return (
    <View style={{ flex: 1, justifyContent: 'center', alignItems: 'center' }}>
      <Text>Status: {isRunning ? 'Running' : 'Stopped'}</Text>
      <Button
        title={isRunning ? 'Stop Task' : 'Start Task'}
        onPress={isRunning ? handleStop : handleStart}
      />
    </View>
  );
};

export default MinimalBackgroundExample;
