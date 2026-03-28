// tests/commands/tap.test.ts
import { definition } from '../../src/commands/tap';

describe('tap command', () => {
  it('should have correct definition', () => {
    expect(definition.name).toBe('tap');
    expect(definition.description).toContain('tap');
    expect(definition.parameters).toHaveLength(2);
    expect(definition.parameters[0].name).toBe('x');
    expect(definition.parameters[0].type).toBe('number');
    expect(definition.parameters[0].required).toBe(true);
    expect(definition.parameters[1].name).toBe('y');
    expect(definition.parameters[1].type).toBe('number');
    expect(definition.parameters[1].required).toBe(true);
  });
});