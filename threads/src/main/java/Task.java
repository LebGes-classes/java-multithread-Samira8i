public class Task {
    private String name;
    private int hours;

    public Task(String name, int hours) {
        this.name = name;
        this.hours = hours;
    }
    public String getName() {
        return name;
    }
    public int getHours() {
        return hours;
    }

    public int work(int availableHours) {
        int worked = Math.min(availableHours, hours);
        hours -= worked;
        return worked;
    }

    boolean theEnd() {
        return hours <= 0;
    }
}
