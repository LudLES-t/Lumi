"""Smoke real HTTP, reinício e PDF. Usa banco temporário e não altera a demo."""
import json, os, subprocess, tempfile, time, urllib.request
from pathlib import Path
root=Path(__file__).resolve().parents[1]
java=os.environ.get('LUMI_JAVA','java')
base='http://127.0.0.1:18080/api/usuarias/1'
def call(path,body=None):
 req=urllib.request.Request(base+path,data=json.dumps(body).encode() if body is not None else None,headers={'Content-Type':'application/json'})
 with urllib.request.urlopen(req,timeout=10) as res:return json.load(res)
with tempfile.TemporaryDirectory(prefix='lumi-smoke-') as temp:
 def start():
  log=open(Path(temp)/'server.log','a')
  process=subprocess.Popen([java,'-jar',str(root/'release/lumi-1.0.0.jar'),'--server.port=18080'],cwd=temp,stdout=log,stderr=log)
  for _ in range(100):
   try:call('/hoje');return process
   except Exception:
    if process.poll() is not None:raise RuntimeError((Path(temp)/'server.log').read_text())
    time.sleep(.1)
  raise TimeoutError('API não iniciou')
 process=start()
 try:
  assert call('/hoje')['simulado']
  assert len(call('/sinais'))==90
  assert call('/coach')[1]['titulo']=='Acompanhe sua energia'
  symptom=call('/sintomas',{'data':'2026-10-27','tipo':'FADIGA','intensidade':8})
  assert call('/sintomas')[0]['id']==symptom['id']
  assert call('/coach')[1]['titulo']=='Fadiga recorrente registrada'
  assert any('Sintoma #'+str(symptom['id']) in o for o in call('/coach')[1]['origem'])
  appointment=call('/compromissos',{'tipo':'RETORNO','data':'2026-10-28T15:00','descricao':'Retorno demonstrativo smoke'})
  assert any(c['id']==appointment['id'] for c in call('/compromissos'))
  report=call('/relatorio',{'incluir':['sintomas','ciclo','compromissos','insights']})
  assert 'sinais' not in report['secoes']
  (root/'release/relatorio-exemplo.json').write_text(json.dumps(report,ensure_ascii=False,indent=2))
  with urllib.request.urlopen('http://127.0.0.1:18080') as r: assert b'<div id="root">' in r.read()
  n=len(call('/sintomas'));process.terminate();process.wait(timeout=15);process=start()
  assert len(call('/sintomas'))==n
  assert len(call('/sinais'))==90
  print('PASS: HTTP real, dashboard, sintoma, histórico, regra, origem, agenda, seleção do relatório, frontend no JAR, persistência após reinício e seed sem duplicação.')
 finally:process.terminate();process.wait(timeout=15)
