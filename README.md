# 📅 Gestor de Tareas con Cifrado AES-256

Un gestor de tareas y calendario multiusuario desarrollado en **Java 25** y **JavaFX**, diseñado con un enfoque estricto en la privacidad y la seguridad local. A diferencia de las aplicaciones tradicionales, este sistema implementa un modelo criptográfico de *Conocimiento Cero (Zero-Knowledge)* donde los datos de cada usuario se protegen de forma independiente.

---

##  Características Principales

* ** Cifrado Simétrico Dinámico (AES-256):** Los títulos, descripciones, ubicaciones y etiquetas de las tareas se encriptan en la base de datos local SQLite. La llave matemática de cifrado se genera al vuelo en la memoria RAM a partir del PIN de cada usuario y se destruye al cerrar la sesión.
* ** Arquitectura Multiusuario:** Soporte para múltiples perfiles locales aislados. Cada usuario visualiza y almacena su información de manera totalmente independiente y segura.
* ** Notificaciones en Segundo Plano:** Implementación de un motor de concurrencia optimizado mediante hilos que monitoriza el reloj del sistema en silencio y lanza alertas visuales de JavaFX cuando llega la hora de una tarea.
* ** Gestión Avanzada de Datos:** * Exportación completa de tareas a formato plano `CSV`.
    * Soporte de importación y exportación de archivos estándar de calendario `ICS` (iCalendar).
    * Sistema automatizado de copias de seguridad locales (*Backups*).

---

##  Tecnologías Utilizadas

* **Desarrollador:** Aday Linux
* **Lenguaje:** Java 25 (BellSoft Liberica JDK Full)
* **Framework Gráfico:** JavaFX 21
* **Base de Datos:** SQLite JDBC
* **Gestor de Dependencias:** Maven
* **Librerías Clave:** iCal4j (Gestión de calendarios), Commons Codec (Codificación)

---

## 💻 Instrucciones de Ejecución

### Requisitos Previos
* Maven instalado en el sistema.
* JDK 21 o superior.

### Compilar y Ejecutar en Desarrollo
Para clonar el repositorio, compilar el código fuente y lanzar la aplicación directamente desde la terminal de Linux, ejecuta:

```bash
git clone https://github.com/adr71912/calendario.git
cd GestorTareas
mvn clean package
mvn exec:java -Dexec.mainClass="app.Main"