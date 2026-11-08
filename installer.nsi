!include "MUI2.nsh"

Name "College Certificate Management System"
OutFile "CollegeCertificateManagementSystem-Installer.exe"
InstallDir "$LOCALAPPDATA\CollegeCertificateManagementSystem"
InstallDirRegKey HKCU "Software\CollegeCertificateManagementSystem" ""
RequestExecutionLevel user

SetCompressor /SOLID lzma

!define MUI_ABORTWARNING
!define MUI_ICON "src/main/resources/images/app_icon.ico"
!define MUI_UNICON "src/main/resources/images/app_icon.ico"

; Pages
!insertmacro MUI_PAGE_WELCOME
!insertmacro MUI_PAGE_DIRECTORY
!insertmacro MUI_PAGE_INSTFILES

!define MUI_FINISHPAGE_RUN "$INSTDIR\CollegeCertificateManagementSystem.exe"
!define MUI_FINISHPAGE_RUN_TEXT "Launch College Certificate Management System"
!insertmacro MUI_PAGE_FINISH

; Uninstaller Pages
!insertmacro MUI_UNPAGE_CONFIRM
!insertmacro MUI_UNPAGE_INSTFILES

; Languages
!insertmacro MUI_LANGUAGE "English"

Section "Install"
  SetOutPath "$INSTDIR"
  
  ; Core Application Binaries
  File "target/CollegeCertificateManagementSystem.exe"
  File "target/CollegeCertGen-Pro.jar"
  File "src/main/resources/images/app_icon.ico"
  File "run.bat"
  File "README_WINDOWS.txt"
  
  ; Bundled Private Windows Java 17 Runtime Environment
  SetOutPath "$INSTDIR\jre"
  File /r "windows_jre/*"
  
  SetOutPath "$INSTDIR"
  
  ; Create Uninstaller
  WriteUninstaller "$INSTDIR\Uninstall.exe"
  
  ; Desktop Shortcut (Runs native exe with embedded icon)
  CreateShortcut "$DESKTOP\College Certificate Management System.lnk" "$INSTDIR\CollegeCertificateManagementSystem.exe" "" "$INSTDIR\app_icon.ico" 0
  
  ; Start Menu Shortcuts
  CreateDirectory "$SMPROGRAMS\College Certificate Management System"
  CreateShortcut "$SMPROGRAMS\College Certificate Management System\College Certificate Management System.lnk" "$INSTDIR\CollegeCertificateManagementSystem.exe" "" "$INSTDIR\app_icon.ico" 0
  CreateShortcut "$SMPROGRAMS\College Certificate Management System\Uninstall.lnk" "$INSTDIR\Uninstall.exe" "" "$INSTDIR\Uninstall.exe" 0
  
  ; Windows Add/Remove Programs Registry Entries
  WriteRegStr HKCU "Software\CollegeCertificateManagementSystem" "" $INSTDIR
  WriteRegStr HKCU "Software\Microsoft\Windows\CurrentVersion\Uninstall\CollegeCertificateManagementSystem" "DisplayName" "College Certificate Management System"
  WriteRegStr HKCU "Software\Microsoft\Windows\CurrentVersion\Uninstall\CollegeCertificateManagementSystem" "UninstallString" '"$INSTDIR\Uninstall.exe"'
  WriteRegStr HKCU "Software\Microsoft\Windows\CurrentVersion\Uninstall\CollegeCertificateManagementSystem" "DisplayIcon" '"$INSTDIR\app_icon.ico"'
  WriteRegStr HKCU "Software\Microsoft\Windows\CurrentVersion\Uninstall\CollegeCertificateManagementSystem" "Publisher" "College Certificate Systems"
  WriteRegStr HKCU "Software\Microsoft\Windows\CurrentVersion\Uninstall\CollegeCertificateManagementSystem" "DisplayVersion" "1.0.0"
SectionEnd

Section "Uninstall"
  Delete "$DESKTOP\College Certificate Management System.lnk"
  Delete "$SMPROGRAMS\College Certificate Management System\College Certificate Management System.lnk"
  Delete "$SMPROGRAMS\College Certificate Management System\Uninstall.lnk"
  RMDir "$SMPROGRAMS\College Certificate Management System"
  
  RMDir /r "$INSTDIR\jre"
  Delete "$INSTDIR\CollegeCertificateManagementSystem.exe"
  Delete "$INSTDIR\CollegeCertGen-Pro.jar"
  Delete "$INSTDIR\app_icon.ico"
  Delete "$INSTDIR\run.bat"
  Delete "$INSTDIR\README_WINDOWS.txt"
  Delete "$INSTDIR\Uninstall.exe"
  RMDir "$INSTDIR"
  
  DeleteRegKey HKCU "Software\Microsoft\Windows\CurrentVersion\Uninstall\CollegeCertificateManagementSystem"
  DeleteRegKey HKCU "Software\CollegeCertificateManagementSystem"
SectionEnd
