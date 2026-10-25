$ErrorActionPreference = 'Stop'
Write-Host 'Verificando TCP 443 desde ESTA maquina (no garantiza TLS completo)...'
Test-NetConnection login.microsoftonline.com -Port 443
Test-NetConnection graph.microsoft.com -Port 443
Write-Host 'En ambos, se espera TcpTestSucceeded : True.'
Write-Host 'Si TCP funciona pero el token no, revisar proxy, inspeccion TLS, DNS y permisos de red.'
