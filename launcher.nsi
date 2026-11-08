!include "LogicLib.nsh"

Name "College Certificate Management System"
OutFile "target/CollegeCertificateManagementSystem.exe"
Icon "src/main/resources/images/app_icon.ico"
SilentInstall silent
RequestExecutionLevel user

VIProductVersion "1.0.0.0"
VIAddVersionKey "ProductName" "College Certificate Management System"
VIAddVersionKey "CompanyName" "College Certificate Systems"
VIAddVersionKey "LegalCopyright" "Copyright (C) 2026"
VIAddVersionKey "FileDescription" "College Certificate Management System Launcher"
VIAddVersionKey "FileVersion" "1.0.0.0"
VIAddVersionKey "ProductVersion" "1.0.0.0"

Section
  SetOutPath "$EXEDIR"
  
  ; 1. Check bundled JRE first (Guarantees zero-dependency execution)
  ${If} ${FileExists} "$EXEDIR\jre\bin\javaw.exe"
    ${If} ${FileExists} "$EXEDIR\CollegeCertGen-Pro.jar"
      Exec '"$EXEDIR\jre\bin\javaw.exe" -jar "$EXEDIR\CollegeCertGen-Pro.jar"'
      Quit
    ${EndIf}
  ${EndIf}

  ; 2. Check system Java as fallback
  ${If} ${FileExists} "$EXEDIR\CollegeCertGen-Pro.jar"
    Exec 'javaw -jar "$EXEDIR\CollegeCertGen-Pro.jar"'
    Quit
  ${EndIf}

  ; 3. If neither worked, alert user with descriptive dialog
  MessageBox MB_ICONSTOP "Unable to launch College Certificate Management System.$\n$\nCould not find CollegeCertGen-Pro.jar or bundled Java 17 runtime in:$\n$EXEDIR$\n$\nPlease run CollegeCertificateManagementSystem-Installer.exe to reinstall."
SectionEnd
