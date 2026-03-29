// tests/commands/write_file.test.ts
import { definition } from '../../src/commands/write_file';

describe('write_file command', () => {
  it('should have correct definition', () => {
    expect(definition.name).toBe('write_file');
    expect(definition.description).toContain('Write');
    expect(definition.parameters).toHaveLength(2);
    expect(definition.parameters[0].name).toBe('path');
    expect(definition.parameters[0].type).toBe('string');
    expect(definition.parameters[0].required).toBe(true);
    expect(definition.parameters[1].name).toBe('content');
    expect(definition.parameters[1].type).toBe('string');
    expect(definition.parameters[1].required).toBe(true);
  });

  it('should have correct adb command template', () => {
    expect(definition.adbCommand).toContain('echo');
    expect(definition.adbCommand).toContain('<path>');
  });
});