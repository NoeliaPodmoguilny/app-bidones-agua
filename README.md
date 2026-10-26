# 💧 Deli Water

<p align="center">
  <b>Plataforma móvil multiempresa para gestionar la venta y entrega de agua envasada</b><br/>
  Roles, pedidos en tiempo real y notificaciones push, con una API REST segura detrás.
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Java-21-orange?style=flat-square&logo=openjdk&logoColor=white" alt="Java 21">
  <img src="https://img.shields.io/badge/Spring%20Boot-4.0-brightgreen?style=flat-square&logo=springboot&logoColor=white" alt="Spring Boot">
  <img src="https://img.shields.io/badge/PostgreSQL-336791?style=flat-square&logo=postgresql&logoColor=white" alt="PostgreSQL">
  <img src="https://img.shields.io/badge/React%20Native-Expo%20SDK%2057-blue?style=flat-square&logo=react&logoColor=white" alt="React Native Expo">
  <img src="https://img.shields.io/badge/TypeScript-strict-3178C6?style=flat-square&logo=typescript&logoColor=white" alt="TypeScript">
  <img src="https://img.shields.io/badge/Docker-ready-2496ED?style=flat-square&logo=docker&logoColor=white" alt="Docker">
  <img src="https://img.shields.io/badge/Google%20Play-closed%20testing-34A853?style=flat-square&logo=googleplay&logoColor=white" alt="Google Play">
</p>

---

## 📌 ¿Qué es?

**Deli Water** es una aplicación Android para distribuidoras de agua. Cada empresa se registra con su propio **administrador**, que crea a sus **repartidores**; cada repartidor da de alta a sus **clientes**. Los clientes piden bidones desde el celular y el repartidor recibe el aviso al instante.

Es una app **multiempresa (multi-tenant)**: varias empresas conviven en el mismo sistema sin ver los datos de las demás.

> **Estado:** en prueba cerrada en Google Play. Backend desplegado en Render.

### Roles

| Rol | Qué puede hacer |
|---|---|
| 👑 **Administrador** | Crear repartidores, ver el resumen del día y reportes con filtros, dar de baja o reactivar repartidores y reasignar sus clientes |
| 🛵 **Repartidor** | Crear clientes, gestionar pedidos (*en camino*, *entregado*, *rechazado* con motivo), llamar al cliente o ver su dirección |
| 🙋 **Cliente** | Hacer, repetir y eliminar pedidos pendientes, ver su historial y notificaciones, y contactar a su repartidor |

---

## 🏗️ Arquitectura

```
┌─────────────────────┐   HTTPS + JWT    ┌──────────────────────────┐   JPA   ┌────────────┐
│  App Android        │ ───────────────▶ │  API REST (Spring Boot)  │ ──────▶ │ PostgreSQL │
│  React Native/Expo  │ ◀─────────────── │  Docker en Render        │         └────────────┘
└─────────▲───────────┘                  └────────────┬─────────────┘
          │ push                                      │ POST /push/send (async, post-commit)
          │                                           ▼
   ┌──────┴───────┐                           ┌───────────────┐
   │ Firebase FCM │ ◀──────────────────────── │ Expo Push API │
   └──────────────┘                           └───────────────┘
```

## 🛠️ Stack tecnológico

### Backend
- **Java 21** y **Spring Boot 4**
- **Spring Security** + **JWT** (`java-jwt`), **BCrypt**
- **Spring Data JPA / Hibernate**, consultas con `Specification` y `@EntityGraph`
- **PostgreSQL** 
- **Bean Validation**, **Lombok**, **springdoc-openapi** 
- **Docker**

### Mobile
- **React Native** y **Expo SDK 57** con **expo-router** 
- **TypeScript** 
- **NativeWind (Tailwind)** 
- **expo-notifications** + **Firebase Cloud Messaging**

(Ver en: https://github.com/NoeliaPodmoguilny/app-bidones-agua-FRONTEND)

---

## 📂 Estructura Backend

```text

          
└── src/main/java/com/agua/bidones
  ├── controller/  # endpoints
  ├── service/     # reglas de negocio
  ├── repository/  # acceso a datos
  ├── model/       # entidades JPA
  ├── dto/         # contratos de entrada y salida
  ├── security/    # JWT, filtro
  └── exception/   # manejo global de errores
```

---

## 🚀 Cómo ejecutarlo

### Requisitos
- JDK 21, PostgreSQL y Maven 
- Node.js y npm
- Un celular Android o emulador

### Backend

Definí las variables de entorno:

| Variable | Descripción |
|---|---|
| `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USER`, `DB_PASSWORD` | Conexión a PostgreSQL |
| `JWT_KEY` | Clave secreta para firmar los tokens  |
| `USER_GENERATOR` | Emisor del token |
| `TOKEN_EXPO` | Access token del servicio de push de Expo |
| `URL-FRONT` | Origen permitido por CORS |
| `SWAGGER_ENABLED` | `true` para habilitar Swagger en desarrollo (por defecto `false`) |

```bash
cd bidones
./mvnw spring-boot:run
```

La API queda en `http://localhost:8080`. Con `SWAGGER_ENABLED=true`, la documentación interactiva está en `/swagger-ui/index.html`.

### 2. App móvil
Ver en: https://github.com/NoeliaPodmoguilny/app-bidones-agua-FRONTEND

---

## 🤝 Contribución

Las contribuciones son bienvenidas. Para cambios importantes, abrí primero un *issue* para discutir qué te gustaría cambiar.
