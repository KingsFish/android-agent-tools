// src/commands/uninstall_app.ts
import { ToolDefinition, CliOptions, ErrorCodes } from '../types';
import { selectDevice, execAdb, success, failure } from '../utils';

export const definition: ToolDefinition = {
  name: 'uninstall_app',
  description: 'Uninstall an application from the device',
  parameters: [
    { name: 'package_name', type: 'string', required: true, description: 'Package name of the application to uninstall' },
    { name: 'keep_data', type: 'boolean', required: false, description: 'Keep data and cache directories', default: false }
  ],
  adbCommand: 'uninstall [-k] <package_name>',
  async execute(packageName: string, keepData?: boolean, options?: CliOptions): Promise<string> {
    try {
      const device = await selectDevice(options?.device);

      const args = ['uninstall'];

      if (keepData) {
        args.push('-k'); // Keep data
      }

      args.push(packageName);

      const result = await execAdb(args, {
        deviceId: device,
        timeout: options?.timeout ?? 60000 // Default 1 minute for uninstall
      });

      if (result.exitCode === 0) {
        if (result.stdout.includes('Success')) {
          return success({ uninstalled: true, package_name: packageName });
        }
        if (result.stdout.includes('Failure')) {
          return failure(ErrorCodes.APP_NOT_FOUND, `App not found: ${packageName}`);
        }
      }

      return failure(ErrorCodes.EXEC_ERROR, result.stderr || 'Failed to uninstall app');
    } catch (err: any) {
      return failure(ErrorCodes.DEVICE_NOT_FOUND, err.message);
    }
  }
};