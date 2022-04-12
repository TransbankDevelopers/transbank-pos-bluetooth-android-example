# Proyecto de ejemplo Android

Este proyecto de ejemplo permite la comunicación con el POS Bluetooth. El ejemplo contiene una función para calcular el LRC de los comandos y permite probar todas las operaciones del POS.

## Requisitos

- El proyecto fue creado utilizando Android Studio Artic Fox 2020.3.1 Patch 4
- Android 4.x (API 14)

## Dependencias

Puedes descargar la librería desde la web de [Transbank developers](https://www.transbankdevelopers.cl/documentacion/pos-bluetooth#descarga-de-librerias).

### Framework iSMP(PCL)

Este framework es necesario para establecer la comunicación Bluetooth entre el terminal de pago (Link2500) y el smartphone.

### Framework Mpos Integrado

Este framework es necesario para iniciar la transacción y capturar la respuesta.

### Instalación

Para poder utilizar la librería mposintegrado.aar es necesario agregarla al proyecto, para ello se deben seguir los siguientes pasos:

1. Copiar el archivo mposintegrado.aar a la carpeta `libs` del proyecto.
2. Añadir como *repositorio* a la carpeta `libs` añadiendo lo siguiente al archivo `build.gradle de proyecto`:

```java
repositories {
    // Otros repositorios...
    
    flatDir {
        dirs 'libs'
    }
}
```
3. Añadir en el archivo `build.gradle de aplicación` lo siguiente:

```java
dependencies {
    // Otras dependencias...
    implementation files('libs/mposintegrado.aar')
}
```

## Ejecutar ejemplo

Es importante mencionar que este proyecto solo puede ser probado utilizando un dispositivo real.

Para ejecutar el proyecto se debe seleccionar el dispositivo donde se va a probar el proyecto de ejemplo.

Posteriormente se debe debe dar click en el ícono de run. Esto comenzará a compilar el proyecto y a instalarlo en el dispositivo.
