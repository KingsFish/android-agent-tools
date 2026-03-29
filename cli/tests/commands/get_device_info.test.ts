// tests/commands/get_device_info.test.ts
import { definition } from '../../src/commands/get_device_info';

describe('get_device_info command', () => {
  it('should have correct definition', () => {
    expect(definition.name).toBe('get_device_info');
    expect(definition.description).toContain('device information');
    expect(definition.parameters).toHaveLength(0);
  });

  it('should have correct adb command', () => {
    expect(definition.adbCommand).toBe('shell getprop');
  });
});