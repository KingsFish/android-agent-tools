// tests/commands/is_app_running.test.ts
import { definition } from '../../src/commands/is_app_running';

describe('is_app_running command', () => {
  it('should have correct definition', () => {
    expect(definition.name).toBe('is_app_running');
    expect(definition.description).toContain('running');
    expect(definition.parameters).toHaveLength(1);
    expect(definition.parameters[0].name).toBe('package_name');
    expect(definition.parameters[0].type).toBe('string');
    expect(definition.parameters[0].required).toBe(true);
  });

  it('should have correct adb command', () => {
    expect(definition.adbCommand).toContain('pidof');
  });
});