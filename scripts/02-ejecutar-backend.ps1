$ErrorActionPreference = 'Stop'
if (-not (Get-Command java -ErrorAction SilentlyContinue)) { throw 'No hay Java en PATH. Se necesita JDK 25.' }
if (-not (Get-Command mvn -ErrorAction SilentlyContinue)) { throw 'No hay Maven en PATH. Se necesita Maven 3.9.x.' }
Write-Host 'Versiones detectadas:'
java -version
mvn -version
$env:SIGER_TENANT_ID = Read-Host 'Tenant ID institucional'
$env:SIGER_CLIENT_ID = Read-Host 'Client ID de SIGER-Notificaciones'
$env:SIGER_MAIL_FROM = 'siger@defensorianinez.cl'
$env:SIGER_TEST_TO = 'siger@defensorianinez.cl'
$secureSecret = Read-Host 'Valor del secreto de cliente (no el ID del secreto)' -AsSecureString
$securePassword = Read-Host 'Defina contrasena LOCAL para el endpoint de prueba' -AsSecureString
function Get-PlainValue([System.Security.SecureString] $secure) {
    $ptr = [Runtime.InteropServices.Marshal]::SecureStringToBSTR($secure)
    try { [Runtime.InteropServices.Marshal]::PtrToStringBSTR($ptr) }
    finally { [Runtime.InteropServices.Marshal]::ZeroFreeBSTR($ptr) }
}
$env:SIGER_CLIENT_SECRET = Get-PlainValue $secureSecret
$env:SIGER_TEST_PASSWORD = Get-PlainValue $securePassword
$secureSecret.Dispose()
$securePassword.Dispose()
Write-Host 'Levante la prueba en 127.0.0.1:8088; en OTRA consola ejecute scripts/03-probar-envio.ps1.'
try {
    Push-Location (Split-Path $PSScriptRoot -Parent)
    try { mvn spring-boot:run }
    finally { Pop-Location }
} finally {
    Remove-Item Env:\SIGER_CLIENT_SECRET -ErrorAction SilentlyContinue
    Remove-Item Env:\SIGER_TEST_PASSWORD -ErrorAction SilentlyContinue
    Remove-Item Env:\SIGER_TENANT_ID -ErrorAction SilentlyContinue
    Remove-Item Env:\SIGER_CLIENT_ID -ErrorAction SilentlyContinue
}
