package com.Abhishek.StudentManagementSystem;

import java.sql.*;
import java.util.Scanner;

public class StudentManagementApp {
	private static final Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        while (true) {
            System.out.println("\nStudent Management System");
            System.out.println("1. Add Student");
            System.out.println("2. View All Students");
            System.out.println("3. Update Student");
            System.out.println("4. Delete Student");
            System.out.println("5. Exit");
            System.out.print("Choose an option: ");

            int choice = scanner.nextInt();
            scanner.nextLine(); // Consume newline

            switch (choice) {
                case 1 -> addStudent();
                case 2 -> viewStudents();
                case 3 -> updateStudent();
                case 4 -> deleteStudent();
                case 5 -> {
                    System.out.println("Exiting...");
                    System.exit(0);
                }
                default -> System.out.println("Invalid option!");
            }
        }
    }

    private static void addStudent() {
        System.out.println("\n--- Add New Student ---");
        
        // Get and validate name
        System.out.print("Enter name: ");
        String name = scanner.nextLine().trim();
        if (name.isEmpty()) {
            System.out.println("❌ Error: Name cannot be empty!");
            return;
        }

        // Get and validate email
        System.out.print("Enter email: ");
        String email = scanner.nextLine().trim();
        if (email.isEmpty()) {
            System.out.println("❌ Error: Email cannot be empty!");
            return;
        }
        if (!email.matches("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$")) {
            System.out.println("❌ Error: Invalid email format!");
            return;
        }

        // Get and validate course
        System.out.print("Enter course: ");
        String course = scanner.nextLine().trim();
        if (course.isEmpty()) {
            System.out.println("❌ Error: Course cannot be empty!");
            return;
        }

        // SQL to insert new student
        String sql = "INSERT INTO students (name, email, course) VALUES (?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, name);
            stmt.setString(2, email);
            stmt.setString(3, course);
            
            int rowsAffected = stmt.executeUpdate();
            
            if (rowsAffected > 0) {
                System.out.println("✅ Student added successfully!");
            } else {
                System.out.println("❌ Failed to add student.");
            }
            
        } catch (SQLException e) {
            if (e.getErrorCode() == 1062) { // MySQL duplicate entry error code
                System.out.println("❌ Error: Email '" + email + "' already exists!");
            } else {
                System.out.println("❌ Database Error: " + e.getMessage());
            }
        }
    }
    private static void viewStudents() {
        String sql = "SELECT * FROM students";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            System.out.println("\nID\tName\tEmail\tCourse");
            while (rs.next()) {
                System.out.println(
                    rs.getInt("id") + "\t" +
                    rs.getString("name") + "\t" +
                    rs.getString("email") + "\t" +
                    rs.getString("course")
                );
            }
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private static void updateStudent() {
        // First show current students
        System.out.println("\nCurrent Students:");
        viewStudents();
        
        // Then ask for ID to update
        System.out.print("\nEnter student ID to update: ");
        int id = scanner.nextInt();
        scanner.nextLine(); // Consume newline

        // Get current student data
        String currentName = "";
        String currentEmail = "";
        String currentCourse = "";
        
        String selectSql = "SELECT * FROM students WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(selectSql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                currentName = rs.getString("name");
                currentEmail = rs.getString("email");
                currentCourse = rs.getString("course");
            } else {
                System.out.println("No student found with ID: " + id);
                return;
            }
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
            return;
        }

        // Get updated information
        System.out.println("\nCurrent Details:");
        System.out.println("Name: " + currentName);
        System.out.println("Email: " + currentEmail);
        System.out.println("Course: " + currentCourse);
        
        System.out.print("\nEnter new name (press Enter to keep current): ");
        String newName = scanner.nextLine();
        if (newName.isEmpty()) {
            newName = currentName;
        }
        
        System.out.print("Enter new email (press Enter to keep current): ");
        String newEmail = scanner.nextLine();
        if (newEmail.isEmpty()) {
            newEmail = currentEmail;
        }
        
        System.out.print("Enter new course (press Enter to keep current): ");
        String newCourse = scanner.nextLine();
        if (newCourse.isEmpty()) {
            newCourse = currentCourse;
        }

        // Update the student
        String updateSql = "UPDATE students SET name = ?, email = ?, course = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(updateSql)) {
            stmt.setString(1, newName);
            stmt.setString(2, newEmail);
            stmt.setString(3, newCourse);
            stmt.setInt(4, id);
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Student updated successfully!");
            } else {
                System.out.println("Failed to update student.");
            }
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private static void deleteStudent() {
        System.out.println("\n--- Delete Student ---");
        viewStudents(); // Show current students
        
        // Get and validate ID input
        System.out.print("Enter student ID to delete (or 0 to cancel): ");
        
        int id;
        try {
            id = Integer.parseInt(scanner.nextLine());
        } catch (NumberFormatException e) {
            System.out.println("❌ Error: ID must be a number!");
            return;
        }
        
        if (id == 0) {
            System.out.println("❌ Deletion cancelled.");
            return;
        }

        // First verify student exists
        String checkSql = "SELECT id FROM students WHERE id = ?";
        String deleteSql = "DELETE FROM students WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement checkStmt = conn.prepareStatement(checkSql);
             PreparedStatement deleteStmt = conn.prepareStatement(deleteSql)) {
            
            // Check if student exists
            checkStmt.setInt(1, id);
            ResultSet rs = checkStmt.executeQuery();
            
            if (!rs.next()) {
                System.out.println("❌ Error: No student found with ID " + id);
                return;
            }
            
            // Confirm deletion
            System.out.print("Are you sure you want to delete student ID " + id + "? (y/n): ");
            String confirmation = scanner.nextLine().trim().toLowerCase();
            
            if (!confirmation.equals("y")) {
                System.out.println("❌ Deletion cancelled.");
                return;
            }
            
            // Execute deletion
            deleteStmt.setInt(1, id);
            int rowsAffected = deleteStmt.executeUpdate();
            
            if (rowsAffected > 0) {
                System.out.println("✅ Student deleted successfully!");
            } else {
                System.out.println("❌ Failed to delete student.");
            }
            
        } catch (SQLException e) {
            System.out.println("❌ Database Error: " + e.getMessage());
        }
    }
}