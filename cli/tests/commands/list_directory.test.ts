// tests/commands/list_directory.test.ts
import { definition } from '../../src/commands/list_directory';

describe('list_directory command', () => {
  it('should have correct definition', () => {
    expect(definition.name).toBe('list_directory');
    expect(definition.description).toContain('directory');
    expect(definition.parameters).toHaveLength(1);
    expect(definition.parameters[0].name).toBe('path');
    expect(definition.parameters[0].type).toBe('string');
    expect(definition.parameters[0].required).toBe(true);
  });

  it('should have correct adb command template', () => {
    expect(definition.adbCommand).toBe('shell ls -la <path>');
  });
});