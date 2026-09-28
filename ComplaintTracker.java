import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ComplaintTracker {

    public static void main(String[] args) throws IOException {
        Scanner sc = new Scanner(System.in);

        // ============================================================
        // CO-1: TEXT ANALYTICS SYSTEM
        // Complaint documents are stored as separate text files.
        // The system accepts a complaint query and searches the
        // complete complaint corpus.
        //
        // CO-1: Pattern search is selected as the main query type.
        // ============================================================
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

                // ====================================================
                // CO-1: PATTERN SEARCH PROBLEM
                //
                // The user provides a complaint phrase/pattern.
                // The program searches the pattern in all complaint
                // documents.
                //
                // CO-2: STRING ALGORITHM
                // Z-Algorithm is used for efficient pattern matching.
                // ====================================================
                case 1:
                    System.out.print("Enter complaint text to search: ");

                    String pattern = sc.nextLine()
                            .trim()
                            .toLowerCase(Locale.ROOT);

                    if (pattern.isEmpty()) {
                        System.out.println("Search text cannot be empty.");
                        break;
                    }

                    // CO-1: Reading all complaint documents from
                    // the text corpus.
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
                    Set<String> recognizedComplaints = new LinkedHashSet<>();

                    StringBuilder result = new StringBuilder();

                    result.append("Z-ALGORITHM SEARCH RESULT\n");
                    result.append("Search text: ").append(pattern).append("\n");
                    result.append("----------------------------------------\n");

                    for (Path file : complaintFiles) {

                        String text = Files.readString(
                                file,
                                StandardCharsets.UTF_8).toLowerCase(Locale.ROOT);
                        String complaintDescription = getFieldValue(
                                text,
                                "complaint description");
                        String recognizedPattern = pattern;

                        for (String word : complaintDescription.split("[^a-z0-9]+")) {
                            if (word.startsWith(pattern)) {
                                recognizedComplaints.add(word);
                                recognizedPattern = word;
                                break;
                            }
                        }

                        // =================================================
                        // CO-2: Z-ALGORITHM
                        //
                        // Pattern P is searched inside text T.
                        //
                        // Combined string:
                        //
                        // P + separator + T
                        //
                        // Example:
                        // "gas leak#there is a gas leak in the kitchen"
                        //
                        // The Z-Algorithm calculates the Z-array.
                        //
                        // Time Complexity:
                        // O(m + n)
                        //
                        // m = pattern length
                        // n = text length
                        // =================================================
                        String combined = recognizedPattern + "\u0000"
                            + complaintDescription;

                        // CO-2: Z-array used for Z-Function calculation.
                        int[] z = new int[combined.length()];

                        // CO-2: Left and right boundaries of the
                        // current Z-box.
                        int left = 0;
                        int right = 0;

                        // =================================================
                        // CO-2: Z-FUNCTION CALCULATION
                        //
                        // The Z-array stores the length of the substring
                        // starting at position i that matches the prefix
                        // of the combined string.
                        //
                        // Previously calculated Z-values are reused
                        // using the current Z-box.
                        //
                        // This gives linear-time pattern matching.
                        // =================================================
                        for (int i = 1; i < combined.length(); i++) {

                            // CO-2: Reuse previously calculated Z-value.
                            if (i <= right) {
                                z[i] = Math.min(
                                        right - i + 1,
                                        z[i - left]);
                            }

                            // CO-2: Extend the Z-value by comparing
                            // matching characters.
                            while (i + z[i] < combined.length()
                                    && combined.charAt(z[i]) == combined.charAt(i + z[i])) {

                                z[i]++;
                            }

                            // CO-2: Update the current Z-box.
                            if (i + z[i] - 1 > right) {
                                left = i;
                                right = i + z[i] - 1;
                            }
                        }

                        // =================================================
                        // CO-2: PATTERN MATCHING
                        //
                        // If Z[i] is equal to or greater than the
                        // pattern length, the complete pattern has
                        // been found in the complaint text.
                        // =================================================
                        int occurrencesInFile = 0;

                        for (int i = recognizedPattern.length() + 1;
                                i < z.length; i++) {

                            if (z[i] >= recognizedPattern.length()) {
                                occurrencesInFile++;
                            }
                        }

                        // =================================================
                        // CO-1: TEXT ANALYTICS RESULT
                        //
                        // Store the files that contain the searched
                        // complaint and count total occurrences.
                        // =================================================
                        if (occurrencesInFile > 0) {

                            matchedFiles++;
                            totalOccurrences += occurrencesInFile;

                            result.append(file.getFileName())
                                    .append(" -> ")
                                    .append(occurrencesInFile)
                                    .append(" match(es)\n");
                        }
                    }

                    // CO-1: Display the final text-search result.
                    result.append("----------------------------------------\n");

                    if (!recognizedComplaints.isEmpty()) {
                        result.append("Recognized complaint: ")
                                .append(String.join(", ", recognizedComplaints))
                                .append("\n");
                    }

                    result.append("Files containing this complaint: ")
                            .append(matchedFiles)
                            .append("\n");

                    result.append("Total matches: ")
                            .append(totalOccurrences)
                            .append("\n");

                    System.out.println("\n" + result);

                    // CO-1: Save the text-analytics result.
                    Files.writeString(
                            searchResultsFile,
                            result.toString(),
                            StandardCharsets.UTF_8,
                            StandardOpenOption.CREATE,
                            StandardOpenOption.TRUNCATE_EXISTING);

                    System.out.println(
                            "Search result saved in: "
                                    + searchResultsFile);

                    break;

                // ====================================================
                // CO-1: DOCUMENT CORPUS MANAGEMENT
                //
                // Displays all complaint documents available in
                // the text corpus.
                // ====================================================
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
                        System.out.println(
                                "No complaint files have been registered yet.");
                        break;
                    }

                    System.out.println(
                            "\n===== REGISTERED COMPLAINT FILES =====");

                    for (Path file : registeredFiles) {
                        System.out.println("\nFile: " + file.getFileName());
                        System.out.println("----------------------------------------");
                        System.out.println(
                                Files.readString(
                                        file,
                                        StandardCharsets.UTF_8));
                    }

                    break;

                // ====================================================
                // CO-1: COMPLAINT RECORD RETRIEVAL
                //
                // Finds and displays the status information of
                // a complaint record.
                // ====================================================
                case 3:
                    System.out.print(
                            "Enter Customer ID (example: 23 or CUS-023): ");

                    String statusId = sc.nextLine().trim();

                    Path statusFile = findComplaintFile(
                            complaintDirectory,
                            statusId);

                    if (statusFile == null) {
                        System.out.println("Complaint ID not found.");
                        break;
                    }

                    String statusText = Files.readString(
                            statusFile,
                            StandardCharsets.UTF_8);

                    System.out.println("\n===== COMPLAINT STATUS =====");

                    System.out.println(
                            "Complaint ID: "
                                    + getFieldValue(
                                            statusText,
                                            "Complaint ID"));

                    System.out.println(
                            "Department: "
                                    + getFieldValue(
                                            statusText,
                                            "Department"));

                    System.out.println(
                            "Staff: "
                                    + getFieldValue(
                                            statusText,
                                            "Staff"));

                    System.out.println(
                            "Status: "
                                    + getFieldValue(
                                            statusText,
                                            "Status"));

                    System.out.println(
                            "Remarks: "
                                    + getFieldValue(
                                            statusText,
                                            "Remarks"));

                    break;

                // ====================================================
                // CO-1: COMPLAINT RECORD RETRIEVAL
                //
                // Retrieves the address associated with a complaint.
                // ====================================================
                case 4:
                    System.out.print(
                            "Enter Customer ID (example: 23 or CUS-023): ");

                    String addressId = sc.nextLine().trim();

                    Path addressFile = findComplaintFile(
                            complaintDirectory,
                            addressId);

                    if (addressFile == null) {
                        System.out.println("Complaint ID not found.");
                        break;
                    }

                    String addressText = Files.readString(
                            addressFile,
                            StandardCharsets.UTF_8);

                    System.out.println(
                            "\n===== COMPLAINT ADDRESS =====");

                    System.out.println(
                            "Complaint ID: "
                                    + getFieldValue(
                                            addressText,
                                            "Complaint ID"));

                    System.out.println(
                            "Address: "
                                    + getFieldValue(
                                            addressText,
                                            "Address"));

                    break;

                case 5:
                    System.out.println(
                            "\nThank you for using Complaint Tracker.");
                    break;

                default:
                    System.out.println(
                            "Invalid option. Choose a number from 1 to 5.");
            }
        }

        sc.close();
    }

    // ================================================================
    // CO-1: TEXT PROCESSING
    //
    // Extracts a specific field from a complaint document.
    // ================================================================
    private static String getFieldValue(
            String text,
            String fieldName) {

        String prefix = fieldName + ": ";

        for (String line : text.split("\\R")) {

            if (line.startsWith(prefix)) {
                return line.substring(prefix.length());
            }
        }

        return "Not Available";
    }

    // ================================================================
    // CO-1: COMPLAINT RECORD UPDATE
    //
    // Updates a field inside a complaint document.
    // ================================================================
    private static String updateFieldValue(
            String text,
            String fieldName,
            String value) {

        String prefix = fieldName + ": ";

        String[] lines = text.split("\\R", -1);

        List<String> updatedLines = new ArrayList<>();

        boolean found = false;

        for (String line : lines) {

            if (line.startsWith(prefix)) {

                updatedLines.add(prefix + value);
                found = true;

            } else {

                updatedLines.add(line);
            }
        }

        if (!found) {
            updatedLines.add(prefix + value);
        }

        return String.join(
                System.lineSeparator(),
                updatedLines);
    }

    // ================================================================
    // CO-1: COMPLAINT RECORD SEARCH
    //
    // Searches the complaint corpus for a particular Customer ID.
    // This is supporting record-retrieval functionality.
    // ================================================================
    private static Path findComplaintFile(
            Path complaintDirectory,
            String customerId)
            throws IOException {

        String normalizedId = customerId.trim();

        if (normalizedId.isEmpty()) {
            return null;
        }

        String numericId = normalizedId.replaceAll(
                "(?i)^CUS-",
                "");

        if (numericId.equals(normalizedId)
                && normalizedId
                        .toUpperCase(Locale.ROOT)
                        .startsWith("CMP-")) {

            return null;
        }

        try {

            numericId = String.format(
                    "%03d",
                    Integer.parseInt(numericId));

        } catch (NumberFormatException exception) {

            return null;
        }

        String customerIdValue = "CUS-" + numericId;

        try (Stream<Path> fileStream = Files.list(complaintDirectory)) {

            return fileStream
                    .filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".txt"))
                    .filter(path -> {

                        try {

                            String text = Files.readString(
                                    path,
                                    StandardCharsets.UTF_8);

                            for (String line : text.split("\\R")) {

                                if (line.equals(
                                        "Customer ID: "
                                                + customerIdValue)) {

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