// tests/commands/get_current_app.test.ts
import { definition } from '../../src/commands/get_current_app';

describe('get_current_app command', () => {
  it('should have correct definition', () => {
    expect(definition.name).toBe('get_current_app');
    expect(definition.description).toContain('currently resumed');
    expect(definition.parameters).toHaveLength(0);
  });

  it('should have correct adb command', () => {
    expect(definition.adbCommand).toContain('dumpsys activity activities');
  });
});