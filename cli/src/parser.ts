// src/parser.ts

export interface UiNode {
  index: number;
  text: string;
  resourceId: string;
  className: string;
  packageName: string;
  clickable: boolean;
  enabled: boolean;
  bounds: { left: number; top: number; right: number; bottom: number };
  children: UiNode[];
  centerX: number;
  centerY: number;
}

function parseAttributes(attrStr: string): Record<string, string> {
  const attrs: Record<string, string> = {};
  const regex = /([\w-]+)="([^"]*)"/g;
  let match;
  while ((match = regex.exec(attrStr)) !== null) {
    attrs[match[1]] = match[2];
  }
  return attrs;
}

function parseBounds(boundsStr: string): { left: number; top: number; right: number; bottom: number } {
  const match = boundsStr.match(/\[(\d+),(\d+)\]\[(\d+),(\d+)\]/);
  if (!match) return { left: 0, top: 0, right: 0, bottom: 0 };
  return {
    left: parseInt(match[1]),
    top: parseInt(match[2]),
    right: parseInt(match[3]),
    bottom: parseInt(match[4])
  };
}

export function parseUiTree(xml: string): UiNode {
  return parseNode(xml, 0);
}

function parseNode(xml: string, startPos: number): UiNode {
  const nodeStart = xml.indexOf('<node', startPos);
  if (nodeStart === -1) throw new Error('Node not found');

  const tagEnd = xml.indexOf('>', nodeStart);
  const attrStr = xml.slice(nodeStart + 5, tagEnd);
  const attrs = parseAttributes(attrStr);

  const bounds = parseBounds(attrs['bounds'] || '[0,0][0,0]');
  const centerX = (bounds.left + bounds.right) / 2;
  const centerY = (bounds.top + bounds.bottom) / 2;

  const node: UiNode = {
    index: parseInt(attrs['index'] || '0'),
    text: attrs['text'] || '',
    resourceId: attrs['resource-id'] || '',
    className: attrs['class'] || '',
    packageName: attrs['package'] || '',
    clickable: attrs['clickable'] === 'true',
    enabled: attrs['enabled'] === 'true',
    bounds,
    children: [],
    centerX,
    centerY
  };

  if (xml[tagEnd - 1] === '/') return node;

  let pos = tagEnd + 1;
  while (pos < xml.length) {
    const childStart = xml.indexOf('<node', pos);
    if (childStart === -1) break;

    const childEnd = xml.indexOf('</node>', childStart);
    const selfCloseEnd = xml.indexOf('/>', childStart);

    if (selfCloseEnd !== -1 && (childEnd === -1 || selfCloseEnd < childEnd)) {
      const childXml = xml.slice(childStart, selfCloseEnd + 2);
      node.children.push(parseNode(childXml, 0));
      pos = selfCloseEnd + 2;
    } else if (childEnd !== -1) {
      const childXml = xml.slice(childStart, childEnd + 8);
      node.children.push(parseNode(childXml, 0));
      pos = childEnd + 8;
    } else {
      break;
    }
  }

  return node;
}

export function findNodeByText(tree: UiNode, text: string, exact: boolean = true): UiNode | null {
  if (exact && tree.text === text) return tree;
  if (!exact && tree.text.includes(text)) return tree;

  for (const child of tree.children) {
    const found = findNodeByText(child, text, exact);
    if (found) return found;
  }
  return null;
}

export function findNodeById(tree: UiNode, resourceId: string): UiNode | null {
  if (tree.resourceId === resourceId) return tree;

  for (const child of tree.children) {
    const found = findNodeById(child, resourceId);
    if (found) return found;
  }
  return null;
}