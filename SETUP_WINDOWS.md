# Recycling Quest - Windows Setup Guide

This guide will help you set up and run the Recycling Quest project on Windows.

## Prerequisites

You need to install Java and Maven on your Windows machine.

### Step 1: Install Java JDK 17 or Higher

1. **Download Java JDK**:
   - Go to [Oracle JDK Downloads](https://www.oracle.com/java/technologies/downloads/) or [Adoptium (OpenJDK)](https://adoptium.net/)
   - Download JDK 17 or higher for Windows (x64 Installer)
   - Run the installer and follow the installation wizard

2. **Set JAVA_HOME Environment Variable**:
   - Right-click on "This PC" or "My Computer" → Properties
   - Click "Advanced system settings" → "Environment Variables"
   - Under "System variables", click "New"
   - Variable name: `JAVA_HOME`
   - Variable value: Path to your JDK installation (e.g., `C:\Program Files\Java\jdk-17`)
   - Click OK

3. **Add Java to PATH**:
   - In "Environment Variables", find "Path" under "System variables"
   - Click "Edit" → "New"
   - Add: `%JAVA_HOME%\bin`
   - Click OK on all dialogs

4. **Verify Java Installation**:
   - Open Command Prompt (cmd) or PowerShell
   - Run: `java -version`
   - You should see Java version 17 or higher

### Step 2: Install Maven

1. **Download Maven**:
   - Go to [Apache Maven Downloads](https://maven.apache.org/download.cgi)
   - Download the "Binary zip archive" (e.g., `apache-maven-3.9.x-bin.zip`)
   - Extract the zip file to a location like `C:\Program Files\Apache\maven`

2. **Set MAVEN_HOME Environment Variable**:
   - Right-click on "This PC" → Properties → Advanced system settings → Environment Variables
   - Under "System variables", click "New"
   - Variable name: `MAVEN_HOME`
   - Variable value: Path to Maven folder (e.g., `C:\Program Files\Apache\maven`)
   - Click OK

3. **Add Maven to PATH**:
   - In "Environment Variables", find "Path" under "System variables"
   - Click "Edit" → "New"
   - Add: `%MAVEN_HOME%\bin`
   - Click OK on all dialogs

4. **Verify Maven Installation**:
   - Open a NEW Command Prompt or PowerShell (important: close old ones)
   - Run: `mvn -version`
   - You should see Maven version and Java version information

## Running the Project

### Step 3: Extract and Navigate to Project

1. Extract the zip file to a folder (e.g., `C:\Users\YourName\Documents\java_main_proj`)
2. Open Command Prompt or PowerShell
3. Navigate to the project folder:
   ```cmd
   cd C:\Users\YourName\Documents\java_main_proj
   ```

### Step 4: Build the Project

Run the following command to download all dependencies and build the project:

```cmd
mvn clean package
```

**What this does:**
- Downloads JavaFX libraries (version 21.0.3) from Maven Central
- Downloads SQLite JDBC driver
- Compiles all Java source files
- Creates the application package

**Note:** First time will take a few minutes as Maven downloads all dependencies.

### Step 5: Run the Application

```cmd
mvn javafx:run
```

The Recycling Quest application window should open!

## Troubleshooting

### Issue: "java is not recognized as an internal or external command"
- **Solution**: Java is not in your PATH. Go back to Step 1 and ensure JAVA_HOME and PATH are set correctly. Close and reopen Command Prompt.

### Issue: "mvn is not recognized as an internal or external command"
- **Solution**: Maven is not in your PATH. Go back to Step 2 and ensure MAVEN_HOME and PATH are set correctly. Close and reopen Command Prompt.

### Issue: "JAVA_HOME is set to an invalid directory"
- **Solution**: Check that JAVA_HOME points to the JDK installation folder (not the JRE). The path should contain a `bin` folder with `java.exe` and `javac.exe`.

### Issue: Build fails with "Source option 17 is no longer supported"
- **Solution**: You have an older Java version. Install JDK 17 or higher.

### Issue: Application doesn't start or shows module errors
- **Solution**: Run `mvn clean install` to rebuild, then try `mvn javafx:run` again.

## What You DON'T Need to Do

❌ **You DON'T need to**:
- Manually download JavaFX SDK
- Set PATH_TO_FX environment variable
- Configure module paths manually
- Install any IDE (though IntelliJ IDEA or Eclipse is recommended for development)

✅ **Maven handles everything automatically**:
- Downloads JavaFX libraries
- Configures module paths
- Sets up the runtime environment

## Optional: Using an IDE (Recommended for Development)

### IntelliJ IDEA (Recommended)

1. Download [IntelliJ IDEA Community Edition](https://www.jetbrains.com/idea/download/) (free)
2. Install and open IntelliJ IDEA
3. Click "Open" and select the project folder
4. IntelliJ will automatically detect the Maven project and import dependencies
5. Wait for indexing to complete
6. Right-click on `pom.xml` → Maven → Reload Project
7. To run: Open Maven panel (right side) → recycling-quest → Plugins → javafx → javafx:run

### VS Code

1. Install [Visual Studio Code](https://code.visualstudio.com/)
2. Install extensions: "Extension Pack for Java" and "Maven for Java"
3. Open the project folder
4. VS Code will detect Maven and import dependencies
5. Use the integrated terminal to run `mvn javafx:run`

## Project Structure

```
java_main_proj/
├── pom.xml                    # Maven configuration (dependencies, plugins)
├── src/
│   └── main/
│       ├── java/              # Java source code
│       └── resources/         # CSS, images, etc.
├── data/                      # SQLite database (created on first run)
└── target/                    # Compiled classes (generated by Maven)
```

## Need Help?

If you encounter any issues not covered here, check:
1. Java version: `java -version` (must be 17+)
2. Maven version: `mvn -version`
3. Try cleaning and rebuilding: `mvn clean install`
4. Check that you're in the correct project directory

---

**Quick Start Summary for Windows:**
1. Install Java JDK 17+ and set JAVA_HOME
2. Install Maven and add to PATH
3. Extract project zip
4. Open Command Prompt in project folder
5. Run: `mvn clean package`
6. Run: `mvn javafx:run`
