# 💧 Bidones de Agua App

<p align="center">
  <b>Sistema integral de gestión y pedidos para distribución de agua embotellada</b>
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Spring%20Boot-4.0.6-brightgreen?style=flat-square&logo=springboot&logoColor=white" alt="Spring Boot">
  <img src="https://img.shields.io/badge/React%20Native-Expo-blue?style=flat-square&logo=react&logoColor=white" alt="React Native Expo">
  <img src="https://img.shields.io/badge/PostgreSQL-Database-336791?style=flat-square&logo=postgresql&logoColor=white" alt="PostgreSQL">
  <img src="https://img.shields.io/badge/Java-21-orange?style=flat-square&logo=openjdk&logoColor=white" alt="Java">
</p>

---

## 📌 Descripción del Proyecto

**Bidones de Agua** es una solución tecnológica completa desarrollada para optimizar la administración y el proceso de pedidos de una distribuidora de agua. Consta de un **Backend robusto** basado en una API REST segura y una **Aplicación Móvil** multiplataforma orientada a los clientes y repartidores, diseñada con una interfaz moderna y fluida.

---

## 🛠️ Tecnologías Principales

### Backend (`/server`)
* **Java 21** & **Spring Boot 4.0.6**
* **Spring Security & JWT (`java-jwt`)**: Autenticación segura y control de acceso basado en tokens.
* **Spring Data JPA / Hibernate**: Persistencia y mapeo objeto-relacional.
* **PostgreSQL**: Base de datos relacional principal.
* **Lombok**: Reducción de código boilerplate.
* **Springdoc OpenAPI (Swagger)**: Documentación interactiva de la API.
* **Jakarta Validation**: Validación de datos de entrada.

### Frontend / Mobile (`/client`)
* **React Native** (v0.81.4) con **Expo** (Router v6)
* **NativeWind & Tailwind CSS**: Estilizado moderno y adaptable.
* **React Navigation**: Navegación fluida por pestañas y stacks.
* **AsyncStorage & NetInfo**: Manejo de estado local y conectividad de red.
* **Expo Notifications**: Sistema de avisos y alertas en tiempo real.

---

## 📂 Estructura del Repositorio

```text
bidones-de-agua/
├── backend/          # API REST (Spring Boot, Java 21)
```

---

## 🚀 Guía de Configuración y Ejecución

### 1. Prerrequisitos
* **Java Development Kit (JDK) 21**
* **PostgreSQL** instalado y configurado
* **Expo CLI** (o la app *Expo Go* en tu dispositivo móvil)

### 2. Configuración del Backend
1. Navega al directorio del backend:
   ```bash
   cd backend
   ```
2. Configura las credenciales de tu base de datos PostgreSQL en el archivo `src/main/resources/application.properties`.
3. Ejecuta la aplicación utilizando Maven:
   ```bash
   ./mvnw spring-boot:run
   ```
   *(La API estará disponible en `http://localhost:8080` y la documentación de Swagger en `/swagger-ui.html`)*

---

## 📑 Documentación de la API

Una vez que el servidor backend se encuentre en ejecución, puedes explorar y probar todos los endpoints disponibles a través de **Swagger UI** ingresando en tu navegador a:
`http://localhost:8080/swagger-ui/index.html`

---

## 🤝 Contribución

Las contribuciones son bienvenidas. Para cambios importantes, por favor abre primero un issue para discutir qué te gustaría cambiar.
