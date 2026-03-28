// src/commands/launch_app.ts
import { ToolDefinition, CliOptions, ErrorCodes } from '../types';
import { selectDevice, execAdb, success, failure } from '../utils';

export const definition: ToolDefinition = {
  name: 'launch_app',
  description: 'Launch an application by package name',
  parameters: [
    { name: 'package_name', type: 'string', required: true, description: 'Package name of the application to launch' },
    { name: 'activity', type: 'string', required: false, description: 'Specific activity to launch (optional)' }
  ],
  adbCommand: 'shell am start -n <package_name>/<activity> or shell monkey -p <package_name>',
  async execute(packageName: string, activity?: string, options?: CliOptions): Promise<string> {
    try {
      const device = await selectDevice(options?.device);

      if (activity) {
        // Launch specific activity using am start
        const result = await execAdb(
          ['shell', 'am', 'start', '-n', `${packageName}/${activity}`],
          { deviceId: device, timeout: options?.timeout }
        );

        if (result.exitCode === 0 && !result.stdout.includes('Error:')) {
          return success({ package_name: packageName, activity, launched: true });
        }
        return failure(ErrorCodes.LAUNCH_FAILED, result.stderr || result.stdout || 'Failed to launch app');
      } else {
        // Use monkey to launch the default activity
        const result = await execAdb(
          ['shell', 'monkey', '-p', packageName, '-c', 'android.intent.category.LAUNCHER', '1'],
          { deviceId: device, timeout: options?.timeout }
        );

        if (result.exitCode === 0) {
          return success({ package_name: packageName, launched: true });
        }
        return failure(ErrorCodes.LAUNCH_FAILED, result.stderr || 'Failed to launch app');
      }
    } catch (err: any) {
      return failure(ErrorCodes.DEVICE_NOT_FOUND, err.message);
    }
  }
};