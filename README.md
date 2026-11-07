# 🎓 College Certificate Management System

A production-grade, multi-institution desktop application for university & college certificate generation, student credential verification, and automated batch issuance.

Built with **Java 17**, **FlatLaf (Modern macOS & Windows 11 UI/UX)**, **OpenPDF (High-Resolution Vector PDF Export)**, **ZXing (Scannable QR Codes)**, and **SQLite / Multi-Database Support**.

---

## 🌟 Key Features

1. **Multi-Institution & Account Management**:
   - Create accounts for any college or university with custom college name, university affiliation, and emblem/logo.
   - Built-in saved accounts switcher on login screen with local credentials persistence.
   - Complete data isolation per institution (students, classes, departments, and certificates remain completely separate).

2. **Real-Time Certificate Studio (4 Dedicated Categories)**:
   - **Academic Certificate**: Classic Imperial Blue & Pure Gold double borders, baroque corner flourishes, and academic seal medallion.
   - **Cultural Program Certificate**: Royal Violet & Vibrant Magenta scroll borders with cultural fest ribbons.
   - **Sports Certificate**: Athletic Crimson & Olympic Gold with track chevrons and golden trophy medallion.
   - **Tech Fest Certificate**: Cybernetic Slate & Electric Cyan with circuit matrix accents and digital badge.

3. **QR Code Verification**:
   - Each certificate includes a real, scannable QR verification code in the bottom corner.
   - Scanning the QR code displays instant authentic academic verification details (student name, roll no, registration number, course, degree, and issue date).

4. **Zero-Overlap Vector PDF & Image Exports**:
   - Publication-ready A4 landscape vector PDF export via OpenPDF with exact symmetry:
     - Left Signatory (e.g. Head of Department)
     - Center Golden Official Seal Medallion
     - Right Signatory (e.g. Principal)
     - Bottom-left Scannable QR Code
     - Bottom verification metadata line with ample margin above border lines.
   - 2x High-resolution PNG image export suitable for photo studio printing.
   - **⚡ Batch Issuance**: Generate and export individual certificates for an entire class in seconds.

5. **Student & Course Management**:
   - Comprehensive student directory with roll number, university registration, department, and semester.
   - **CSV Import / Export**: One-click bulk enrollment from attendance sheets or CSV files.
   - Class and semester organization (`BCA-1`, `BCA-2`, `BCA-3`, `MAC`, `MBA`, `BBA`, `BA`, `MA`, etc.).

6. **Native Windows Executable & Installer**:
   - Bundled native Windows executable: `CollegeCertificateManagementSystem.exe`
   - One-click installer batch script (`install_windows.bat`) that creates a Desktop shortcut with the application icon.
   - Inno Setup configuration (`windows_installer.iss`) for standalone Windows Setup Wizard.

---

## 🚀 Quick Start

### Windows
- **Direct Run**: Double-click `CollegeCertificateManagementSystem.exe`
- **1-Click Installer**: Run `install_windows.bat` to create a Desktop shortcut with icon.
- **Batch Launcher**: Double-click `run.bat`

### macOS / Linux
Run the launch script:
```bash
./run.sh
```

Or run via Java:
```bash
java -jar target/CollegeCertGen-Pro.jar
```

---

## 🛠 Building from Source

Requirements:
- JDK 17 or higher
- Apache Maven 3.8+

```bash
mvn clean package -DskipTests
```

This compiles all classes, builds the fat JAR (`target/CollegeCertGen-Pro.jar`), and creates the native Windows executable (`target/CollegeCertificateManagementSystem.exe`).

---

## 📄 License
This project is open-source and available under the MIT License.
