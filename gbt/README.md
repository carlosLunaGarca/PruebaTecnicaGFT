# 🏦 GBT - Plataforma de Gestión de Fondos de Inversión BTG Pactual

[![Java](https://img.shields.io/badge/Java-17-orange.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.3-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![MongoDB](https://img.shields.io/badge/MongoDB-7.0-green.svg)](https://www.mongodb.com/)
[![Docker](https://img.shields.io/badge/Docker-20.10+-blue.svg)](https://www.docker.com/)
[![AWS](https://img.shields.io/badge/AWS-CloudFormation-orange.svg)](https://aws.amazon.com/cloudformation/)
[![License](https://img.shields.io/badge/license-MIT-blue.svg)](LICENSE)

> **Plataforma backend robusta para la gestión autónoma de fondos de inversión BTG Pactual, permitiendo a los clientes suscribirse, cancelar y gestionar sus inversiones sin necesidad de contactar asesores.**

## 📋 Tabla de Contenidos

- [🌟 Características Principales](#-características-principales)
- [🏗️ Arquitectura del Sistema](#️-arquitectura-del-sistema)
- [🛠️ Stack Tecnológico](#️-stack-tecnológico)
- [🚀 Inicio Rápido](#-inicio-rápido)
- [📚 Documentación API](#-documentación-api)
- [💡 Ejemplos de Uso](#-ejemplos-de-uso)
- [🔐 Seguridad y Autenticación](#-seguridad-y-autenticación)
- [🧪 Testing](#-testing)
- [🐳 Contenedorización](#-contenedorización)
- [☁️ Despliegue AWS](#️-despliegue-aws)
- [⚙️ Configuración](#️-configuración)
- [🚨 Resolución de Problemas](#-resolución-de-problemas)

## 🌟 Características Principales

### 💰 Gestión de Fondos de Inversión
- ✅ **Catálogo de 5 fondos BTG Pactual** preconfigurados con montos mínimos específicos
- ✅ **Consulta pública** de información de fondos sin autenticación requerida
- ✅ **Filtrado por categoría** (FPV, FIC) y monto mínimo de inversión
- ✅ **Información detallada** de cada fondo con características específicas

### 👥 Sistema de Clientes
- ✅ **Balance inicial** de COP $500.000 por cliente
- ✅ **Preferencias de notificación** configurables (EMAIL/SMS)
- ✅ **Gestión de suscripciones** activas por cliente
- ✅ **Historial completo** de todas las transacciones

### 🔄 Operaciones de Inversión
- ✅ **Suscripción a fondos** con validación automática de saldo suficiente
- ✅ **Cancelación de suscripciones** con reembolso instantáneo al balance
- ✅ **Validación de montos mínimos** según el fondo seleccionado
- ✅ **Identificadores únicos** para cada transacción

### 📬 Sistema de Notificaciones
- ✅ **Notificaciones automáticas** al suscribirse a fondos
- ✅ **Múltiples canales** (Email y SMS) según preferencia del cliente
- ✅ **Factory pattern** para extensibilidad de nuevos canales
- ✅ **Configuración por cliente** de preferencias de comunicación

### 🔐 Seguridad Empresarial
- ✅ **Autenticación Basic Auth** con credenciales seguras
- ✅ **Autorización basada en roles** (ADMIN/CLIENT)
- ✅ **Endpoints protegidos** según el rol del usuario
- ✅ **Validación exhaustiva** de entrada en todos los endpoints

### 🔧 Operaciones
- ✅ **Health checks** integrados
- ✅ **Logging estructurado** con diferentes niveles
- ✅ **Métricas y monitoreo** con Actuator
- ✅ **Documentación OpenAPI** interactiva

## 🛠️ Tecnologías

### Backend
- **☕ Java 17** - LTS version
- **🚀 Spring Boot 3.3.3** - Framework principal
- **🔒 Spring Security 6** - Autenticación y autorización
- **📊 Spring Data MongoDB** - Acceso a datos
- **🔍 Spring Boot Actuator** - Monitoreo y métricas

### Base de Datos
- **🍃 MongoDB 7.0** - Base de datos NoSQL
- **🗂️ MongoDB Reactive** - Soporte reactivo opcional

### Documentación
- **📖 SpringDoc OpenAPI 3** - Documentación API automática
- **🎯 Swagger UI** - Interfaz interactiva de API

### Testing
- **✅ JUnit 5** - Framework de testing
- **🎭 Mockito** - Mocking framework
- **🧪 TestContainers** - Testing de integración

### DevOps
- **🐳 Docker & Docker Compose** - Contenedorización
- **☁️ AWS CloudFormation** - Infrastructure as Code
- **📦 Amazon ECS** - Orquestación de contenedores
- **🔍 Amazon ECR** - Registry de imágenes

## 📦 Requisitos Previos

### Desarrollo Local

Aplicación backend para la gestión de fondos de inversión, desarrollada con Spring Boot 3.x, MongoDB y Spring Security.

## 🚀 Características Principales

- Catálogo de fondos de inversión
- Sistema de autenticación con roles (ADMIN y CLIENTE)
- API RESTful documentada con OpenAPI/Swagger
- Despliegue en contenedores con Docker
- Integración con MongoDB
- Configuración para despliegue en AWS con CloudFormation

## 🛠️ Requisitos Previos

- Java 17 o superior
- Docker y Docker Compose
- MongoDB (se incluye configuración para MongoDB en Docker)
- Gradle 7.0+

## 🛠️ Tecnologías Utilizadas

- **Backend**: Spring Boot 3.3.3
- **Base de datos**: MongoDB
- **Seguridad**: Spring Security con autenticación básica
- **Documentación**: SpringDoc OpenAPI 3.0
- **Contenedorización**: Docker
- **Orquestación**: Docker Compose
- **Despliegue**: AWS CloudFormation

## 🏗️ Estructura del Proyecto

```
gbt/
├── src/
│   ├── main/
│   │   ├── java/org/gft/gbt/
│   │   │   ├── config/         # Configuraciones de Spring
│   │   │   ├── controller/     # Controladores REST
│   │   │   ├── exception/      # Manejo de excepciones
│   │   │   ├── handler/        # Manejadores de excepciones
│   │   │   ├── model/          # Entidades del dominio
│   │   │   ├── repository/     # Repositorios de datos
│   │   │   ├── security/       # Configuración de seguridad
│   │   │   └── service/        # Lógica de negocio
│   │   └── resources/          # Archivos de configuración
│   └── test/                   # Pruebas unitarias y de integración
├── cloudformation/             # Plantillas de CloudFormation
├── docker/                     # Configuración de Docker
├── gradle/                     # Configuración de Gradle Wrapper
├── .gitignore                  # Archivos ignorados por Git
├── build.gradle                # Configuración de dependencias
├── docker-compose.yml          # Configuración para desarrollo local
└── README.md                   # Este archivo
```

## 🚀 Configuración Rápida

### Desarrollo Local con Docker

1. Clonar el repositorio:
   ```bash
   git clone <repositorio>
   cd gbt
   ```

2. Crear archivo de variables de entorno:
   ```bash
   cp .env.example .env
   ```
   Edita el archivo `.env` según sea necesario.

3. Iniciar la aplicación con Docker Compose:
   ```bash
   docker-compose up -d
   ```

4. La aplicación estará disponible en: http://localhost:8080

5. Documentación de la API (Swagger UI): http://localhost:8080/swagger-ui.html

6. MongoDB Express (UI de administración): http://localhost:8081

### Variables de Entorno

Crea un archivo `.env` en la raíz del proyecto con las siguientes variables:

```env
# MongoDB Configuration
SPRING_DATA_MONGODB_URI=mongodb://admin:admin123@mongodb:27017/gbt?authSource=admin

# Security Configuration
APP_SECURITY_CLIENT_USERNAME=client
APP_SECURITY_CLIENT_PASSWORD=client123
APP_SECURITY_ADMIN_USERNAME=admin
APP_SECURITY_ADMIN_PASSWORD=admin123

# Server Configuration
SERVER_PORT=8080
```

## 📚 Documentación de la API

La documentación de la API está disponible en formato OpenAPI 3.0 y puede ser accedida a través de:

- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **OpenAPI JSON**: http://localhost:8080/v3/api-docs

### Endpoints Disponibles

#### 🔐 Autenticación

- `GET /api/v1/auth/login` - Iniciar sesión (Basic Auth)

#### 📊 Fondos

- `GET /api/funds` - Listar todos los fondos (público)

#### 👥 Clientes

- `GET /api/customers/{customerId}/**` - Acceso a datos de cliente (requiere autenticación)

#### 🛠️ Administración

- `GET /api/admin/**` - Endpoints de administración (rol ADMIN requerido)

## 🚀 Despliegue en Producción

Para desplegar la aplicación en AWS usando CloudFormation, consulta el archivo [DEPLOYMENT.md](DEPLOYMENT.md).

## 🧪 Ejecución de Pruebas

Para ejecutar las pruebas unitarias:

```bash
./gradlew test
```

## 🤝 Contribución

Las contribuciones son bienvenidas. Por favor sigue estos pasos:

1. Haz un fork del proyecto
2. Crea una rama para tu feature (`git checkout -b feature/AmazingFeature`)
3. Haz commit de tus cambios (`git commit -m 'Add some AmazingFeature'`)
4. Haz push a la rama (`git push origin feature/AmazingFeature`)
5. Abre un Pull Request

## 📄 Licencia

Distribuido bajo la licencia MIT. Ver `LICENSE` para más información.

## 📧 Contacto

Equipo de Desarrollo - [lunaexpres123@gmail.com](mailto:contacto@ejemplo.com)

Enlace del Proyecto: [https://github.com/carlosLunaGarca/PruebaTecnicaGFT/tree/main/gbt](https://github.com/tu-usuario/gbt)
