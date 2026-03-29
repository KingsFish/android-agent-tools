// src/commands/list_apps.ts
import { ToolDefinition, CliOptions, ErrorCodes } from '../types';
import { selectDevice, execAdb, success, failure } from '../utils';

export const definition: ToolDefinition = {
  name: 'list_apps',
  description: 'List installed applications',
  parameters: [
    { name: 'include_system', type: 'boolean', required: false, description: 'Include system apps', default: false }
  ],
  adbCommand: 'shell pm list packages [-3]',
  async execute(includeSystem?: boolean, options?: CliOptions): Promise<string> {
    try {
      const device = await selectDevice(options?.device);
      const args = includeSystem
        ? ['shell', 'pm', 'list', 'packages']
        : ['shell', 'pm', 'list', 'packages', '-3'];
      const result = await execAdb(args, { deviceId: device, timeout: options?.timeout });
      if (result.exitCode === 0) {
        const apps = result.stdout.split('\n')
          .filter(line => line.startsWith('package:'))
          .map(line => ({ package_name: line.replace('package:', '').trim() }));
        return success({ apps });
      }
      return failure(ErrorCodes.EXEC_ERROR, result.stderr || 'Failed to list apps');
    } catch (err: any) {
      return failure(ErrorCodes.DEVICE_NOT_FOUND, err.message);
    }
  }
};