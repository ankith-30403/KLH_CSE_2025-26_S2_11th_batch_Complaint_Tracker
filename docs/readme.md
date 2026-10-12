# Complaint Tracker

## Project Information

- **Course:** Data Structures and Algorithms – 3 (25CS2103E)
- **Academic Year:** 2026–2027
- **Team:** 11
- **Section:** 2
- **Supervisor:** Vinay Kumar Sriperambuduri, Associate Professor, CSE, Hyderabad-500090, Telangana, India.
- **Current Phase:** In-Memory Table Management & Core Operations – Review 2

---

## Team Members

| Name | ID |
| :--- | :--- |
| U. Ankith | 2520030403 |
| G. Bhargav | 2520030451 |
| Santosh Kumar | 2520030474 |

---

## Abstract

The Complaint Tracker is a Java-based application developed to manage customer complaints efficiently using file handling and Data Structures and Algorithms. The system stores complaint records in individual text files, reducing the need for manual record management and external database systems. It allows users to search complaints using the Z-Algorithm, view and filter registered complaints, retrieve complaint status and address details, sort complaints by priority using Merge Sort, and search for complaint IDs using Binary Search.

The project also demonstrates advanced algorithmic techniques, including the Edmonds-Karp maximum-flow algorithm for staff assignment, a greedy approximation algorithm for staff coverage, and randomized sampling for complaint auditing. Complaint records are stored in the `data/corpus` directory, and search results can be saved in the `results/search_results.txt` file. The main objective of this project is to demonstrate the practical application of Java programming, file handling, searching, sorting, network flow, approximation, and randomized algorithms in a complaint management system.

---

## Objectives

Centralize complaint record management by storing customer complaints in individual text files for easy access and organization.
Implement efficient searching, sorting, and filtering techniques using the Z-Algorithm, Merge Sort, and Binary Search.
Enable quick retrieval of complaint details, including complaint ID, status, address, and other available information.
Apply network flow algorithms, particularly Edmonds-Karp, to demonstrate maximum-flow-based staff assignment.
Implement a greedy approximation algorithm to improve staff coverage of complaints.
Apply randomized sampling techniques to select complaint records for auditing and demonstrate the practical use of advanced Data Structures and Algorithms in Java.
---

## Algorithms and Data Structures
- Z-Algorithm (String Matching): Efficiently searches for a given text pattern within complaint descriptions to identify matching complaint files.
- File Handling and ArrayList: Stores complaint records in individual text files and uses dynamic collections to process complaint data.
- Merge Sort: Sorts complaint records according to priority, such as High, Medium, and Low.
- Binary Search: Searches for a specific complaint using its ID in a correctly sorted collection of complaint IDs.
- Edmonds-Karp Algorithm (Maximum Flow): Calculates the maximum possible flow in a network to demonstrate staff assignment to complaints.
- Greedy Approximation Algorithm: Selects staff members to cover complaints using an approximation-based approach.
- Randomized Sampling: Randomly selects complaint records for auditing and demonstrates randomized algorithm techniques.
- HashMap and Set: Support data mapping, tracking, and coverage calculations in the advanced algorithm modules.

---

## Current Phase Status

Review 2 – Complaint Search, Sorting & Advanced Algorithm Implementation
- Status: Core complaint management and algorithm modules implemented.
- Complaint records stored and retrieved using text files in the data/corpus directory.
- Z-Algorithm implemented for efficient complaint description searching.
- Merge Sort and Binary Search implemented for complaint priority sorting and complaint ID retrieval.
- Edmonds-Karp maximum-flow algorithm implemented for network-based staff assignment.
- Greedy approximation algorithm implemented for staff coverage.
- Randomized sampling implemented for complaint auditing.
- Search results saved in results/search_results.txt for future reference.
---

## Project Flow

 Customer Input / Console
                     |
                     v
              ComplaintTracker.java
                     |
                     v
              Display Menu Options
                     |
                     v
             Read Complaint Files
                     |
                     v
          Search / Filter Complaints
                     |
                     v
              Z-Algorithm Search
                     |
                     v
          +----------+----------+
          |                     |
          v                     v
      Merge Sort           Binary Search
   (Priority Sorting)    (Complaint ID Search)
          |                     |
          +----------+----------+
                     |
                     v
           Edmonds-Karp Algorithm
             (Staff Assignment)
                     |
                     v
          Greedy Approximation Algorithm
              (Staff Coverage)
                     |
                     v
             Randomized Sampling
             (Complaint Auditing)
                     |
                     v
              Display Results
                     |
                     v
       Save Results to search_results.txt
