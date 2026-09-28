import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
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

        Files.createDirectories(complaintDirectory);
        Files.createDirectories(resultsDirectory);

        int choice = 0;

        System.out.println("===== COMPLAINT TRACKER =====");

        while (choice != 5) {
            System.out.println("\n===== MENU =====");
            System.out.println("1. Search Complaints Using Z-Algorithm");
            System.out.println("2. View All Registered Complaints");
            System.out.println("3. View Updated Status of a Complaint");
            System.out.println("4. View Address");
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
                    System.out.print("Enter Complaint ID or Customer ID (example: 23, CMP-023, or CUS-023): ");
                    String statusId = sc.nextLine().trim();
                    Path statusFile = findComplaintFile(complaintDirectory, statusId);

                    if (statusFile == null) {
                        System.out.println("Complaint ID not found.");
                        break;
                    }

                    String statusText = Files.readString(statusFile, StandardCharsets.UTF_8);
                    System.out.println("\n===== COMPLAINT STATUS =====");
                    System.out.println("Complaint ID: " + getFieldValue(statusText, "Complaint ID"));
                    System.out.println("Status: " + getFieldValue(statusText, "Status"));
                    System.out.println("Remarks: " + getFieldValue(statusText, "Remarks"));
                    break;

                case 4:
                    System.out.print("Enter Complaint ID or Customer ID (example: 23, CMP-023, or CUS-023): ");
                    String addressId = sc.nextLine().trim();
                    Path addressFile = findComplaintFile(complaintDirectory, addressId);

                    if (addressFile == null) {
                        System.out.println("Complaint ID not found.");
                        break;
                    }

                    String addressText = Files.readString(addressFile, StandardCharsets.UTF_8);
                    System.out.println("\n===== COMPLAINT ADDRESS =====");
                    System.out.println("Complaint ID: " + getFieldValue(addressText, "Complaint ID"));
                    System.out.println("Address: " + getFieldValue(addressText, "Address"));
                    break;

                case 5:
                    System.out.println("\nThank you for using Complaint Tracker.");
                    break;

                default:
                    System.out.println("Invalid option. Choose a number from 1 to 5.");
            }
        }

        sc.close();
    }

    private static String getFieldValue(String text, String fieldName) {
        String prefix = fieldName + ": ";
        for (String line : text.split("\\R")) {
            if (line.startsWith(prefix)) {
                return line.substring(prefix.length());
            }
        }
        return "Not Available";
    }

    private static Path findComplaintFile(Path complaintDirectory, String complaintId)
            throws IOException {
        String numericId = complaintId.replaceAll("(?i)^(CMP-|CUS-)", "");
        try {
            numericId = String.format("%03d", Integer.parseInt(numericId));
        } catch (NumberFormatException exception) {
            return null;
        }
        String complaintIdValue = "CMP-" + numericId;
        String customerIdValue = "CUS-" + numericId;

        try (Stream<Path> fileStream = Files.list(complaintDirectory)) {
            return fileStream
                    .filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".txt"))
                    .filter(path -> {
                        try {
                            String text = Files.readString(path, StandardCharsets.UTF_8);
                            for (String line : text.split("\\R")) {
                                if (line.equals("Complaint ID: " + complaintIdValue)
                                        || line.equals("Customer ID: " + customerIdValue)) {
                                    return true;
                                }
                            }
                            return false;
                        } catch (IOException exception) {
                            return false;
                        }
                    })
                    .findFirst()
                    .orElse(null);
        }
    }
}
