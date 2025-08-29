# 🚀 Guía de Despliegue de GBT

Esta guía proporciona instrucciones detalladas para desplegar la aplicación GBT en AWS usando CloudFormation y gestionar el ciclo de vida del despliegue.

## Tabla de Contenidos
- [Requisitos Previos](#-requisitos-previos)
- [1. Configuración de Infraestructura](#1-configuración-de-infraestructura)
- [2. Construir y Subir la Imagen Docker](#2-construir-y-subir-la-imagen-docker)
- [3. Desplegar con CloudFormation](#3-desplegar-con-cloudformation)
- [4. Verificar el Despliegue](#4-verificar-el-despliegue)
- [5. Actualizar la Aplicación](#5-actualizar-la-aplicación)
- [6. Monitoreo y Registros](#6-monitoreo-y-registros)
- [7. Limpieza](#7-limpieza)
- [Solución de Problemas](#-solución-de-problemas)

## 🛠 Requisitos Previos

- Cuenta de AWS con permisos de administrador
- AWS CLI instalado y configurado
- Docker instalado y en ejecución
- Cluster de MongoDB Atlas o MongoDB autogestionado
- VPC con al menos 2 subredes públicas en diferentes zonas de disponibilidad
- Java 17+ y Gradle para construcciones locales

## 1. Configuración de Infraestructura

### 1.1 VPC y Redes
- Crea una VPC con subredes públicas en al menos 2 zonas de disponibilidad
- Asegúrate de que las tablas de enrutamiento y la puerta de enlace a Internet estén configuradas correctamente
- Anota el ID de la VPC y los IDs de las subredes

### 1.2 Configuración de MongoDB
- Configura un cluster de MongoDB (se recomienda Atlas)
- Crea un usuario de base de datos con permisos de lectura/escritura
- Anota la cadena de conexión (se usará como `MongoDBUri`)

## 2. Construir y Subir la Imagen Docker

### 2.1 Construir la Aplicación
```bash
./gradlew clean build
```

### 2.2 Construir la Imagen Docker
```bash
docker build -t gbt-application:latest .
```

### 2.3 Crear Repositorio ECR
```bash
aws ecr create-repository --repository-name gbt-application
```

### 2.4 Autenticar Docker en ECR
```bash
aws ecr get-login-password --region <región> | docker login --username AWS --password-stdin <id-cuenta>.dkr.ecr.<región>.amazonaws.com
```

### 2.5 Etiquetar y Subir la Imagen
```bash
docker tag gbt-application:latest <id-cuenta>.dkr.ecr.<región>.amazonaws.com/gbt-application:latest
docker push <id-cuenta>.dkr.ecr.<región>.amazonaws.com/gbt-application:latest
```

## 3. Desplegar con CloudFormation

### 3.1 Preparar Parámetros
Crea un archivo `params.json`:
```json
[
  {
    "ParameterKey": "EnvironmentName",
    "ParameterValue": "prod"
  },
  {
    "ParameterKey": "VpcId",
    "ParameterValue": "vpc-xxxxxxxx"
  },
  {
    "ParameterKey": "SubnetIds",
    "ParameterValue": "subnet-xxxxxxxx,subnet-yyyyyyyy"
  },
  {
    "ParameterKey": "MongoDBUri",
    "ParameterValue": "mongodb+srv://<usuario>:<contraseña>@cluster.xxxxx.mongodb.net/gbt?retryWrites=true&w=majority"
  },
  {
    "ParameterKey": "ContainerCpu",
    "ParameterValue": "1024"
  },
  {
    "ParameterKey": "ContainerMemory",
    "ParameterValue": "2048"
  }
]
```

### 3.2 Crear Pila de CloudFormation
```bash
aws cloudformation create-stack \
  --stack-name gbt-application \
  --template-body file://cloudformation/backend.yaml \
  --parameters file://params.json \
  --capabilities CAPABILITY_NAMED_IAM \
  --region <tu-región>
```

### 3.3 Monitorear la Creación de la Pila
```bash
aws cloudformation describe-stacks \
  --stack-name gbt-application \
  --query 'Stacks[0].StackStatus' \
  --output text
```

## 4. Verificar el Despliegue

### 4.1 Obtener URL del Servicio
```bash
aws cloudformation describe-stacks \
  --stack-name gbt-application \
  --query 'Stacks[0].Outputs[?OutputKey==`ServiceURL`].OutputValue' \
  --output text
```

### 4.2 Probar Endpoints de la API
```bash
# Endpoints públicos (sin autenticación)
curl https://<dns-del-balanceador>/api/funds

# Endpoints de administración (requieren autenticación básica)
curl -u admin:admin123 https://<dns-del-balanceador>/api/admin/customers
```

## 5. Actualizar la Aplicación

### 5.1 Actualizar y Subir Nueva Imagen
```bash
# Construir y etiquetar nueva versión
docker build -t gbt-application:nueva-version .
docker tag gbt-application:nueva-version <id-cuenta>.dkr.ecr.<región>.amazonaws.com/gbt-application:latest
docker push <id-cuenta>.dkr.ecr.<región>.amazonaws.com/gbt-application:latest
```

### 5.2 Forzar Nuevo Despliegue
```bash
aws ecs update-service \
  --cluster gbt-application-prod-cluster \
  --service gbt-application-prod-service \
  --force-new-deployment \
  --region <tu-región>
```

## 6. Monitoreo y Registros

### 6.1 Ver Registros en CloudWatch
```bash
GRUPO_REGISTRO=$(aws ecs describe-services \
  --cluster gbt-application-prod-cluster \
  --services gbt-application-prod-service \
  --query 'services[0].deployments[0].taskDefinition' \
  --output text \
  --region <tu-región> \
  | cut -d'/' -f2)

aws logs tail /ecs/$GRUPO_REGISTRO --follow --region <tu-región>
```

### 6.2 Verificar Estado del Servicio
```bash
aws ecs describe-services \
  --cluster gbt-application-prod-cluster \
  --services gbt-application-prod-service \
  --region <tu-región>
```

## 7. Limpieza

### 7.1 Eliminar Pila de CloudFormation
```bash
aws cloudformation delete-stack --stack-name gbt-application --region <tu-región>
```

### 7.2 Eliminar Repositorio ECR
```bash
aws ecr delete-repository \
  --repository-name gbt-application \
  --force \
  --region <tu-región>
```

## 🚨 Solución de Problemas

### Problemas Comunes

1. **Tareas de ECS que Fallan al Iniciar**
   - Revisa los registros de CloudWatch en busca de errores
   - Verifica los permisos del repositorio ECR
   - Asegúrate de que el contenedor pueda acceder a MongoDB

2. **Tiempos de Espera en la Conexión**
   - Verifica las reglas del grupo de seguridad
   - Revisa los puntos de enlace de VPC y el enrutamiento
   - Asegúrate de que MongoDB permita conexiones desde las tareas de ECS

3. **Fallos en las Comprobaciones de Salud**
   - Verifica la ruta de comprobación de salud en el grupo de destino
   - Revisa los registros de la aplicación en busca de errores de inicio
   - Asegúrate de que el contenedor esté escuchando en el puerto correcto

4. **Problemas de Permisos de IAM**
   - Verifica los permisos del rol de ejecución de tareas
   - Revisa las políticas del repositorio ECR
   - Asegúrate de los permisos del grupo de registros de CloudWatch

### Obtener Ayuda
Para soporte adicional, consulta:
- [Documentación de AWS ECS](https://docs.aws.amazon.com/ecs/)
- [Guía del Usuario de CloudFormation](https://docs.aws.amazon.com/cloudformation/)
- [Documentación de MongoDB Atlas](https://docs.atlas.mongodb.com/)
