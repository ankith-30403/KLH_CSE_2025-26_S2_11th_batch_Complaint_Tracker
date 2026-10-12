import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
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

        while (choice != 10) {
            System.out.println("\n===== MENU =====");
            System.out.println("1. Search Complaints Using Z-Algorithm");
            System.out.println("2. View All Registered Complaints");
            System.out.println("3. View Updated Status of a Complaint");
            System.out.println("4. View Address");
            System.out.println("5. Sort Complaints by Priority (CO-3)");
            System.out.println("6. Binary Search Complaint by ID (CO-3)");
            System.out.println("7. Max-Flow Staff Assignment (CO-4)");
            System.out.println("8. Approximate Staff Coverage (CO-5)");
            System.out.println("9. Randomized Audit Sampling (CO-6)");
            System.out.println("10. Exit");
            System.out.print("Choose an option: ");

            if (!sc.hasNextInt()) {
                System.out.println("Please enter a number from 1 to 10.");
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
                    System.out.print(
                            "Enter complaint text to filter (or press Enter to show all): ");

                    String filterText = sc.nextLine().trim().toLowerCase(Locale.ROOT);

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

                    List<Path> filteredFiles = new ArrayList<>();

                    for (Path file : registeredFiles) {
                        String text = Files.readString(
                                file,
                                StandardCharsets.UTF_8)
                                .toLowerCase(Locale.ROOT);
                        String complaintDescription = getFieldValue(
                                text,
                                "complaint description");

                        if (filterText.isEmpty()
                                || complaintDescription.contains(filterText)) {
                            filteredFiles.add(file);
                        }
                    }

                    if (filteredFiles.isEmpty()) {
                        System.out.println(
                                "No registered complaints match: " + filterText);
                        break;
                    }

                    System.out.println(
                            "\n===== REGISTERED COMPLAINT FILES =====");

                    if (!filterText.isEmpty()) {
                        System.out.println("Filter: " + filterText);
                    }

                    for (Path file : filteredFiles) {
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
                            "Enter Customer ID (example: 23): ");

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
                            "Enter Customer ID (example: 23): ");

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
                    module3SortComplaints(complaintDirectory);
                    break;

                case 6:
                    module3BinarySearch(complaintDirectory, sc);
                    break;

                case 7:
                    module4MaxFlowAssignment(complaintDirectory);
                    break;

                case 8:
                    module5ApproximateCoverage(complaintDirectory);
                    break;

                case 9:
                    module6RandomizedAudit(complaintDirectory, sc);
                    break;

                case 10:
                    System.out.println(
                            "\nThank you for using Complaint Tracker.");
                    break;

                default:
                    System.out.println(
                            "Invalid option. Choose a number from 1 to 10.");
            }
        }

        sc.close();
    }


    // ================================================================
    // MODULE-3 / CO-3: DIVIDE AND CONQUER
    //
    // Merge Sort is used to arrange complaint records by priority.
    // It repeatedly divides the list into smaller parts and merges
    // them in sorted order.
    //
    // Time Complexity: O(n log n)
    // Space Complexity: O(n)
    // ================================================================
    private static void module3SortComplaints(Path complaintDirectory)
            throws IOException {

        List<ComplaintRecord> records = loadComplaintRecords(complaintDirectory);

        if (records.isEmpty()) {
            System.out.println("No complaint records found.");
            return;
        }

        mergeSort(records, 0, records.size() - 1);

        System.out.println("\n===== MODULE-3: SORTED COMPLAINTS =====");
        System.out.println("Priority order: High -> Medium -> Low");

        for (ComplaintRecord record : records) {
            System.out.println(
                    record.complaintId + " | "
                            + record.priority + " | "
                            + record.status + " | "
                            + record.description);
        }
    }

    private static void mergeSort(
            List<ComplaintRecord> records,
            int left,
            int right) {

        if (left >= right) {
            return;
        }

        int mid = left + (right - left) / 2;

        mergeSort(records, left, mid);
        mergeSort(records, mid + 1, right);
        merge(records, left, mid, right);
    }

    private static void merge(
            List<ComplaintRecord> records,
            int left,
            int mid,
            int right) {

        List<ComplaintRecord> temp = new ArrayList<>();

        int i = left;
        int j = mid + 1;

        while (i <= mid && j <= right) {

            if (priorityRank(records.get(i).priority)
                    <= priorityRank(records.get(j).priority)) {

                temp.add(records.get(i));
                i++;

            } else {

                temp.add(records.get(j));
                j++;
            }
        }

        while (i <= mid) {
            temp.add(records.get(i));
            i++;
        }

        while (j <= right) {
            temp.add(records.get(j));
            j++;
        }

        for (int k = 0; k < temp.size(); k++) {
            records.set(left + k, temp.get(k));
        }
    }

    private static int priorityRank(String priority) {

        if ("High".equalsIgnoreCase(priority)) {
            return 1;
        }

        if ("Medium".equalsIgnoreCase(priority)) {
            return 2;
        }

        if ("Low".equalsIgnoreCase(priority)) {
            return 3;
        }

        return 4;
    }

    // ================================================================
    // MODULE-3 / CO-3: BINARY SEARCH
    //
    // The records are first sorted by numeric complaint ID.
    // Binary Search then locates a complaint in O(log n) time.
    // ================================================================
    private static void module3BinarySearch(
            Path complaintDirectory,
            Scanner sc) throws IOException {

        List<ComplaintRecord> records = loadComplaintRecords(complaintDirectory);

        if (records.isEmpty()) {
            System.out.println("No complaint records found.");
            return;
        }

        records.sort(Comparator.comparingInt(
                record -> numericComplaintId(record.complaintId)));

        System.out.print("Enter Complaint ID (example: CMP-023): ");
        String target = sc.nextLine().trim().toUpperCase(Locale.ROOT);

        int low = 0;
        int high = records.size() - 1;

        while (low <= high) {

            int mid = low + (high - low) / 2;
            int midId = numericComplaintId(records.get(mid).complaintId);
            int targetId = numericComplaintId(target);

            if (midId == targetId) {

                ComplaintRecord record = records.get(mid);

                System.out.println("\n===== BINARY SEARCH RESULT =====");
                System.out.println("Complaint ID: " + record.complaintId);
                System.out.println("Customer: " + record.customerName);
                System.out.println("Priority: " + record.priority);
                System.out.println("Department: " + record.department);
                System.out.println("Staff: " + record.staff);
                System.out.println("Status: " + record.status);
                System.out.println("Description: " + record.description);
                return;
            }

            if (midId < targetId) {
                low = mid + 1;
            } else {
                high = mid - 1;
            }
        }

        System.out.println("Complaint ID not found.");
    }

    // ================================================================
    // MODULE-4 / CO-4: NETWORK FLOW
    //
    // Edmonds-Karp is used to model assignment of unresolved
    // complaints to available staff members.
    //
    // Network:
    // Source -> Staff -> Complaint -> Sink
    //
    // Every staff member has capacity 1 in this demonstration,
    // so one staff member can receive one complaint at a time.
    //
    // Edmonds-Karp Time Complexity: O(VE^2)
    // ================================================================
    private static void module4MaxFlowAssignment(
            Path complaintDirectory) throws IOException {

        List<ComplaintRecord> records = loadComplaintRecords(complaintDirectory);

        List<ComplaintRecord> pending = records.stream()
                .filter(record -> !isClosed(record.status))
                .collect(Collectors.toList());

        if (pending.isEmpty()) {
            System.out.println("No unresolved complaints available.");
            return;
        }

        List<String> staffList = records.stream()
                .map(record -> record.staff)
                .filter(staff -> !staff.equalsIgnoreCase("Not Available"))
                .filter(staff -> !staff.trim().isEmpty())
                .distinct()
                .sorted()
                .collect(Collectors.toList());

        if (staffList.isEmpty()) {
            System.out.println("No staff records available for assignment.");
            return;
        }

        int source = 0;
        int staffStart = 1;
        int complaintStart = staffStart + staffList.size();
        int sink = complaintStart + pending.size();
        int vertices = sink + 1;

        FlowNetwork network = new FlowNetwork(vertices);

        for (int i = 0; i < staffList.size(); i++) {
            network.addEdge(source, staffStart + i, 1);
        }

        for (int j = 0; j < pending.size(); j++) {
            network.addEdge(complaintStart + j, sink, 1);
        }

        for (int i = 0; i < staffList.size(); i++) {

            String staffName = staffList.get(i);
            String staffDepartment = departmentForStaff(records, staffName);

            for (int j = 0; j < pending.size(); j++) {

                ComplaintRecord complaint = pending.get(j);

                if (staffDepartment.equalsIgnoreCase(complaint.department)) {
                    network.addEdge(
                            staffStart + i,
                            complaintStart + j,
                            1);
                }
            }
        }

        int maxFlow = network.edmondsKarp(source, sink);

        System.out.println("\n===== MODULE-4: MAX-FLOW ASSIGNMENT =====");
        System.out.println("Maximum possible assignments: " + maxFlow);

        for (int i = 0; i < staffList.size(); i++) {

            for (FlowEdge edge : network.graph[staffStart + i]) {

                if (edge.to >= complaintStart
                        && edge.to < sink
                        && edge.originalCapacity == 1
                        && edge.capacity == 0) {

                    int complaintIndex = edge.to - complaintStart;
                    ComplaintRecord complaint = pending.get(complaintIndex);

                    System.out.println(
                            staffList.get(i)
                                    + " -> "
                                    + complaint.complaintId
                                    + " ("
                                    + complaint.department
                                    + ")");
                }
            }
        }
    }

    // ================================================================
    // MODULE-5 / CO-5: NP-COMPLETENESS & APPROXIMATION
    //
    // Selecting the smallest number of staff members to cover all
    // pending complaints can be modeled as a Set Cover problem.
    // Exact Set Cover is NP-hard.
    //
    // The program uses the standard greedy approximation idea:
    // repeatedly choose the staff member who covers the largest
    // number of currently uncovered complaints.
    //
    // This does not guarantee the optimal answer, but it provides
    // an efficient practical approximation.
    // ================================================================
    private static void module5ApproximateCoverage(
            Path complaintDirectory) throws IOException {

        List<ComplaintRecord> records = loadComplaintRecords(complaintDirectory);

        List<ComplaintRecord> pending = records.stream()
                .filter(record -> !isClosed(record.status))
                .collect(Collectors.toList());

        if (pending.isEmpty()) {
            System.out.println("No unresolved complaints available.");
            return;
        }

        Map<String, Set<Integer>> staffCoverage = new LinkedHashMap<>();

        for (String staff : records.stream()
                .map(record -> record.staff)
                .filter(staff -> !staff.equalsIgnoreCase("Not Available"))
                .filter(staff -> !staff.trim().isEmpty())
                .distinct()
                .sorted()
                .collect(Collectors.toList())) {

            Set<Integer> covered = new LinkedHashSet<>();

            String department = departmentForStaff(records, staff);

            for (int i = 0; i < pending.size(); i++) {
                if (department.equalsIgnoreCase(pending.get(i).department)) {
                    covered.add(i);
                }
            }

            if (!covered.isEmpty()) {
                staffCoverage.put(staff, covered);
            }
        }

        Set<Integer> uncovered = new LinkedHashSet<>();

        for (int i = 0; i < pending.size(); i++) {
            uncovered.add(i);
        }

        List<String> selectedStaff = new ArrayList<>();

        while (!uncovered.isEmpty()) {

            String bestStaff = null;
            int bestGain = 0;

            for (Map.Entry<String, Set<Integer>> entry
                    : staffCoverage.entrySet()) {

                int gain = 0;

                for (Integer index : entry.getValue()) {
                    if (uncovered.contains(index)) {
                        gain++;
                    }
                }

                if (gain > bestGain) {
                    bestGain = gain;
                    bestStaff = entry.getKey();
                }
            }

            if (bestStaff == null) {
                break;
            }

            selectedStaff.add(bestStaff);
            uncovered.removeAll(staffCoverage.get(bestStaff));
        }

        System.out.println("\n===== MODULE-5: GREEDY APPROXIMATION =====");
        System.out.println("Selected staff members: " + selectedStaff.size());

        for (String staff : selectedStaff) {
            System.out.println(
                    staff
                            + " covers "
                            + staffCoverage.get(staff).size()
                            + " pending complaint(s)");
        }

        if (uncovered.isEmpty()) {
            System.out.println("All pending complaints are covered.");
        } else {
            System.out.println(
                    "Uncovered complaints: " + uncovered.size()
                            + " (no matching department staff found)");
        }
    }

    // ================================================================
    // MODULE-6 / CO-6: RANDOMIZED ALGORITHM
    //
    // A random sample of complaint records is selected for audit.
    // Randomized sampling is useful when the complete corpus is
    // large and management wants a quick quality-control sample.
    //
    // Expected running time: O(n) for shuffling the records.
    // ================================================================
    private static void module6RandomizedAudit(
            Path complaintDirectory,
            Scanner sc) throws IOException {

        List<ComplaintRecord> records = loadComplaintRecords(complaintDirectory);

        if (records.isEmpty()) {
            System.out.println("No complaint records found.");
            return;
        }

        System.out.print("Enter audit sample size: ");

        if (!sc.hasNextInt()) {
            System.out.println("Please enter a valid number.");
            sc.nextLine();
            return;
        }

        int sampleSize = sc.nextInt();
        sc.nextLine();

        if (sampleSize <= 0 || sampleSize > records.size()) {
            System.out.println(
                    "Sample size must be between 1 and "
                            + records.size() + ".");
            return;
        }

        List<ComplaintRecord> shuffled = new ArrayList<>(records);
        Collections.shuffle(shuffled, new Random());

        System.out.println("\n===== MODULE-6: RANDOMIZED AUDIT SAMPLE =====");

        for (int i = 0; i < sampleSize; i++) {

            ComplaintRecord record = shuffled.get(i);

            System.out.println(
                    record.complaintId
                            + " | "
                            + record.priority
                            + " | "
                            + record.status
                            + " | "
                            + record.department);
        }
    }

    // ================================================================
    // SUPPORTING DATA STRUCTURE FOR MODULES 3-6
    // ================================================================
    private static List<ComplaintRecord> loadComplaintRecords(
            Path complaintDirectory) throws IOException {

        List<ComplaintRecord> records = new ArrayList<>();

        try (Stream<Path> fileStream = Files.list(complaintDirectory)) {

            List<Path> files = fileStream
                    .filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".txt"))
                    .sorted(Comparator.comparing(Path::getFileName))
                    .collect(Collectors.toList());

            for (Path file : files) {

                String text = Files.readString(
                        file,
                        StandardCharsets.UTF_8);

                ComplaintRecord record = new ComplaintRecord();

                record.file = file;
                record.complaintId = getFieldValue(text, "Complaint ID");
                record.customerName = getFieldValue(text, "Customer Name");
                record.description = getFieldValue(text, "Complaint Description");
                record.priority = getFieldValue(text, "Priority");
                record.department = getFieldValue(text, "Department");
                record.staff = getFieldValue(text, "Staff");
                record.status = getFieldValue(text, "Status");

                records.add(record);
            }
        }

        return records;
    }

    private static int numericComplaintId(String complaintId) {

        String digits = complaintId.replaceAll("[^0-9]", "");

        if (digits.isEmpty()) {
            return Integer.MAX_VALUE;
        }

        try {
            return Integer.parseInt(digits);
        } catch (NumberFormatException exception) {
            return Integer.MAX_VALUE;
        }
    }

    private static boolean isClosed(String status) {

        return status.equalsIgnoreCase("Resolved")
                || status.equalsIgnoreCase("Closed");
    }

    private static String departmentForStaff(
            List<ComplaintRecord> records,
            String staff) {

        for (ComplaintRecord record : records) {

            if (record.staff.equalsIgnoreCase(staff)
                    && !record.department.equalsIgnoreCase("Not Available")) {

                return record.department;
            }
        }

        return "Not Available";
    }

    // ================================================================
    // MODULE-4: EDMONDS-KARP FLOW NETWORK
    // ================================================================
    private static class FlowEdge {

        int to;
        int capacity;
        int originalCapacity;

        FlowEdge reverse;

        FlowEdge(int to, int capacity) {
            this.to = to;
            this.capacity = capacity;
            this.originalCapacity = capacity;
        }
    }

    private static class FlowNetwork {

        List<FlowEdge>[] graph;

        @SuppressWarnings("unchecked")
        FlowNetwork(int vertices) {

            graph = new ArrayList[vertices];

            for (int i = 0; i < vertices; i++) {
                graph[i] = new ArrayList<>();
            }
        }

        void addEdge(int from, int to, int capacity) {

            FlowEdge forward = new FlowEdge(to, capacity);
            FlowEdge reverse = new FlowEdge(from, 0);

            forward.reverse = reverse;
            reverse.reverse = forward;

            graph[from].add(forward);
            graph[to].add(reverse);
        }

        int edmondsKarp(int source, int sink) {

            int maxFlow = 0;

            while (true) {

                FlowEdge[] parent = new FlowEdge[graph.length];
                boolean[] visited = new boolean[graph.length];

                java.util.ArrayDeque<Integer> queue =
                        new java.util.ArrayDeque<>();

                queue.add(source);
                visited[source] = true;

                while (!queue.isEmpty() && !visited[sink]) {

                    int current = queue.poll();

                    for (FlowEdge edge : graph[current]) {

                        if (!visited[edge.to]
                                && edge.capacity > 0) {

                            visited[edge.to] = true;
                            parent[edge.to] = edge;
                            queue.add(edge.to);
                        }
                    }
                }

                if (!visited[sink]) {
                    break;
                }

                int pathFlow = Integer.MAX_VALUE;

                for (int v = sink; v != source;) {

                    FlowEdge edge = parent[v];
                    pathFlow = Math.min(pathFlow, edge.capacity);
                    v = edge.reverse.to;
                }

                for (int v = sink; v != source;) {

                    FlowEdge edge = parent[v];

                    edge.capacity -= pathFlow;
                    edge.reverse.capacity += pathFlow;

                    v = edge.reverse.to;
                }

                maxFlow += pathFlow;
            }

            return maxFlow;
        }
    }

    private static class ComplaintRecord {

        Path file;
        String complaintId;
        String customerName;
        String description;
        String priority;
        String department;
        String staff;
        String status;
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

        String numericId = normalizedId;

        if (normalizedId.toUpperCase(Locale.ROOT).startsWith("CUS-")) {
            numericId = normalizedId.substring(4);
        }

        if (normalizedId.toUpperCase(Locale.ROOT).startsWith("CMP-")) {
            return null;
        }

        try {
            Integer.parseInt(numericId);
        } catch (NumberFormatException exception) {
            return null;
        }

        String customerIdValue = numericId;

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
                                                + customerIdValue)
                                        || line.equals(
                                        "Customer ID: CUS-"
                                                + String.format("%03d",
                                                        Integer.parseInt(customerIdValue)))) {

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
