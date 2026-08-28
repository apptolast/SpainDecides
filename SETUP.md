# SpainDecides - Configuración de Credenciales

Este documento explica cómo configurar las credenciales de Supabase, Google OAuth y el resto de
servicios para que la aplicación funcione correctamente.

> **Importante:** la app tiene dos flavors de Android — `dev` (entorno de desarrollo) y `prod`
> (producción). Las claves con sufijo `_DEBUG` las usa el flavor `dev` y las claves con sufijo
> `_RELEASE` las usa el flavor `prod`. Si falta la clave con sufijo, se usa la clave sin sufijo
> como fallback. Ver la configuración de BuildKonfig en `composeApp/build.gradle.kts`.

## Paso 1: Crear archivo local.properties

1. Copia el archivo `local.properties.template` a `local.properties`:
   ```bash
   cp local.properties.template local.properties
   ```

2. El archivo `local.properties` está en el `.gitignore`, por lo que **NUNCA** se subirá al
   repositorio.

## Paso 2: Obtener credenciales de Supabase

1. Ve a tu proyecto en [Supabase Dashboard](https://app.supabase.com)
2. Ve a **Settings** → **API**
3. Copia los siguientes valores:
    - **Project URL** (algo como `https://xxxxx.supabase.co`)
    - **Anon/Public Key** (una clave larga que empieza con `eyJ...`)

## Paso 3: Obtener credencial de Google OAuth

1. Ve a [Google Cloud Console](https://console.cloud.google.com)
2. Selecciona tu proyecto
3. Ve a **APIs & Services** → **Credentials**
4. Busca tu **Web client** OAuth credential
5. Copia el **Client ID** (termina en `.apps.googleusercontent.com`)

## Paso 4: Rellenar local.properties

Abre el archivo `local.properties` y reemplaza los valores:

```properties
# Supabase - entorno de desarrollo (flavor dev)
SUPABASE_URL_DEBUG=https://tu-proyecto-dev.supabase.co
SUPABASE_ANON_KEY_DEBUG=tu_clave_anon_dev

# Supabase - entorno de producción (flavor prod)
SUPABASE_URL_RELEASE=https://tu-proyecto-prod.supabase.co
SUPABASE_ANON_KEY_RELEASE=tu_clave_anon_prod

# Google OAuth
GOOGLE_WEB_CLIENT_ID=tu_client_id.apps.googleusercontent.com
```

El resto de claves (n8n, Firebase Cloud Function, EmailJS, firma de release) están documentadas
en `local.properties.template`. Para las notificaciones push consulta
`docs/PUSH_NOTIFICATIONS_SETUP.md`.

## Paso 5: Compilar el proyecto

Una vez configurado el `local.properties`, compila el proyecto:

```bash
./gradlew :composeApp:assembleDevDebug
```

El plugin BuildKonfig leerá automáticamente estos valores y los inyectará de forma segura en la
aplicación.

## Verificar configuración

Si las credenciales están correctamente configuradas, la aplicación podrá:

- ✅ Conectarse a Supabase
- ✅ Registrar nuevos usuarios con email/contraseña
- ✅ Iniciar sesión con email/contraseña
- ✅ Iniciar sesión con Google OAuth

## Solución de problemas

### Error: "SUPABASE_URL is empty"

- Verifica que el archivo `local.properties` existe en la raíz del proyecto
- Verifica que los valores están correctamente escritos sin espacios extra

### Error al compilar

- Asegúrate de que el formato del `local.properties` es correcto (sin comillas en los valores)
- Ejecuta `./gradlew clean` y luego `./gradlew build`

### OAuth no funciona

- Verifica que las URLs de redirección están configuradas en Supabase
- Verifica que el Client ID de Google es el **Web client**, no el Android o iOS client
