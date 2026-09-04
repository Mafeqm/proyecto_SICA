# SICA - Sistema Integrado de Control de Acceso "Zona Acme"

## 📖 Descripción del Proyecto
El Complejo Empresarial "Zona Acme" requería modernizar su sistema de seguridad. SICA es una solución de software robusta, desarrollada en Java, diseñada para automatizar y asegurar el proceso de entrada y salida del complejo. Elimina el uso de bitácoras manuales, previene brechas de seguridad mediante un Control de Acceso Basado en Roles (RBAC) y garantiza la trazabilidad absoluta de las operaciones a través de una bitácora de auditoría inmutable.

## 🏗️ Arquitectura y Tecnologías
El sistema está construido utilizando **Java 17+** (sin frameworks como Spring) y **JDBC** puro para la persistencia en **MySQL**. Sigue estrictamente la arquitectura **MVC (Modelo-Vista-Controlador)**:
*   **Modelo:** Entidades (POJOs) y capa de acceso a datos (DAO).
*   **Vista:** Interfaz gráfica construida en código JavaFX.
*   **Controlador:** Intermediarios que procesan las acciones del usuario e interactúan con los servicios.

## 🧠 Decisiones de Diseño (Patrones y SOLID)
Para garantizar un código mantenible, escalable y limpio, se aplicaron rigurosamente los principios SOLID y los siguientes patrones de diseño:

1.  **Singleton (Creacional):** Implementado en la clase `ConexionDB` para garantizar una única instancia de la conexión a MySQL durante todo el ciclo de vida de la aplicación, optimizando los recursos.
2.  **DAO - Data Access Object (Estructural):** Se separó completamente la lógica de base de datos (consultas SQL en MySQL) de la lógica de negocio mediante interfaces (ej. `VisitaDAO`). Esto cumple con el **Principio de Responsabilidad Única (SRP)**.
3.  **Proxy (Estructural):** Utilizado para el módulo de Seguridad RBAC. `SeguridadServiceProxy` intercepta las peticiones a los servicios y consulta la base de datos para verificar si el usuario tiene el permiso granular exacto antes de permitir la ejecución, lanzando una `AccesoDenegadoException` si no es así.
4.  **Strategy (De Comportamiento):** Implementado para manejar los diferentes flujos de ingreso (Invitado Pre-registrado, No Anunciado, Salida Olvidada) mediante la interfaz `EstrategiaAcceso`. Esto cumple con el **Principio Abierto/Cerrado (OCP)**, permitiendo agregar nuevos tipos de acceso en el futuro sin alterar el servicio principal.
5.  **Observer (De Comportamiento):** Aplicado para la notificación en tiempo real. Cuando un Guarda registra un "Invitado No Anunciado", el sistema notifica automáticamente a la vista del Funcionario correspondiente para que apruebe o rechace la solicitud.
6.  **Decorator (Estructural):** Implementado en `AuditoriaVisitaDecorator` para envolver los servicios principales. Automáticamente captura las acciones exitosas y las inserta en la tabla `bitacora_auditoria`, manteniendo la trazabilidad sin ensuciar la lógica de negocio central.

*Nota: La inyección de dependencias a través de constructores en los controladores garantiza el cumplimiento del **Principio de Inversión de Dependencias (DIP)**.*

## 🗄️ Modelo de Base de Datos
La base de datos relacional (MySQL) está normalizada e incluye lookup tables, RBAC y auditoría.
> **[¡Importante!]** *Inserta aquí la imagen de tu Diagrama Entidad-Relación exportada desde MySQL Workbench o tu código de Mermaid.* 
> `![Diagrama ER](./docs/diagrama_er.png)`

## 🚀 Instrucciones de Instalación y Ejecución

### Prerrequisitos
*   Java Development Kit (JDK) 17 o superior.
*   MySQL Server y MySQL Workbench.
*   IDE (Visual Studio Code, IntelliJ, Eclipse) con soporte para JavaFX.

### Pasos de Instalación
1.  **Clonar el repositorio:**
    ```bash
    git clone [https://github.com/TU_USUARIO/SICA-Zona-Acme.git](https://github.com/TU_USUARIO/SICA-Zona-Acme.git)
    cd SICA-Zona-Acme
    ```
2.  **Configurar la Base de Datos:**
    *   Abre MySQL y ejecuta el archivo `scripts/schema.sql` para crear la base de datos `sica_db` y las tablas.
    *   Ejecuta el archivo `scripts/data.sql` para poblar el sistema con los datos, estados, roles y usuarios de prueba.
3.  **Configurar credenciales (JDBC):**
    *   Verifica que en la clase `ConexionDB` los parámetros coincidan con tu usuario de base de datos local (por defecto usa `sica_user` / `Sica2026*`).
4.  **Ejecución:**
    *   Compila y ejecuta la clase `MainApp.java` para lanzar la interfaz de JavaFX.

## 🔐 Guía de Uso (Credenciales de Prueba)
El script `data.sql` incluye los siguientes usuarios de prueba (la contraseña para todos es `1234`):

| Rol | Correo (Usuario) | Funciones Principales |
| :--- | :--- | :--- |
| **Superusuario** | `admin@zonaacme.com` | Acceso total al sistema y auditoría. |
| **Guarda de Seguridad** | `carlos@seguridad.com` | Búsqueda de personas y registro de ingresos/salidas. |
| **Funcionario Empresa** | `ana@empresa.com` | Recepción de notificaciones y aprobación de visitas. |

---
**Autor:** María Fernanda Quiñonez Moreno 
*Proyecto de Desarrollo de Software y Bases de Datos - 2026*
