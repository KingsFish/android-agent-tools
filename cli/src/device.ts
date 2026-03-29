// src/device.ts
import { execAdb } from './adb';
import { Device } from './types';

export function parseDeviceList(output: string): Device[] {
  const lines = output.split('\n').filter(line => line.trim());
  const devices: Device[] = [];

  for (const line of lines) {
    if (line.startsWith('List of devices attached')) continue;

    const parts = line.split('\t');
    if (parts.length >= 2) {
      const id = parts[0].trim();
      const status = parts[1].trim() as Device['status'];
      devices.push({ id, status });
    }
  }

  return devices;
}

export async function listDevices(): Promise<Device[]> {
  const result = await execAdb(['devices']);
  return parseDeviceList(result.stdout);
}

export async function selectDevice(deviceId?: string): Promise<string> {
  if (deviceId) return deviceId;

  const envDevice = process.env.ADB_DEVICE;
  if (envDevice) return envDevice;

  const devices = await listDevices();

  if (devices.length === 0) {
    throw new Error('No devices connected. Connect a device via USB or ADB network.');
  }

  const availableDevices = devices.filter(d => d.status === 'device');

  if (availableDevices.length === 0) {
    throw new Error('No authorized devices. Check device authorization.');
  }

  if (availableDevices.length === 1) {
    return availableDevices[0].id;
  }

  const deviceIds = availableDevices.map(d => d.id).join(', ');
  throw new Error(`Multiple devices connected (${deviceIds}). Specify device with -d option or ADB_DEVICE env.`);
}