// tests/parser.test.ts
import { parseUiTree, findNodeByText, findNodeById } from '../src/parser';

describe('parser', () => {
  const sampleXml = `<node index="0" text="Hello" resource-id="com.app:id/title" bounds="[0,0][100,50]">
  <node index="1" text="Click Me" resource-id="com.app:id/button" bounds="[10,60][90,100]" clickable="true"/>
</node>`;

  it('parseUiTree should parse XML to JSON', () => {
    const tree = parseUiTree(sampleXml);
    expect(tree.text).toBe('Hello');
    expect(tree.resourceId).toBe('com.app:id/title');
    expect(tree.children).toHaveLength(1);
  });

  it('parseUiTree should extract bounds', () => {
    const tree = parseUiTree(sampleXml);
    expect(tree.bounds).toEqual({ left: 0, top: 0, right: 100, bottom: 50 });
  });

  it('findNodeByText should find matching node', () => {
    const tree = parseUiTree(sampleXml);
    const node = findNodeByText(tree, 'Click Me');
    expect(node?.text).toBe('Click Me');
    expect(node?.resourceId).toBe('com.app:id/button');
  });

  it('findNodeById should find matching node', () => {
    const tree = parseUiTree(sampleXml);
    const node = findNodeById(tree, 'com.app:id/button');
    expect(node?.resourceId).toBe('com.app:id/button');
  });
});