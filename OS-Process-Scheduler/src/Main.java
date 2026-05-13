import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

import java.util.regex.Pattern;
import java.util.regex.Matcher;
public class Main {
    static final int QUANTUM = 5;
    static final int AGING_THRESHOLD = 10;
    static final int MAX_PRIORITY = 20;
    static final int MIN_PRIORITY = 0;

    static HashMap<Integer, Integer> availableResources = new HashMap<>();
    static HashMap<Integer, Integer> maxResources = new HashMap<>(); // Track max for deadlock info
    static ArrayList<ProcessC> allProcesses = new ArrayList<>();
    static ArrayList<ProcessC> readyQueue = new ArrayList<>();
    static ArrayList<ProcessC> ioQueue = new ArrayList<>();
    static ArrayList<ProcessC> waitingQueue = new ArrayList<>();
    static ArrayList<ProcessC> terminated = new ArrayList<>();
    static StringBuilder ganttChart = new StringBuilder();
    static List<String> deadlockLog = new ArrayList<>();
    static boolean schedualledWakeUP =false;
    static boolean schedualledDeadlockDetect =false;
    static ProcessC processToBeMovedToIO =null;

    public static void main(String[] args) throws FileNotFoundException {
        parseInput("src/input.txt");

        System.out.println("=== Initial State ===");
        System.out.println("Resources: " + availableResources);
        System.out.println("Processes: " + allProcesses.size());
        for (ProcessC p : allProcesses) {
            System.out.println("  " + p);
        }
        System.out.println();

        int currentTime = 0;
        ProcessC runningProcess = null;
        int totalProcesses = allProcesses.size();

        while (terminated.size() < totalProcesses) {
            System.out.println("\n--- Time " + currentTime + " ---");
            if(schedualledDeadlockDetect){
                detectAndRecoverDeadlock(currentTime);
                schedualledDeadlockDetect=false;
            }
            // check new arrivals
            for (ProcessC p : allProcesses)
                if (p.arrivalTime == currentTime) {
                    priorityAdd(readyQueue,p,true);
                    System.out.println("Process P" + p.pid + " arrived");
                }


            //  IO Completion
            Iterator<ProcessC> ioIter = ioQueue.iterator();
            while (ioIter.hasNext()) {
                ProcessC p = ioIter.next();
                SimTime ioAction = (SimTime) p.actions.peek();
                if (ioAction.duration <= 0) {
                    p.actions.poll();
                    ioIter.remove();
                    p.priority=p.constPriority;
                    priorityAdd(readyQueue,p,true);
                    System.out.println("P" + p.pid + " completed IO, moved to ready queue");
                }
            }
            Iterator<ProcessC> temp = readyQueue.iterator();
            List<ProcessC> ttt = new ArrayList<>();
            //check if any process in ready has aged
            while(temp.hasNext()){
                ProcessC p = temp.next();
                if (p.timeInReadyQueue >= AGING_THRESHOLD) {
                    if (p.priority > MIN_PRIORITY) {
                        temp.remove();
                        ttt.add(p);
                        p.priority--;
                        System.out.println("P" + p.pid + " aged, new priority: " + p.priority);
                    }
                }
            }
            for(ProcessC p : ttt){
                priorityAdd(readyQueue,p,true);
            }
            if(schedualledWakeUP){
                wakeUpWaitingProcesses();
                schedualledWakeUP=false;
            }
            if(processToBeMovedToIO!=null){
                System.out.println("P" + processToBeMovedToIO.pid + " moved to IO queue");
                ioQueue.add(processToBeMovedToIO);
                processToBeMovedToIO=null;
            }

            // Scheduling (Preemptive Priority + Round Robin)
            if(runningProcess!=null&&runningProcess.quantumUsed >= QUANTUM&&readyQueue.isEmpty())runningProcess.quantumUsed = 0;

            if (!readyQueue.isEmpty()) {
                ProcessC highestPrio = readyQueue.get(0);
                if (runningProcess == null) {
                    runningProcess = readyQueue.remove(0);
                    System.out.println("Scheduled P" + runningProcess.pid);
                }
                else if (highestPrio.priority < runningProcess.priority) {
                    // Preempt due to higher priority
                    System.out.println("Preempting P" + runningProcess.pid + " for higher priority P" + highestPrio.pid);

                    if(runningProcess.quantumUsed >= QUANTUM){
                        runningProcess.quantumUsed = 0;
                        priorityAdd(readyQueue,runningProcess,true);
                    }
                    else priorityAdd(readyQueue,runningProcess,false);
                    runningProcess = readyQueue.remove(0);
                } else if (runningProcess.quantumUsed >= QUANTUM) {
                    // Quantum expired - Round Robin
                    System.out.println("Quantum expired for P" + runningProcess.pid);
                    System.out.println(readyQueue);
                    runningProcess.quantumUsed = 0;
                    if(runningProcess.priority>= highestPrio.priority)
                    {
                        priorityAdd(readyQueue, runningProcess, true);
                        runningProcess = readyQueue.remove(0);
                    }

                    System.out.println(readyQueue);

                }
            }

            // Execute Running Process if applicable
            if (runningProcess != null) {
                ExecuteResult result = executeStep(runningProcess, currentTime);
                switch (result) {
                    case TERMINATED:
                        runningProcess.finishTime = runningProcess.hasFinishedUnit? currentTime + 1:currentTime;
                        runningProcess.turnaroundTime = runningProcess.finishTime - runningProcess.arrivalTime;
                        // If there is an error in input and didn't release all resources
                        // we force to release

                        addAllocatedToWork(runningProcess,availableResources);
                        runningProcess.allocatedResources.clear();
                        schedualledWakeUP=true;

                        terminated.add(runningProcess);
                        System.out.println("P" + runningProcess.pid + " terminated");

                        if(!runningProcess.hasFinishedUnit){
                            runningProcess = null;
                            continue;
                        }
                        ganttChart.append(runningProcess.pid).append(" ");
                        runningProcess.hasFinishedUnit=false;
                        runningProcess = null;
                        break;
                    case MOVED_TO_IO:
                        processToBeMovedToIO = runningProcess;
                        runningProcess.quantumUsed=0;
                        if(!runningProcess.hasFinishedUnit){
                            runningProcess = null;
                            continue;
                        }
                        ganttChart.append(runningProcess.pid).append(" ");
                        runningProcess.hasFinishedUnit=false;
                        runningProcess = null;
                        break;
                    case BLOCKED:
                        waitingQueue.add(runningProcess);
                        runningProcess.quantumUsed=0;
                        System.out.println("P" + runningProcess.pid + " blocked on resource");
                        schedualledDeadlockDetect=true;
                        if(!runningProcess.hasFinishedUnit){
                            runningProcess = null;
                            continue;
                        }
                        ganttChart.append(runningProcess.pid).append(" ");
                        runningProcess.hasFinishedUnit=false;
                        runningProcess = null;
                        break;
                    case CONTINUE:
                        if(!runningProcess.hasFinishedUnit){
                            runningProcess = null;
                            continue;
                        }
                        ganttChart.append(runningProcess.pid).append(" ");
                        runningProcess.hasFinishedUnit=false;
                        // Process continues running
                        break;
                }
            } else {
                ganttChart.append("- ");
                System.out.println("CPU idle");
            }
            // Aging - increment wait time and adjust priority



            // Update wait times
            for (ProcessC p : readyQueue) {
                p.waitTime++;
                p.timeInReadyQueue++;
            }
            for (ProcessC p : waitingQueue) {
                p.waitTime++;//waiting time can extend to time in blocked queue since it is not working actually but waiting as if it is in ready Queue
                //
            }
            // increase time in IO Handle IO Completion
            for (ProcessC p : ioQueue) {
                SimTime ioAction = (SimTime) p.actions.peek();
                ioAction.duration--;//we are sure that it is ioAction. since if not then it won't enter ioQueue
                System.out.println("P" + p.pid + " IO remaining: " + ioAction.duration);
            }

            // Debug output
            System.out.println("Ready: " + getProcessIds(readyQueue));
            System.out.println("Waiting: " + getProcessIds(waitingQueue));
            System.out.println("IO: " + getProcessIds(ioQueue));
            System.out.println("Running: " + (runningProcess != null ? "P" + runningProcess.pid : "none"));

            currentTime++;
            if (currentTime > 5000) {
                System.out.println("Safety break triggered!");
                break;
            }
        }

        printResults(currentTime);
    }

    enum ExecuteResult {
        CONTINUE, TERMINATED, MOVED_TO_IO, BLOCKED
    }

    private static ExecuteResult executeStep(ProcessC p, int time) {
        if (p.actions.isEmpty()) {
            return ExecuteResult.TERMINATED;
        }
        Action current = p.actions.peek();

        if (current instanceof ResourceOp op) {
            if (op.isRequest) {
                if(!p.hasFinishedUnit){
                    int available = availableResources.getOrDefault(op.resourceId, 0);
                    if (available >= op.amount) {
                        // Allocate resources
                        availableResources.put(op.resourceId, available - op.amount);
                        p.allocatedResources.merge(op.resourceId, op.amount, (oldVal,newVal) -> oldVal+newVal);
                        p.actions.poll();
                        System.out.println("P" + p.pid + " acquired " + op.amount + " of R" + op.resourceId);
                        // Continue to next action (resource ops don't consume time)
                        return executeStep(p, time);
                    } else {
                        System.out.println("P" + p.pid + " waiting for " + op.amount + " of R" + op.resourceId + " (available: " + available + ")");
                        return ExecuteResult.BLOCKED;
                    }
                }else return ExecuteResult.CONTINUE;
            } else {
                // Release resources
                int currentAlloc = p.allocatedResources.getOrDefault(op.resourceId, 0);
                int releaseAmount = Math.min(op.amount, currentAlloc);
                availableResources.merge(op.resourceId, releaseAmount, (oldVal,newVal) -> oldVal+newVal);
                p.allocatedResources.put(op.resourceId, currentAlloc - releaseAmount);
                p.actions.poll();
                System.out.println("P" + p.pid + " released " + releaseAmount + " of R" + op.resourceId);

                // Check if release wakes up waiting processes
                schedualledWakeUP=true;
                return executeStep(p, time);
            }
        }
        else{
            SimTime simTime = (SimTime) current;

            if (!simTime.isCpu) {
                // This is an IO burst - move to IO queue
                return ExecuteResult.MOVED_TO_IO;
            }
            if (!p.hasFinishedUnit) {
                p.hasFinishedUnit = true;
                // Execute one time unit of CPU burst
                simTime.duration--;
                p.quantumUsed++;
                System.out.println("P" + p.pid + " executing CPU, remaining: " + simTime.duration + ", quantum: " + p.quantumUsed);

                if (simTime.duration <= 0) {
                    p.actions.poll();
                    return executeStep(p, time);
                }
            }
            return ExecuteResult.CONTINUE;
        }
    }

    private static void wakeUpWaitingProcesses() {
        Iterator<ProcessC> it = waitingQueue.iterator();
        while (it.hasNext()) {
            ProcessC w = it.next();
            ResourceOp req = (ResourceOp) w.actions.peek();
            if (availableResources.getOrDefault(req.resourceId, 0) >= req.amount) {
                it.remove();
                priorityAdd(readyQueue,w,true);
                System.out.println("P" + w.pid + " woke up - resource available");
            }

        }
    }

    private static void detectAndRecoverDeadlock(int time) {
        if (waitingQueue.isEmpty()) return;
        HashMap<Integer, Integer> work = new HashMap<>(availableResources);

        ArrayList<ProcessC> processesThatHaveAllocations= new ArrayList<>(allProcesses);
        Iterator<ProcessC> it = processesThatHaveAllocations.iterator();
        while (it.hasNext()) {
            ProcessC current = it.next();
            if (current.allocatedResources.isEmpty()) {
                it.remove();
            }
        }

        while(!processesThatHaveAllocations.isEmpty()) {
            boolean foundAny =false;
            it = processesThatHaveAllocations.iterator();
            while (it.hasNext()) {
                ProcessC current = it.next();
                if (checkIfRIsLessThanWork(current, work)) {
                    addAllocatedToWork(current, work);
                    it.remove();
                    foundAny=true;
                }
            }
            if(!foundAny) break;
        }



        // no deadlock
        if (processesThatHaveAllocations.isEmpty()) {return;}

        // deadlock exists and the processes involved are in processesThatHaveAllocations
        String deadlockMsg = "DEADLOCK DETECTED at time " + time + "!";
        System.out.println("\n*** " + deadlockMsg + " *");
        deadlockLog.add(deadlockMsg);

        // Show current state
        System.out.println("Waiting processes:");
        for (ProcessC p : processesThatHaveAllocations) {
            ResourceOp op = (ResourceOp) p.actions.peek();
            System.out.println("  P" + p.pid + " waiting for " + op.amount + " of R" + op.resourceId
                    + ", holding: " + p.allocatedResources);
        }
        System.out.println("Available resources: " + availableResources);

        // Recovery: terminate lowest priority  process
        processesThatHaveAllocations.sort((p1, p2) -> p2.priority - p1.priority);
        ProcessC victim = processesThatHaveAllocations.get(0);

        // Remove victim from queues (wherever it exists)
        waitingQueue.remove(victim);
        readyQueue.remove(victim);
        ioQueue.remove(victim);
        String recoveryMsg = "Terminating P" + victim.pid + " (priority " + victim.priority + ") for recovery";
        System.out.println(recoveryMsg);
        deadlockLog.add(recoveryMsg);

        addAllocatedToWork(victim,availableResources);
        victim.allocatedResources.clear();
        allProcesses.remove(victim);

        wakeUpWaitingProcesses();

        reInitProcess("src/input.txt",time,victim);

        detectAndRecoverDeadlock(time);
    }
    private static boolean checkIfRIsLessThanWork(ProcessC p, HashMap<Integer, Integer> work ){
        if(p.actions.isEmpty()||!(p.actions.peek() instanceof ResourceOp op && op.isRequest)) return  true;
        ResourceOp request = (ResourceOp) p.actions.peek();
        return request.amount <= work.get(request.resourceId);
    }
    private static void addAllocatedToWork(ProcessC current,HashMap<Integer, Integer> work ){
        Map<Integer, Integer> alloc = current.allocatedResources;
        for(Integer key : alloc.keySet()){
            System.out.println("  Released " + alloc.get(key) + " of R" + key);
            work.merge(key,alloc.get(key),(a,b)->a+b);
        }
    }

    private static void parseInput(String filename) throws FileNotFoundException {
        Scanner sc = new Scanner(new File(filename));

        String resLine = sc.nextLine().trim();
        System.out.println("Parsing resources: " + resLine);

        Pattern pattern = Pattern.compile("\\[(\\d+),(\\d+)\\]");
        Matcher matcher = pattern.matcher(resLine);
        while (matcher.find()) {
            int id = Integer.parseInt(matcher.group(1));
            int count = Integer.parseInt(matcher.group(2));
            availableResources.put(id, count);
            maxResources.put(id, count);
            System.out.println("  Resource R" + id + ": " + count + " instances");
        }

        while (sc.hasNextLine()) {
            String line = sc.nextLine().trim();
            if (line.isEmpty()) continue;

            System.out.println("Parsing process line: " + line);

            // Split into parts
            String[] parts = line.split("\\s+", 4); // Split into max 4 parts
            if (parts.length < 4) {
                System.out.println("  Skipping invalid line");
                continue;
            }

            int pid = Integer.parseInt(parts[0]);
            int arrival = Integer.parseInt(parts[1]);
            int priority = Integer.parseInt(parts[2]);

            ProcessC p = new ProcessC(pid, arrival, priority);
            // Parse actions from remaining string
            String actionsStr = parts[3];
            parseActions(p, actionsStr);

            allProcesses.add(p);
            System.out.println("  Created: " + p);
        }
        sc.close();
    }

    private static void parseActions(ProcessC p, String s) {
        boolean isCpu = true;

        // Tokenize the string
        Scanner scanner = new Scanner(s);
        scanner.useDelimiter("[\\s,{}\\[\\]]+");

        while (scanner.hasNext()) {
            String token = scanner.next().trim();
            if (token.isEmpty()) continue;

            if (token.equalsIgnoreCase("CPU")) {
                isCpu = true;
            } else if (token.equalsIgnoreCase("IO")) {
                isCpu = false;
            } else if (token.equalsIgnoreCase("R")) {

                int resId = scanner.nextInt();
                int amount =scanner.nextInt();
                ResourceOp op = new ResourceOp(true, resId, amount);
                p.actions.add(op);

            } else if (token.equalsIgnoreCase("F")) {

                int resId = scanner.nextInt();
                int amount = scanner.nextInt();
                ResourceOp op = new ResourceOp(false, resId, amount);
                p.actions.add(op);

            } else if (token.matches("\\d+")) {
                // Duration
                int duration = Integer.parseInt(token);
                SimTime simTime = new SimTime(duration, isCpu);
                p.actions.add(simTime);
            }
        }
        scanner.close();
    }

    private static String getProcessIds(List<ProcessC> list) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < list.size(); i++) {
            if (i > 0) sb.append(", ");
            sb.append("P").append(list.get(i).pid);
        }
        sb.append("]");
        return sb.toString();
    }

    private static void printResults(int totalTime) {
        StringBuilder[] transformedChart=new StringBuilder[]{new StringBuilder(),new StringBuilder(),new StringBuilder()};
        String [] IDs = ganttChart.toString().split(" ");
        if(IDs.length==0) return;
        int counter=0;
        ArrayList<ArrayList<String>> groups= findGroups(IDs);

        for(ArrayList<String> group : groups){

            transformedChart[0].append("┌");
            transformedChart[1].append("│"+counter);
            transformedChart[2].append("└");

            for(int i =0;i<group.size();i++){
                transformedChart[1].append(" ");
                if(i== group.size()/2){
                    transformedChart[1].append(group.get(0).equals("-")?" Idle ":" P"+group.get(0)+" ");
                }
                counter++;
            }
            transformedChart[1].append(counter-1).append("│");
            while(transformedChart[0].length()<transformedChart[1].length()-1){
                transformedChart[0].append("─");
                transformedChart[2].append("─");
            }
            transformedChart[0].append("┐");
            transformedChart[2].append("┘");
        }


        System.out.println("\n========================================");
        System.out.println("         SIMULATION RESULTS");
        System.out.println("========================================");

        System.out.println("\nGantt Chart:");
        //System.out.println(ganttChart.toString());
        System.out.println(transformedChart[0].toString());
        System.out.println(transformedChart[1].toString());
        System.out.println(transformedChart[2].toString());
        System.out.println("\n+========================== Final Results ==============================+");
        double totalWait = 0, totalTurnaround = 0;
        int count = 0;

        for (ProcessC p : terminated) {
            if (allProcesses.contains(p)) { // Only count non-victim processes
                System.out.printf("|P%-3s | Arrival: %-5s | Finish: %-5s | Wait: %-5s | Turnaround: %-5s|\n",
                        p.pid, p.arrivalTime, p.finishTime, p.waitTime, p.turnaroundTime);
                totalWait += p.waitTime;
                totalTurnaround += p.turnaroundTime;
                count++;
            }
        }
        System.out.println("+=======================================================================+\n");

        if (count > 0) {
            System.out.printf("\nAvg Waiting Time: %.2f\n", totalWait / count);
            System.out.printf("Avg Turnaround Time: %.2f\n", totalTurnaround / count);
        }


        System.out.println("\n--- Deadlock Report ---");
        if (deadlockLog.isEmpty()) {
            System.out.println("No deadlocks detected during simulation.");
        } else {
            for (String log : deadlockLog) {
                System.out.println(log);
            }
        }

        System.out.println("\nTotal simulation time: " + totalTime + " units");
    }
    private static void priorityAdd(List<ProcessC> list , ProcessC p, boolean isLast) {
        int i = 0;
        if (isLast) {
            while (i < list.size() && list.get(i).priority <= p.priority) i++;
        } else {
            while (i < list.size() && list.get(i).priority < p.priority) i++;
        }
        p.timeInReadyQueue = 0;
        list.add(i, p);
    }
    private static void reInitProcess(String filename,int time,ProcessC p) {
        Scanner sc = null;
        try {
            sc = new Scanner(new File(filename));
            sc.nextLine();
            while (sc.hasNextLine()) {
                String line = sc.nextLine().trim();
                if (line.isEmpty()) continue;

                // Split into parts
                String[] parts = line.split("\\s+", 4); // Split into max 4 parts
                if (parts.length < 4) {
                    continue;
                }
                if(p.pid!=Integer.parseInt(parts[0]))
                    continue;

                p = new ProcessC(p.pid, time, p.constPriority);

                // Parse actions from remaining string
                String actionsStr = parts[3];
                parseActions(p, actionsStr);
                allProcesses.add(p);
                System.out.println("  Created Again: " + p);
                break;
            }
            sc.close();
        }
        catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
    private static ArrayList<ArrayList<String>> findGroups(String [] IDs){
        ArrayList<ArrayList<String>> groups = new ArrayList<>();
        int counter =0;
        ArrayList<String> temp=new ArrayList<>();
        String lastValue = IDs[0];
        while(counter<IDs.length){
            if(!IDs[counter].equals(lastValue)){
                groups.add(temp);
                temp=new ArrayList<>();
                lastValue=IDs[counter];
            }
            temp.add(IDs[counter]);
            counter++;
        }
        groups.add(temp);
        return  groups;
    }
}