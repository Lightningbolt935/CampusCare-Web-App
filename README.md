# CampusCare – Smart Campus Complaint Management System

CampusCare is a web-based and an android application too that is designed to streamline communication between students, incharge staff, workers, and administrators within an academic institution.
It serves as a centralized platform where users can report issues in their campus buildings (e.g., "Lights not working in room 913"), and concerned authorities can efficiently manage, track, and resolve them.

 Features
> User (Student / Staff / General User)

Submit complaints related to any academic block or facility.

Specify issue details (e.g., location, room number, type of problem).

Track the status of their submitted complaints (Pending → Assigned → In Progress → Completed).

Receive updates when the in-charge acts on their complaint.

> InCharge (Block / Department In-charge)

Receives complaints submitted by users within their assigned block/area.

Reviews and verifies complaints.

Assigns workers/technicians to handle specific issues.

Updates complaint progress and resolution.

Communicates with workers if more details are needed.

> Workers / Technicians

View tasks assigned by the in-charge.

Update job progress (e.g., “Work started”, “Need replacement parts”, etc.).

Mark tasks as completed once resolved.

> Admin

Full system overview.

Access to all complaints across all blocks.

Manage users, in-charges, and workers.

View statistical data like:

Total complaints

Resolved complaints

Pending tasks

Block-wise issue distribution

# How It Works

User submits complaint
→ Provides details such as block, room, type of issue, and description.

In-charge receives complaint
→ Reviews and assigns an appropriate worker.

Worker resolves issue
→ Updates task status during the process.

Completion & Notification
→ User is informed that the issue is fixed.

Admin monitors everything
→ Dashboard shows full visibility and analytics.

# Tech Stack

Frontend: Java (JSWING and APPLETS)

Backend: Python

Database: SQLITE3(Python)

Version Control: Git & GitHub

IDE: Eclipse

📁 Project Structure

CampusCare/
│── src/
│   ├── main/
│   │   └── java/... (business logic)
│   └── resources/... (config files)
│
│── public/ (optional)
│── README.md
│── pom.xml / build.gradle
│── ...

> Installation & Setup

Clone the repository:

git clone https://github.com/Lightningbolt935/CampusCare-Web-App.git


Import into Eclipse (as Maven/Java project).

Configure database:

Create DB

Update application properties with username/password

Build & run:

mvn spring-boot:run

📌 Future Enhancements

Push notifications (SMS/Email)

Real-time worker location tracking

Chat system between users and in-charges
