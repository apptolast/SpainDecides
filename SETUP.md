# SpainDecides - Configuración de Autenticación con Supabase

Este documento explica cómo configurar las credenciales de Supabase y Google OAuth para que la
autenticación funcione correctamente.

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
# Supabase Configuration
SUPABASE_URL=https://tu-proyecto.supabase.co
SUPABASE_ANON_KEY=tu_clave_anon_aqui
# Google OAuth Configuration
GOOGLE_WEB_CLIENT_ID=tu_client_id.apps.googleusercontent.com
```

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
