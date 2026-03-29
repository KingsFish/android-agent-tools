// tests/commands/click_node_by_id.test.ts
import { definition } from '../../src/commands/click_node_by_id';

describe('click_node_by_id command', () => {
  it('should have correct definition', () => {
    expect(definition.name).toBe('click_node_by_id');
    expect(definition.description).toContain('UI node by resource-id');
    expect(definition.parameters).toHaveLength(1);
  });

  it('should have resource_id parameter as required string', () => {
    const resourceIdParam = definition.parameters.find(p => p.name === 'resource_id');
    expect(resourceIdParam).toBeDefined();
    expect(resourceIdParam?.type).toBe('string');
    expect(resourceIdParam?.required).toBe(true);
  });
});