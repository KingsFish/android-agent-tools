// src/commands/force_stop_app.ts
import { ToolDefinition, CliOptions, ErrorCodes } from '../types';
import { selectDevice, execAdb, success, failure } from '../utils';

export const definition: ToolDefinition = {
  name: 'force_stop_app',
  description: 'Force stop an application',
  parameters: [
    { name: 'package_name', type: 'string', required: true, description: 'Package name of the application to stop' }
  ],
  adbCommand: 'shell am force-stop <package_name>',
  async execute(packageName: string, options?: CliOptions): Promise<string> {
    try {
      const device = await selectDevice(options?.device);

      const result = await execAdb(
        ['shell', 'am', 'force-stop', packageName],
        { deviceId: device, timeout: options?.timeout }
      );

      // force-stop usually returns 0 even if package doesn't exist
      // We consider it successful if exit code is 0
      if (result.exitCode === 0) {
        return success({ stopped: true, package_name: packageName });
      }

      return failure(ErrorCodes.EXEC_ERROR, result.stderr || 'Failed to force stop app');
    } catch (err: any) {
      return failure(ErrorCodes.DEVICE_NOT_FOUND, err.message);
    }
  }
};