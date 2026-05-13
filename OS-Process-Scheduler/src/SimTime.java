class SimTime extends Action {
    int duration;
    boolean isCpu; // true for CPU, false for IO
    SimTime(int d, boolean c) { this.duration = d; this.isCpu = c; }
    SimTime(){
    }
    @Override
    public String toString() {
        return String.valueOf(duration)+(isCpu?"{CPU}":"{IO}");
    }
}