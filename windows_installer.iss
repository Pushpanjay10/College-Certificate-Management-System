; Script generated for Inno Setup Compiler
; College Certificate Management System Windows Installer

#define MyAppName "College Certificate Management System"
#define MyAppVersion "1.0.0"
#define MyAppPublisher "College Certificate Systems"
#define MyAppExeName "CollegeCertificateManagementSystem.exe"

[Setup]
AppId={{C78F599B-4D2A-4C2E-8E5A-7A5722E1A4D0}
AppName={#MyAppName}
AppVersion={#MyAppVersion}
AppPublisher={#MyAppPublisher}
DefaultDirName={autopf}\{#MyAppName}
DefaultGroupName={#MyAppName}
AllowNoIcons=yes
OutputDir=dist
OutputBaseFilename=CollegeCertificateManagementSystem-Setup
SetupIconFile=src\main\resources\images\app_icon.ico
Compression=lzma
SolidCompression=yes
WizardStyle=modern

[Languages]
Name: "english"; MessagesFile: "compiler:Default.isl"

[Tasks]
Name: "desktopicon"; Description: "{cm:CreateDesktopIcon}"; GroupDescription: "{cm:AdditionalIcons}"; Flags: unchecked

[Files]
Source: "target\{#MyAppExeName}"; DestDir: "{app}"; Flags: ignoreversion
Source: "target\CollegeCertGen-Pro.jar"; DestDir: "{app}"; Flags: ignoreversion
Source: "src\main\resources\images\app_icon.ico"; DestDir: "{app}"; Flags: ignoreversion
Source: "run.bat"; DestDir: "{app}"; Flags: ignoreversion

[Icons]
Name: "{group}\{#MyAppName}"; Filename: "{app}\{#MyAppExeName}"; IconFilename: "{app}\app_icon.ico"
Name: "{group}\{cm:UninstallProgram,{#MyAppName}}"; Filename: "{uninstallexe}"
Name: "{autodesktop}\{#MyAppName}"; Filename: "{app}\{#MyAppExeName}"; IconFilename: "{app}\app_icon.ico"; Tasks: desktopicon

[Run]
Filename: "{app}\{#MyAppExeName}"; Description: "{cm:LaunchProgram,{#StringChange(MyAppName, '&', '&&')}}"; Flags: nowait postinstall skipifsilent

