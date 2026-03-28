// tests/commands/install_app.test.ts
import { definition } from '../../src/commands/install_app';

describe('install_app command', () => {
  it('should have correct definition', () => {
    expect(definition.name).toBe('install_app');
    expect(definition.description).toContain('Install an APK');
    expect(definition.parameters).toHaveLength(3);
    expect(definition.parameters[0].name).toBe('apk_path');
    expect(definition.parameters[0].type).toBe('string');
    expect(definition.parameters[0].required).toBe(true);
    expect(definition.parameters[1].name).toBe('reinstall');
    expect(definition.parameters[1].type).toBe('boolean');
    expect(definition.parameters[1].required).toBe(false);
    expect(definition.parameters[2].name).toBe('grant_permissions');
    expect(definition.parameters[2].type).toBe('boolean');
    expect(definition.parameters[2].required).toBe(false);
  });

  it('should have correct adb command', () => {
    expect(definition.adbCommand).toContain('install');
  });
});