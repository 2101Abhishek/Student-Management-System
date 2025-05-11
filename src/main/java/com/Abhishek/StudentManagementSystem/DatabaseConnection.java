package com.Abhishek.StudentManagementSystem;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    private static final String URL = "jdbc:mysql://localhost:3306/student_management";
    private static final String USER = "root"; // Change if your MySQL has a different username
    private static final String PASSWORD = "Mysql@123"; // Change if your MySQL has a password

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
