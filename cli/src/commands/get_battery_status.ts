// src/commands/get_battery_status.ts
import { ToolDefinition, CliOptions, ErrorCodes } from '../types';
import { selectDevice, execAdb, success, failure } from '../utils';

export const definition: ToolDefinition = {
  name: 'get_battery_status',
  description: 'Get battery status (level, charging state, health, temperature, etc.)',
  parameters: [],
  adbCommand: 'shell dumpsys battery',
  async execute(options?: CliOptions): Promise<string> {
    try {
      const device = await selectDevice(options?.device);
      const result = await execAdb(['shell', 'dumpsys', 'battery'], { deviceId: device, timeout: options?.timeout });
      if (result.exitCode === 0) {
        const battery = parseBatteryInfo(result.stdout);
        return success(battery);
      }
      return failure(ErrorCodes.EXEC_ERROR, result.stderr || 'Failed to get battery status');
    } catch (err: any) {
      return failure(ErrorCodes.DEVICE_NOT_FOUND, err.message);
    }
  }
};

interface BatteryInfo {
  level: number;
  scale: number;
  status: string;
  health: string;
  plugged: string;
  temperature: number;
  voltage: number;
  technology: string;
}

function parseBatteryInfo(output: string): BatteryInfo {
  const info: BatteryInfo = {
    level: 0,
    scale: 100,
    status: 'unknown',
    health: 'unknown',
    plugged: 'none',
    temperature: 0,
    voltage: 0,
    technology: 'unknown'
  };

  // Parse battery level
  const levelMatch = output.match(/level:\s*(\d+)/);
  if (levelMatch) info.level = parseInt(levelMatch[1]);

  // Parse scale
  const scaleMatch = output.match(/scale:\s*(\d+)/);
  if (scaleMatch) info.scale = parseInt(scaleMatch[1]);

  // Parse status
  const statusMatch = output.match(/status:\s*(\d+)/);
  if (statusMatch) {
    info.status = getBatteryStatusText(parseInt(statusMatch[1]));
  }

  // Parse health
  const healthMatch = output.match(/health:\s*(\d+)/);
  if (healthMatch) {
    info.health = getBatteryHealthText(parseInt(healthMatch[1]));
  }

  // Parse plugged status
  const pluggedMatch = output.match(/plugged:\s*(\d+)/);
  if (pluggedMatch) {
    info.plugged = getPluggedText(parseInt(pluggedMatch[1]));
  }

  // Parse temperature (in tenths of a degree Celsius)
  const tempMatch = output.match(/temperature:\s*(\d+)/);
  if (tempMatch) {
    info.temperature = parseInt(tempMatch[1]) / 10;
  }

  // Parse voltage (in millivolts)
  const voltageMatch = output.match(/voltage:\s*(\d+)/);
  if (voltageMatch) {
    info.voltage = parseInt(voltageMatch[1]);
  }

  // Parse technology
  const techMatch = output.match(/technology:\s*(.+)/);
  if (techMatch) info.technology = techMatch[1].trim();

  return info;
}

function getBatteryStatusText(status: number): string {
  const statusMap: Record<number, string> = {
    1: 'unknown',
    2: 'charging',
    3: 'discharging',
    4: 'not_charging',
    5: 'full'
  };
  return statusMap[status] || 'unknown';
}

function getBatteryHealthText(health: number): string {
  const healthMap: Record<number, string> = {
    1: 'unknown',
    2: 'good',
    3: 'overheat',
    4: 'dead',
    5: 'over_voltage',
    6: 'unspecified_failure',
    7: 'cold'
  };
  return healthMap[health] || 'unknown';
}

function getPluggedText(plugged: number): string {
  const pluggedMap: Record<number, string> = {
    0: 'none',
    1: 'ac',
    2: 'usb',
    4: 'wireless'
  };
  return pluggedMap[plugged] || 'none';
}