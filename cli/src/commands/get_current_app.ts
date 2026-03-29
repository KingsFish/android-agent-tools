// src/commands/get_current_app.ts
import { ToolDefinition, CliOptions, ErrorCodes } from '../types';
import { selectDevice, execAdb, success, failure } from '../utils';

export const definition: ToolDefinition = {
  name: 'get_current_app',
  description: 'Get the currently resumed (foreground) activity information',
  parameters: [],
  adbCommand: 'shell dumpsys activity activities | grep mResumedActivity',
  async execute(options?: CliOptions): Promise<string> {
    try {
      const device = await selectDevice(options?.device);
      const result = await execAdb(
        ['shell', 'dumpsys', 'activity', 'activities'],
        { deviceId: device, timeout: options?.timeout }
      );
      if (result.exitCode === 0) {
        const app = parseResumedActivity(result.stdout);
        if (app) {
          return success(app);
        }
        return failure(ErrorCodes.EXEC_ERROR, 'No resumed activity found');
      }
      return failure(ErrorCodes.EXEC_ERROR, result.stderr || 'Failed to get current app');
    } catch (err: any) {
      return failure(ErrorCodes.DEVICE_NOT_FOUND, err.message);
    }
  }
};

interface CurrentApp {
  package_name: string;
  activity: string;
  full_name: string;
}

function parseResumedActivity(output: string): CurrentApp | null {
  // Look for mResumedActivity in the output
  const match = output.match(/mResumedActivity:\s*ActivityRecord\{[^}]+\s+(\S+)\/(\S+)\s+/);
  if (match) {
    const packageName = match[1];
    const activity = match[2];
    return {
      package_name: packageName,
      activity: activity,
      full_name: `${packageName}/${activity}`
    };
  }

  // Alternative pattern for some Android versions
  const altMatch = output.match(/mResumedActivity:.*?(\S+)\/(\S+)/);
  if (altMatch) {
    const packageName = altMatch[1];
    const activity = altMatch[2];
    return {
      package_name: packageName,
      activity: activity,
      full_name: `${packageName}/${activity}`
    };
  }

  return null;
}