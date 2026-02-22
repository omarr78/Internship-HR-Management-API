package com.internship.generatedata;

import com.internship.enums.Degree;
import com.internship.enums.Gender;
import com.internship.enums.SalaryReason;
import org.springframework.boot.CommandLineRunner;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

import static com.internship.enums.Degree.*;

//@Component
public class InsertScriptGenerator implements CommandLineRunner {
    private static final String PATH = "src/main/resources/sql/";
    private static final int EMPLOYEES = 10_000;
    private static final Set<String> employeeNationalIds = new HashSet<>();
    private static final Long MANAGER_ID = 1L;
    private static final Integer MAX_LEAVE_DAYS = 40;
    private static final Integer MAX_BONUS_COUNT = 8;
    private final List<Long> architectEmployeeIds = new ArrayList<>();
    private final List<Long> seniorEmployeeIds = new ArrayList<>();
    private final List<Long> intermediateEmployeeIds = new ArrayList<>();
    private final List<EmployeeInfo> insertedEmployeeInfo = new ArrayList<>();

    private Path createOrReplacePath(String fileName) throws IOException {
        Path path = Paths.get(PATH + fileName);
        Files.createDirectories(path.getParent());
        return path;
    }

    private void writeToFile(Path path, List<String> lines) throws IOException {
        Files.write(path, lines, StandardCharsets.UTF_8);
        System.out.println(path.getFileName() + " data written!");
    }

    private void generateInsertScript(String fileName, String tableName, String[] data) throws IOException {
        Path path = createOrReplacePath(fileName);
        List<String> lines = new ArrayList<>();

        for (String value : data) {
            String sql = String.format(
                    "INSERT INTO %s (name) VALUES ('%s');",
                    tableName,
                    value
            );
            lines.add(sql);
        }
        writeToFile(path, lines);
    }

    private int generateRandomNumberOfSpecificSize(int size) {
        // from 0 to size - 1
        return new Random().nextInt(size);
    }

    private Person generateFirstNameAndGender() {
        // get the firstName and gender
        int peopleSize = Data.PEOPLE.length;
        int random = generateRandomNumberOfSpecificSize(peopleSize);
        return Data.PEOPLE[random];
    }

    private String generateLastName() {
        int lastNameSize = Data.LAST_NAMES.length;
        int random = generateRandomNumberOfSpecificSize(lastNameSize);
        return Data.LAST_NAMES[random];
    }

    private LocalDate generateBirthDate() {
        // birthdate from 1960 to 2002
        long minDay = LocalDate.of(1960, 1, 1).toEpochDay();
        long maxDay = LocalDate.of(2002, 12, 31).toEpochDay();
        long randomDay = ThreadLocalRandom.current().nextLong(minDay, maxDay + 1); // Upper bound is exclusive
        return LocalDate.ofEpochDay(randomDay);
    }

    private LocalDate generateGraduationDateFromBirthDate(LocalDate birthDate) {
        // 1. Add 22 years
        int gradYear = birthDate.getYear() + 22;

        // 2. Set Month to May (Common Graduation Month)
        int gradMonth = 5;

        // 3. Pick a random day in the common window (May 10 - May 24)
        // threadLocalRandom uses (origin, bound) where bound is exclusive
        int gradDay = ThreadLocalRandom.current().nextInt(10, 25);

        return LocalDate.of(gradYear, gradMonth, gradDay);
    }

    private LocalDate generateJoinedDateFromGraduationDate(LocalDate graduationDate) {
        // suppose the employee hired after graduated
        // randomly pick joinedDate from graduationDate to currentDate
        LocalDate today = LocalDate.now();

        // Convert both to Epoch Days to create a numeric range
        long startEpoch = graduationDate.toEpochDay();
        long endEpoch = today.toEpochDay();

        // Pick a random day between them (origin inclusive, bound exclusive)
        long randomEpochDay = ThreadLocalRandom.current().nextLong(startEpoch, endEpoch + 1);

        // return the result
        return LocalDate.ofEpochDay(randomEpochDay);
    }

    private Degree getDegreeFromGraduationDate(LocalDate graduationDate) {
        // Calculate Years of Experience (Current Year - Grad Year)
        long expYears = ChronoUnit.YEARS.between(graduationDate, LocalDate.now());

        if (expYears >= 0 && expYears <= 2) {
            return FRESH;
        } else if (expYears >= 3 && expYears <= 5) {
            return INTERMEDIATE;
        } else if (expYears >= 6 && expYears <= 10) {
            return SENIOR;
        } else if (expYears >= 11) {
            return ARCHITECT;
        }
        return FRESH; // Default fallback
    }

    private long generateManagerIdFromDegree(Degree degree) {
        switch (degree) {
            case FRESH:
                return !intermediateEmployeeIds.isEmpty() ?
                        intermediateEmployeeIds.get(generateRandomNumberOfSpecificSize(intermediateEmployeeIds.size())) : 1;
            case INTERMEDIATE:
                return !seniorEmployeeIds.isEmpty() ?
                        seniorEmployeeIds.get(generateRandomNumberOfSpecificSize(seniorEmployeeIds.size())) : 1;
            case SENIOR:
                return !architectEmployeeIds.isEmpty() ?
                        architectEmployeeIds.get(generateRandomNumberOfSpecificSize(architectEmployeeIds.size())) : 1;
            case ARCHITECT:
                return MANAGER_ID;
        }
        return MANAGER_ID;
    }

    private void generateEmployeeInsertScript() throws IOException {
        Path path = createOrReplacePath("3.insert_employees.sql");
        List<String> lines = new ArrayList<>();

        for (int i = 0; i < EMPLOYEES; i++) {
            final long id = i + 1;
            Person person = generateFirstNameAndGender();
            String firstName = person.firstName();
            Gender gender = person.gender();
            String lastName = generateLastName();
            String nationalId = "TEMP-" + id;

            LocalDate birthDate = (id == 1) ? LocalDate.of(1960, 1, 1) : generateBirthDate();
            LocalDate graduationDate = generateGraduationDateFromBirthDate(birthDate);
            LocalDate joinedDate = generateJoinedDateFromGraduationDate(graduationDate);
            long pastExperienceYears = ChronoUnit.YEARS.between(graduationDate, joinedDate);
            Degree degree = getDegreeFromGraduationDate(graduationDate);

            long departmentId = generateRandomNumberOfSpecificSize(Data.DEPARTMENTS.length + 1);
            if (departmentId == 0) departmentId++;

            long teamId = generateRandomNumberOfSpecificSize(Data.TEAMS.length + 1);
            if (teamId == 0) teamId++;

            Long managerId;
            if (id == 1) {
                managerId = null;
            } else {
                managerId = generateManagerIdFromDegree(degree);
            }
            lines.add(String.format(
                    "INSERT INTO employees (id, first_name, last_name, gender, national_id, date_of_birth," +
                            " graduation_date, joined_date, past_experience_year, degree, department_id," +
                            " team_id, manager_id) " +
                            "VALUES (%d, '%s', '%s', '%s', '%s', '%s', '%s', '%s', %d, '%s', %d, %d, %s);",
                    id, firstName, lastName, gender, nationalId, birthDate, graduationDate, joinedDate,
                    pastExperienceYears, degree, departmentId, teamId, managerId
            ));

            switch (degree) {
                case INTERMEDIATE:
                    intermediateEmployeeIds.add(id);
                    break;
                case SENIOR:
                    seniorEmployeeIds.add(id);
                    break;
                case ARCHITECT:
                    architectEmployeeIds.add(id);
                    break;
                default:
                    break;
            }
            insertedEmployeeInfo.add(new EmployeeInfo(id, degree, joinedDate));
        }
        writeToFile(path, lines);
    }

    private BigDecimal generateSalary(Degree degree) {
        Random rand = new Random();
        double salaryAmount = switch (degree) {
            case FRESH -> 25000 + (45000 - 25000) * rand.nextDouble();
            case INTERMEDIATE -> 45001 + (85000 - 45001) * rand.nextDouble();
            case SENIOR -> 85001 + (150000 - 85001) * rand.nextDouble();
            case ARCHITECT -> 150001 + (300000 - 150001) * rand.nextDouble();
        };
        long roundedToHundred = Math.round(salaryAmount / 100.0) * 100;
        return BigDecimal.valueOf(roundedToHundred).setScale(2, RoundingMode.HALF_UP);
    }

    private void generateEmployeeSalary() throws IOException {
        Path path = createOrReplacePath("4.insert_employeeSalaries.sql");
        List<String> lines = new ArrayList<>();
        for (EmployeeInfo employeeInfo : insertedEmployeeInfo) {
            BigDecimal grossSalary = generateSalary(employeeInfo.degree());

            // Set a fixed simple time (e.g., 5:00 PM)
            LocalTime fixedTime = LocalTime.of(17, 0, 0);
            // Combine them into a LocalDateTime
            LocalDateTime creationDate = LocalDateTime.of(employeeInfo.joinedDate(), fixedTime);

            String reason = SalaryReason.INITIAL_BASE_SALARY.getMessage();
            long employeeId = employeeInfo.id();

            lines.add(String.format(
                    "INSERT INTO employee_salaries (gross_salary, reason, employee_id, creation_date) VALUES (%s, '%s', %d, '%s');",
                    grossSalary.toPlainString(), reason, employeeId, creationDate
            ));
        }
        writeToFile(path, lines);
    }

    private Integer[] generateEmployeeExpertise(Degree degree) {
        Set<Integer> employeeExpertiseIdsSet = new HashSet<>();
        switch (degree) {
            case FRESH: {
                // fresh will have at max 10 expertise, because duplication is removed
                for (int i = 0; i < 10; i++) {
                    int row = generateRandomNumberOfSpecificSize(1); // from 0 to 0
                    int col = generateRandomNumberOfSpecificSize(10);
                    int indexOfExpertise = row * 10 + col;
                    indexOfExpertise++; // cuz the id is 1-based
                    employeeExpertiseIdsSet.add(indexOfExpertise);
                }
            }
            case INTERMEDIATE: {
                // intermediate will have at max 20 expertise, because duplication is removed
                for (int i = 0; i < 20; i++) {
                    int row = generateRandomNumberOfSpecificSize(2);  // from 0 to 1 inclusive
                    int col = generateRandomNumberOfSpecificSize(10);
                    int indexOfExpertise = row * 10 + col;
                    indexOfExpertise++; // cuz the id is 1-based
                    employeeExpertiseIdsSet.add(indexOfExpertise);
                }
            }
            case SENIOR: {
                // senior will have at max 30 expertise, because duplication is removed
                for (int i = 0; i < 30; i++) {
                    int row = generateRandomNumberOfSpecificSize(3);  // from 0 to 2 inclusive
                    int col = generateRandomNumberOfSpecificSize(10);
                    int indexOfExpertise = row * 10 + col;
                    indexOfExpertise++; // cuz the id is 1-based
                    employeeExpertiseIdsSet.add(indexOfExpertise);
                }
            }
            case ARCHITECT: {
                // architect will have at max 40 expertise, because duplication is removed
                for (int i = 0; i < 40; i++) {
                    int row = generateRandomNumberOfSpecificSize(4);  // from 0 to 3 inclusive
                    int col = generateRandomNumberOfSpecificSize(10);
                    int indexOfExpertise = row * 10 + col;
                    indexOfExpertise++; // cuz the id is 1-based
                    employeeExpertiseIdsSet.add(indexOfExpertise);
                }
            }
        }
        return employeeExpertiseIdsSet.toArray(Integer[]::new);
    }

    private void generateEmployeeExpertise() throws IOException {
        Path path = createOrReplacePath("6.insert_employee_expertise.sql");
        List<String> lines = new ArrayList<>();
        for (EmployeeInfo employeeInfo : insertedEmployeeInfo) {
            Integer[] employeeExpertiseIds = generateEmployeeExpertise(employeeInfo.degree());
            for (Integer expertiseId : employeeExpertiseIds) {
                lines.add(String.format(
                        "INSERT INTO employee_expertise (employee_id, expertise_id) VALUES (%d, %d);",
                        employeeInfo.id(), expertiseId
                ));
            }
        }
        writeToFile(path, lines);
    }

    private LocalDate[] generateDatesInSpecificYear(int year, int days) {
        Set<LocalDate> dates = new HashSet<>();
        int numberOfLeaves = generateRandomNumberOfSpecificSize(days + 1);

        LocalDate start = LocalDate.of(year, 1, 1);
        LocalDate end = LocalDate.of(year, 12, 31);

        for (int i = 0; i < numberOfLeaves; i++) {
            int randomDay = ThreadLocalRandom.current()
                    .nextInt(start.getDayOfYear(), end.getDayOfYear() + 1);

            LocalDate randomDate = LocalDate.ofYearDay(year, randomDay);
            DayOfWeek day = randomDate.getDayOfWeek();

            // Skip Friday and Saturday
            if (day != DayOfWeek.FRIDAY && day != DayOfWeek.SATURDAY) {
                dates.add(randomDate); // Set guarantees uniqueness
            }
        }
        return dates.toArray(LocalDate[]::new);
    }

    private void generateLeavesInSpecificYear(int year) throws IOException {
        Path path = createOrReplacePath("7.insert_employee_leaves.sql");
        List<String> lines = new ArrayList<>();

        for (EmployeeInfo employeeInfo : insertedEmployeeInfo) {
            LocalDate[] dates = generateDatesInSpecificYear(year, MAX_LEAVE_DAYS);
            for (LocalDate date : dates) {
                lines.add(String.format(
                        "INSERT INTO leaves (leave_date, employee_id) VALUES ('%s', %d);",
                        date.toString(), employeeInfo.id()
                ));
            }
        }
        writeToFile(path, lines);
    }

    private BigDecimal generateBonusAmountBasedOnDegree(Degree degree) {
        Random rand = new Random();
        double salaryAmount = switch (degree) {
            case FRESH -> 1000 + (2000 - 1000) * rand.nextDouble();
            case INTERMEDIATE -> 2001 + (4000 - 2001) * rand.nextDouble();
            case SENIOR -> 4001 + (6000 - 4001) * rand.nextDouble();
            case ARCHITECT -> 6001 + (8000 - 6001) * rand.nextDouble();
        };
        long roundedToHundred = Math.round(salaryAmount / 100.0) * 100;
        return BigDecimal.valueOf(roundedToHundred).setScale(2, RoundingMode.HALF_UP);
    }

    private void generateBonusInSpecificYear(int year) throws IOException {
        Path path = createOrReplacePath("8.insert_employee_bonuses.sql");
        List<String> lines = new ArrayList<>();

        for (EmployeeInfo employeeInfo : insertedEmployeeInfo) {
            LocalDate[] dates = generateDatesInSpecificYear(year, MAX_BONUS_COUNT);
            for (LocalDate date : dates) {
                BigDecimal amount = generateBonusAmountBasedOnDegree(employeeInfo.degree());
                lines.add(String.format(
                        "INSERT INTO bonuses (amount, bonus_date, employee_id) VALUES (%s, '%s', %d);",
                        amount.toPlainString(), date.toString(), employeeInfo.id()
                ));
            }
        }
        writeToFile(path, lines);
    }

    @Override
    public void run(String... args) throws Exception {
        generateInsertScript("1.insert_departments.sql", "departments", Data.DEPARTMENTS);
        generateInsertScript("2.insert_teams.sql", "teams", Data.TEAMS);
        // insert employees
        generateEmployeeInsertScript();
        // insert salaries for inserted employees
        generateEmployeeSalary();
        // prepare data for inserting expertises
        List<String> allExpertiseList = new ArrayList<>();
        for (int i = 0; i < Data.EXPERTISES.length; i++) {
            allExpertiseList.addAll(Arrays.asList(Data.EXPERTISES[i]));
        }
        String[] allExpertiseArray = allExpertiseList.toArray(String[]::new);
        // insert expertise
        generateInsertScript("5.insert_expertise.sql", "expertises", allExpertiseArray);
        // link employees with expertises
        generateEmployeeExpertise();
        // insert employee leaves in 2025
        generateLeavesInSpecificYear(2025);
        // insert employee bonus in 2025
        generateBonusInSpecificYear(2025);
    }
}
