# GBT - Gestión de Inversiones

Aplicación backend para la gestión de inversiones, desarrollada con Spring Boot y MongoDB.

## Características Principales

- Gestión de clientes y sus inversiones
- Catálogo de fondos de inversión
- Sistema de autenticación y autorización
- API RESTful
- Despliegue en contenedores con Docker
- Integración con MongoDB

## Requisitos Previos

- Java 17 o superior
- Docker y Docker Compose
- MongoDB (puede ser local o en la nube)
- Gradle 7.0+

## Tecnologías Utilizadas

- **Backend**: Spring Boot 3.x
- **Base de datos**: MongoDB
- **Autenticación**: Spring Security
- **Contenedorización**: Docker
- **Orquestación**: Docker Compose
- **Despliegue**: AWS CloudFormation

## Estructura del Proyecto

```
gbt/
├── src/
│   ├── main/
│   │   ├── java/org/gft/gbt/
│   │   │   ├── config/         # Configuraciones de Spring
│   │   │   ├── controller/     # Controladores REST
│   │   │   ├── model/          # Entidades del dominio
│   │   │   ├── repository/     # Repositorios de datos
│   │   │   └── security/       # Configuración de seguridad
│   │   └── resources/          # Archivos de configuración
│   └── test/                   # Pruebas unitarias y de integración
├── cloudformation/             # Plantillas de CloudFormation
├── init/                       # Scripts de inicialización de MongoDB
├── docker-compose.yml          # Configuración para desarrollo local
└── README.md                   # Este archivo
```

## Configuración Rápida

### Desarrollo Local

1. Clonar el repositorio:
   ```bash
   git clone <repositorio>
   cd gbt
   ```

2. Iniciar la aplicación con Docker Compose:
   ```bash
   docker-compose up -d
   ```

3. La aplicación estará disponible en: http://localhost:8080

4. MongoDB Express (UI de administración): http://localhost:8081

### Variables de Entorno

Crea un archivo `.env` en la raíz del proyecto con las siguientes variables:

```env
SPRING_DATA_MONGODB_URI=mongodb://admin:admin123@localhost:27017/gbt?authSource=admin
APP_SECURITY_CLIENT_USERNAME=client
APP_SECURITY_CLIENT_PASSWORD=client123
APP_SECURITY_ADMIN_USERNAME=admin
APP_SECURITY_ADMIN_PASSWORD=admin123
```

## API Endpoints

### Autenticación

- `POST /api/auth/login` - Iniciar sesión
- `POST /api/auth/refresh` - Refrescar token

### Clientes

- `GET /api/customers` - Listar clientes (ADMIN)
- `GET /api/customers/{id}` - Obtener cliente por ID
- `POST /api/customers` - Crear nuevo cliente
- `PUT /api/customers/{id}` - Actualizar cliente

### Fondos

- `GET /api/funds` - Listar todos los fondos
- `GET /api/funds/{id}` - Obtener fondo por ID
- `POST /api/funds` - Crear nuevo fondo (ADMIN)
- `PUT /api/funds/{id}` - Actualizar fondo (ADMIN)

### Inversiones

- `GET /api/investments` - Listar inversiones del usuario
- `POST /api/investments` - Crear nueva inversión
- `GET /api/investments/{id}` - Obtener inversión por ID

## Despliegue en Producción

Consulta el archivo [DEPLOYMENT.md](DEPLOYMENT.md) para instrucciones detalladas sobre cómo desplegar la aplicación en AWS usando CloudFormation.

## Contribución

1. Haz un fork del proyecto
2. Crea una rama para tu feature (`git checkout -b feature/AmazingFeature`)
3. Haz commit de tus cambios (`git commit -m 'Add some AmazingFeature'`)
4. Haz push a la rama (`git push origin feature/AmazingFeature`)
5. Abre un Pull Request

## Licencia

Distribuido bajo la licencia MIT. Ver `LICENSE` para más información.

## Contacto

Equipo de Desarrollo - [contacto@ejemplo.com](mailto:contacto@ejemplo.com)

Enlace del Proyecto: [https://github.com/tu-usuario/gbt](https://github.com/tu-usuario/gbt)
