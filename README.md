
# 📅 Task Manager with AES-256 Encryption

A multi-user task manager and calendar developed in **Java** and **JavaFX**, designed with a strict focus on local privacy and security. Unlike traditional applications, this system implements a *Zero-Knowledge* cryptographic model where each user's data is protected independently.

---

## 🚀 Core Features

* **Dynamic Symmetric Encryption (AES-256):** Task titles, descriptions, locations, and labels are encrypted in the local SQLite database. The mathematical encryption key is generated on the fly in RAM from each user's PIN and is destroyed upon logout.
* **Multi-User Architecture:** Support for multiple isolated local profiles. Each user views and stores their information completely independently and securely.
* **Background Notifications:** Implementation of an optimized concurrency engine using threads that silently monitors the system clock and triggers visual JavaFX alerts when a task is due.
* **Advanced Data Management:** 
  * Full task export to plain `CSV` format.
  * Import and export support for standard calendar `ICS` (iCalendar) files.
  * Automated local backup system with time-based versioning.

---

## 🛠️ Technologies Used

* **Developer:** Aday Linux
* **Language:** Java 21 / 25 (BellSoft Liberica JDK Full)
* **Graphical Framework:** JavaFX 26
* **Database:** SQLite JDBC
* **Dependency Manager:** Maven
* **Key Libraries:** iCal4j (Calendar management)

---

## 💻 Execution Instructions

### Prerequisites
* Maven installed on your system.
* JDK 21 or higher (Liberica JDK Full or any JDK that includes JavaFX modules is highly recommended).

### Contributions & Feedback
* This is an open project in constant evolution. The code is available to be modified, adapted, and improved according to your needs.
* If you detect any bugs, vulnerabilities, or have proposals to optimize the performance of the manager, all kinds of fixes and suggestions are warmly welcome. You can collaborate by opening an Issue or submitting a Pull Request directly to the repository.

### Compile and Run in Development
To clone the repository, compile the source code, and launch the application directly from your terminal, run:

```bash
git clone [https://github.com/adrt1912/calendario.git](https://github.com/adrt1912/calendario.git)
cd calendario
mvn clean package
mvn exec:java -Dexec.mainClass="app.Main"
```


# 📅 Gestor de Tareas con Cifrado AES-256

Un gestor de tareas y calendario multiusuario desarrollado en **Java** y **JavaFX**, diseñado con un enfoque estricto en la privacidad y la seguridad local. A diferencia de las aplicaciones tradicionales, este sistema implementa un modelo criptográfico de *Conocimiento Cero (Zero-Knowledge)* donde los datos de cada usuario se protegen de forma independiente.

---

## 🚀 Características Principales

* **Cifrado Simétrico Dinámico (AES-256):** Los títulos, descripciones, ubicaciones y etiquetas de las tareas se encriptan en la base de datos local SQLite. La llave matemática de cifrado se genera al vuelo en la memoria RAM a partir del PIN de cada usuario y se destruye al cerrar la sesión.
* **Arquitectura Multiusuario:** Soporte para múltiples perfiles locales aislados. Cada usuario visualiza y almacena su información de manera totalmente independiente y segura.
* **Notificaciones en Segundo Plano:** Implementación de un motor de concurrencia optimizado mediante hilos que monitoriza el reloj del sistema en silencio y lanza alertas visuales de JavaFX cuando llega la hora de una tarea.
* **Gestión Avanzada de Datos:** * Exportación completa de tareas a formato plano `CSV`.
  * Soporte de importación y exportación de archivos estándar de calendario `ICS` (iCalendar).
  * Sistema automatizado de copias de seguridad locales (*Backups*) con versionado temporal.

---

## 🛠️ Tecnologías Utilizadas

* **Desarrollador:** Aday Linux
* **Lenguaje:** Java 21 / 25 (BellSoft Liberica JDK Full)
* **Framework Gráfico:** JavaFX 26
* **Base de Datos:** SQLite JDBC
* **Gestor de Dependencias:** Maven
* **Librerías Clave:** iCal4j (Gestión de calendarios)

---

## 💻 Instrucciones de Ejecución

### Requisitos Previos
* Maven instalado en el sistema.
* JDK 21 o superior (Se recomienda Liberica JDK Full o un JDK que incluya los módulos de JavaFX).

### Mensaje
* Este es un proyecto abierto y en constante evolución. El código está disponible para ser modificado, adaptado y mejorado según tus necesidades.
* Si detectas algún error, vulnerabilidad o tienes propuestas para optimizar el funcionamiento del gestor, 
se aceptan con gusto todo tipo de correcciones y sugerencias. 
Puedes colaborar abriendo un Issue o enviando un Pull Request directamente al repositorio.

### Compilar y Ejecutar en Desarrollo
Para clonar el repositorio, compilar el código fuente y lanzar la aplicación directamente desde la terminal, ejecuta:

```bash
git clone https://github.com/adrt1912/calendario.git
cd calendario
mvn clean package
mvn exec:java -Dexec.mainClass="app.Main"
