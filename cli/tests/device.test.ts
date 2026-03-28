// tests/device.test.ts
import { parseDeviceList } from '../src/device';
import { Device } from '../src/types';

describe('device', () => {
  describe('parseDeviceList', () => {
    it('should parse adb devices output', () => {
      const output = 'emulator-5554\tdevice\nABC123\tdevice\nXYZ789\toffline\n';
      const devices = parseDeviceList(output);
      expect(devices).toHaveLength(3);
      expect(devices[0].id).toBe('emulator-5554');
      expect(devices[0].status).toBe('device');
      expect(devices[2].status).toBe('offline');
    });

    it('should handle empty output', () => {
      const output = 'List of devices attached\n';
      const devices = parseDeviceList(output);
      expect(devices).toHaveLength(0);
    });

    it('should parse unauthorized devices', () => {
      const output = 'ABC123\tunauthorized\nDEF456\tdevice\n';
      const devices = parseDeviceList(output);
      expect(devices).toHaveLength(2);
      expect(devices[0].status).toBe('unauthorized');
    });
  });
});