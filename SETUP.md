# SpainDecides - Configuración de credenciales

Este documento explica cómo configurar las credenciales de Supabase y Google OAuth para que la
aplicación funcione correctamente.

> **La autenticación ya no la hace Supabase, sino Firebase Auth** a través de la librería
> [BaseLogin](https://github.com/apptolast/BaseLogin). Supabase se queda como base de datos y
> acepta el token de Firebase mediante Third-Party Auth. La configuración de la consola de Firebase,
> las políticas RLS y la migración de datos están en
> [docs/FIREBASE_SUPABASE_AUTH.md](docs/FIREBASE_SUPABASE_AUTH.md) — sin esos pasos la app compila
> pero no lee ni escribe nada.

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

1. Ve a la consola de [Firebase](https://console.firebase.google.com) → tu proyecto
2. **Authentication** → **Sign-in method** → **Google** → **Web SDK configuration**
3. Copia el **Web client ID** (termina en `.apps.googleusercontent.com`)
4. Solo para iOS: copia también el **iOS client ID** desde
   **Project settings** → **Your apps** → app de iOS

Es el cliente OAuth del proyecto de Firebase, que no tiene por qué ser el mismo que se configuró en
su día en Supabase.

## Paso 4: Rellenar local.properties

Abre el archivo `local.properties` y reemplaza los valores:

```properties
# Supabase Configuration
SUPABASE_URL=https://tu-proyecto.supabase.co
SUPABASE_ANON_KEY=tu_clave_anon_aqui
# Google OAuth Configuration (cliente del proyecto de Firebase)
GOOGLE_WEB_CLIENT_ID=tu_client_id.apps.googleusercontent.com
# Solo necesario para compilar iOS con login de Google
GOOGLE_IOS_CLIENT_ID=tu_ios_client_id.apps.googleusercontent.com
```

Para iOS, configura también el reversed client ID en `iosApp/Configuration/Config.xcconfig`:

```xcconfig
GOOGLE_IOS_REVERSED_CLIENT_ID=com.googleusercontent.apps.tu_ios_client_id
```

También necesitas los ficheros de configuración de Firebase, que están en el `.gitignore`:
`composeApp/google-services.json` (Android) y `iosApp/iosApp/GoogleService-Info.plist` (iOS). Sin el
primero la compilación de Android falla en `processDevDebugGoogleServices`.

## Paso 5: Compilar el proyecto

Una vez configurado el `local.properties`, compila el proyecto:

```bash
./gradlew build
```

El plugin BuildKonfig leerá automáticamente estos valores y los inyectará de forma segura en la
aplicación.

## Verificar configuración

Si las credenciales están correctamente configuradas, la aplicación podrá:

- ✅ Conectarse a Supabase
- ✅ Registrar nuevos usuarios con email/contraseña (en Firebase Auth)
- ✅ Iniciar sesión con email/contraseña
- ✅ Iniciar sesión con Google
- ✅ Recuperar contraseña (pantallas de Forgot / Reset password de BaseLogin)

## Solución de problemas

### Error: "SUPABASE_URL is empty"

- Verifica que el archivo `local.properties` existe en la raíz del proyecto
- Verifica que los valores están correctamente escritos sin espacios extra

### Error al compilar

- Asegúrate de que el formato del `local.properties` es correcto (sin comillas en los valores)
- Ejecuta `./gradlew clean` y luego `./gradlew build`

### El login de Google no funciona

- Verifica que el Client ID de Google es el **Web client** del proyecto de Firebase, no el de
  Android ni el de iOS
- Comprueba que las huellas **SHA-1 y SHA-256** del keystore están dadas de alta en la consola de
  Firebase: sin ellas Credential Manager falla en Android
- Comprueba que **Google** está habilitado en Firebase → Authentication → Sign-in method

### Las consultas a la base de datos devuelven vacío o dan permiso denegado

Falta la configuración de Third-Party Auth en Supabase o las políticas RLS siguen filtrando por
`auth.uid()`. Ver [docs/FIREBASE_SUPABASE_AUTH.md](docs/FIREBASE_SUPABASE_AUTH.md).
