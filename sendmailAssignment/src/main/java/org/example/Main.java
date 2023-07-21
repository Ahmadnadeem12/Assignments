package org.example;

import org.example.email.EmailSender;

import java.sql.*;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        EmailSender gEmailSender= new EmailSender();
        String to = "ahmed.nadeem@venturedive.com";
        String from = "anadeem443122@gmail.com";
        String subject="Sending mail using gmail";
        String text="How are you";
        boolean isEmailSent=gEmailSender.sendEmail(to,from,subject,text);
        if(isEmailSent){
            System.out.println("Email send succesfully");
        }else{
            System.out.println("There is a problem in sendingemail");
        }

//db
        String url = "jdbc:mysql://localhost:3306/EmailSend";
        String username = "root";
        String password = "root";

        try (Connection connection = DriverManager.getConnection(url, username, password)) {

            String selectUserQuery = "SELECT id FROM User WHERE id = ?";
            PreparedStatement selectUserStatement = connection.prepareStatement(selectUserQuery);
            selectUserStatement.setInt(1, 1); // Set the primary key value to check
            ResultSet resultSet = selectUserStatement.executeQuery();

            if (resultSet.next()) {

                String insertProgressQuery = "INSERT INTO User_Progress (user_id, email_subject, email_body, success_status) " +
                        "VALUES (?, ?, ?, ?)";
                PreparedStatement progressStatement = connection.prepareStatement(insertProgressQuery);
                progressStatement.setInt(1, 1);
                progressStatement.setString(2, "Sending mail using gmail"); // Replace with the actual email subject
                progressStatement.setString(3, "How are you"); // Replace with the actual email body
                if (isEmailSent) {
                    progressStatement.setString(4, "success");
                } else {
                    progressStatement.setString(4, "failure");
                }
                int rowsAffected = progressStatement.executeUpdate();

                if (rowsAffected > 0) {
                    System.out.println("Email response saved successfully.");
                } else {
                    System.out.println("Failed to save email response.");
                }
            } else {
                System.out.println("User having id=1 does not exist in the User table.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        try (Connection connection = DriverManager.getConnection(url, username, password)) {

            try (Statement selectStatement = connection.createStatement()) {
                ResultSet resultSet = selectStatement.executeQuery("SELECT * FROM User;");
                while (resultSet.next()) {
                    int userId = resultSet.getInt("id");
                    String name = resultSet.getString("name");
                    String email = resultSet.getString("email");
                    System.out.println("ID: " + userId + ", Name: " + name + ", Email: " + email);
                }
            }

            Scanner scanner = new Scanner(System.in);
            System.out.println("Enter the ID of the user to send an email:");
            int userId = scanner.nextInt();
            String emailTo = "";
            String emailSubject = "Sending mail";
            String emailBody = "Hello How are you";

            String getEmailQuery = "SELECT email FROM User WHERE id = ?";
            try (PreparedStatement getEmailStatement = connection.prepareStatement(getEmailQuery)) {
                getEmailStatement.setInt(1, userId);
                try (ResultSet emailResult = getEmailStatement.executeQuery()) {
                    if (emailResult.next()) {
                        emailTo = emailResult.getString("email");
                    }
                }
            }
            if (emailTo.isEmpty()) {
                System.out.println("User with ID " + userId + " does not exist");
                return;
            }

            isEmailSent = gEmailSender.sendEmail(to,from,subject,text);
            String insertQuery = "INSERT INTO User_Progress (user_id, email_subject, email_body, success_status) " +
                    "VALUES (?, ?, ?, ?)";
            try (PreparedStatement progressStatement = connection.prepareStatement(insertQuery)) {
                progressStatement.setInt(1, userId);
                progressStatement.setString(2, emailSubject);
                progressStatement.setString(3, emailBody);
                progressStatement.setString(4, isEmailSent ? "success" : "failure");
                progressStatement.executeUpdate();
            }
            System.out.println("Response saved successfully.");

            if (isEmailSent) {
                System.out.println("Email sent successfully do you want to see the report? (yes/no)");
                String response = scanner.next();
                if (response.equalsIgnoreCase("yes")) {
                    String reportQuery = "SELECT * FROM User_Progress WHERE user_id = ? ORDER BY id DESC LIMIT 1";
                    try (PreparedStatement reportStatement = connection.prepareStatement(reportQuery)) {
                        reportStatement.setInt(1, userId);
                        try (ResultSet reportResult = reportStatement.executeQuery()) {
                            if (reportResult.next()) {
                                String reportSubject = reportResult.getString("email_subject");
                                String reportBody = reportResult.getString("email_body");
                                String reportStatus = reportResult.getString("success_status");
                                System.out.println("Last Sent Email Report:");
                                System.out.println("subject: " + reportSubject);
                                System.out.println("text: " + reportBody);
                                System.out.println("status: " + reportStatus);
                            }
                        }
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}