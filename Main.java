package model;

import java.io.Serializable;

/**
 * Represents an Employee with basic details and salary information.
 */
public class Employee implements Serializable {
    private static final long serialVersionUID = 1L;
    private String id;
    private String name;
    private String department;
    private double baseSalary;
    private double bonus;
    private double deduction;

    /**
     * Constructs an Employee object with the given details.
     *
     * @param id          the unique identifier of the employee
     * @param name        the name of the employee
     * @param department  the department the employee belongs to
     * @param baseSalary  the base salary of the employee (must be non-negative)
     * @throws IllegalArgumentException if any parameter is invalid
     */
    public Employee(String id, String name, String department, double baseSalary) {
        if (id.isEmpty() || name.isEmpty() || department.isEmpty() || baseSalary < 0) {
            throw new IllegalArgumentException("Invalid Employee details!");
        }
        this.id = id;
        this.name = name;
        this.department = department;
        this.baseSalary = baseSalary;
        this.bonus = 0;
        this.deduction = 0;
    }

    // Getters and Setters

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDepartment() {
        return department;
    }

    public double getBaseSalary() {
        return baseSalary;
    }

    public double getBonus() {
        return bonus;
    }

    public double getDeduction() {
        return deduction;
    }

    public double getSalary() {
        return baseSalary + bonus - deduction;
    }

    public void setBonus(double bonus) {
        if (bonus < 0) {
            throw new IllegalArgumentException("Bonus cannot be negative.");
        }
        this.bonus = bonus;
    }

    public void setDeduction(double deduction) {
        if (deduction < 0) {
            throw new IllegalArgumentException("Deduction cannot be negative.");
        }
        this.deduction = deduction;
    }

    @Override
    public String toString() {
        return "ID: " + id + ", Name: " + name + ", Department: " + department + ", Salary: $" + String.format("%.2f", getSalary());
    }
}

// EmployeePortal.java
package service;

import model.Employee;
import java.io.*;
import java.util.*;

/**
 * Handles employee management functionalities such as adding, removing, and updating employees.
 */
public class EmployeePortal {
    private Map<String, Employee> employees;
    private List<String> auditLogs;
    private final String adminUsername = "admin";
    private final String adminPassword = "admin123";
    private boolean loggedIn;

    public EmployeePortal() {
        employees = new HashMap<>();
        auditLogs = new ArrayList<>();
        loggedIn = false;
    }

    public boolean login(String username, String password) {
        if (adminUsername.equals(username) && adminPassword.equals(password)) {
            loggedIn = true;
            return true;
        }
        return false;
    }

    public void logout() {
        loggedIn = false;
    }

    public boolean isLoggedIn() {
        return loggedIn;
    }

    public void addEmployee(Employee employee) {
        if (loggedIn) {
            if (employees.containsKey(employee.getId())) {
                System.out.println("Employee with this ID already exists.");
                return;
            }
            employees.put(employee.getId(), employee);
            logAudit("Employee " + employee.getName() + " added with ID: " + employee.getId());
        } else {
            System.out.println("You must be logged in as an admin to perform this action.");
        }
    }

    public void removeEmployee(String employeeId) {
        if (loggedIn) {
            Employee removedEmployee = employees.remove(employeeId);
            if (removedEmployee != null) {
                logAudit("Employee " + removedEmployee.getName() + " removed with ID: " + employeeId);
            } else {
                System.out.println("Employee with ID " + employeeId + " not found.");
            }
        } else {
            System.out.println("You must be logged in as an admin to perform this action.");
        }
    }

    public Employee getEmployee(String employeeId) {
        return employees.get(employeeId);
    }

    public void updateSalary(String employeeId, double bonus, double deduction) {
        if (loggedIn) {
            Employee employee = employees.get(employeeId);
            if (employee != null) {
                employee.setBonus(bonus);
                employee.setDeduction(deduction);
                logAudit("Salary updated for employee " + employee.getName() + " with ID: " + employeeId);
            } else {
                System.out.println("Employee with ID " + employeeId + " not found.");
            }
        } else {
            System.out.println("You must be logged in as an admin to perform this action.");
        }
    }

    public void displayAllEmployees() {
        if (employees.isEmpty()) {
            System.out.println("No employees found.");
        } else {
            for (Employee employee : employees.values()) {
                System.out.println(employee);
            }
        }
    }

    public void viewAuditLogs() {
        if (loggedIn) {
            if (auditLogs.isEmpty()) {
                System.out.println("No audit logs available.");
            } else {
                for (String log : auditLogs) {
                    System.out.println(log);
                }
            }
        } else {
            System.out.println("You must be logged in as an admin to perform this action.");
        }
    }

    private void logAudit(String message) {
        auditLogs.add(message);
    }

    public void saveToFile() {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream("employees.dat"))) {
            out.writeObject(employees);
            out.writeObject(auditLogs);
            System.out.println("Data saved successfully.");
        } catch (IOException e) {
            System.out.println("Error saving data: " + e.getMessage());
        }
    }

    public void loadFromFile() {
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream("employees.dat"))) {
            employees = (Map<String, Employee>) in.readObject();
            auditLogs = (List<String>) in.readObject();
            System.out.println("Data loaded successfully.");
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Error loading data: " + e.getMessage());
        }
    }
}

// Main.java
package ui;

import model.Employee;
import service.EmployeePortal;
import java.util.Scanner;

public class Main {
    private static final Scanner scanner = new Scanner(System.in);
    private static final EmployeePortal portal = new EmployeePortal();

    public static void main(String[] args) {
        portal.loadFromFile();
        while (true) {
            displayMenu();
            int choice = getUserChoice();
            switch (choice) {
                case 1: // Admin Login
                    adminLogin();
                    break;
                case 2: // Logout
                    logout();
                    break;
                case 3: // Add Employee
                    addEmployee();
                    break;
                case 4: // Remove Employee
                    removeEmployee();
                    break;
                case 5: // Update Salary
                    updateSalary();
                    break;
                case 6: // View All Employees
                    viewAllEmployees();
                    break;
                case 7: // View Audit Logs
                    viewAuditLogs();
                    break;
                case 8: // Exit
                    portal.saveToFile();
                    System.out.println("Exiting the portal. Goodbye!");
                    scanner.close();
                    return;
                default:
                    System.out.println("Invalid option, please try again.");
            }
        }
    }

    private static void displayMenu() {
        System.out.println("\nEmployee Portal");
        if (!portal.isLoggedIn()) {
            System.out.println("1. Admin Login");
        } else {
            System.out.println("2. Logout");
            System.out.println("3. Add Employee");
            System.out.println("4. Remove Employee");
            System.out.println("5. Update Salary");
            System.out.println("6. View All Employees");
            System.out.println("7. View Audit Logs");
        }
        System.out.println("8. Exit");
        System.out.print("Enter your choice: ");
