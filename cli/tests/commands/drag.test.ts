// tests/commands/drag.test.ts
import { definition } from '../../src/commands/drag';

describe('drag command', () => {
  it('should have correct definition', () => {
    expect(definition.name).toBe('drag');
    expect(definition.description).toContain('drag');
    expect(definition.parameters).toHaveLength(5);
    expect(definition.parameters[0].name).toBe('start_x');
    expect(definition.parameters[0].type).toBe('number');
    expect(definition.parameters[0].required).toBe(true);
    expect(definition.parameters[1].name).toBe('start_y');
    expect(definition.parameters[1].type).toBe('number');
    expect(definition.parameters[1].required).toBe(true);
    expect(definition.parameters[2].name).toBe('end_x');
    expect(definition.parameters[2].type).toBe('number');
    expect(definition.parameters[2].required).toBe(true);
    expect(definition.parameters[3].name).toBe('end_y');
    expect(definition.parameters[3].type).toBe('number');
    expect(definition.parameters[3].required).toBe(true);
    expect(definition.parameters[4].name).toBe('duration');
    expect(definition.parameters[4].type).toBe('number');
    expect(definition.parameters[4].required).toBe(false);
    expect(definition.parameters[4].default).toBe(300);
  });

  it('should have adb command defined', () => {
    expect(definition.adbCommand).toContain('drag');
  });

  it('should have execute function', () => {
    expect(typeof definition.execute).toBe('function');
  });
});