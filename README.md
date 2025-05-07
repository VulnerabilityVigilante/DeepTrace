# DeepTrace Anti-Virus

DeepTrace Anti-Virus is an Android application robustly designed to scan your device for suspicious files and potential threats. It empowers users to identify and remove unwanted files, view scan history, and customize settings for a personalized and secure experience.

## Contributors

-   **Jianyu Huang** ([@jianeral](https://github.com/jianeral)) - UI/UX Design, Settings Management
-   **Javier Rosales** ([@javii820](https://github.com/javii820)) - Testing, Documentation
-   **Sergio Yazdi** ([@VulnerabilityVigilante](https://github.com/VulnerabilityVigilante)) - File Scanning Logic, Project and Development Lead
## Features

-   **File Scanning:** Scans the device's storage for suspicious files based on a predefined virus database (loaded from `virus_db.csv` in assets) and basic heuristics (e.g., suspicious file extensions, patterns like `.pdf.exe`).
-   **Threat Detection:** Identifies potential threats and lists them clearly for the user to review and act upon.
-   **File Deletion:** Allows users to confidently select and delete identified suspicious files.
-   **Scan History:** Keeps a detailed log of the last five scans, including the precise date and time of each scan.
-   **Customizable Settings:**
    -   **Change Font Size:** Allows users to make the font size larger, enhancing readability.
    -   **Dim Light Mode:** Enables a dark theme for the application interface, enhancing usability in low-light conditions.
    -   **View Last Scan Time:** Provides quick access to the timestamp of the most recent scan.
    -   **Access Scan History:** Allows users to review past scan activities.
-   **Demo Virus Generation & Storage (for Testing):**
    -   Upon startup (after permissions are granted), the application's `MainActivity` calls a `setupDemoFiles()` method.
    -   This method creates a `/tmp/` directory within the app's specific external storage (`getExternalFilesDir(null)`).
    -   **Demo Files Created:**
        -   In `/tmp/`: `csv_test_virus.bin` (CSV database detection).
        -   In public `Downloads` directory: `downloaded_malware.apk.exe`.
        -   In public `Documents` directory: `important_doc.docx.vbs`.
        -   In public `Pictures` directory: `family_photo.jpg.sh`.
        -   In public `Movies` directory: `free_movie.mp4.bat`.
        -   In public `Music` directory: `latest_hit.mp3.js`.
    -   Each demo file contains the text: "This is a demo suspicious file named [fileName]".
    -   These files are created to test the scanning functionality across different common directories and detection methods.
-   **User-Friendly Interface:** Provides a clear and intuitive interface for seamless navigation and straightforward operation.

## Technologies Used

-   **Java:** The core programming language for this robust Android application.
-   **XML:** Utilized for meticulously designing the application's user interface layouts.
-   **Android SDK:** The fundamental software development kit leveraged for native Android application development.

## Project Structure

The project is meticulously organized into several key packages and components:

-   **`activities`**: Contains the Java classes responsible for managing the application's different screens (Activities).
    -   `MainActivity.java`: The main entry point. Manages permissions, calls `setupDemoFiles()` for creating test viruses, initializes UI, and handles navigation to scan or settings.
    -   `ResultActivity.java`: Displays the comprehensive results of a file scan, listing suspicious files and enabling users to delete them.
    -   `HistoryActivity.java`: Shows a clear list of past scan activities.
    -   `SettingsActivity.java`: Allows users to configure application settings such as dark mode and view scan history.
-   **`adapters`**: Includes classes like `SuspiciousFileAdapter.java`, which are expertly used to display lists of items, such as suspicious files, in a `ListView`.
-   **`models`**: Contains the data models and core logic for the application's functionalities.
    -   `ActivityLogger.java`, `HistoryLogger.java`, `LastLogger.java`: Involved in meticulously logging scan activities and timestamps.
    -   `CsvPathScanner.java`, `FileScanner.java`: Responsible for the efficient file scanning mechanism. The `FileScanner` handles heuristic checks, while `VirusDatabase` (used in conjunction) handles CSV-based matching.
    -   `FileRemover.java`: Handles the secure deletion of files.
    -   `Settings.java`: Manages application settings.
    -   `VirusDatabase.java`: Manages the database of virus signatures loaded from an internal `virus_db.csv` file (copied from assets). This is used for pattern matching against known threats.
-   **`layout (res/layout)`**: Contains the XML files that define the user interface of each activity and list item.
    -   `activity_main.xml`: Layout for the main screen.
    -   `activity_result.xml`: Layout for displaying scan results.
    -   `activity_history.xml`: Layout for the scan history screen.
    -   `activity_settings.xml`: Layout for the settings screen.
    -   `item_suspicious_file.xml`: Layout for individual items in the suspicious files list.

## How to Use (Assumed Flow)

1.  **Launch the App:** Open the DeepTrace Anti-Virus application. The main screen will prominently display options to "Start Scan" and access "Settings". Demo viruses are set up in the background once permissions are granted.
2.  **Start Scan:** Tap the "Start Scan" button to initiate a comprehensive scan of your device's files (including directories where demo viruses were placed).
3.  **View Results:** Once the scan is complete, the `ResultActivity` will display a list of any suspicious files found (which should include the demo viruses if present). The screen will clearly indicate if threats are found, potentially with an image of a hazard logo.
4.  **Delete Files (Optional):** If suspicious files are found, you can select them from the list and tap a "DELETE" button to remove them. The delete button will only become visible if suspicious files are present.
5.  **Check Settings:**
    -   Navigate to "Settings" from the main screen.
    -   Change the font size for better readability.
    -   Enable or disable "Dim Light Mode".
    -   View the "Last Time Scanned".
    -   Access "Scan History" to see details of the last five scans.
6.  **View Scan History:** Access the scan history from settings or potentially another part of the app to see a list of past scan dates.

## Known Issues

-   Scanning very large directories may take a significant amount of time and could appear unresponsive.
-   On some older Android versions (e.g., Android 6.0), the Dim Light Mode might not render perfectly in all dialogs.
-   The app may occasionally report false positives for files that have unusual naming conventions similar to heuristic patterns.
-   Background music might not resume correctly if a phone call is received during a scan.

## Future Scope / To-Do (Potential)

-   Implement real-time protection for continuous security.
-   Allow users to define custom scan paths for targeted scanning.
-   Implement a mechanism to update the virus database (`virus_db.csv`) regularly, perhaps from a remote server.
-   Provide more detailed and contextual information about detected threats (e.g., threat type, risk level).
-   Add options for quarantining files as an alternative to direct deletion.
-   Enhance the UI/UX with more detailed progress indicators during scans and refined visual elements.
