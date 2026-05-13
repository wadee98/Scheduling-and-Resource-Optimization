import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

class ProcessC {
    int pid;
    int arrivalTime;
    int priority;
    int constPriority;
    LinkedList<Action> actions = new LinkedList<>();
    int waitTime = 0;
    int turnaroundTime = 0;
    int finishTime = 0;
    int timeInReadyQueue = 0;
    int quantumUsed = 0;
    boolean hasFinishedUnit=false;
    Map<Integer, Integer> allocatedResources = new HashMap<>();

    ProcessC(int pid, int arrivalTime, int priority) {
        this.pid = pid;
        this.arrivalTime = arrivalTime;
        this.constPriority = priority;
        this.priority = priority;
    }

    @Override
    public String toString() {
        return "P" + pid + "(pri=" + priority + ", actions=" + actions + ")";
    }
}