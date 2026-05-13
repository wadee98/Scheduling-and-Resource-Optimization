# Shell-Based Schedule Compliance Utility

A lightweight Linux-based automation tool for rapid validation of manually prepared instructor schedules using Bash scripting.

### 📜 Automated Validations
* **Workload Compression:** Ensures schedules span at least four separate teaching days.
* **Consecutive Teaching:** Limits instructors to a maximum of two consecutive time-slots to prevent overload.
* **Office Hour Ratio:** Validates that office hours scale correctly with the total teaching load (e.g., 50% ratio).
* **Syntax Checking:** Verifies the `Day | [Start-End] CourseCode;` file format using standard Linux utilities (`awk`, `sed`, `grep`).

### 💻 Usage
```bash
./validator.sh schedule.txt