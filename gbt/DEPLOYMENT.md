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

Crea un archivo `params.json` con el siguiente contenido. **Asegúrate de establecer una contraseña segura para la base de datos.**

```json
[
  {
    "ParameterKey": "EnvironmentName",
    "ParameterValue": "prod"
  },
  {
    "ParameterKey": "DBUser",
    "ParameterValue": "usermongo"
  },
  {
    "ParameterKey": "DBPassword",
    "ParameterValue": "<PON_AQUÍ_UNA_CONTRASEÑA_SEGURA>"
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

## ⚠️ Solución de Problemas: El Repositorio ya Existe

Si CloudFormation falla con un error `AlreadyExists` para el repositorio ECR (`gbt-application`), significa que el repositorio ya existe en tu cuenta de AWS. Sigue estos pasos para "importarlo" y permitir que CloudFormation lo gestione.

### 1. Prepara el Archivo de Importación

Crea un archivo llamado `resources-to-import.txt` con el siguiente contenido. Esto le dice a CloudFormation qué recurso existente corresponde a qué recurso en la plantilla.

```text
[
    {
        "ResourceType":"AWS::ECR::Repository",
        "LogicalResourceId":"ECRRepository",
        "ResourceIdentifier": {
            "RepositoryName":"gbt-application"
        }
    }
]
```

### 2. Ejecuta el Despliegue de Importación

Ejecuta el siguiente comando. En lugar de `create-stack`, usarás `deploy` con la opción `--no-execute-changeset` para revisar los cambios, y luego `import-existing-resources`.

```bash
aws cloudformation deploy \
  --stack-name gbt-application \
  --template-file cloudformation/backend.yaml \
  --parameter-overrides $(cat params.json | jq -r 'map(.ParameterKey + "=" + .ParameterValue) | join(" ")') \
  --capabilities CAPABILITY_NAMED_IAM \
  --no-execute-changeset

aws cloudformation import-existing-resources \
  --stack-name gbt-application \
  --resources-to-import file://resources-to-import.txt
```

Después de que la importación sea exitosa, tu stack estará sincronizado y podrás realizar actualizaciones normalmente.

---

## 2. Construcción y Despliegue de la Aplicación

Sigue estos pasos para compilar la aplicación, construir la imagen de Docker, subirla a ECR y desplegarla.

### 1. Compila la Aplicación

Desde la raíz del proyecto, ejecuta el siguiente comando para crear el archivo `.jar`:

```bash
./gradlew build
```

### 2. Construye y Sube la Imagen Docker

Reemplaza `<tu-aws-account-id>` con tu ID de cuenta de AWS. Si no lo conoces, puedes obtenerlo con `aws sts get-caller-identity --query Account --output text`.

```bash
# 1. Inicia sesión en ECR
aws ecr get-login-password --region us-east-1 | docker login --username AWS --password-stdin <tu-aws-account-id>.dkr.ecr.us-east-1.amazonaws.com

# 2. Construye la imagen
docker build -t gbt-application .

# 3. Etiqueta la imagen para ECR
docker tag gbt-application:latest <tu-aws-account-id>.dkr.ecr.us-east-1.amazonaws.com/gbt-application:latest

# 4. Sube la imagen a ECR
docker push <tu-aws-account-id>.dkr.ecr.us-east-1.amazonaws.com/gbt-application:latest
```

### 3. Fuerza un Nuevo Despliegue (Opcional)

Después de subir una nueva imagen, ECS normalmente desplegará la nueva versión automáticamente. Para forzar el despliegue inmediato, ejecuta:

```bash
aws ecs update-service --cluster gbt-application-prod-cluster --service gbt-application-prod-service --force-new-deployment --region us-east-1
```

---

## 3. Verificar el Despliegue

Una vez que el servicio está en ejecución, la tarea de ECS tendrá una IP pública asignada. Usa los siguientes comandos para encontrarla.

### 3.1 Encontrar la IP Pública de la Tarea

1.  **Listar las tareas en ejecución:**
    ```bash
    aws ecs list-tasks --cluster gbt-application-prod-cluster --service-name gbt-application-prod-service --query 'taskArns[0]' --output text --region <tu-región>
    ```

2.  **Describir la tarea para obtener su IP pública:**
    Copia el ARN de la tarea del comando anterior y úsalo aquí.
    ```bash
    aws ecs describe-tasks --cluster gbt-application-prod-cluster --tasks <ARN_DE_LA_TAREA> --query 'tasks[0].attachments[0].details[?name==`publicIPv4Address`].value' --output text --region <tu-región>
    ```

### 3.2 Probar la Aplicación

Una vez que tengas la IP pública, puedes acceder a la aplicación en el puerto `8080`. Por ejemplo:

```bash
curl http://<IP_PÚBLICA_DE_LA_TAREA>:8080/api/funds
```

También puedes abrir `http://<IP_PÚBLICA_DE_LA_TAREA>:8080/swagger-ui/index.html` en tu navegador para ver la documentación de la API de Swagger.

---

## 4. Actualizar la Aplicación

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

## 5. Limpieza

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
    CloudFormation no eliminará el repositorio si contiene imágenes. Si deseas borrarlo, primero elimina todas las imágenes y luego ejecuta:
    ```bash
    aws ecr delete-repository --repository-name gbt-application --force --region <tu-región>
    ```
