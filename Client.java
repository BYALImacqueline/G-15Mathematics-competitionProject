import java.io.*;
import java.net.*;
import java.util.Scanner;

public class Client {
    private static String currentUsername;
    private static String currentRole;

    public static void main(String[] args) {
        String host = "localhost";
        int port = 9999;

        try (Socket socket = new Socket(host, port);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             Scanner scanner = new Scanner(System.in)) {

            System.out.println("Connected to the server.");

            while (true) {
                if (currentUsername == null) {
                    showInitialMenu(scanner, out, in);
                } else if ("pupil".equalsIgnoreCase(currentRole)) {
                    showPupilMenu(scanner, out, in);
                } else if ("teacher".equalsIgnoreCase(currentRole)) {
                    showTeacherMenu(scanner, out, in);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void showInitialMenu(Scanner scanner, PrintWriter out, BufferedReader in) throws IOException {
        System.out.println("Welcome to the MCC Application:");
        System.out.println("1. Register");
        System.out.println("2. Login");
        System.out.println("3. Exit");
        System.out.print("Enter your command: ");
        String command = scanner.nextLine().toLowerCase();

        switch (command) {
            case "1":
            case "register":
                register(scanner, out, in);
                break;
            case "2":
            case "login":
                login(scanner, out, in);
                break;
            case "3":
            case "exit":
                System.out.println("Exiting...");
                System.exit(0);
                break;
            default:
                System.out.println("Invalid command. Please try again.");
                break;
        }
    }

    private static void register(Scanner scanner, PrintWriter out, BufferedReader in) throws IOException {
        System.out.print("Enter registration details (username password firstname lastname email dob schoolregistrationnumber role): ");
        String input = scanner.nextLine();
        String[] userDetails = input.split(" ");

        if (userDetails.length != 8) {
            System.out.println("Invalid input. Please enter all details in the correct format.");
            return;
        }

        String request = String.format("register:%s;%s;%s;%s;%s;%s;%s;%s", (Object[]) userDetails);
        out.println(request);

        String response = in.readLine();
        System.out.println("Response from server: " + response);
    }

    private static void login(Scanner scanner, PrintWriter out, BufferedReader in) throws IOException {
        System.out.print("Enter username: ");
        String username = scanner.nextLine();
        System.out.print("Enter password: ");
        String password = scanner.nextLine();

        String request = String.format("login:%s;%s", username, password);
        out.println(request);

        String response = in.readLine();
        System.out.println("Response from server: " + response);

        if (response.startsWith("Login successful:")) {
            String[] parts = response.split(":");
            currentUsername = parts[1];
            currentRole = parts[2];
            System.out.println("Logged in as " + currentUsername + " with role " + currentRole);
        } else {
            System.out.println("Login failed. Please try again.");
        }
    }

    private static void showPupilMenu(Scanner scanner, PrintWriter out, BufferedReader in) throws IOException {
        System.out.println("Pupil Menu: Type one of the following commands:");
        System.out.println("1. viewChallenges");
        System.out.println("2. Attempt Challenge");
        System.out.println("3. Logout");
        System.out.print("Enter your command: ");
        String command = scanner.nextLine().toLowerCase();

        switch (command) {
            case "1":
            case "viewChallenges":
                //handleCommand("viewChallenges", scanner, out, in);
                viewChallenges("viewChallenges",out,in);
                break;
            case "2":
            case "attempt challenge":
                attemptChallenge(scanner, out, in);
                break;
            case "3":
            case "logout":
                logout();
                break;
            default:
                System.out.println("Invalid command. Please try again.");
                break;
        }
    }



    private static void viewChallenges(String command, PrintWriter out, BufferedReader in) throws IOException {
        String data = ""; 
    
        String request = String.format("%s:%s", command, data);
        out.println(request);
    

        String line;
        while ((line = in.readLine()) != null) {
            System.out.println("Received from server: " + line);
        }


    }
    
    
    
    
    
    




    private static void showTeacherMenu(Scanner scanner, PrintWriter out, BufferedReader in) throws IOException {
        System.out.println("Teacher Menu: Type one of the following commands:");
        System.out.println("1. View Participants");
        System.out.println("2. Confirm Participant");
        System.out.println("3. Logout");
        System.out.print("Enter your command: ");
        String command = scanner.nextLine().toLowerCase();

        System.out.println("Debug: Current Role - " + currentRole); // Debug statement added

        switch (command) {
            case "1":
            case "view participants":
                handleCommandWithRole("viewparticipant", currentRole, out, in);
                break;
            case "2":
            case "confirm participant":
                confirmParticipant(scanner, out, in);
                break;
            case "3":
            case "logout":
                logout();
                break;
            default:
                System.out.println("Invalid command. Please try again.");
                break;
        }
    }

    private static void handleCommand(String commandType, Scanner scanner, PrintWriter out, BufferedReader in) throws IOException {
        System.out.print("Enter additional data if required (or press Enter to skip): ");
        String data = scanner.nextLine();

        String request = String.format("%s:%s", commandType, data);
        out.println(request);

        String response = in.readLine();
        System.out.println("Response from server: " + response);
    }

    private static void handleCommandWithRole(String commandType, String role, PrintWriter out, BufferedReader in) throws IOException {
    String request = String.format("%s:%s", commandType, role);
    out.println(request);

    // Read the response from the server until the end marker is encountered
    StringBuilder responseBuilder = new StringBuilder();
    String line;
    while ((line = in.readLine()) != null) {
        if (line.trim().equals("END")) {
            break;
        }
        responseBuilder.append(line).append(System.lineSeparator());
    }
    String response = responseBuilder.toString();
    System.out.println("Response from server: " + response);
}



    private static void attemptChallenge(Scanner scanner, PrintWriter out, BufferedReader in) throws IOException {
        System.out.print("Enter challenge ID to attempt: ");
        String challengeId = scanner.nextLine();
        handleCommand("attemptchallenge:" + challengeId, scanner, out, in);

        while (true) {
            System.out.print("Enter 'next' to get the next question or 'submit' to submit your answer: ");
            String action = scanner.nextLine().toLowerCase();

            if ("next".equals(action)) {
                handleCommand("nextquestion:" + challengeId, scanner, out, in);
            } else if ("submit".equals(action)) {
                System.out.print("Enter your answer: ");
                String answer = scanner.nextLine();
                handleCommand("submitanswer:" + answer, scanner, out, in);
                break; // Exit the loop after submitting the answer
            } else {
                System.out.println("Invalid command. Please try again.");
            }
        }
    }

    private static void confirmParticipant(Scanner scanner, PrintWriter out, BufferedReader in) throws IOException {
        System.out.print("Enter participant ID to confirm: ");
        String participantId = scanner.nextLine();
        handleCommand("confirmparticipant:" + participantId, scanner, out, in);
    }

    private static void logout() {
        currentUsername = null;
        currentRole = null;
        System.out.println("Logged out successfully.");
    }
}
