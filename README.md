# Online Recipe Sharing Platform

A Java web application where people can publish recipes, explore dishes shared by others, leave ratings and comments, and manage their own submissions. Recipes go through an approval step before they appear in the public listing, so administrators can review new submissions and moderate content.

This project was built as an academic prototype using Java, Jakarta Servlets and JSP, Firebase Authentication, and Cloud Firestore.

> **Current project status:** `mvn clean package` completed successfully, and the 32 existing unit tests passed with no failures or errors. Local Tomcat deployment and full browser-based testing are still to be confirmed, so the project should not yet be described as production-ready.

## What the application includes

### For users

- Register and sign in with Firebase Authentication.
- Browse approved recipes and search or filter by category.
- Create recipes with ingredients, instructions, preparation details, and an image URL.
- View and manage your own submissions.
- Rate recipes on a 1–5 scale and leave comments.
- Update or remove your own comments where supported by the application.
- View a user dashboard and profile.

New recipes start with a `PENDING` status. If a user edits a recipe, it returns to pending review according to the current workflow.

### For administrators

- Review recipes awaiting approval.
- Approve or reject submissions.
- Access the administration dashboard and statistics.
- Manage user accounts, including account activation and role changes, where configured.
- Review activity logs.

Administrator features require an authorized admin account. Do not attempt to make a user an administrator by changing browser form values or request parameters.

## Technology

| Area | Technology |
|---|---|
| Language | Java (project compiler configuration targets Java 26) |
| Web layer | Jakarta Servlets 6.0 and JSP 3.1 |
| Application server | Apache Tomcat 10.1.x |
| Build and packaging | Maven; WAR packaging |
| Authentication | Firebase Authentication and Firebase Admin SDK |
| Database | Cloud Firestore |
| Frontend | HTML, CSS, JavaScript, Bootstrap 5, Font Awesome |
| Unit testing | JUnit 4 |
| Logging | SLF4J and Logback |

The project uses a traditional Servlet/JSP architecture. It does not use Spring Boot or a JavaScript server framework.

## How the code is organized

The application is split into layers so that request handling, application rules, and database access are not all mixed together.

```text
src/
├── main/
│   ├── java/com/recipeapp/
│   │   ├── bootstrap/      # Initial setup helpers
│   │   ├── concurrent/     # Statistics cache and concurrency utilities
│   │   ├── config/         # Firebase initialization
│   │   ├── exception/      # Application-specific exceptions
│   │   ├── filter/         # Authentication and role checks
│   │   ├── model/          # Recipe, user, rating, comment, and related models
│   │   ├── repository/     # Repository interfaces and Firestore implementations
│   │   ├── seed/           # Seed-data utilities
│   │   ├── service/        # Application rules and workflows
│   │   ├── servlet/        # HTTP request handlers
│   │   └── util/           # Mapping and shared utilities
│   └── webapp/
│       ├── admin/           # Administrator pages
│       ├── user/            # User dashboard pages
│       ├── WEB-INF/         # Web application configuration
│       ├── index.jsp
│       ├── login.jsp
│       ├── register.jsp
│       ├── recipes.jsp
│       ├── recipe-details.jsp
│       └── ...
└── test/java/               # Unit tests

pom.xml                      # Maven build configuration
```

Some filenames or folders may change as the project develops; the source tree is the definitive reference.

## Before you start

Install or configure the following on your development machine:

- A JDK compatible with the Java version configured in `pom.xml`.
- Maven.
- Apache Tomcat 10.1.x for local web deployment.
- A Firebase project with Authentication and Cloud Firestore enabled.
- A Firebase service-account JSON file for the server-side Admin SDK.

This project currently references the Firebase project ID `recipe-sharing-63c2b`. If you use a different Firebase project, update the Firebase configuration to match it.

## Firebase configuration

The server uses Firebase Admin SDK credentials to access Firebase services. Keep the service-account JSON file **outside the repository** and provide its location through the `GOOGLE_APPLICATION_CREDENTIALS` environment variable.

### Windows Command Prompt

Set the variable for the current terminal session (replace the example path with the real location of your JSON file):

```bat
set "GOOGLE_APPLICATION_CREDENTIALS=C:\path\to\your-service-account.json"
```

### Windows PowerShell

```powershell
$env:GOOGLE_APPLICATION_CREDENTIALS = 'C:\path\to\your-service-account.json'
```

### macOS or Linux

```bash
export GOOGLE_APPLICATION_CREDENTIALS="/path/to/your-service-account.json"
```

Set the environment variable in the environment used to launch Tomcat, not just in an unrelated terminal. If the variable is missing or points to the wrong file, the server may fail to initialize Firebase.

**Never commit the service-account JSON, private keys, or other credentials to GitHub.** A `.gitignore` rule helps prevent accidental commits, but check the staged files before every push.

## Build and run the tests

Open a terminal in the project directory, then run:

```bash
mvn clean package
```

This command cleans old build output, compiles the application, runs the configured tests, and packages the web application as a WAR file.

To run the tests separately:

```bash
mvn test
```

The recorded build completed with `BUILD SUCCESS`. The test output showed **32 tests, 0 failures, 0 errors, and 0 skipped**. These are unit-test results; they do not by themselves confirm that Tomcat deployment, Firebase access, or all browser workflows are working.

The generated WAR should be located at:

```text
target/recipe-sharing-platform.war
```

The `target/` directory is generated by Maven and should not be committed to Git.

## Deploy to Tomcat

Use a Tomcat 10.1.x installation compatible with the Servlet/JSP APIs in the project. The existing local deployment setup has required additional configuration, so check the Tomcat logs rather than assuming the application has started successfully.

1. Build the project with `mvn clean package`.
2. Configure `GOOGLE_APPLICATION_CREDENTIALS` in the environment used by Tomcat.
3. Copy `target/recipe-sharing-platform.war` into Tomcat's `webapps` directory.
4. Start Tomcat and inspect its logs for deployment or Firebase initialization errors.
5. Open the application using the port and context path configured for your Tomcat installation.

For a standard local installation, the URL may look like this:

```text
http://localhost:8080/recipe-sharing-platform/
```

The URL can differ if you use another port or context path. Do not change or stop another application that is already using the port you need; configure a separate available port instead.

## How recipe approval works

Recipes use three main statuses:

- `PENDING` — submitted and waiting for review; not shown in public discovery.
- `APPROVED` — visible to users browsing public recipes.
- `REJECTED` — reviewed and not approved for public listing.

The intended flow is: a user submits a recipe, an administrator reviews it, and an approved recipe becomes publicly discoverable.

## Firestore data

The application stores data in Cloud Firestore. The main collections described by the project are:

| Collection | Purpose |
|---|---|
| `users` | User profile, role, active status, and account metadata |
| `recipes` | Recipe details, author, approval status, and rating/comment counts |
| `ratings` | A user's rating for a recipe |
| `comments` | Comments associated with recipes and their authors |
| `activityLogs` | Recorded user and administration activities |
| `settings` | Application settings, where used |

A rating ID is generated from the recipe ID and user ID, allowing the application to keep one rating record per user per recipe. The actual data available in Firebase depends on how the project is configured and used.

## Authentication and access control

The sign-in flow uses Firebase Authentication. The browser sends a Firebase ID token to the application, and the server verifies the token before establishing an HTTP session. The session holds the authenticated user's identity and role for subsequent requests.

Access should be checked on the server, not just hidden in the interface. Recipe updates and deletes should verify ownership, and administrator endpoints should require the administrator role. Never trust a user ID or role simply because it was submitted by the browser.

These are important security requirements for the project. Before using the application beyond a local academic demonstration, review the implementation and test the actual deployed behavior rather than relying on this description alone.

## Current test coverage

The existing unit-test suites cover model validation and mapping, along with statistics-related classes. The recorded test run includes:

| Test suite | Tests reported |
|---|---:|
| `RatingTest` | 5 |
| `CommentTest` | 4 |
| `RecipeTest` | 5 |
| `UserTest` | 6 |
| `FirestoreMapperTest` | 8 |
| `StatisticsSnapshotTest` | 4 |
| **Total** | **32** |

Once the application has been deployed, the next step is to run browser-based tests for registration, login, recipe creation, moderation, comments, ratings, authorization, and Firestore persistence. Those end-to-end checks should be recorded separately from unit-test results.

## Known limitations

The current project documentation identifies these limitations:

- Recipe images are provided by URL; image uploading is not implemented.
- Editing a recipe sends it back for approval.
- Recipe history/versioning is not implemented.
- Recipe listings do not currently include pagination.
- Complex Firestore queries may require indexes to be created in the Firebase console.
- Real-time updates and offline persistence are not part of the current prototype.

This is an academic prototype, not a production-hardened service. A production deployment would need further security review, operational monitoring, broader integration testing, and a review of data retention and recovery requirements.

## Troubleshooting

**The application does not deploy**

- Confirm that you are using Tomcat 10.1.x and a compatible JDK.
- Check the Tomcat logs for the first deployment error.
- Confirm that the WAR exists in `target/` and was copied to the correct `webapps` directory.
- Check that the port is not already in use.

**Firebase initialization fails**

- Verify that `GOOGLE_APPLICATION_CREDENTIALS` is set in the Tomcat process environment.
- Confirm that the referenced file exists and is a valid service-account credential for the intended Firebase project.
- Check that the service account has the necessary permissions and that network access is available.
- Do not paste service-account JSON or private keys into issue reports.

**A recipe does not appear in public search**

- Check that its status is `APPROVED`.
- Review the application logs for query errors.
- Check the Firebase console for any required Firestore indexes.

**The site returns 404 or 500**

- Confirm the deployed context path and URL.
- Inspect the browser's developer console and the Tomcat logs for the underlying error.

## Useful project documents

The repository may also include supporting notes and reports, such as:

- `FIREBASE_SETUP.md` — Firebase configuration notes.
- `PHASE_2_REPORT.md`, `PHASE_3_REPORT.md`, and `PHASE_5_REPORT.md` — implementation and verification records.
- `SUBMISSION_CHECKLIST.md` — items to review before submission.
- `VIVA_GUIDE.md` — notes for explaining the project during a demonstration or viva.

Check each document before publishing to ensure it contains no private information, credentials, or outdated instructions.

## References

- [Firebase Admin SDK setup](https://firebase.google.com/docs/admin/setup)
- [Firebase Authentication](https://firebase.google.com/docs/auth)
- [Cloud Firestore documentation](https://firebase.google.com/docs/firestore)
- [Jakarta Servlet 6.0 specification](https://jakarta.ee/specifications/servlet/6.0/)
- [Apache Tomcat 10.1 documentation](https://tomcat.apache.org/tomcat-10.1-doc/)
- [Bootstrap documentation](https://getbootstrap.com/docs/5.3/)


No open-source license has been specified yet. Add a `LICENSE` file if you decide to publish the project under a particular license.
