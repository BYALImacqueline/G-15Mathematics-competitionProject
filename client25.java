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
        System.out.println("register");
        System.out.println("login");
        System.out.println("exit");
        System.out.print("Enter your command: ");
        String command = scanner.nextLine().toLowerCase();

        switch (command) {
            case "register":
                register(scanner, out, in);
                break;
            case "login":
                login(scanner, out, in);
                break;
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
        String[] prompts = {"Enter username: ", "Enter password: ", "Enter first name: ", "Enter last name: ", 
                            "Enter email: ", "Enter date of birth (YYYY-MM-DD): ", 
                            "Enter school registration number: ", "Enter role (pupil/teacher): "};
        String[] userDetails = new String[8];
        for (int i = 0; i < prompts.length; i++) {
            System.out.print(prompts[i]);
            userDetails[i] = scanner.nextLine();
        }

        // Validate role
        if (!"pupil".equalsIgnoreCase(userDetails[7]) && !"teacher".equalsIgnoreCase(userDetails[7])) {
            System.out.println("Invalid role. Please enter 'pupil' or 'teacher'.");
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
        System.out.println("viewchallenge");
        System.out.println("attemptchallenge");
        System.out.println("nextquestion");
        System.out.println("submitanswer");
        System.out.println("logout");
        System.out.print("Enter your command: ");
        String command = scanner.nextLine().toLowerCase();

        switch (command) {
            case "viewchallenge":
                handleCommand("viewchallenge", currentUsername, out, in);
                break;
            case "attemptchallenge":
                handleCommand("attemptchallenge", scanner, out, in);
                break;
            case "nextquestion":
                handleCommand("nextquestion", scanner, out, in);
                break;
            case "submitanswer":
                handleCommand("submitanswer", scanner, out, in);
                break;
            case "logout":
                logout();
                break;
            default:
                System.out.println("Invalid command. Please try again.");
                break;
        }
    }

    private static void showTeacherMenu(Scanner scanner, PrintWriter out, BufferedReader in) throws IOException {
        System.out.println("Teacher Menu: Type one of the following commands:");
        System.out.println("viewparticipant");
        System.out.println("confirmparticipant");
        System.out.println("logout");
        System.out.print("Enter your command: ");
        String command = scanner.nextLine().toLowerCase();

        switch (command) {
            case "viewparticipant":
                handleCommand("viewparticipant", currentRole, out, in);
                break;
            case "confirmparticipant":
                handleCommand("confirmparticipant", scanner, out, in);
                break;
            case "logout":
                logout();
                break;
            default:
                System.out.println("Invalid command. Please try again.");
                break;
        }
    }

    private static void handleCommand(String commandType, Scanner scanner, PrintWriter out, BufferedReader in) throws IOException {
        System.out.print("Enter the necessary information: ");
        String input = scanner.nextLine();
        String request = commandType + ":" + input;
        out.println(request);
        String response = in.readLine();
        System.out.println("Response from server: " + response);
    }

    private static void handleCommand(String commandType, String data, PrintWriter out, BufferedReader in) throws IOException {
        String request = commandType + ":" + data;
        out.println(request);
        String response = in.readLine();
        System.out.println("Response from server: " + response);
    }

    private static void logout() {
        currentUsername = null;
        currentRole = null;
        System.out.println("Logged out successfully.");
    }
}
