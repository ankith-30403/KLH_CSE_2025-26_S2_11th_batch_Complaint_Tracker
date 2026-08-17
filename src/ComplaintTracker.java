import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ComplaintTracker {

    public static void main(String[] args) throws IOException {
        Scanner sc = new Scanner(System.in);

        // Each complaint is stored in its own text file in this folder.
        Path complaintDirectory = Paths.get("data", "corpus");
        Path resultsDirectory = Paths.get("results");
        Path searchResultsFile = resultsDirectory.resolve("search_results.txt");
        Path notificationsFile = resultsDirectory.resolve("notifications.txt");

        Files.createDirectories(complaintDirectory);
        Files.createDirectories(resultsDirectory);

        String userEmail = "";
        boolean signedIn = false;
        int choice = 0;

        System.out.println("===== COMPLAINT TRACKER =====");

        // Simple sign-in: any correctly formatted email and non-empty password.
        while (!signedIn) {
            System.out.print("Enter email: ");
            userEmail = sc.nextLine().trim();

            System.out.print("Enter password: ");
            String password = sc.nextLine().trim();

            if (userEmail.contains("@") && !password.isEmpty()) {
                signedIn = true;
                System.out.println("Sign in successful. Welcome, " + userEmail + "!");
            } else {
                System.out.println("Enter a valid email and a non-empty password.\n");
            }
        }

        while (choice != 6) {
            System.out.println("\n===== MENU =====");
            System.out.println("1. Register a New Complaint");
            System.out.println("2. Search Complaints Using Z-Algorithm");
            System.out.println("3. View All Registered Complaints");
            System.out.println("4. Update Complaint Status and Notify User");
            System.out.println("5. View Notifications");
            System.out.println("6. Exit");
            System.out.print("Choose an option: ");

            if (!sc.hasNextInt()) {
                System.out.println("Please enter a number from 1 to 6.");
                sc.nextLine();
                continue;
            }

            choice = sc.nextInt();
            sc.nextLine();

            switch (choice) {
                case 1:
                    System.out.print("Enter Customer ID: ");
                    String customerId = sc.nextLine().trim();

                    System.out.print("Enter Customer Name: ");
                    String customerName = sc.nextLine().trim();

                    System.out.print("Enter Customer Email (press Enter to use sign-in email): ");
                    String customerEmail = sc.nextLine().trim();
                    if (customerEmail.isEmpty()) {
                        customerEmail = userEmail;
                    }

                    System.out.print("Enter Phone Number: ");
                    String phone = sc.nextLine().trim();

                    System.out.print("Enter Address: ");
                    String address = sc.nextLine().trim();

                    System.out.print("Enter Complaint Description: ");
                    String description = sc.nextLine().trim();

                    System.out.println("Choose Priority: 1. Low  2. Medium  3. High");
                    System.out.print("Enter priority number: ");
                    String priority = "Medium";

                    if (sc.hasNextInt()) {
                        int priorityChoice = sc.nextInt();
                        sc.nextLine();
                        if (priorityChoice == 1) {
                            priority = "Low";
                        } else if (priorityChoice == 3) {
                            priority = "High";
                        }
                    } else {
                        sc.nextLine();
                        System.out.println("Invalid input. Medium priority selected.");
                    }

                    int nextNumber = 1;
                    Path complaintFile;
                    do {
                        complaintFile = complaintDirectory.resolve(
                                String.format("complaint_%03d.txt", nextNumber));
                        nextNumber++;
                    } while (Files.exists(complaintFile));

                    String complaintId = String.format("CMP-%03d", nextNumber - 1);
                    String registeredTime = LocalDateTime.now().format(
                            DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss"));

                    String complaintReport =
                            "Complaint ID: " + complaintId + "\n"
                            + "Registered On: " + registeredTime + "\n"
                            + "Customer ID: " + customerId + "\n"
                            + "Customer Name: " + customerName + "\n"
                            + "Customer Email: " + customerEmail + "\n"
                            + "Phone Number: " + phone + "\n"
                            + "Address: " + address + "\n"
                            + "Complaint Description: " + description + "\n"
                            + "Priority: " + priority + "\n"
                            + "Category: Not Categorized\n"
                            + "Department: Not Assigned\n"
                            + "Staff: Not Assigned\n"
                            + "Status: Submitted\n"
                            + "Remarks: Complaint registered successfully.\n"
                            + "Status History: " + registeredTime + " - Submitted\n";

                    Files.writeString(complaintFile, complaintReport,
                            StandardCharsets.UTF_8, StandardOpenOption.CREATE_NEW);

                    System.out.println("Complaint registered successfully.");
                    System.out.println("Complaint ID: " + complaintId);
                    System.out.println("Saved in file: " + complaintFile);
                    break;

                case 2:
                    System.out.print("Enter complaint text to search: ");
                    String pattern = sc.nextLine().trim().toLowerCase(Locale.ROOT);

                    if (pattern.isEmpty()) {
                        System.out.println("Search text cannot be empty.");
                        break;
                    }

                    List<Path> complaintFiles;
                    try (Stream<Path> fileStream = Files.list(complaintDirectory)) {
                        complaintFiles = fileStream
                                .filter(Files::isRegularFile)
                                .filter(path -> path.toString().endsWith(".txt"))
                                .sorted(Comparator.comparing(Path::getFileName))
                                .collect(Collectors.toList());
                    }

                    int matchedFiles = 0;
                    int totalOccurrences = 0;
                    StringBuilder result = new StringBuilder();
                    result.append("Z-ALGORITHM SEARCH RESULT\n");
                    result.append("Search text: ").append(pattern).append("\n");
                    result.append("----------------------------------------\n");

                    for (Path file : complaintFiles) {
                        String text = Files.readString(file, StandardCharsets.UTF_8)
                                .toLowerCase(Locale.ROOT);

                        // Z algorithm searches pattern in the full complaint file.
                        String combined = pattern + "\u0000" + text;
                        int[] z = new int[combined.length()];
                        int left = 0;
                        int right = 0;

                        for (int i = 1; i < combined.length(); i++) {
                            if (i <= right) {
                                z[i] = Math.min(right - i + 1, z[i - left]);
                            }

                            while (i + z[i] < combined.length()
                                    && combined.charAt(z[i])
                                    == combined.charAt(i + z[i])) {
                                z[i]++;
                            }

                            if (i + z[i] - 1 > right) {
                                left = i;
                                right = i + z[i] - 1;
                            }
                        }

                        int occurrencesInFile = 0;
                        for (int i = pattern.length() + 1; i < z.length; i++) {
                            if (z[i] >= pattern.length()) {
                                occurrencesInFile++;
                            }
                        }

                        if (occurrencesInFile > 0) {
                            matchedFiles++;
                            totalOccurrences += occurrencesInFile;
                            result.append(file.getFileName())
                                    .append(" -> ")
                                    .append(occurrencesInFile)
                                    .append(" match(es)\n");
                        }
                    }

                    result.append("----------------------------------------\n");
                    result.append("Files containing this complaint: ")
                            .append(matchedFiles).append("\n");
                    result.append("Total matches: ")
                            .append(totalOccurrences).append("\n");

                    System.out.println("\n" + result);
                    Files.writeString(searchResultsFile, result.toString(),
                            StandardCharsets.UTF_8,
                            StandardOpenOption.CREATE,
                            StandardOpenOption.TRUNCATE_EXISTING);
                    System.out.println("Search result saved in: " + searchResultsFile);
                    break;

                case 3:
                    List<Path> registeredFiles;
                    try (Stream<Path> fileStream = Files.list(complaintDirectory)) {
                        registeredFiles = fileStream
                                .filter(Files::isRegularFile)
                                .filter(path -> path.toString().endsWith(".txt"))
                                .sorted(Comparator.comparing(Path::getFileName))
                                .collect(Collectors.toList());
                    }

                    if (registeredFiles.isEmpty()) {
                        System.out.println("No complaint files have been registered yet.");
                        break;
                    }

                    System.out.println("\n===== REGISTERED COMPLAINT FILES =====");
                    for (Path file : registeredFiles) {
                        System.out.println("\nFile: " + file.getFileName());
                        System.out.println("----------------------------------------");
                        System.out.println(Files.readString(file, StandardCharsets.UTF_8));
                    }
                    break;

                case 4:
                    System.out.print("Enter Complaint ID to update (example: CMP-001): ");
                    String updateId = sc.nextLine().trim();
                    Path selectedFile = null;
                    String selectedText = "";

                    try (Stream<Path> fileStream = Files.list(complaintDirectory)) {
                        List<Path> files = fileStream
                                .filter(Files::isRegularFile)
                                .filter(path -> path.toString().endsWith(".txt"))
                                .collect(Collectors.toList());

                        for (Path file : files) {
                            String fileText = Files.readString(file, StandardCharsets.UTF_8);
                            if (fileText.contains("Complaint ID: " + updateId)) {
                                selectedFile = file;
                                selectedText = fileText;
                                break;
                            }
                        }
                    }

                    if (selectedFile == null) {
                        System.out.println("Complaint ID not found.");
                        break;
                    }

                    System.out.println("Choose updated status:");
                    System.out.println("1. Under Review");
                    System.out.println("2. In Progress");
                    System.out.println("3. Resolved");
                    System.out.println("4. Closed");
                    System.out.print("Enter status number: ");

                    if (!sc.hasNextInt()) {
                        System.out.println("Invalid status input.");
                        sc.nextLine();
                        break;
                    }

                    int statusChoice = sc.nextInt();
                    sc.nextLine();
                    String newStatus = "";

                    if (statusChoice == 1) {
                        newStatus = "Under Review";
                    } else if (statusChoice == 2) {
                        newStatus = "In Progress";
                    } else if (statusChoice == 3) {
                        newStatus = "Resolved";
                    } else if (statusChoice == 4) {
                        newStatus = "Closed";
                    } else {
                        System.out.println("Invalid status number.");
                        break;
                    }

                    System.out.print("Enter update remarks: ");
                    String remarks = sc.nextLine().trim();
                    String updateTime = LocalDateTime.now().format(
                            DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss"));

                    String updatedText = selectedText
                            .replaceFirst("(?m)^Status:.*$", "Status: " + newStatus)
                            .replaceFirst("(?m)^Remarks:.*$", "Remarks: " + remarks)
                            + "Status History: " + updateTime + " - " + newStatus
                            + ". Remarks: " + remarks + "\n";

                    Files.writeString(selectedFile, updatedText, StandardCharsets.UTF_8,
                            StandardOpenOption.TRUNCATE_EXISTING);

                    String notification = updateTime
                            + " | To: " + userEmail
                            + " | Complaint " + updateId
                            + " updated to: " + newStatus
                            + " | Remarks: " + remarks + "\n";

                    Files.writeString(notificationsFile, notification, StandardCharsets.UTF_8,
                            StandardOpenOption.CREATE, StandardOpenOption.APPEND);

                    System.out.println("\n===== USER NOTIFICATION =====");
                    System.out.println(notification);
                    System.out.println("Complaint file and notification file updated successfully.");
                    break;

                case 5:
                    if (!Files.exists(notificationsFile)) {
                        System.out.println("No notifications have been generated yet.");
                    } else {
                        System.out.println("\n===== NOTIFICATIONS =====");
                        System.out.println(Files.readString(notificationsFile, StandardCharsets.UTF_8));
                    }
                    break;

                case 6:
                    System.out.println("\nThank you for using Complaint Tracker.");
                    break;

                default:
                    System.out.println("Invalid option. Choose a number from 1 to 6.");
            }
        }

        sc.close();
    }
}
