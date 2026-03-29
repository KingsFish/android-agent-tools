// src/commands/is_app_running.ts
import { ToolDefinition, CliOptions, ErrorCodes } from '../types';
import { selectDevice, execAdb, success, failure } from '../utils';

export const definition: ToolDefinition = {
  name: 'is_app_running',
  description: 'Check if an application is currently running',
  parameters: [
    { name: 'package_name', type: 'string', required: true, description: 'Package name of the application to check' }
  ],
  adbCommand: 'shell pidof <package_name>',
  async execute(packageName: string, options?: CliOptions): Promise<string> {
    if (!packageName || typeof packageName !== 'string') {
      return failure(ErrorCodes.INVALID_PARAMETER, 'Package name is required');
    }

    try {
      const device = await selectDevice(options?.device);
      const result = await execAdb(
        ['shell', 'pidof', packageName],
        { deviceId: device, timeout: options?.timeout }
      );

      // pidof returns 0 and PID if running, non-zero if not found
      const pid = result.stdout.trim();
      const isRunning = result.exitCode === 0 && pid !== '';

      return success({
        package_name: packageName,
        is_running: isRunning,
        pid: isRunning ? parseInt(pid) : null
      });
    } catch (err: any) {
      return failure(ErrorCodes.DEVICE_NOT_FOUND, err.message);
    }
  }
};