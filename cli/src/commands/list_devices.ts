// src/commands/list_devices.ts
import { ToolDefinition, CliOptions } from '../types';
import { listDevices, success } from '../utils';

export const definition: ToolDefinition = {
  name: 'list_devices',
  description: 'List all connected ADB devices',
  parameters: [],
  async execute(options?: CliOptions): Promise<string> {
    const devices = await listDevices();
    return success({ devices });
  }
};