// tests/commands/get_ui_tree.test.ts
import { definition } from '../../src/commands/get_ui_tree';

describe('get_ui_tree command', () => {
  it('should have correct definition', () => {
    expect(definition.name).toBe('get_ui_tree');
    expect(definition.description).toContain('UI hierarchy');
    expect(definition.parameters).toHaveLength(0);
  });

  it('should have correct adbCommand', () => {
    expect(definition.adbCommand).toContain('uiautomator dump');
  });
});