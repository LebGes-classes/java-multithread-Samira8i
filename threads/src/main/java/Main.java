import java.io.File;
import java.util.*;

public class Main {
    public static void main(String[] args) throws Exception {
        String input = "input.xlsx";
        String output = "output.xlsx";

        File f = new File(input);
        if (!f.exists()) {
            ExcelUtil.generateTemplate(input);
            System.out.println("Файл input.xlsx создан. Заполни его и перезапусти.");
            return;
        }

        Map<String, List<Task>> taskMap = ExcelUtil.readTasks(input);

        List<Employee> employees = new ArrayList<>();
        for (String name : taskMap.keySet()) {
            Employee e = new Employee(name);
            taskMap.get(name).forEach(e::addTask);
            employees.add(e);
        }

        int day = 1;
        boolean hasWork;

        System.out.println("Начало работы. Сотрудники и их задачи:");
        employees.forEach(e -> {
            System.out.println(e.getName() + " имеет " + e.getTaskCount() + " задач");
        });

        do {
            hasWork = false;
            List<Thread> threads = new ArrayList<>();

            System.out.println("\nДень " + day + ":");

            for (Employee e : employees) {
                if (e.hasWork()) {
                    hasWork = true;
                    Employee currentEmployee = e;
                    int finalDay = day;
                    Thread t = new Thread(() -> {
                        currentEmployee.workDay(finalDay);
                        System.out.println("  " + currentEmployee.getName() + " отработал " +
                                currentEmployee.getDailyWorkLog().get(finalDay) + " часов, " +
                                "простаивал " + (8 - currentEmployee.getDailyWorkLog().get(finalDay)) + " часов");
                    });
                    threads.add(t);
                    t.start();
                }
            }

            for (Thread t : threads) t.join();

            System.out.println("Остаток задач после дня " + day + ":");
            employees.forEach(e -> {
                if (e.hasWork()) {
                    System.out.println("  " + e.getName() + ": " + e.getTaskCount() + " задач осталось");
                } else {
                    System.out.println("  " + e.getName() + ": все задачи выполнены");
                }
            });

            day++;

        } while (hasWork);

        ExcelUtil.writeStats(output, employees);
        System.out.println("Всё готово. Результат в: " + output);
    }
}