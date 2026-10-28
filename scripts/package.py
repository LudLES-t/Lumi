"""Build reproduzível. Executar na raiz: python scripts/package.py."""
from pathlib import Path
import subprocess, shutil
root=Path(__file__).resolve().parents[1]
def run(args,cwd): subprocess.run(args,cwd=cwd,check=True,shell=False)
npm=shutil.which('npm.cmd') or shutil.which('npm')
mvn=shutil.which('mvn.cmd') or shutil.which('mvn')
if not npm or not mvn: raise SystemExit('Instale Node/npm e Maven; configure JDK 21 no JAVA_HOME.')
run([npm,'ci'],root/'frontend');run([npm,'run','build'],root/'frontend')
static=root/'backend/src/main/resources/static'
if static.exists(): shutil.rmtree(static)
shutil.copytree(root/'frontend/dist',static)
run([mvn,'clean','package'],root/'backend')
(root/'release').mkdir(exist_ok=True)
shutil.copy2(root/'backend/target/lumi-1.0.0.jar',root/'release/lumi-1.0.0.jar')
print('Pronto: java -jar release/lumi-1.0.0.jar — http://localhost:8080')
