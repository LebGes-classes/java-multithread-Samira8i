import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.util.*;

public class ExcelUtil {
    public static Map<String, List<Task>> readTasks(String path) throws Exception {
        Map<String, List<Task>> map = new HashMap<>();
        try (FileInputStream fis = new FileInputStream(path);
             Workbook wb = WorkbookFactory.create(fis)) {
            Sheet sheet = wb.getSheetAt(0);
            for (Row r : sheet) {
                if (r.getRowNum() == 0) continue;
                String name = r.getCell(0).getStringCellValue();
                String task = r.getCell(1).getStringCellValue();
                int hours = (int) r.getCell(2).getNumericCellValue();

                map.putIfAbsent(name, new ArrayList<>());
                map.get(name).add(new Task(task, hours));
            }
        }
        return map;
    }

    public static void writeStats(String path, List<Employee> employees) throws Exception {
        XSSFWorkbook wb = new XSSFWorkbook();
        XSSFSheet sheet = wb.createSheet("Report");

        Row head = sheet.createRow(0);
        head.createCell(0).setCellValue("Employee");
        head.createCell(1).setCellValue("Total Worked");
        head.createCell(2).setCellValue("Total Idle");
        head.createCell(3).setCellValue("Efficiency (%)");

        int row = 1;
        for (Employee e : employees) {
            Row r = sheet.createRow(row++);
            r.createCell(0).setCellValue(e.getName());
            r.createCell(1).setCellValue(e.getTotalWorked());
            r.createCell(2).setCellValue(e.getTotalIdle());
            r.createCell(3).setCellValue(e.getEfficiency());
        }

        XSSFSheet daySheet = wb.createSheet("Daily Work");
        Row h2 = daySheet.createRow(0);
        h2.createCell(0).setCellValue("Employee");

        int maxDays = employees.stream()
                .mapToInt(e -> e.getDailyWorkLog().size())
                .max().orElse(0);

        for (int i = 1; i <= maxDays; i++) {
            h2.createCell(i).setCellValue("Day " + i);
        }

        int rIdx = 1;
        for (Employee e : employees) {
            Row r = daySheet.createRow(rIdx++);
            r.createCell(0).setCellValue(e.getName());
            Map<Integer, Integer> workLog = e.getDailyWorkLog();
            for (int i = 1; i <= maxDays; i++) {
                r.createCell(i).setCellValue(workLog.getOrDefault(i, 0));
            }
        }

        try (FileOutputStream fos = new FileOutputStream(path)) {
            wb.write(fos);
        }
        wb.close();
    }

    public static void generateTemplate(String filePath) throws Exception {
        XSSFWorkbook wb = new XSSFWorkbook();
        XSSFSheet sheet = wb.createSheet("Tasks");
        Row header = sheet.createRow(0);
        header.createCell(0).setCellValue("Employee");
        header.createCell(1).setCellValue("Task Name");
        header.createCell(2).setCellValue("Task Hours");

        String[][] example = {
                {"Alice", "Task A1", "10"},
                {"Alice", "Task A2", "6"},
                {"Bob", "Task B1", "8"},
                {"Bob", "Task B2", "12"},
                {"Charlie", "Task C1", "4"},
        };

        for (int i = 0; i < example.length; i++) {
            Row r = sheet.createRow(i + 1);
            for (int j = 0; j < 3; j++) {
                r.createCell(j).setCellValue(example[i][j]);
            }
        }

        try (FileOutputStream fos = new FileOutputStream(filePath)) {
            wb.write(fos);
        }
        wb.close();
    }
}