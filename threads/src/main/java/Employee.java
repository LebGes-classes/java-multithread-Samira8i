import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

public class Employee {
    private String name;
    private Queue<Task> tasks = new LinkedList<>(); //так как первый пришел-первый ушел
    private final Map<Integer, Integer> dailyWork = new HashMap<>();
    private final Map<Integer, Integer> dailyIdle = new HashMap<>();
    public Employee(String name) {
        this.name = name;
    }
    public String getName() {
        return name;
    }
    public void addTask(Task task) {
        tasks.add(task);
    }
    public boolean hasWork() {
        return !tasks.isEmpty();
    }

    public void workDay(int dayNumber) {
        int availableHours = 8;
        int worked = 0;
        while (availableHours > 0 && hasWork()) {
            Task task = tasks.peek();
            int done = task.work(availableHours);
            worked += done;
            availableHours -= done;

            if (task.theEnd()) tasks.poll();
        }

        dailyWork.put(dayNumber, worked);
        dailyIdle.put(dayNumber, 8 - worked);
    }
    public int getTotalWorked() {
        return dailyWork.values().stream().mapToInt(i -> i).sum();
    }

    public int getTotalIdle() {
        return dailyIdle.values().stream().mapToInt(i -> i).sum();
    }

    public double getEfficiency() {
        int total = getTotalWorked() + getTotalIdle();
        return total == 0 ? 0 : (100.0 * getTotalWorked() / total);
    }

    public Map<Integer, Integer> getDailyWorkLog() {
        return dailyWork;
    }
    public int getTaskCount() {
        return tasks.size();
    }

}
