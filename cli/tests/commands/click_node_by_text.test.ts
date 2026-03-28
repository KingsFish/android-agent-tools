// tests/commands/click_node_by_text.test.ts
import { definition } from '../../src/commands/click_node_by_text';

describe('click_node_by_text command', () => {
  it('should have correct definition', () => {
    expect(definition.name).toBe('click_node_by_text');
    expect(definition.description).toContain('UI node by text');
    expect(definition.parameters).toHaveLength(2);
  });

  it('should have text parameter as required string', () => {
    const textParam = definition.parameters.find(p => p.name === 'text');
    expect(textParam).toBeDefined();
    expect(textParam?.type).toBe('string');
    expect(textParam?.required).toBe(true);
  });

  it('should have exact parameter as optional boolean with default true', () => {
    const exactParam = definition.parameters.find(p => p.name === 'exact');
    expect(exactParam).toBeDefined();
    expect(exactParam?.type).toBe('boolean');
    expect(exactParam?.required).toBe(false);
    expect(exactParam?.default).toBe(true);
  });
});