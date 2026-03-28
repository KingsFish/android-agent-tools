// src/index.ts
import { Command } from 'commander';
import * as fs from 'fs';
import * as path from 'path';
import { ToolDefinition, CliOptions, ErrorCodes } from './types';
import { success, failure, prettyPrint } from './output';

const program = new Command();

program
  .name('aat')
  .description('Android Agent Tools - ADB-based tool commands for LLM agents')
  .version('1.0.0')
  .option('-d, --device <id>', 'Specify device ID')
  .option('--pretty', 'Pretty print JSON output')
  .option('-t, --timeout <ms>', 'Timeout in milliseconds', '30000');

function getCliOptions(): CliOptions {
  const opts = program.opts();
  return {
    device: opts.device,
    timeout: parseInt(opts.timeout),
    pretty: opts.pretty
  };
}

function registerCommand(def: ToolDefinition): void {
  const positionalArgs = def.parameters
    .filter(p => p.required)
    .map(p => `<${p.name}>`)
    .join(' ');

  const cmd = program.command(`${def.name}${positionalArgs ? ' ' + positionalArgs : ''}`)
    .description(def.description);

  def.parameters.filter(p => !p.required).forEach(p => {
    const flag = p.type === 'boolean' ? `--${p.name}` : `--${p.name} <value>`;
    if (p.type === 'boolean') {
      cmd.option(flag, p.description ?? '', p.default as boolean | undefined);
    } else if (p.type === 'number') {
      cmd.option(flag, p.description ?? '', parseFloat, p.default as number | undefined);
    } else {
      cmd.option(flag, p.description ?? '', p.default as string | undefined);
    }
  });

  cmd.action(async (...args) => {
    const cmdOpts = cmd.opts();
    const combinedOpts: CliOptions = { ...getCliOptions(), ...cmdOpts };
    try {
      const result = await def.execute(...args, combinedOpts);
      const output = combinedOpts.pretty ? prettyPrint(result) : result;
      console.log(output);
    } catch (err: any) {
      const output = combinedOpts.pretty
        ? prettyPrint(failure(ErrorCodes.EXEC_ERROR, err.message))
        : failure(ErrorCodes.EXEC_ERROR, err.message);
      console.error(output);
      process.exit(1);
    }
  });
}

function loadCommands(): void {
  const commandsDir = path.join(__dirname, 'commands');
  if (!fs.existsSync(commandsDir)) return;

  const files = fs.readdirSync(commandsDir).filter(f => f.endsWith('.js'));
  for (const file of files) {
    try {
      const { definition } = require(path.join(commandsDir, file));
      if (definition?.name) registerCommand(definition);
    } catch {
      // Ignore files that don't export valid definitions
    }
  }
}

loadCommands();
program.parse();