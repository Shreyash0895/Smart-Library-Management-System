# Smart-Library-Management-System
A Smart Library Management System built in Java Swing with MySQL. Features role-based access for Admin, Librarian &amp; Student. Admin approves users, Librarian manages books &amp; issues/returns, Students borrow books &amp; track fines. Includes QR code scanner, notifications &amp; real-time availability tracking.

Tech Stack
ComponentTechnologyLanguageJava (JDK 11+)UI FrameworkJava SwingDatabaseMySQL 8.0DB ConnectivityJDBC (MySQL Connector/J)IDEVS Code / IntelliJ IDEABuild ToolManual Classpath / Maven

✨ Features
👤 Admin

Secure login with role-based access
Dashboard with real-time stats (Total Users, Books, Active Loans, Pending Approvals)
Approve or reject new Librarian and Student accounts
Manage Librarians (add, view, remove)
View Reports and Fine summaries
System Settings and Theme toggle (Light/Dark)
ISBN / QR Code Scanner

📖 Librarian

Add, edit, and delete books
Issue books to students
Process book returns
Calculate and manage fines
Send notifications to students
View borrowing history

🎓 Student

Register and login after Admin approval
Browse and search available books (by title, author, ISBN)
Request and borrow books
Return books
View borrowing history and due dates
Check outstanding fines
Receive notifications


🗄️ Database Schema
Database Name: library_management
TableDescriptionusersStores Admin, Librarian, Student accountsbooksStores book details and availabilityissued_booksTracks issued books per studentbook_borrowingsDetailed borrowing records with fine infobook_requestsStudent book requestsbook_reviewsStudent reviews and ratingsfinesFine records for overdue booksnotificationsUser notifications and alertssettingsUser preferences and theme settings

🔄 Application Flow
Student / Librarian Signs Up
           ↓
    Admin Approves Account
           ↓
      User Logs In
           ↓
   ┌───────┴────────┐
Student          Librarian
   ↓                 ↓
Search Books     Add New Books
   ↓                 ↓
Request Book     Issue Book to Student
   ↓                 ↓
Return Book      Process Return
   ↓                 ↓
Fine Calculated  Mark Fine as Paid

⚙️ Setup Instructions
Prerequisites

Java JDK 11 or higher
MySQL Server 8.0
MySQL Workbench (optional but recommended)
VS Code with Java Extension Pack

Smart-Library-Management-System/
│
├── LoginScreen.java          # Entry point - Login UI
├── SignUpScreen.java         # New user registration
├── DatabaseConnection.java   # MySQL connection handler
│
├── AdminDashboard.java       # Admin main dashboard
├── UserApprovalPanel.java    # Approve/reject users
├── LibrarianManagementPanel.java
├── ReportsPanel.java
├── FineManagementPanel.java
│
├── LibrarianDashboard.java   # Librarian main dashboard
├── BookManagementPanel.java  # Add/edit/delete books
├── IssueBooksPanel.java      # Issue books to students
├── IssuedBooksPanel.java     # View issued books
├── ReturnBooksPanel.java     # Process returns
├── ReissueBooksPanel.java    # Reissue books
│
├── StudentDashboard.java     # Student main dashboard (implied)
├── BorrowBooksPanel.java     # Browse and borrow books
├── RequestBooksPanel.java    # Request unavailable books
├── NotificationPanel.java    # View notifications
│
├── QRScannerPanel.java       # ISBN / QR Code scanner
├── QRCodeGenerator.java      # QR code generation utility
├── ISBNScannerPanel.java     # ISBN scanner UI
│
├── Book.java                 # Book model class
├── GradientPanel.java        # UI gradient component
├── RoundedPanel.java         # UI rounded panel component
├── CustomButton.java         # Styled button component
├── StatusPanel.java          # Status bar component
│
├── libraryyyyyyyy.sql        # Database schema and seed data
└── README.md                 # Project documentation
