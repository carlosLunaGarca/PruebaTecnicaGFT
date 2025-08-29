# Guía de Despliegue de la Aplicación GBT

Esta guía explica cómo desplegar la aplicación GBT tanto localmente usando Docker Compose como en AWS usando CloudFormation.

## Tabla de Contenidos
1. [Desarrollo Local con Docker Compose](#desarrollo-local-con-docker-compose)
2. [Despliegue en AWS con CloudFormation](#despliegue-en-aws-con-cloudformation)
3. [Mejores Prácticas de Seguridad](#mejores-prácticas-de-seguridad)
4. [Solución de Problemas](#solución-de-problemas)
5. [Limpieza](#limpieza)

## Desarrollo Local con Docker Compose

### Requisitos Previos

- Docker y Docker Compose instalados
- Java 17 o superior
- Gradle 7.0+

### Inicio Rápido

1. Clonar el repositorio:
   ```bash
   git clone https://github.com/carlosLunaGarca/PruebaTecnicaGFT.git
   cd PruebaTecnicaGFT/gbt
   ```

2. Crear un archivo `.env` con tu configuración:
   ```bash
   cp .env.example .env
   # Editar el archivo .env con tus preferencias
   ```

3. Iniciar la aplicación con Docker Compose:
   ```bash
   docker-compose up -d
   ```

4. Esperar a que todos los servicios se inicien (puede tardar unos minutos en la primera ejecución)

5. Acceder a la aplicación:
   - API: http://localhost:8080/api/funds
   - Interfaz Swagger: http://localhost:8080/swagger-ui.html
   - MongoDB Express (Interfaz de administración): http://localhost:8081

### Servicios

- **gbt-application**: Aplicación Spring Boot (puerto 8080)
- **mongo**: Base de datos MongoDB (puerto 27017)
- **mongo-express**: Interfaz web de administración de MongoDB (puerto 8081)

### Variables de Entorno

Crear un archivo `.env` con las siguientes variables:

```env
# Configuración de MongoDB
SPRING_DATA_MONGODB_URI=mongodb://admin:admin123@mongodb:27017/gbt?authSource=admin

# Configuración de Seguridad
APP_SECURITY_CLIENT_USERNAME=client
APP_SECURITY_CLIENT_PASSWORD=client123
APP_SECURITY_ADMIN_USERNAME=admin
APP_SECURITY_ADMIN_PASSWORD=admin123

# Configuración del Servidor
SERVER_PORT=8080
```

### Ejecución de Pruebas

Para ejecutar pruebas localmente:

```bash
./gradlew test
```

### Detener la Aplicación

```bash
docker-compose down
```

Para eliminar volúmenes (incluyendo datos de la base de datos):

```bash
docker-compose down -v
```

## Despliegue en AWS con CloudFormation

### Requisitos Previos

1. Cuenta de AWS con los permisos apropiados
2. AWS CLI configurado con credenciales
3. Docker instalado y en ejecución
4. Clúster de MongoDB Atlas o MongoDB auto-alojado
5. VPC con al menos 2 subredes públicas en diferentes zonas de disponibilidad (AZs)
6. Repositorio ECR para las imágenes de contenedores

### Permisos IAM Requeridos

El usuario/rol de IAM que realice el despliegue debe tener permisos para:
- CloudFormation
- ECS
- ECR
- IAM (para la creación de roles)
- VPC (para redes)
- CloudWatch Logs

### Pasos para el Despliegue

#### 1. Empaquetar la Aplicación

Construir el archivo JAR de la aplicación:

```bash
./gradlew clean build
```

#### 2. Construir y Subir la Imagen de Docker

1. Autenticar Docker en tu registro ECR:
   ```bash
   aws ecr get-login-password --region <región> | \
   docker login --username AWS --password-stdin <id-cuenta>.dkr.ecr.<región>.amazonaws.com
   ```

2. Construir la imagen de Docker:
   ```bash
   docker build -t <id-cuenta>.dkr.ecr.<región>.amazonaws.com/gbt-application:latest .
   ```

3. Crear el repositorio ECR (si no existe):
   ```bash
   aws ecr create-repository --repository-name gbt-application
   ```

4. Subir la imagen a ECR:
   ```bash
   docker push <id-cuenta>.dkr.ecr.<región>.amazonaws.com/gbt-application:latest
   ```

#### 3. Desplegar con CloudFormation

1. Crear una pila de CloudFormación usando la plantilla:
   ```bash
   aws cloudformation create-stack \
     --stack-name gbt-application-stack \
     --template-body file://cloudformation/backend.yaml \
     --parameters \
         ParameterKey=EnvironmentName,ParameterValue=prod \
         ParameterKey=VpcId,ParameterValue=vpc-xxxxxxxx \
         ParameterKey=SubnetIds,ParameterValue="subnet-xxxxxxxx,subnet-yyyyyyyy" \
         ParameterKey=MongoDBUri,ParameterValue="mongodb+srv://<usuario>:<contraseña>@cluster0.xxxxx.mongodb.net/gbt?retryWrites=true&w=majority" \
         ParameterKey=ContainerCpu,ParameterValue=1024 \
         ParameterKey=ContainerMemory,ParameterValue=2048 \
     --capabilities CAPABILITY_NAMED_IAM \
     --region <tu-región>
   ```

2. Monitorear la creación de la pila:
   ```bash
   aws cloudformation describe-stacks \
     --stack-name gbt-application-stack \
     --query 'Stacks[0].StackStatus' \
     --output text
   ```

#### 4. Verificar el Despliegue

1. Obtener la URL del servicio:
   ```bash
   aws cloudformation describe-stacks \
     --stack-name gbt-application-stack \
     --query 'Stacks[0].Outputs[?OutputKey==`ServiceURL`].OutputValue' \
     --output text
   ```

2. Probar los endpoints de la API:
   ```bash
   curl $(aws cloudformation describe-stacks \
     --stack-name gbt-application-stack \
     --query 'Stacks[0].Outputs[?OutputKey==`ServiceURL`].OutputValue' \
     --output text)/api/funds
   ```

## Mejores Prácticas de Seguridad

### 1. Gestión de Secretos
- Usar AWS Secrets Manager o Parameter Store para datos sensibles
- Nunca comprometer secretos en el control de versiones
- Rotar credenciales regularmente

### 2. Seguridad de Red
- Usar subredes privadas para tareas ECS
- Configurar grupos de seguridad con el principio de mínimo privilegio
- Habilitar VPC Flow Logs
- Considerar usar AWS WAF para protección adicional

### 3. Mejores Prácticas de IAM
- Seguir el principio de mínimo privilegio
- Usar roles de IAM en lugar de claves de acceso cuando sea posible
- Habilitar MFA para usuarios privilegiados

### 4. Monitoreo y Registros
- Habilitar CloudWatch Container Insights
- Configurar alertas de CloudWatch
- Configurar políticas de retención de registros

## Actualización de la Aplicación

1. Construir y subir una nueva imagen de Docker:
   ```bash
   docker build -t <id-cuenta>.dkr.ecr.<región>.amazonaws.com/gbt-application:latest .
   docker push <id-cuenta>.dkr.ecr.<región>.amazonaws.com/gbt-application:latest
   ```

2. Actualizar el servicio ECS para usar la nueva imagen:
   ```bash
   aws ecs update-service \
     --cluster gbt-application-prod-cluster \
     --service gbt-application-prod-service \
     --force-new-deployment \
     --region <tu-región>
   ```

## Limpieza

Para eliminar todos los recursos y evitar cargos adicionales:

1. Eliminar la pila de CloudFormation:
   ```bash
   aws cloudformation delete-stack --stack-name gbt-application-stack --region <tu-región>
   ```

2. Eliminar el repositorio ECR (opcional):
   ```bash
   aws ecr delete-repository \
     --repository-name gbt-application \
     --force \
     --region <tu-región>
   ```

## Solución de Problemas

### Verificar Estado del Servicio ECS
```bash
aws ecs describe-services \
  --cluster gbt-application-prod-cluster \
  --services gbt-application-prod-service \
  --query 'services[0]' \
  --region <tu-región>
```

### Ver Registros de CloudWatch
```bash
GRUPO_REGISTRO=$(aws ecs describe-services \
  --cluster gbt-application-prod-cluster \
  --services gbt-application-prod-service \
  --query 'services[0].deployments[0].taskDefinition' \
  --output text \
  --region <tu-región> \
  | cut -d'/' -f2)

aws logs tail /ecs/$GRUPO_REGISTRO \
  --follow \
  --region <tu-región>
```

### Verificar Estado del Balanceador de Carga
```bash
aws elbv2 describe-load-balancers \
  --names gbt-application-lb \
  --query 'LoadBalancers[0].DNSName' \
  --region <tu-región>
```

### Problemas Comunes
1. **Error al Iniciar la Tarea**: Verificar registros de la tarea ECS en CloudWatch
2. **Tiempos de Espera de Conexión**: Verificar grupos de seguridad y ACLs de red
3. **Fallos en las Comprobaciones de Salud**: Asegurarse de que la ruta de comprobación de salud es correcta
4. **Problemas de Permisos**: Verificar roles y políticas de IAM

Para obtener ayuda adicional, consulta la [Documentación de AWS ECS](https://docs.aws.amazon.com/ecs/index.html).
