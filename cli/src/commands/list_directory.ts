// src/commands/list_directory.ts
import { ToolDefinition, CliOptions, ErrorCodes } from '../types';
import { selectDevice, execAdb, success, failure } from '../utils';

export const definition: ToolDefinition = {
  name: 'list_directory',
  description: 'List contents of a directory on the device',
  parameters: [
    { name: 'path', type: 'string', required: true, description: 'Absolute path to the directory' }
  ],
  adbCommand: 'shell ls -la <path>',
  async execute(path: string, options?: CliOptions): Promise<string> {
    try {
      const device = await selectDevice(options?.device);
      const result = await execAdb(
        ['shell', 'ls', '-la', path],
        { deviceId: device, timeout: options?.timeout }
      );

      if (result.exitCode === 0) {
        // Parse ls -la output
        const lines = result.stdout.trim().split('\n').filter(line => line.length > 0);
        const entries = lines.slice(1).map(line => {
          // Parse ls -la format: permissions, links, owner, group, size, date, time, name
          const parts = line.split(/\s+/);
          if (parts.length >= 8) {
            const permissions = parts[0];
            const size = parts[4];
            const name = parts.slice(7).join(' ');
            const isDirectory = permissions.startsWith('d');
            const isFile = permissions.startsWith('-');

            return {
              name,
              type: isDirectory ? 'directory' : isFile ? 'file' : 'other',
              permissions,
              size: parseInt(size, 10) || 0
            };
          }
          return null;
        }).filter((entry): entry is NonNullable<typeof entry> => entry !== null);

        return success({ path, entries, count: entries.length });
      }

      return failure(ErrorCodes.FILE_NOT_FOUND, `Directory does not exist: ${path}`);
    } catch (err: any) {
      return failure(ErrorCodes.DEVICE_NOT_FOUND, err.message);
    }
  }
};