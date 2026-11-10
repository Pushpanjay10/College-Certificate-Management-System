#define UNICODE
#define _UNICODE
#include <windows.h>
#include <stdio.h>
#include <wchar.h>

static BOOL file_exists(const wchar_t *path) {
    DWORD attr = GetFileAttributesW(path);
    return (attr != INVALID_FILE_ATTRIBUTES && !(attr & FILE_ATTRIBUTE_DIRECTORY));
}

static void log_message(const wchar_t *dir, const wchar_t *msg) {
    wchar_t logPath[MAX_PATH];
    swprintf(logPath, MAX_PATH, L"%s\\launcher_debug.log", dir);
    FILE *f = _wfopen(logPath, L"a, ccs=UTF-8");
    if (f) {
        SYSTEMTIME st;
        GetLocalTime(&st);
        fwprintf(f, L"[%04d-%02d-%02d %02d:%02d:%02d] %s\n",
                 st.wYear, st.wMonth, st.wDay, st.wHour, st.wMinute, st.wSecond, msg);
        fclose(f);
    }
}

int WINAPI wWinMain(HINSTANCE hInstance, HINSTANCE hPrevInstance, LPWSTR lpCmdLine, int nCmdShow) {
    wchar_t exePath[MAX_PATH];
    if (GetModuleFileNameW(NULL, exePath, MAX_PATH) == 0) {
        MessageBoxW(NULL, L"Failed to determine application path.", L"College Certificate Management System", MB_ICONERROR | MB_OK);
        return 1;
    }

    // Strip executable name to obtain containing directory
    wchar_t dirPath[MAX_PATH];
    wcscpy(dirPath, exePath);
    wchar_t *lastSlash = wcsrchr(dirPath, L'\\');
    if (lastSlash) {
        *lastSlash = L'\0';
    } else {
        wcscpy(dirPath, L".");
    }

    // Always set working directory to the application directory
    SetCurrentDirectoryW(dirPath);

    log_message(dirPath, L"=== Launcher started ===");

    // 1. Locate CollegeCertGen-Pro.jar
    wchar_t jarPath[MAX_PATH];
    swprintf(jarPath, MAX_PATH, L"%s\\CollegeCertGen-Pro.jar", dirPath);
    if (!file_exists(jarPath)) {
        swprintf(jarPath, MAX_PATH, L"%s\\target\\CollegeCertGen-Pro.jar", dirPath);
        if (!file_exists(jarPath)) {
            wchar_t errMsg[1024];
            swprintf(errMsg, 1024,
                     L"Application core file not found:\n\n%s\\CollegeCertGen-Pro.jar\n\nPlease reinstall using the installer.",
                     dirPath);
            log_message(dirPath, errMsg);
            MessageBoxW(NULL, errMsg, L"College Certificate Management System - Missing Files", MB_ICONERROR | MB_OK);
            return 1;
        }
    }

    // 2. Locate Java Runtime (Bundled JRE preferred, then system java)
    wchar_t javaExe[MAX_PATH];
    BOOL isBundled = FALSE;

    // Check bundled private JRE javaw.exe
    swprintf(javaExe, MAX_PATH, L"%s\\jre\\bin\\javaw.exe", dirPath);
    if (file_exists(javaExe)) {
        isBundled = TRUE;
    } else {
        // Check bundled private JRE java.exe
        swprintf(javaExe, MAX_PATH, L"%s\\jre\\bin\\java.exe", dirPath);
        if (file_exists(javaExe)) {
            isBundled = TRUE;
        } else {
            // Check system javaw in PATH
            wcscpy(javaExe, L"javaw.exe");
        }
    }

    // 3. Build command line
    wchar_t cmdLine[2048];
    if (isBundled) {
        swprintf(cmdLine, 2048, L"\"%s\" -jar \"%s\"", javaExe, jarPath);
    } else {
        swprintf(cmdLine, 2048, L"javaw -jar \"%s\"", jarPath);
    }

    wchar_t logBuf[2048];
    swprintf(logBuf, 2048, L"Executing command: %s", cmdLine);
    log_message(dirPath, logBuf);

    STARTUPINFOW si;
    PROCESS_INFORMATION pi;
    ZeroMemory(&si, sizeof(si));
    si.cb = sizeof(si);
    ZeroMemory(&pi, sizeof(pi));

    BOOL success = CreateProcessW(
        NULL,              // lpApplicationName
        cmdLine,           // lpCommandLine
        NULL,              // lpProcessAttributes
        NULL,              // lpThreadAttributes
        FALSE,             // bInheritHandles
        CREATE_NO_WINDOW,  // dwCreationFlags (silent background execution)
        NULL,              // lpEnvironment
        dirPath,           // lpCurrentDirectory
        &si,               // lpStartupInfo
        &pi                // lpProcessInformation
    );

    if (!success) {
        DWORD err = GetLastError();
        wchar_t errMsg[1024];
        swprintf(errMsg, 1024,
                 L"Unable to launch application process (Windows Error Code: %lu).\n\nCommand attempted:\n%s\n\nPlease ensure Java 17+ is installed or run run.bat inside the folder.",
                 err, cmdLine);
        log_message(dirPath, errMsg);
        MessageBoxW(NULL, errMsg, L"College Certificate Management System - Launch Error", MB_ICONERROR | MB_OK);
        return 1;
    }

    log_message(dirPath, L"Application process started successfully.");
    CloseHandle(pi.hProcess);
    CloseHandle(pi.hThread);
    return 0;
}

