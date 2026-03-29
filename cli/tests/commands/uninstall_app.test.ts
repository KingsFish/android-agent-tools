// tests/commands/uninstall_app.test.ts
import { definition } from '../../src/commands/uninstall_app';

describe('uninstall_app command', () => {
  it('should have correct definition', () => {
    expect(definition.name).toBe('uninstall_app');
    expect(definition.description).toContain('Uninstall an application');
    expect(definition.parameters).toHaveLength(2);
    expect(definition.parameters[0].name).toBe('package_name');
    expect(definition.parameters[0].type).toBe('string');
    expect(definition.parameters[0].required).toBe(true);
    expect(definition.parameters[1].name).toBe('keep_data');
    expect(definition.parameters[1].type).toBe('boolean');
    expect(definition.parameters[1].required).toBe(false);
  });

  it('should have correct adb command', () => {
    expect(definition.adbCommand).toContain('uninstall');
  });
});