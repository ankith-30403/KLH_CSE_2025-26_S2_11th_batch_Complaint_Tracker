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

        int choice = 0;

        System.out.println("===== COMPLAINT TRACKER =====");
        String userEmail = "Not Provided";

        while (choice != 5) {
            System.out.println("\n===== MENU =====");
            System.out.println("1. Search Complaints Using Z-Algorithm");
            System.out.println("2. View All Registered Complaints");
            System.out.println("3. Update Complaint Status and Notify User");
            System.out.println("4. View Notifications");
            System.out.println("5. Exit");
            System.out.print("Choose an option: ");

            if (!sc.hasNextInt()) {
                System.out.println("Please enter a number from 1 to 5.");
                sc.nextLine();
                continue;
            }

            choice = sc.nextInt();
            sc.nextLine();

            switch (choice) {
                case 1:
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

                case 2:
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

                case 3:
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

                case 4:
                    if (!Files.exists(notificationsFile)) {
                        System.out.println("No notifications have been generated yet.");
                    } else {
                        System.out.println("\n===== NOTIFICATIONS =====");
                        System.out.println(Files.readString(notificationsFile, StandardCharsets.UTF_8));
                    }
                    break;

                case 5:
                    System.out.println("\nThank you for using Complaint Tracker.");
                    break;

                default:
                    System.out.println("Invalid option. Choose a number from 1 to 6.");
            }
        }

        sc.close();
    }
}
