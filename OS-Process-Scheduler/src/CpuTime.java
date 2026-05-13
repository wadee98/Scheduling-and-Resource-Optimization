class CpuTime extends Action {
    int duration;
    boolean isCpu; // true for CPU, false for IO
    CpuTime(int d, boolean c) { this.duration = d; this.isCpu = c; }
    CpuTime(){
        
    }
    @Override
    public String toString() {
        return String.valueOf(duration)+(isCpu?"{CPU}":"{IO}");
    }
}