# LAB3_20190057 - **RICARDO ALVARADO RUIZ**
Este es mi laboratorio 3 :)

## **Importante:**
La API tiene un número limitado de preguntas tipo Boolean para ciertos rubros y ciertas dificultades.
En caso no se encuentre la suficiente cantidad de preguntas, el código regresará automáticamente
a la página principal, por favor, evitar manipular la app mientras salga ese toast

## **Implementación de Hilos:**
En este proyecto se han implementado hilos de las siguientes maneras:
1. CountDownTimer para el temporizador, el cual see utiliza la clase CountDownTimer que implementa un hilo separado para mantener el contador de tiempo funcionando de manera independiente a la interfaz de usuario.
2. Llamadas asíncronas a la API con Retrofit, donde también se utilizan hilos en segundo plano para realizar las solicitudes a la API OpenTrivia mediante Retrofit, con el método "enqueue()" que opera de forma asíncrona:

## **Información del Proyecto:** 
La versión de Android utilizada es:  
**API 31 ("S", Android 12.0)**

## **Entorno de Desarrollo**
**Android Studio**  
Emulador configurado:  
**Pixel 4 API 31 (Android 12.0 "S") x86_64**

## **Configuración del Proyecto**
**Nombre:** LAB3_20190057
**Package name:** com.example.LAB3_20190057 
**Lenguaje:** Java  
**Build configuration:** Groovy DSL (build.gradle)

## **Ejercicio 4**
Se agregó validaciones del máximo de preguntas de verdadero o falso que puede enviar la API.
Además, validaciones por si se excedió las solicitudes de la API (Código de respuesta HTTP 429)

