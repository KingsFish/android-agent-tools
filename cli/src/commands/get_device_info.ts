// src/commands/get_device_info.ts
import { ToolDefinition, CliOptions, ErrorCodes } from '../types';
import { selectDevice, execAdb, success, failure } from '../utils';

export const definition: ToolDefinition = {
  name: 'get_device_info',
  description: 'Get device information (model, brand, Android version, screen, etc.)',
  parameters: [],
  adbCommand: 'shell getprop',
  async execute(options?: CliOptions): Promise<string> {
    try {
      const device = await selectDevice(options?.device);
      const result = await execAdb(['shell', 'getprop'], { deviceId: device, timeout: options?.timeout });
      if (result.exitCode === 0) {
        const props = parseGetprop(result.stdout);
        return success(props);
      }
      return failure(ErrorCodes.EXEC_ERROR, result.stderr || 'Failed to get device info');
    } catch (err: any) {
      return failure(ErrorCodes.DEVICE_NOT_FOUND, err.message);
    }
  }
};

interface DeviceInfo {
  brand: string;
  model: string;
  device: string;
  android_version: string;
  sdk_version: number;
  locale: string;
  timezone: string;
}

function parseGetprop(output: string): DeviceInfo {
  const props: Record<string, string> = {};
  const lines = output.split('\n');
  for (const line of lines) {
    const match = line.match(/\[(.+)\]:\s*\[(.+)\]/);
    if (match) {
      props[match[1]] = match[2];
    }
  }
  return {
    brand: props['ro.product.brand'] || '',
    model: props['ro.product.model'] || '',
    device: props['ro.product.device'] || '',
    android_version: props['ro.build.version.release'] || '',
    sdk_version: parseInt(props['ro.build.version.sdk'] || '0'),
    locale: props['persist.sys.locale'] || '',
    timezone: props['persist.sys.timezone'] || ''
  };
}