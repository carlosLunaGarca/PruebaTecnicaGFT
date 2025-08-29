# GBT - Gestión de Fondos de Inversión

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
