// tests/commands/force_stop_app.test.ts
import { definition } from '../../src/commands/force_stop_app';

describe('force_stop_app command', () => {
  it('should have correct definition', () => {
    expect(definition.name).toBe('force_stop_app');
    expect(definition.description).toContain('Force stop');
    expect(definition.parameters).toHaveLength(1);
    expect(definition.parameters[0].name).toBe('package_name');
    expect(definition.parameters[0].type).toBe('string');
    expect(definition.parameters[0].required).toBe(true);
  });

  it('should have correct adb command', () => {
    expect(definition.adbCommand).toContain('am force-stop');
  });
});