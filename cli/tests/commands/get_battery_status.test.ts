// tests/commands/get_battery_status.test.ts
import { definition } from '../../src/commands/get_battery_status';

describe('get_battery_status command', () => {
  it('should have correct definition', () => {
    expect(definition.name).toBe('get_battery_status');
    expect(definition.description).toContain('battery status');
    expect(definition.parameters).toHaveLength(0);
  });

  it('should have correct adb command', () => {
    expect(definition.adbCommand).toBe('shell dumpsys battery');
  });
});