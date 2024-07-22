import java.io.*;
import java.net.*;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Server {
    private static final int PORT = 9999;
    private static Connection connection;

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server started and listening on port " + PORT);
            initializeDatabase();

            while (true) {
                Socket clientSocket = serverSocket.accept();
                new ClientHandler(clientSocket).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            closeDatabaseConnection();
        }
    }

    private static void initializeDatabase() {
        try {
            connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/mathematicschallenge", "root", "");
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

    public static Connection getConnection() {
        return connection;
    }
}

class ClientHandler extends Thread {
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            String request;
            while ((request = in.readLine()) != null) {
                System.out.println("Received from client: " + request);
                processClientRequest(request);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                in.close();
                out.close();
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void processClientRequest(String request) {
        String[] parts = request.split(":", 2);
        String command = parts[0];
        String data = parts.length > 1 ? parts[1] : "";

        switch (command.toLowerCase()) {
            case "register":
                out.println(handleRegister(data));
                break;
            case "login":
                out.println(handleLogin(data));
                break;
            case "viewchallenges":
                out.println(handleViewChallenge(data));
                break;
            case "attemptchallenge":
                out.println(handleAttemptChallenge(data));
                break;
            case "nextquestion":
                out.println(handleNextQuestion(data));
                break;
            case "submitanswer":
                out.println(handleSubmitAnswer(data));
                break;
            case "viewparticipant":
                handleViewParticipant();
                break;
            case "confirmparticipant":
                out.println(handleConfirmParticipant(data));
                break;
            default:
                out.println("Invalid option. Please try again.");
                break;
        }
    }

    private String handleRegister(String data) {
        String[] fields = data.split(";");
        if (fields.length != 8) {
            return "Invalid registration data.";
        }

        String username = fields[0];
        if (isUserInDatabase(username)) {
            return "Username already exists.";
        }

        User newUser = new User(username, fields[1], fields[2], fields[3], fields[4], fields[5], fields[6], fields[7]);
        if ("pupil".equals(newUser.getRole())) {
            savePupilToDatabase(newUser);
        } else if ("teacher".equals(newUser.getRole())) {
            saveTeacherToDatabase(newUser);
        }
        System.out.println("Registered new user: " + username);

        return "Registration successful.";
    }

    private String handleLogin(String data) {
        String[] fields = data.split(";");
        if (fields.length != 2) {
            return "Invalid login data.";
        }

        String username = fields[0];
        String password = fields[1];

        try {
            String query = "SELECT * FROM students WHERE username = ? AND password = ? " +
                           "UNION SELECT * FROM schoolrepresentatives WHERE username = ? AND password = ?";
            try (PreparedStatement statement = Server.getConnection().prepareStatement(query)) {
                statement.setString(1, username);
                statement.setString(2, password);
                statement.setString(3, username);
                statement.setString(4, password);

                try (ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next()) {
                        String role = resultSet.getString("role");
                        System.out.println("Debug: Role fetched from DB - " + role); // Debug statement added
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

    // private String handleViewChallenge(String data) {
    //     String[] fields = data.split(";");
    //     if (fields.length != 1) {
    //         return "Invalid view challenge data.";
    //     }

    //     String username = fields[0];
    //     User user = getUserFromDatabase(username);
    //     if (user == null) {
    //         return "User not found.";
    //     }

    //     if ("teacher".equals(user.getRole())) {
    //         return getStudentUsernames();
    //     } else {
    //         return "Only teachers can view challenges.";
    //     }
    // }



    private String handleViewChallenge(String data) {
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        String combinedString = "";

        try {
            // Establishing a statement
            conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/mathematicschallenge", "root", "");

            // Creating a statement
            stmt = conn.createStatement();

            // Executing a query
            String sql = "SELECT * FROM challenge";
            rs = stmt.executeQuery(sql);

            List<String> dynamicArray = new ArrayList<>();

            while (rs.next()) {
                int ChallengeNumber = rs.getInt("ChallengeNumber");
                String ChallengeName = rs.getString("ChallengeName");
                java.sql.Date OpeningDate = rs.getDate("OpeningDate");
                java.sql.Date ClosingDate = rs.getDate("ClosingDate");
                int Duration = rs.getInt("Duration");
                LocalDate today = LocalDate.now();
                LocalDate CloseDate = ClosingDate.toLocalDate();
                String Status;

                if (today.isAfter(CloseDate)) {
                    Status = "Closed";
                } else {
                    Status = "Open";
                }

                String stored = ChallengeNumber + " " + ChallengeName + " " + OpeningDate + " " + ClosingDate + " " + Duration + " " + Status;
                dynamicArray.add(stored);
            }

            // Combine all elements of dynamicArray into a single string
            combinedString = String.join("\n", dynamicArray);

            // Print or use the combined string
           // System.out.println(combinedString);

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            // Closing the connections
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return combinedString;
    }

    private String getStudentUsernames() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getStudentUsernames'");
    }

    private String handleAttemptChallenge(String data) {
        return "Attempting challenge ID: " + data;
    }

    private String handleNextQuestion(String data) {
        return "Next question for challenge ID: " + data;
    }

    private String handleSubmitAnswer(String data) {
        return "Answer submitted for: " + data;
    }

    private void handleViewParticipant() {
        StringBuilder result = new StringBuilder("Student Usernames:\n");
        String query = "SELECT username FROM students WHERE role = 'pupil'";

        try (PreparedStatement statement = Server.getConnection().prepareStatement(query);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                String username = resultSet.getString("username");
                result.append(username).append("\n");
            }

            if (result.length() == "Student Usernames:\n".length()) {
                out.println("No students found.");
            } else {
                out.println(result.toString());
            }

        } catch (SQLException e) {
            e.printStackTrace();
            out.println("An error occurred while retrieving student usernames.");
        }
        out.println("END"); // End marker to indicate the end of the response
    }

    private String handleConfirmParticipant(String data) {
        return "Participant confirmed for ID: " + data;
    }

    private boolean isUserInDatabase(String username) {
        String query = "SELECT COUNT(*) FROM students WHERE username = ? " +
                       "UNION SELECT COUNT(*) FROM schoolrepresentatives WHERE username = ?";
        try (PreparedStatement statement = Server.getConnection().prepareStatement(query)) {
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

    private void savePupilToDatabase(User user) {
        String query = "INSERT INTO students (username, password, first_name, last_name, email, dob, school_reg_number, role) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement statement = Server.getConnection().prepareStatement(query)) {
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

    private void saveTeacherToDatabase(User user) {
        String query = "INSERT INTO schoolrepresentatives (username, password, first_name, last_name, email, dob, school_reg_number, role) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement statement = Server.getConnection().prepareStatement(query)) {
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

    private User getUserFromDatabase(String username) {
        String query = "SELECT * FROM students WHERE username = ? " +
                       "UNION SELECT * FROM schoolrepresentatives WHERE username = ?";
        try (PreparedStatement statement = Server.getConnection().prepareStatement(query)) {
            statement.setString(1, username);
            statement.setString(2, username);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return new User(
                        resultSet.getString("username"),
                        resultSet.getString("password"),
                        resultSet.getString("first_name"),
                        resultSet.getString("last_name"),
                        resultSet.getString("email"),
                        resultSet.getString("dob"),
                        resultSet.getString("school_reg_number"),
                        resultSet.getString("role")
                    );
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
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

class Challenge {
    private final int id;
    private final String title;
    private final List<Question> questions;

    public Challenge(int id, String title, List<Question> questions) {
        this.id = id;
        this.title = title;
        this.questions = questions;
    }

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public List<Question> getQuestions() {
        return questions;
    }
}

class Question {
    private final int id;
    private final String questionText;
    private final String correctAnswer;

    public Question(int id, String questionText, String correctAnswer) {
        this.id = id;
        this.questionText = questionText;
        this.correctAnswer = correctAnswer;
    }

    public int getId() {
        return id;
    }

    public String getQuestionText() {
        return questionText;
    }

    public String getCorrectAnswer() {
        return correctAnswer;
    }
}
