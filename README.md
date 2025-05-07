# DeepTrace Anti-Virus

DeepTrace Anti-Virus is an Android application designed to scan your device for suspicious files and potential threats. It allows users to identify and remove unwanted files, view scan history, and customize settings for a personalized experience.

## Features

-   **File Scanning:** Scans the device's storage for suspicious files based on a predefined virus database and basic heuristics.
-   **Threat Detection:** Identifies potential threats and lists them for the user to review.
-   **File Deletion:** Allows users to select and delete identified suspicious files.
-   **Scan History:** Keeps a log of the last five scans, including the date and time of each scan.
-   **Customizable Settings:**
    -   Dim Light Mode: Enables a dark theme for the application interface.
    -   View the last scan time.
    -   Access scan history.
-   **User-Friendly Interface:** Provides a clear and intuitive interface for easy navigation and operation.

## Technologies Used

-   **Java:** The core programming language for the Android application.
-   **XML:** Used for designing the application's user interface layouts.
-   **Android SDK:** Utilized for developing the native Android application.

## Project Structure

The project is organized into several packages and components:

-   **`activities`**: Contains the Java classes responsible for managing the application's different screens (Activities) such as `MainActivity`, `ResultActivity`, `HistoryActivity`, and `SettingsActivity`.
    -   `MainActivity.java`: The main entry point of the application, providing options to start a scan or go to settings.
    -   `ResultActivity.java`: Displays the results of a file scan, listing suspicious files and allowing users to delete them.
    -   `HistoryActivity.java`: Shows a list of past scan activities.
    -   `SettingsActivity.java`: Allows users to configure application settings like dark mode and view scan history.
-   **`adapters`**: Includes classes like `SuspiciousFileAdapter.java` which are used to display lists of items, such as suspicious files in a ListView.
-   **`models`**: Contains the data models and logic for the application's core functionalities.
    -   `ActivityLogger.java`, `HistoryLogger.java`, `LastLogger.java`: Involved in logging scan activities and timestamps.
    -   `CsvPathScanner.java`, `FileScanner.java`: Responsible for the file scanning mechanism.
    -   `FileRemover.java`: Handles the deletion of files.
    -   `Settings.java`: Manages application settings.
    -   `VirusDatabase.java`: Manages the database of virus signatures or patterns used for threat detection.
-   **`layout (res/layout)`**: Contains the XML files that define the user interface of each activity and list item.
    -   `activity_main.xml`: Layout for the main screen.
    -   `activity_result.xml`: Layout for displaying scan results.
    -   `activity_history.xml`: Layout for the scan history screen.
    -   `activity_settings.xml`: Layout for the settings screen.
    -   `item_suspicious_file.xml`: Layout for individual items in the suspicious files list.

## How to Use (Assumed Flow)

1.  **Launch the App:** Open the DeepTrace Anti-Virus application. The main screen will display options to "Start Scan" and access "Settings".
2.  **Start Scan:** Tap the "Start Scan" button to initiate a scan of your device's files.
3.  **View Results:** Once the scan is complete, the `ResultActivity` will display a list of any suspicious files found. The screen will indicate if threats are found, potentially with an image of a hazard logo.
4.  **Delete Files (Optional):** If suspicious files are found, you can select them from the list and tap a "DELETE" button to remove them. The delete button will only become visible if suspicious files are present.
5.  **Check Settings:**
    -   Navigate to "Settings" from the main screen.
    -   Change the font size for larger text.
    -   Enable or disable "Dim Light Mode".
    -   View the "Last Time Scanned".
    -   Access "Scan History" to see details of the last five scans.
6.  **View Scan History:** Access the scan history from settings or potentially another part of the app to see a list of past scan dates.

## Future Scope / To-Do (Potential)

-   Implement real-time protection.
-   Allow users to define custom scan paths.
-   Update the virus database regularly.
-   Provide more detailed information about detected threats.
-   Add options for quarantining files instead of direct deletion.
-   Improve the UI/UX with more detailed progress indicators during scans.
