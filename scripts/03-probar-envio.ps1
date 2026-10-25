$ErrorActionPreference = 'Stop'
$secure = Read-Host 'Contrasena LOCAL elegida al arrancar el backend' -AsSecureString
$ptr = [Runtime.InteropServices.Marshal]::SecureStringToBSTR($secure)
try { $plain = [Runtime.InteropServices.Marshal]::PtrToStringBSTR($ptr) }
finally { [Runtime.InteropServices.Marshal]::ZeroFreeBSTR($ptr); $secure.Dispose() }
try {
    $pair = 'tester:' + $plain
    $basic = [Convert]::ToBase64String([Text.Encoding]::UTF8.GetBytes($pair))
    $response = Invoke-WebRequest -Uri 'http://127.0.0.1:8088/_test/mail' -Method Post -Headers @{ Authorization = 'Basic ' + $basic } -UseBasicParsing
    Write-Host ('HTTP local: ' + [int]$response.StatusCode)
    Write-Host $response.Content
} finally {
    $plain = $null
    $pair = $null
    $basic = $null
}
