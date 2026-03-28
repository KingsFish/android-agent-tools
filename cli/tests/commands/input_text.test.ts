// tests/commands/input_text.test.ts
import { definition } from '../../src/commands/input_text';

describe('input_text command', () => {
  it('should have correct definition', () => {
    expect(definition.name).toBe('input_text');
    expect(definition.description).toContain('text');
    expect(definition.parameters).toHaveLength(1);
    expect(definition.parameters[0].name).toBe('text');
    expect(definition.parameters[0].type).toBe('string');
    expect(definition.parameters[0].required).toBe(true);
  });

  it('should have adb command defined', () => {
    expect(definition.adbCommand).toContain('input text');
  });

  it('should have execute function', () => {
    expect(typeof definition.execute).toBe('function');
  });
});