
[![Apache Maven](https://img.shields.io/badge/Apache%20Maven-%23C71A36.svg?logo=data:image/svg+xml;base64,PHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHdpZHRoPSIyNTBweCIgaGVpZ2h0PSIyNXB4IiB2aWV3Qm94PSIwIDAgOTYxIDQwMCI+PHBhdGggZD0iTTI1IDBoMzI4bDUwLjUgOTBoLTIzNy41bDkwIDE2MEg0MTVMMjk3LjUgMjIwaDIzNy41bC01MC41LTkwSDYzN2w5MCAxNjBIMTI1bC05MC0xNjBIMjUtbDUwLjUtOTBaIiBmaWxsPSIjMDAwIi8+PC9zdmc+)](https://maven.apache.org/)
[![Node.js](https://img.shields.io/badge/Node.js-339933.svg?logo=data:image/svg+xml;base64,PHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIGhlaWdodD0iNjQiIHZpZXdCb3g9IjAgMCAyNTYgMjg4IiB3aWR0aD0iNTYiPjxwYXRoIGQ9Ik0xMjcuOTk4LjAyOGMtMS43NTQgMC0zLjUwOC40NTctNS4wNzQgMS4zNjRMNy41NjQgNTMuMjY3Qy4yMDQgNTcuMjE5IDAgNTguMzA2IDAgNjQuNzQ5djE1OC41MDZjMCAyLjY2Ni45OTYgNS4yMiAyLjc2NCA3LjIyMyAxLjc2NyAyLjAwMyA0LjE2OSA0LjIwNSA2LjkwNSA1LjYxN2wxMTUuMzQ3IDU3Ljg5YzIuNzc2IDEuNDA2IDYuMTkyIDEuNDA2IDguOTY4IDBsMTE1LjM0Ny01Ny44OWMyLjc0OC0xLjQxMiA1LjEzOC0zLjYxNCA2LjkwNS01LjYxNyAxLjc2OC0yLjAwMyAyLjc2NC00LjU1NyAyLjc2NC03LjIyM1Y2NC43NDljMC02LjQ0My0uMjA1LTcuNTMtNy41NjQtMTEuNDgyTDEzMy4wNzIgMS4zOTJjLTEuNTY2LS45MDctMy4zMjEtMS4zNjQtNS4wNzQtMS4zNjRoLS4wMDZabTAgMz)](https://nodejs.org/en)
[![Java](https://img.shields.io/badge/Java_21-ED8B00?logo=openjdk&logoColor=white)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-6DB33F?logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![Spring Security](https://img.shields.io/badge/Spring%20Security-6DB33F?logo=springsecurity&logoColor=white)](https://spring.io/projects/spring-security)
[![MySQL](https://img.shields.io/badge/MySQL-4479A1?logo=mysql&logoColor=white)](https://www.mysql.com/)
[![Hibernate](https://img.shields.io/badge/Hibernate-59666C?logo=hibernate&logoColor=white)](https://hibernate.org/)
[![JWT](https://img.shields.io/badge/JWT-000000?logo=jsonwebtokens&logoColor=white)](https://jwt.io/)
[![React](https://img.shields.io/badge/React-61DAFB?logo=react&logoColor=black)](https://react.dev/)
[![React Router](https://img.shields.io/badge/React%20Router-CA4245?logo=reactrouter&logoColor=white)](https://reactrouter.com/)
[![Vite](https://img.shields.io/badge/Vite-646CFF?logo=vite&logoColor=white)](https://vite.dev/)
[![PrimeReact](https://img.shields.io/badge/PrimeReact-EE2D7D?logo=primereact&logoColor=white)](https://primereact.org/)


# CliniCore

<img width="849" height="260" alt="CliniCore Banner" src="https://github.com/user-attachments/assets/d4f7763f-08f9-4dea-848e-476ca41eae95" />

CliniCore is a custom-built Electronic Health Record (EHR) web application developed by **Lattice Labs**, a team of undergraduate Computer Science students at California State University, Sacramento. The application was created to replace an outdated EHR system used by a local medical care facility, providing staff with a modern, intuitive, and secure platform tailored specifically to the client's workflows and business needs.

---

## Table of Contents

- [What the Application Does](#what-the-application-does)
- [Why It Was Created](#why-it-was-created)
- [Features](#features)
- [Screenshots](#screenshots)
- [Installation & Setup](#installation--setup)
- [Testing](#testing)
- [Team Members](#team-members)

---

## What the Application Does

CliniCore gives administrators, caregivers, and residents a role-based portal for managing the day-to-day operations of a residential care facility. Admins and caregivers can manage resident health records, track medication schedules, monitor supply inventory, and communicate with staff and family members — all from a single secure platform. Residents have read-only access to their own health information, medication schedules, and messages. Every action is protected by JWT-based authentication and AES-256 encrypted messaging to ensure patient data remains confidential.

## Why It Was Created

The client's previous EHR system was rigid, difficult to navigate, and lacked the specific features their care team relied on daily. CliniCore was built from the ground up to match the exact specifications of their practice — from the structure of resident medical profiles to how medication intake is tracked and reported. The goal was to deliver a production-ready replacement that the care team could adopt without retraining, while meeting modern security and compliance standards for handling sensitive health data.

## Features

- [x] Allows easy management of medical supplies and medication stock levels
- [x] Provides caregivers with a dashboard to track daily tasks and patient information
- [x] Streamlines communication between staff members, family members, and residents through an internal messaging system
- [x] Fast and secure logins for both patients and staff to access and store confidential medical information
- [x] Tailor made for the client's specifications and business needs
- [x] Role-based access control for Admin, Caregiver, and Resident accounts
- [x] Resident health records with General, Medical, and Medications tabs
- [x] Encrypted internal messaging (AES-256)

---

## Screenshots

<table>
  <tr>
    <td align="center" width="50%">
      <strong>Login Page</strong><br/>
      <sub>Secure login screen where staff and residents authenticate with their credentials.</sub><br/><br/>
      <img src="ReadMe-Images/LoginPage.png" width="100%" alt="Login Page"/>
    </td>
    <td align="center" width="50%">
      <strong>Admin Dashboard</strong><br/>
      <sub>Full overview of residents, active tasks, and quick navigation to all management areas.</sub><br/><br/>
      <img src="ReadMe-Images/AdminDashboard.png" width="100%" alt="Admin Dashboard"/>
    </td>
  </tr>
  <tr>
    <td align="center" width="50%">
      <strong>Resident Portal — Medications Tab</strong><br/>
      <sub>Medication list with intake status badges, dosage, and schedule. Admins and caregivers can add, edit, update status, and remove medications.</sub><br/><br/>
      <img src="ReadMe-Images/MedicationTab.png" width="100%" alt="Medications Tab"/>
    </td>
    <td align="center" width="50%">
      <strong>Inventory Management</strong><br/>
      <sub>Track medication stock levels, view quantities, and identify low-stock items.</sub><br/><br/>
      <img src="ReadMe-Images/Inventory.png" width="100%" alt="Inventory"/>
    </td>
  </tr>
  <tr>
    <td align="center" colspan="2">
      <strong>Internal Messaging</strong><br/>
      <sub>Encrypted messaging interface for secure communication between staff members.</sub><br/><br/>
      <img src="ReadMe-Images/Messages.png" width="60%" alt="Messages"/>
    </td>
  </tr>
</table>

---

## Installation & Setup

### Prerequisites

- Java 21+
- Apache Maven 3.8+
- Node.js 18+ and npm
- MySQL 8+

### 1. Clone the Repository

```sh
git clone https://github.com/your-org/clinicore.git
cd clinicore
```



### 4. Start the Backend

```sh
cd Backend
mvn spring-boot:run
```

The backend starts on `http://localhost:8080` by default.

### 5. Start the Frontend

```sh
cd Frontend
npm install
npm run dev
```

The frontend starts on `http://localhost:5173` by default.

---

## Testing

The backend includes a full suite of integration and unit tests built with **JUnit 5**, **Spring Boot Test**, and **Mockito**. Tests run against a real MySQL database using the credentials in `Backend/src/test/resources/application.properties`.

### Run All Tests

```sh
cd Backend
mvn test
```

### Run a Specific Test Class

```sh
mvn test -Dtest=ResidentMedicationManagementIntegrationTest
```

### Test Coverage

<details>
<summary>View full test coverage table</summary>

| Test File | What It Covers |
|---|---|
| `ResidentMedicationManagementIntegrationTest` | Add, edit, update status, and delete medications via Admin and Caregiver roles; invalid inputs (missing resident, bad medication ID, invalid status value) |
| `ResidentMedicationInformationControllerIntegrationTest` | Role-based read access to medication fields by Resident, Caregiver, and Admin |
| `ResidentMedicalInformationControllerIntegrationTest` | Role-based read access to medical profile, capabilities, and services |
| `ResidentControllerIntegrationTest` | Resident CRUD, medical profile updates, resident info updates |
| `CaregiverControllerIntegrationTest` | Caregiver list and assignment endpoints |
| `InventoryControllerIntegrationTest` | Inventory management and low-stock queries |
| `MessagesControllerIntegrationTest` | Encrypted messaging between users |
| `AccountCredentialControllerIntegrationTest` | Login, account creation, credential management |
| `AccountRecoveryIntegrationTest` | Password reset and account recovery flows |
| `JwtServiceTest` | JWT token generation, validation, tamper detection |
| `EncryptionServiceTest` | AES-256 GCM encryption/decryption round-trips |
| `PasswordServiceTest` | Argon2 password hashing and verification |

</details>

---

## Team Members

**Lattice Labs** — California State University, Sacramento


<table>
  <tr>
    <td align="center">
      <a href="https://github.com/aimsngn">
        <img src="https://github.com/aimsngn.png" width="80px" alt="Olei Amelie Mae Ngan"/><br/>
        <sub><b>Olei Amelie Mae Ngan</b></sub>
      </a><br/>
      <sub>Developer</sub>
    </td>
    <td align="center">
      <a href="https://github.com/OniiSean">
        <img src="https://github.com/OniiSean.png" width="80px" alt="Sean Bombay"/><br/>
        <sub><b>Sean Bombay</b></sub>
      </a><br/>
      <sub>Developer</sub>
    </td>
    <td align="center">
      <a href="https://github.com/anhminh34">
        <img src="https://github.com/anhminh34.png" width="80px" alt="Minh Nguyen"/><br/>
        <sub><b>Minh Nguyen</b></sub>
      </a><br/>
      <sub>Developer</sub>
    </td>
    <td align="center">
      <a href="https://github.com/mileslle">
        <img src="https://github.com/mileslle.png" width="80px" alt="Miles Le"/><br/>
        <sub><b>Miles Le</b></sub>
      </a><br/>
      <sub>Developer</sub>
    </td>
  </tr>
  <tr>
    <td align="center">
      <a href="https://github.com/rushabhpatell">
        <img src="https://github.com/rushabhpatell.png" width="80px" alt="Rushabh Patel"/><br/>
        <sub><b>Rushabh Patel</b></sub>
      </a><br/>
      <sub>Developer · rushabhpatel109@gmail.com</sub>
    </td>
    <td align="center">
      <a href="https://github.com/adrianpxrez">
        <img src="https://github.com/adrianpxrez.png" width="80px" alt="Adrian Perez"/><br/>
        <sub><b>Adrian Perez</b></sub>
      </a><br/>
      <sub>Developer</sub>
    </td>
    <td align="center">
      <a href="https://github.com/winniwu07">
        <img src="https://github.com/winniwu07.png" width="80px" alt="Winni Wu"/><br/>
        <sub><b>Winni Wu</b></sub>
      </a><br/>
      <sub>Developer</sub>
    </td>
    <td align="center">
      <a href="https://github.com/hyemdanu">
        <img src="https://github.com/hyemdanu.png" width="80px" alt="Edison Ho"/><br/>
        <sub><b>Edison Ho</b></sub>
      </a><br/>
      <sub>Developer</sub>
    </td>
  </tr>
</table>
