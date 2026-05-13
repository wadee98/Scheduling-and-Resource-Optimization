# Multi-Instructor Schedule Validation System

An Object-Oriented Python application designed to automate the analysis of university faculty schedules against complex institutional regulations.

### 🔍 Technical Highlights
* **Advanced Regex Parsing:** Robust extraction of instructor IDs, course codes, and time-slots from malformed or semi-structured text files.
* **Configurable Policy Engine:** A modular system to validate:
    * **Credit Load:** Ensuring weekly hours are between 12-18.
    * **Time Integrity:** Detection of overlapping courses or office hours.
    * **Day Grouping:** Compliance with (S, M, W) or (T, Th) scheduling.
* **Automated Reporting:** Generates per-instructor statistical summaries and error logs.

### 🛠️ Architecture
* Developed using **Object-Oriented Programming (OOP)** for clean separation between Data Parsing and Policy Enforcement.
* Features a robust error-handling layer to prevent termination during malformed input.