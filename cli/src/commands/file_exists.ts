// src/commands/file_exists.ts
import { ToolDefinition, CliOptions, ErrorCodes } from '../types';
import { selectDevice, execAdb, success, failure } from '../utils';

export const definition: ToolDefinition = {
  name: 'file_exists',
  description: 'Check if a file or directory exists on the device',
  parameters: [
    { name: 'path', type: 'string', required: true, description: 'Absolute path to check' }
  ],
  adbCommand: 'shell test -e <path>',
  async execute(path: string, options?: CliOptions): Promise<string> {
    try {
      const device = await selectDevice(options?.device);

      // Test if file exists (-e tests for existence)
      const result = await execAdb(
        ['shell', 'test', '-e', path, '&&', 'echo', 'exists', '||', 'echo', 'not_found'],
        { deviceId: device, timeout: options?.timeout }
      );

      const exists = result.stdout.trim() === 'exists';

      // Also check if it's a file or directory
      let fileType = 'unknown';
      if (exists) {
        const fileResult = await execAdb(
          ['shell', 'test', '-f', path, '&&', 'echo', 'file', '||', 'echo', 'not_file'],
          { deviceId: device, timeout: options?.timeout }
        );

        if (fileResult.stdout.trim() === 'file') {
          fileType = 'file';
        } else {
          const dirResult = await execAdb(
            ['shell', 'test', '-d', path, '&&', 'echo', 'directory', '||', 'echo', 'not_directory'],
            { deviceId: device, timeout: options?.timeout }
          );
          if (dirResult.stdout.trim() === 'directory') {
            fileType = 'directory';
          }
        }
      }

      return success({ path, exists, type: fileType });
    } catch (err: any) {
      return failure(ErrorCodes.DEVICE_NOT_FOUND, err.message);
    }
  }
};