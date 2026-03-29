// tests/commands/wait_for_element.test.ts
import { definition } from '../../src/commands/wait_for_element';

describe('wait_for_element command', () => {
  it('should have correct definition', () => {
    expect(definition.name).toBe('wait_for_element');
    expect(definition.description).toContain('Wait for a UI element');
    expect(definition.parameters).toHaveLength(3);
  });

  it('should have selector parameter as required string', () => {
    const selectorParam = definition.parameters.find(p => p.name === 'selector');
    expect(selectorParam).toBeDefined();
    expect(selectorParam?.type).toBe('string');
    expect(selectorParam?.required).toBe(true);
  });

  it('should have timeout parameter as optional number with default 30', () => {
    const timeoutParam = definition.parameters.find(p => p.name === 'timeout');
    expect(timeoutParam).toBeDefined();
    expect(timeoutParam?.type).toBe('number');
    expect(timeoutParam?.required).toBe(false);
    expect(timeoutParam?.default).toBe(30);
  });

  it('should have by parameter as optional string with default "text"', () => {
    const byParam = definition.parameters.find(p => p.name === 'by');
    expect(byParam).toBeDefined();
    expect(byParam?.type).toBe('string');
    expect(byParam?.required).toBe(false);
    expect(byParam?.default).toBe('text');
  });
});