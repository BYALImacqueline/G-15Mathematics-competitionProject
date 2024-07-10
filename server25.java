import java.io.*;
import java.net.*;
import java.sql.*;
import java.util.*;

public class Server {
    private static final Map<String, User> users = new HashMap<>();
    private static Connection connection;

    public static void main(String[] args) {
        int port = 9999;

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Server listening on port " + port);
            initializeDatabase();

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Client connected: " + clientSocket.getInetAddress());

                new Thread(() -> handleClientRequest(clientSocket)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            closeDatabaseConnection();
        }
    }

    private static void handleClientRequest(Socket clientSocket) {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
             PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)) {

            String clientRequest;
            while ((clientRequest = in.readLine()) != null) {
                System.out.println("Received from client: " + clientRequest);
                String response = processClientRequest(clientRequest);
                out.println(response);
            }

            System.out.println("Client disconnected: " + clientSocket.getInetAddress());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String processClientRequest(String request) {
        String[] parts = request.split(":", 2);
        String command = parts[0];
        String data = parts.length > 1 ? parts[1] : "";

        switch (command.toLowerCase()) {
            case "register":
                return handleRegister(data);
            case "login":
                return handleLogin(data);
            case "viewchallenge":
                return handleViewChallenge(data);
            case "attemptchallenge":
                return handleAttemptChallenge(data);
            case "nextquestion":
                return handleNextQuestion(data);
            case "submitanswer":
                return handleSubmitAnswer(data);
            case "viewparticipant":
                return handleViewParticipant(data);
            case "confirmparticipant":
                return handleConfirmParticipant(data);
            default:
                return "Invalid option. Please try again.";
        }
    }

    private static String handleRegister(String data) {
        String[] fields = data.split(";");
        if (fields.length != 8) {
            return "Invalid registration data.";
        }

        String username = fields[0];
        if (users.containsKey(username) || isUserInDatabase(username)) {
            return "Username already exists.";
        }

        User newUser = new User(username, fields[1], fields[2], fields[3], fields[4], fields[5], fields[6], fields[7]);
        users.put(username, newUser);
        if ("pupil".equals(newUser.getRole())) {
            savePupilToDatabase(newUser);
        } else if ("teacher".equals(newUser.getRole())) {
            saveTeacherToDatabase(newUser);
        }
        System.out.println("Registered new user: " + username);

        return "Registration successful.";
    }

    private static String handleLogin(String data) {
        String[] fields = data.split(";");
        if (fields.length != 2) {
            return "Invalid login data.";
        }

        String username = fields[0];
        String password = fields[1];

        try {
            String query = "SELECT * FROM students WHERE username = ? AND password = ? " +
                           "UNION SELECT * FROM schoolrepresentatives WHERE username = ? AND password = ?";
            try (PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setString(1, username);
                statement.setString(2, password);
                statement.setString(3, username);
                statement.setString(4, password);

                try (ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next()) {
                        String role = resultSet.getString("role");
                        return "Login successful:" + username + ":" + role;
                    } else {
                        return "Invalid username or password.";
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return "An error occurred during login.";
        }
    }

    private static String handleViewChallenge(String data) {
        String[] fields = data.split(";");
        if (fields.length != 1) {
            return "Invalid view challenge data.";
        }

        String username = fields[0];
        User user = users.get(username);
        if (user == null) {
            return "User not found.";
        }

        if ("teacher".equals(user.getRole())) {
            return getStudentUsernames();
        } else {
            return "Only teachers can view challenges.";
        }
    }

    private static String handleAttemptChallenge(String data) {
        return "Attempting challenge ID: " + data;
    }

    private static String handleNextQuestion(String data) {
        return "Next question for challenge ID: " + data;
    }

    private static String handleSubmitAnswer(String data) {
        return "Answer submitted for: " + data;
    }

    private static String handleViewParticipant(String data) {
        if (data.equals("teacher")) {
            return getStudentUsernames();
        } else {
            return "Only teachers can view participants.";
        }
    }

    private static String handleConfirmParticipant(String data) {
        return "Participant confirmed for ID: " + data;
    }

    private static void initializeDatabase() {
        try {
            connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/myproject123", "root", "");
            // The table creation SQL is now handled by Laravel migrations
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void closeDatabaseConnection() {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private static boolean isUserInDatabase(String username) {
        String query = "SELECT COUNT(*) FROM students WHERE username = ? " +
                       "UNION SELECT COUNT(*) FROM schoolrepresentatives WHERE username = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, username);
            statement.setString(2, username);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    if (resultSet.getInt(1) > 0) {
                        return true;
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private static void savePupilToDatabase(User user) {
        String query = "INSERT INTO students (username, password, first_name, last_name, email, dob, school_reg_number, role) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, user.getUsername());
            statement.setString(2, user.getPassword());
            statement.setString(3, user.getFirstName());
            statement.setString(4, user.getLastName());
            statement.setString(5, user.getEmail());
            statement.setString(6, user.getDob());
            statement.setString(7, user.getSchoolRegNumber());
            statement.setString(8, user.getRole());
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void saveTeacherToDatabase(User user) {
        String query = "INSERT INTO schoolrepresentatives (username, password, first_name, last_name, email, dob, school_reg_number, role) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, user.getUsername());
            statement.setString(2, user.getPassword());
            statement.setString(3, user.getFirstName());
            statement.setString(4, user.getLastName());
            statement.setString(5, user.getEmail());
            statement.setString(6, user.getDob());
            statement.setString(7, user.getSchoolRegNumber());
            statement.setString(8, user.getRole());
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static String getStudentUsernames() {
        StringBuilder result = new StringBuilder("Student Usernames:\n");
        String query = "SELECT username FROM students WHERE role = 'pupil'";

        try (PreparedStatement statement = connection.prepareStatement(query);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                String username = resultSet.getString("username");
                result.append(username).append("\n");
            }

            if (result.length() == 18) { // "Student Usernames:\n".length() == 18
                return "No students found.";
            }

        } catch (SQLException e) {
            e.printStackTrace();
            return "An error occurred while retrieving student usernames.";
        }

        return result.toString();
    }
}

class User {
    private final String username;
    private final String password;
    private final String firstName;
    private final String lastName;
    private final String email;
    private final String dob;
    private final String schoolRegNumber;
    private final String role;

    public User(String username, String password, String firstName, String lastName, String email, String dob, String schoolRegNumber, String role) {
        this.username = username;
        this.password = password;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.dob = dob;
        this.schoolRegNumber = schoolRegNumber;
        this.role = role;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getEmail() {
        return email;
    }

    public String getDob() {
        return dob;
    }

    public String getSchoolRegNumber() {
        return schoolRegNumber;
    }

    public String getRole() {
        return role;
    }
}
