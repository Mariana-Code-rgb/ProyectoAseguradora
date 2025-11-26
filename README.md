# ProyectoAseguradora

Este repositorio contiene un ejemplo completo de sistema de seguros basado en **procesos de negocio**, construido sobre Camunda 7 y Spring Boot. El caso implementa la emisión de pólizas, la gestión de reclamaciones y la renovación/fidelización de clientes.

El sistema simula una aseguradora que ofrece pólizas de autos a diferentes clientes, permitiendo gestionar todo el ciclo de vida: solicitud de seguro, evaluación de riesgo, gestión de siniestros y renovaciones automatizadas con interacción vía portal del cliente.

## Índice

1. [Descripción](#descripción)
2. [Aplicaciones](#aplicaciones)
3. [Tecnologías](#tecnologías)
4. [Requisitos-de-instalación](#requisitos-de-instalación)
5. [Uso](#uso)

## Descripción

El objetivo principal de este proyecto es mostrar cómo diseñar e implementar un **sistema de información centrado en procesos** usando Camunda Platform 7 como motor BPM, orquestando varias aplicaciones Spring Boot que interactúan entre sí por REST.

Las funcionalidades principales incluyen:

- Emisión y suscripción de pólizas (evaluación de riesgo, cotización y emisión).
- Gestión de reclamaciones (registro de siniestros, evaluación, pagos y notificaciones).
- Gestión de renovaciones y fidelización (recordatorios, ofertas, negociación de retención y pagos de renovación).
- Portal de cliente para ver trámites pendientes y responder a ofertas o confirmar pagos.

La arquitectura se organiza como un monorepo Maven multi-módulo, con separación clara entre motor de procesos, aplicaciones web y librería compartida.

## Aplicaciones

1. **BPM-Engine**  
   Módulo que integra el motor de procesos Camunda 7 en un proyecto Spring Boot.  
   Aloja los modelos BPMN, los delegates Java, la API REST de Camunda y el histórico de procesos.  
   - Puerto por defecto: `9000`.

2. **App-Cliente**  
   Portal web para los asegurados.  
   Permite:
   - Iniciar sesión como cliente de prueba.
   - Ver trámites pendientes (responder oferta de renovación, confirmar pagos, etc.).
   - Enviar respuestas que se correlacionan con mensajes BPMN hacia el engine.  
   - Puerto por defecto: `9001`.

3. **App-Gestion**  
   Aplicación interna para empleados de la aseguradora.  
   Proporciona:
   - Bandejas de tareas para Emisión, Reclamaciones y Renovaciones.
   - Formularios Thymeleaf para completar tareas humanas (revisar casos, registrar motivos, negociar retención, etc.).  
   - Puerto por defecto: `9003`.

4. **Shared-Lib**  
   Librería compartida que contiene DTOs y clases comunes entre los módulos (por ejemplo, estructuras de datos para solicitudes y contratos).

## Tecnologías

El sistema está desarrollado principalmente con:

- **Java 17 / 21** como lenguaje base.
- **Spring Boot 3** para las aplicaciones web (App-Cliente, App-Gestion, BPM-Engine).
- **Camunda Platform 7** como motor BPMN/DMN para modelar y ejecutar los procesos de negocio.
- **Spring Web / Spring MVC** para exponer controladores REST y vistas Thymeleaf.
- **Spring Data JPA + H2** para persistir datos de histórico de renovaciones/reclamaciones y tablas de apoyo.
- **Thymeleaf** para la capa de presentación de las aplicaciones HTML.
- **Maven** como sistema de construcción multi-módulo.

## Requisitos de instalación

Para ejecutar el proyecto localmente necesitas:

1. **Git** – para clonar el repositorio.  
2. **JDK 17 o superior** – configurar `JAVA_HOME` apuntando al JDK.  
3. **Maven 3.8+** – disponible en la variable de entorno `PATH`.  
4. **IntelliJ IDEA** (recomendado) u otro IDE compatible con proyectos Maven multi‑módulo.  
5. **Docker (opcional)** si más adelante deseas levantar bases de datos externas (por ahora se usa H2 embebida).

## Uso

1. Clonar el repositorio:

git clone https://github.com/Mariana-Code-rgb/ProyectoAseguradora.git
cd ProyectoAseguradora

2. Construir todos los módulos con Maven:

mvn clean package

3. Desde el IDE o desde la línea de comandos, levantar las tres aplicaciones Spring Boot:

- `BpmEngineApplication` (módulo **BPM-Engine**) – puerto `9000`.
- `AppGestionApplication` (módulo **App-Gestion**) – puerto `9003`.
- `AppClienteApplication` (módulo **App-Cliente**) – puerto `9001`.

4. Acceder a las aplicaciones:

- Motor Camunda y API REST: `http://localhost:9000`
- App-Gestion (empleados): `http://localhost:9003`
- Portal de Cliente: `http://localhost:9001`

5. Iniciar un flujo de negocio (por ejemplo, una renovación) y seguirlo:

- Desde el engine, los timers y delegates crean tareas de renovación.
- En App-Gestion, un agente revisa y completa las tareas.
- En App-Cliente, el cliente ve sus trámites pendientes y responde a las ofertas o confirma pagos.

