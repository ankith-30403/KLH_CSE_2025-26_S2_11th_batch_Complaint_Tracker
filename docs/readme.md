# Complaint Tracker

## Project Information

- **Course:** Data Structures and Algorithms – 3 (25CS2103E)
- **Academic Year:** 2026–2027
- **Team:** 11
- **Section:** 2
- **Supervisor:** Vinay Kumar Sriperambuduri, Associate Professor, CSE, Hyderabad-500090, Telangana, India[cite: 1]
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

The Complaint Tracker is a Java-based application developed to simplify and organize customer complaint management without relying on external database management systems[cite: 1]. In manual workflows, complaints are tracked across fragmented channels like spreadsheets, emails, and paper logs, resulting in missed records, delayed resolution, duplicate entries, and poor accountability.

The system provides structured customer registration, automated complaint classification into categories (Billing, Technical Issue, Product Quality, Delivery, and Customer Service), and automatic routing to designated support staff[cite: 1]. Using custom in-memory tables and indexing, the application supports instant lookups by Customer ID, tracks lifecycle status transitions (*Under Review*, *In Progress*, *Resolved*, *Closed*), and logs remarks chronologically while generating status change notifications

---

## Objectives

- Centralize complaint logging to prevent scattered, lost, or duplicated records
- Implement efficient in-memory data structures for fast retrieval, indexing, and multi-attribute filtering.
- Automate complaint classification and department/staff routing based on category rules.
- Maintain an audit trail and complete status history through linked lifecycle logs.
- Trigger real-time notifications for customers whenever a complaint status changes.
- Demonstrate the application of relational schema modeling and algorithms in a pure Java in-memory architecture.

---

## Algorithms and Data Structures

- **Hash-Based Indexing & Lookups:** $O(1)$ constant-time primary key retrieval for Customer IDs and Complaint IDs.
- **Custom In-Memory Tables:** Dynamic array-backed collections representing core relational entities (`Customer`, `Complaint`, `Category`, `Staff`, `StatusLog`).
- **Relational Foreign Key Mapping:** In-memory pointers establishing relationships between complaints, customers, and assigned support staff.
- **Linear & Multi-Attribute Search:** Query filtering across dates, priority levels, categories, and resolution statuses.
- **Workflow State Management:** Finite-state transitions handling complaint lifecycles from submission to closure.

---

## Current Phase Status

### Review 2 – Core Table Operations & Lifecycle Workflow
- **Status:** Implemented and tested.
- Complete implementation of in-memory data tables for Customers, Complaints, Categories, Staff, and Status Logs.
- Primary-key lookup and relational query linking verified.
- Status progression engine tested (*Under Review* → *In Progress* → *Resolved* → *Closed*).
- Customer notification triggers and chronological remark logging fully operational.

---

## Project Flow

```text
       Customer Input / Console
                  |
                  v
         ComplaintController
                  |
                  v
    Validation & Category Routing
                  |
        +---------+---------+
        |                   |
        v                   v
  Customer Table     Complaint Table
 (customer_id PK)   (complaint_id PK)
        |                   |
        +---------+---------+
                  |
                  v
       Staff Assignment Engine
                  |
                  v
       StatusLog Audit Records
                  |
                  v
    Customer Status Notifications
