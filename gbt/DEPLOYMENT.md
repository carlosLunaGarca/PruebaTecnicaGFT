# Guía de Despliegue de GBT

Esta guía proporciona instrucciones detalladas para desplegar la aplicación GBT en AWS. El proceso se divide en dos fases principales:
1.  **Despliegue de la infraestructura base** con AWS CloudFormation.
2.  **Construcción y despliegue de la aplicación** en la infraestructura creada.

## Requisitos Previos

- **Cuenta de AWS** con permisos para crear recursos (IAM, VPC, ECS, ECR, CloudWatch).
- **AWS CLI** instalado y configurado (`aws configure`).
- **Docker** instalado y en ejecución.
- **Java 17+** y **Gradle** para construir el proyecto.

---

## 1. Despliegue de Infraestructura con CloudFormation

El template `cloudformation/backend.yaml` provisionará la infraestructura necesaria. Deberás proporcionar los parámetros en un archivo `params.json`.

### 1.1 Preparar Parámetros

Crea un archivo `params.json` con el siguiente contenido, reemplazando los valores según tu configuración:

```json
[
  {
    "ParameterKey": "EnvironmentName",
    "ParameterValue": "prod"
  },
  {
    "ParameterKey": "MongoDBUri",
    "ParameterValue": "mongodb+srv://<usuario>:<contraseña>@cluster.../?retryWrites=true&w=majority"
  }
]
```

### 1.2 Crear la Pila de CloudFormation

Ejecuta el siguiente comando para **crear** la pila. Este comando solo debe usarse la primera vez.

```bash
aws cloudformation create-stack \
  --stack-name gbt-application \
  --template-body file://cloudformation/backend.yaml \
  --parameters file://params.json \
  --capabilities CAPABILITY_NAMED_IAM \
  --region <tu-región>
```

Para **actualizar** la pila en el futuro, usa el comando `update-stack`:

```bash
aws cloudformation update-stack \
  --stack-name gbt-application \
  --template-body file://cloudformation/backend.yaml \
  --parameters file://params.json \
  --capabilities CAPABILITY_NAMED_IAM \
  --region <tu-región>
```

---

## 2. Construcción y Despliegue de la Aplicación

### 2.1 Obtener el URI del Repositorio ECR

Busca el URI del repositorio ECR creado por CloudFormation.

```bash
aws cloudformation describe-stacks \
  --stack-name gbt-application \
  --query "Stacks[0].Outputs[?OutputKey=='ECRRepositoryURI'].OutputValue" \
  --output text
```

### 2.2 Construir y Publicar la Imagen Docker

1.  **Construir la aplicación:**
    ```bash
    ./gradlew clean build
    ```

2.  **Construir la imagen Docker:**
    ```bash
    docker build -t gbt-application:latest .
    ```

3.  **Autenticar Docker en ECR:**
    ```bash
    aws ecr get-login-password --region <tu-región> | docker login --username AWS --password-stdin <uri-del-repositorio-ecr>
    ```

4.  **Etiquetar y subir la imagen:**
    ```bash
    docker tag gbt-application:latest <uri-del-repositorio-ecr>:latest
    docker push <uri-del-repositorio-ecr>:latest
    ```

### 2.3 Iniciar el Servicio en ECS

Actualiza el servicio para que ejecute una tarea.

```bash
aws ecs update-service \
  --cluster gbt-application-prod-cluster \
  --service gbt-application-prod-service \
  --desired-count 1 \
  --region <tu-región>
```

---

## Actualizar la Aplicación

1.  **Construye y publica la nueva imagen Docker** (pasos 2.1 y 2.2).
2.  **Fuerza un nuevo despliegue en ECS** para que el servicio tome la imagen actualizada.

    ```bash
    aws ecs update-service \
      --cluster gbt-application-prod-cluster \
      --service gbt-application-prod-service \
      --force-new-deployment \
      --region <tu-región>
    ```

---

## Limpieza

1.  **Escala el servicio a 0 tareas:**
    ```bash
    aws ecs update-service \
      --cluster gbt-application-prod-cluster \
      --service gbt-application-prod-service \
      --desired-count 0 \
      --region <tu-región>
    ```

2.  **Elimina la pila de CloudFormation:**
    ```bash
    aws cloudformation delete-stack --stack-name gbt-application --region <tu-región>
    ```

3.  **Elimina el repositorio ECR (opcional):**
    ```bash
    aws ecr delete-repository --repository-name gbt-application-prod --force --region <tu-región>
    ```
