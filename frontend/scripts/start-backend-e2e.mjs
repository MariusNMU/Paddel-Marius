import { spawn } from 'node:child_process';
import { dirname, resolve } from 'node:path';
import { fileURLToPath } from 'node:url';

const dossierScripts = dirname(fileURLToPath(import.meta.url));
const dossierBackend = resolve(dossierScripts, '../../backend');

const commandeMaven = process.platform === 'win32'
  ? 'mvnw.cmd'
  : './mvnw';

const backend = spawn(
  commandeMaven,
  ['spring-boot:run'],
  {
    cwd: dossierBackend,
    stdio: 'inherit',
    shell: process.platform === 'win32'
  }
);

backend.on('error', (error) => {
  console.error('Impossible de démarrer le backend Spring Boot.', error);
  process.exit(1);
});

backend.on('exit', (code) => {
  process.exit(code ?? 1);
});
