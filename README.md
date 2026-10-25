# SIGER - backend minimo para probar Microsoft Graph

Prueba aislada para **Nexbyte / SIGER institucional**, basada en `nexbytedevstack` (2026-10-09).
**No es el modulo final de notificaciones, ni corresponde al Formulario Publico.**

## Objetivo

Realizar un envio de correo desde `siger@defensorianinez.cl` hacia
`siger@defensorianinez.cl` con Microsoft Graph `Mail.Send` **application-only**,
mediante `client_credentials` y la aplicacion `SIGER-Notificaciones` creada por TI.

El endpoint es local, autenticado y de un unico disparo por arranque; no recibe
remitentes, destinatarios ni contenido desde la peticion. Se guarda el correo
entre los enviados de Exchange. No se imprime el token ni el secreto.

## Requisitos

- Windows institucional autorizado, JDK 25, Maven 3.9.x.
- Salida HTTPS a `login.microsoftonline.com` y `graph.microsoft.com` por 443.
- Tenant ID y Client ID (son identificadores, no secretos).
- **Valor actual** del secreto de cliente entregado por TI por canal seguro;
  el ID del secreto o la captura de la pantalla NO sirven. Alternativa de
  produccion: certificado y clave privada custodiada apropiadamente.
- Permiso `Application Mail.Send` restringido en Exchange RBAC a la casilla SIGER,
  y ausencia de permisos adicionales amplios en Entra ID que extiendan el alcance.
- Autorizacion institucional para ejecutar la prueba en el ambiente.

**No enviar contrasenas, certificados privados ni tokens a IA, chats, correos,
GitHub, capturas o archivos dentro de este proyecto.**

## Paso a paso (PowerShell)

Desde la carpeta que contiene `pom.xml`:

```powershell
java -version
mvn -version
.\scripts\01-conectividad.ps1
```

Ambos resultados de `Test-NetConnection` deben informar `TcpTestSucceeded: True`.
Ojo: la prueba TCP no valida que todas las peticiones TLS/HTTP funcionen a traves
de un proxy o de inspeccion HTTPS.

Con la **credencial de prueba autorizada** por TI:

```powershell
.\scripts\02-ejecutar-backend.ps1
```

La consola solicita Tenant ID, Client ID, valor del secreto y una contrasena
LOCAL para autenticacion HTTP Basic del endpoint. Mantenga esa consola abierta.
El servidor escucha solo en `127.0.0.1:8088`.

En otra ventana de PowerShell, en la misma carpeta:

```powershell
.\scripts\03-probar-envio.ps1
```

La segunda consola solicita la contrasena LOCAL de la primera consola.
Respuestas tipicas:

| Resultado | Significado |
| --- | --- |
| `202` + `ACCEPTED_BY_GRAPH` | Graph acepto el envio; verificar recepcion manualmente en Exchange. |
| `502` + `stage=TOKEN` | Credencial incorrecta/expirada, problema con el tenant o autenticacion / red. |
| `502` + `stage=GRAPH` y `upstreamHttpStatus=403` | App sin autorizacion efectiva para usar ese buzon; verificar RBAC / permisos. |
| `401` del backend local | Contrasena de prueba incorrecta. |
| `409` | Se intento enviar mas de una vez en el mismo arranque. |
| `404` / sin respuesta | URL equivocada, programa sin arrancar, firewall/proxy local. |

Importante: si `Invoke-WebRequest` recibe un HTTP no 2xx, PowerShell 5.1 puede
lanzar excepcion en vez de mostrar la respuesta; mire tambien el cuerpo/estado
de la excepcion. NO copie el secreto ni los headers de autorizacion al compartir
resultados.

### Seguridad y alcance de esta prueba

- Solo loopback (`server.address=127.0.0.1`).
- HTTP Basic protegido con contrasena solo para prueba local; nunca exponer
  por reverse proxy ni cambiar `server.address` a `0.0.0.0`.
- Spring Security deniega cualquier ruta salvo el POST autenticado.
- CSRF deshabilitado **unicamente aqui**, ya que es una API de prueba local,
  sin sesion web y sin integracion con un navegador. Revisar CSRF segun el
  sistema real.
- Una operacion de envio por arranque, incluso ante falla: reiniciar solo si
  se aprueba otro intento. Evita dobles envios por reintentos accidentales.
- No hay base de datos, frontend, correo SMTP, Graph SDK ni acceso externo
  entrante: solo Spring Boot 4.1.1, Spring MVC, Spring Security, Jackson 3 y RestClient.
- El secreto esta en memoria/variables de entorno solo durante la ejecucion
  de la consola de lanzamiento. Es un mecanismo transitorio de prueba; para
  SIGER final usar custodia institucional de secretos/certificados.
- El codigo de status 202 NO garantiza entrega; verificar correo recibido,
  elementos enviados y trazas administrativas si estan disponibles.
- **No usar datos personales ni correos reales de casos o NNA.**

### Probar alcance RBAC (TI)

El envio desde otra casilla no se prueba cambiando la configuracion del
servicio ni abriendo una API publica. Solicitar a TI que valide con su
`Test-ServicePrincipalAuthorization -Identity <id> -Resource <otra-casilla>`
y, si corresponde, un intento real autorizado a un buzon fuera de alcance
que debe devolver 403. Verificar tambien consentimientos vigentes en Entra ID
porque pueden ser aditivos a Exchange RBAC. El cmdlet de Exchange por si solo
no examina los permisos consentidos directamente en Entra ID.

### Estado de verificacion

- Incluye test unitario del contenido fijo del correo.
- No se ha ejecutado Java 25/Maven ni una prueba real contra el tenant de la DDN
  desde el entorno de generacion. El artefacto requiere JDK 25, Maven y
  credenciales institucionales para probarlo en la maquina autorizada.

## Hacia SIGER institucional

Al integrar la funcionalidad final: reemplazar el endpoint manual por un
servicio interno de notificaciones; definir destinatarios autorizados por
proceso; plantilla, bitacora, id de correlacion, persistencia de intentos y
estado, retencion, idempotencia y tratamiento de errores/transitoriedad.
No reintentar automaticamente POST `/sendMail` tras un timeout incierto.

Documentacion oficial:

- https://learn.microsoft.com/en-us/graph/api/user-sendmail?view=graph-rest-1.0
- https://learn.microsoft.com/en-us/entra/identity-platform/v2-oauth2-client-creds-grant-flow
- https://learn.microsoft.com/en-us/exchange/permissions-exo/application-rbac
