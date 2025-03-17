# ExpenseTracker - Multi-user household budget management system with expense analysis module

**ExpenseTracker** is a **multi-user IT system** designed to support **household budget management** and provides an **expense analysis module** for graphical representation of statistics over user-defined time periods. The system is designed in a **three-tier architecture**, consisting of a **web application**, a **network application**, and a **relational database**. The web application communicates with the network application via HTTP requests to a REST web service. The network application communicates with the database using a JDBC connection.
![Main](https://github.com/malinowskaMM/Expense-Tracker/blob/master/ui/main.png)

ExpenseTracker offers a user interface through **dynamically generated web pages (SPA)** supporting three access levels: **System Administrator, Budget Manager, Guest**, with functionality dependent on the user's role. The system focuses on group financial management with basic expense analytics, avoiding access to bank account history data.

![Analysis1](https://github.com/malinowskaMM/Expense-Tracker/blob/master/ui/analzy1.png)

![Analysis2](https://github.com/malinowskaMM/Expense-Tracker/blob/master/ui/analzy2.png)

### Screenshots

Below is a walkthrough of ExpenseTracker's key interfaces, ordered according to a typical user journey:

1. **Registration Form** – Create a new user account with email verification.
   
   ![Registration](https://github.com/malinowskaMM/Expense-Tracker/blob/master/ui/rejestracja.png)

2. **Account Activation Confirmation** – Screen shown after successful activation via email.

   ![Account Activation](https://github.com/malinowskaMM/Expense-Tracker/blob/master/ui/potwierdzenieaktywacjikonta.png)

3. **Login Page** – Authenticate with email and password to access the system.

   ![Login](https://github.com/malinowskaMM/Expense-Tracker/blob/master/ui/logowanie.png)

4. **User Account Panel** – Manage personal data, change email or password.

   ![User Account](https://github.com/malinowskaMM/Expense-Tracker/blob/master/ui/konto.png)

5. **System Settings** – Customize preferences, including language and visual theme.

   ![Settings](https://github.com/malinowskaMM/Expense-Tracker/blob/master/ui/opcje.png)

6. **Category Management** – Define and manage categories for income and expenses.

   ![Categories](https://github.com/malinowskaMM/Expense-Tracker/blob/master/ui/kategorie.png)

7. **Group Management** – Create and manage user groups for shared budget tracking.

   ![Groups](https://github.com/malinowskaMM/Expense-Tracker/blob/master/ui/grupy.png)

8. **Calendar View** – Visualize scheduled financial operations (income/expenses) on specific dates.

   ![Calendar](https://github.com/malinowskaMM/Expense-Tracker/blob/master/ui/kalendarz_zprzyk%C5%82adow%C4%85zawarto%C5%9Bci%C4%85.png)

9. **User Detail (Admin)** – View detailed account info for any user (Admin access).

   ![User Details - Admin](https://github.com/malinowskaMM/Expense-Tracker/blob/master/ui/konto_szczeg%C3%B3%C5%82ykontauzytkownikasystemu.png)

10. **User List (Admin)** – Manage all registered users in the system.

   ![User List - Admin](https://github.com/malinowskaMM/Expense-Tracker/blob/master/ui/listakontu%C5%BCytkownik%C3%B3w.png)


### Local Setup

To run ExpenseTracker locally, follow these steps:

1. In the command line, run: `docker-compose up --build`.
2. The database container will start first, followed by the network application and finally the web application.
3. When you see the message `backend_server  | (…) INFO  p.l.p.i.e.ExpenseTrackerApplication - Started ExpenseTrackerApplication` in the console, the system should be fully operational.
4. The **web application** will be available at **http://localhost:3000/**.
5. The **API for the network application** will be available at **http://localhost:8080/swagger-ui/index.html#/**.
6. The **database** will be running on port **5432**.

### Implementation

ExpenseTracker is implemented with the following key components:

*   **Three-tier architecture:** Separation of data layer, business logic (network application), and presentation layer (web application).
*   **Web application:** Implemented as a **Single-Page Application (SPA)** using **TypeScript and React**. It provides a dynamic user interface, role-based view access (Guest, Budget Manager, Administrator), and view internationalization (Polish and English). Communication with the network application is handled via **Axios**. Features include **account registration with email activation**, **password recovery**, **password and email change**. The application allows **group management of Budget Managers**, **management of financial operation categories**, **adding and viewing financial operations (income and expenses)**, **generating financial reports and analyses (income vs expenses, category-wise expenses)** with **chart visualizations** (ChartJS) for any selected period. **Client-side form validation** is implemented.
*   **Network application:** Built with **Spring Boot**. A **stateless application** communicating with the web application via **REST API (JSON, HTTP, HTTPS)**. It implements **JWT-based authentication and authorization** and **RBAC (Role-Based Access Control)**. It provides **transactional data processing**, **optimistic locking**, and **ETag** for data consistency in a multi-user environment. Logging is handled via **Slf4j and Logback**. Email functionality (e.g., account activation, password reset) is provided using **Spring Email**, with support for internationalized emails and error messages. It performs **HTTP request data validation** using **Bean Validation** and business-level validation. Error handling includes converting exceptions into HTTP responses with appropriate error codes and internationalization keys. Database communication is managed using **Spring Data JPA and Hibernate (ORM)**.
*   **Relational database:** Powered by **PostgreSQL**. Data is stored in static table structures, with access provided through **JDBC** and the **JPA ORM**. Two separate data storage units with different permissions are used for user account management and budget management, increasing system security.

### Tools and Technologies

| Name                        | Version             |
| :-------------------------- | :------------------ |
| PostgreSQL                  | 16.2.0              |
| PostgreSQL JDBC Driver      | 42.6.0              |
| Hibernate                   | 7.0.1.Final         |
| Spring Data JPA             | 3.1.3               |
| OpenJDK                     | 21                  |
| Spring Boot                 | 3.2.5               |
| Spring Security             | 6.1.3               |
| Spring Email                | 3.1.3               |
| Spring Web MVC              | 6.0.11              |
| Spring Retry                | 2.0.3               |
| Lombok                      | 1.18.28             |
| Jsonwebtoken                | 0.11.5              |
| Springfox-swagger2          | 3.0.0               |
| Slf4j                       | 2.0.7               |
| Logback                     | 1.4.11              |
| Rest-assured                | 3.3.0 (for testing) |
| Hamcrest                    | 1.3 (for testing)   |
| Testcontainers              | 1.19.3 (for testing)|
| Jaxb                        | 2.3.1               |
| Apache Tomcat               | 10.1.12             |
| @Emotion/react              | 11.11.1             |
| @Emotion/styled             | 11.11.0             |
| @Fortawesome                | 6.4.2               |
| @Fortawesome/react-fontawesome | 0.2.0           |
| @Mui                        | 5.14.7              |
| Axios                       | 1.6.2               |
| Dayjs                       | 1.11.10             |
| I18next                     | 23.4.6              |
| I18next-browser-languagedetector | 7.1.0         |
| I18next-http-backend        | 2.2.1               |
| Js-cookie                   | 3.0.5               |
| React                       | 18.2.0              |
| React-color                 | 2.19.3              |
| React-dom                   | 18.2.0              |
| React-hook-form             | 7.48.2              |
| React-i18next               | 13.2.1              |
| React-router, React-router-dom | 6.15.0          |
| React-scripts               | 5.0.1               |
| Git                         | 2.33.0.windows2     |
| Apache Maven                | 3.8.3               |
| Docker                      | 20.10.21            |
| IntelliJ IDEA Ultimate      | 2023.1.2            |
| Figma                       | 116.15.4            |
| Visual Studio Code          | 1.85.1              |
| Spring Boot Testcontainers  | 3.1.3 (for testing) |
| REST Assured                | 5.3.0 (for testing) |
| JUnit Jupiter               | 1.19.3 (for testing)|
| AssertJ                     | 3.8.0 (for testing) |
| Docker Desktop              | 4.27.1 (for testing)|
| ChartJS                     | (for charts)        |
