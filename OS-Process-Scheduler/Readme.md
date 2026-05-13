# CPU Scheduler & Deadlock Detection System

A Java-based simulation of an operating system's core management unit, combining process scheduling with resource allocation safety.

### 🛠️ Core Functionalities
* **CPU Scheduling:** Implements process execution simulation including CPU and I/O bursts.
* **Deadlock Detection:** Monitors resource requests (R) and releases (F) across multiple resource instances.
* **Recovery Logic:** Features an automated scenario handler for deadlock detection and system recovery to a safe state.
* **Resource Management:** Handles multiple resource types and instances based on an initial system state configuration.

### 📊 Input Structure
The system parses a structured text file:
1. **Initial Resources:** e.g., `[1,5], [2,3]` (Resource ID, Instances)
2. **Process Data:** `[PID] [Arrival] [Priority] [CPU/IO Bursts]`

### 🚀 Execution
```bash
javac Main.java
java Main input.txt