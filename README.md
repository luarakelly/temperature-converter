# Unit Converter

## 1. Assignment Description

This individual in-class assignment was completed independently to practise software development tools and technologies used in a software project.

The project is a JavaFX desktop application for converting temperatures between Celsius, Fahrenheit, and Kelvin, as well as speed, distance, and time units. The application saves every conversion to a MariaDB database and displays the last five conversions in a table.

Static UI labels (titles, buttons, messages) come from `MessagesBundle_*.properties` files, while unit names and descriptions are stored in the database and read in the selected language.

The interface supports English, Finnish, Swedish, Japanese, and Arabic. Its FXML views are split into language selection, converter input, results, and history components, coordinated by `main.fxml` and `MainController`.

The assignment demonstrates the use of:

- Java and JavaFX for application development
- Maven for project and dependency management
- JUnit for automated testing
- JaCoCo for code coverage
- Jenkins for continuous integration
- Docker for containerisation
- MariaDB for persistent data storage

The main requirements addressed in the assignment were:

- Implement a functional JavaFX desktop application.
- Support five languages with UTF-8 ResourceBundle property files and dynamic language switching.
- Store localized unit names and descriptions in the database using the translation table approach, with English fallback.
- Switch to right-to-left layout when Arabic is selected.
- Implement temperature conversion functionality.
- Validate user input.
- Save conversion results to a database.
- Display saved conversion records.
- Create automated tests.
- Generate a JaCoCo code coverage report.
- Create a Jenkins pipeline for automated build and testing.
- Build and publish the application as a Docker image.

---

## 2. Technologies & Tools Used

| Area | Technology |
| --- | --- |
| Programming language | Java 21 |
| GUI | JavaFX 21 |
| Build tool | Maven |
| Database | MariaDB |
| Database driver | MariaDB Java Client 3.5.10 |
| Configuration | dotenv-java 3.2.0 |
| Unit testing | JUnit Jupiter 5.12.2 |
| Test runner | Maven Surefire 3.5.2 |
| Code coverage | JaCoCo 0.8.14 |
| CI/CD | Jenkins |
| Containerisation | Docker |
| Container image registry | Docker Hub |
| Version control | Git and GitHub |

---

## 3. Design Approach & Implementation Method

The application uses a layered design with a coordinating FXML view and focused component views/controllers. Java packages are organized under `app.controllers`, `app.model`, and `app.db`; localization bundles live in `src/main/resources/i18n`.

| Part | Implementation |
| --- | --- |
| Model | `MeasurementUnit` (unit with localized name and description) and `ConversionRecord` (saved conversion). |
| View | `main.fxml` coordinates `converter.fxml`, `results.fxml`, `history.fxml`, and `language-selector.fxml`. |
| Coordinator | `MainController` loads ResourceBundles, switches languages, coordinates conversion, and connects the interface to the DAOs. |
| Component controllers | Each included FXML view has a controller responsible for its own controls and presentation. |
| Conversion logic | `UnitConverter` converts between units of the same category; temperatures use `TemperatureConverter`, other units use `factor_to_base`. |
| DAO | `MeasurementUnitDAO` returns units localized to the selected language with English fallback; `ConversionRecordDAO` saves conversions and loads the last five. |
| Database | MariaDB stores categories, units, unit translations, and conversion records. |

The main application flow is:

1. The user enters a value.
2. The user selects the source unit; the target list shows only units of the same category.
3. The application validates the input.
4. The value is converted to the target unit.
5. The result is displayed with the localized unit names from the database.
6. The conversion (value, from unit, to unit, result, language, timestamp) is saved to MariaDB.
7. The last five conversions are displayed in the history table.

The database is created by `database/init.sql` and contains:

| Group | Tables |
| --- | --- |
| Measurement | `conversion_record` |
| Unit/lookup data | `measurement_category`, `measurement_unit` |
| Localization | `unit_translation` |

---

## 4. Localization

Localization has two layers, and the language selector changes both at once:

1. **UI labels** use UTF-8 `MessagesBundle_*.properties` files in `src/main/resources/i18n` for English, Finnish, Swedish, Japanese, and Arabic. `MainController` loads the selected `ResourceBundle` and passes it to each included view controller.
2. **Unit names and descriptions** are read from the database in the selected language (`en`, `fi`, `sv`, `ja`, `ar`). After switching language, `MainController` reloads the unit dropdowns, the last result, and the history table from the database.

When Arabic is selected, the root node orientation is set to right-to-left.

### Database Localization Approach: Translation Table

**Chosen approach:** translation table.

**Schema:**

```text
measurement_category (category_code PK)
        1
        |
        n
measurement_unit (id PK, unit_key UNIQUE, category_code FK, factor_to_base)
        1
        |
        n
unit_translation (unit_id FK, language_code, name, description,
                  PRIMARY KEY (unit_id, language_code))

conversion_record (id PK, input_value, from_unit_id FK, to_unit_id FK,
                   result_value, language_code, created_at)
```

`measurement_unit` is language-neutral: it only holds the unit key, its category, and the conversion factor. All text that depends on the language lives in `unit_translation`, one row per unit and language. All tables and the connection use `utf8mb4`, so Japanese and Arabic are stored correctly.

The DAO joins the translation table twice, once for the selected language and once for English, and uses `COALESCE` to fall back to English (and finally to the `unit_key`) when a translation is missing:

```sql
SELECT u.id, u.unit_key, u.category_code, u.factor_to_base,
       COALESCE(t.name, d.name, u.unit_key) AS name,
       COALESCE(t.description, d.description, '') AS description
FROM measurement_unit u
LEFT JOIN unit_translation t ON t.unit_id = u.id AND t.language_code = ?
LEFT JOIN unit_translation d ON d.unit_id = u.id AND d.language_code = 'en'
```

**Why this approach:**

- **No duplicated non-text data.** The conversion factor and category are stored once per unit. With the row approach they would be repeated in every language row and could drift out of sync.
- **Adding a language needs no schema or code change.** A new language only needs new rows in `unit_translation`. The field approach would need a new column and the table approach a new table plus code to pick it at runtime.
- **Clean foreign keys.** `conversion_record` references `measurement_unit.id`, a single language-neutral row, so a saved conversion can be shown in whatever language is currently selected.
- **Built-in fallback.** A missing translation never breaks the GUI; the English name is shown instead. This is covered by unit tests.
- **No NULL-heavy columns.** Unlike the field approach, a missing translation is simply a missing row.

The cost is one extra JOIN per query and slightly more SQL, which is acceptable for a table of a few dozen rows. This is also the approach commonly used in production systems.

---

## 5. Jenkins Pipeline

A Jenkins Pipeline was created for the project using a `Jenkinsfile`.

The pipeline automates the main build, testing, reporting, and Docker steps.

### Pipeline Stages

#### 1. Checkout

Jenkins checks out the `main` branch from the GitHub repository.

#### 2. Build and Test

Maven builds the project and runs the automated tests:

mvn clean install


#### 3. Code Coverage

JaCoCo generates the code coverage report:

mvn jacoco:report


#### 4. Publish Test Results

Jenkins publishes the JUnit test results generated by Maven.

#### 5. Publish Coverage Report

The Jenkins JaCoCo plugin publishes the generated code coverage information.

#### 6. Build Docker Image

Jenkins builds a Docker image for the application using the project's `Dockerfile`.

The Docker image uses Eclipse Temurin JDK 21 and JavaFX 21.

#### 7. Push Docker Image to Docker Hub

After the Docker image is successfully built, Jenkins authenticates with Docker Hub using Jenkins credentials and pushes the image to the configured Docker Hub repository.

---

## 6. Testing & Quality Assurance

The project uses automated JUnit tests to verify the application's functionality.

### Test Areas

| Test area | Examples |
| --- | --- |
| Temperature conversion | Celsius, Fahrenheit, and Kelvin conversions |
| Unit conversion | Speed, distance, and time conversions and category checks |
| Value validation | Values below absolute zero and negative distance, time, and speed |
| Input validation | Empty input and invalid numeric values |
| DAO | Localized unit names in Japanese and Arabic, English fallback for missing translations, saving conversions, and loading the last five |
| Application functionality | Language switching of both layers, right-to-left layout, conversion, and saving |

The DAO tests run real SQL against an in-memory H2 database (MariaDB mode) created from `src/test/resources/test-db.sql`, which leaves some translations out on purpose to test the fallback.

The JaCoCo `check` goal requires at least 80% line and branch coverage for the core logic (`TemperatureConverter`, `UnitConverter`, `MeasurementUnitDAO`, `ConversionRecordDAO`).

### Test Results

The automated tests were executed using Maven:

mvn test


All tests passed successfully.

### Code Coverage

JaCoCo is used to measure test coverage.

The coverage report can be generated using:

mvn jacoco:report


The generated report is located at:

target/site/jacoco/index.html


The Jenkins pipeline also generates and publishes the JaCoCo coverage report.

---

## 7. How to Run

### Prerequisites

Install:

- JDK 21
- Maven
- MariaDB
- Git

Docker Desktop is required if the Docker version of the application is used.

### Run Locally

1. Clone the repository:

git clone https://github.com/luarakelly/temperature-converter.git cd temperature-converter


2. Create the MariaDB database, tables, translations, and application user with `database/init.sql`:

```sh
mariadb -u root -p --default-character-set=utf8mb4 < database/init.sql
```

The default database configuration is:

Database: `temperature_converter_db`; user: `temperature_app`; password: `temperature_app`; host: `localhost`; port: `3306`. Override these values with the `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USER`, and `DB_PASSWORD` environment variables or a local `.env` file.


3. Build the project:

mvn clean install


4. Run the automated tests:

mvn test


5. Start the JavaFX application:

mvn javafx:run


### Generate Code Coverage

Run:

mvn jacoco:report


The report is generated in:

target/site/jacoco/index.html


### Run with Docker

1. Start Docker Desktop.

2. Build the Maven project:

mvn clean package


3. Build the Docker image:

docker build -t luaram/temperature-converter:latest .


4. Run the Docker container:

docker run --rm -e DISPLAY=host.docker.internal:0.0 -e DB_HOST=host.docker.internal luaram/temperature-converter:latest

Configure Xming or another X server to accept the container's display connection. The database must also be running and reachable from the container. The image includes the IPA Gothic and DejaVu fonts so Japanese and Arabic text is rendered correctly.

## 8. Kubernetes / Minikube

The deployment template is `kubernetes/temperature-converter.yaml`. It deploys the Docker Hub image and configures the display and database host through a ConfigMap. It expects a MariaDB server reachable from Minikube and a Kubernetes Secret containing its credentials.

1. Start Minikube and ensure the MariaDB server is reachable from it. Update `DB_HOST` and `DISPLAY` in the ConfigMap in the YAML if `host.minikube.internal` is not correct for your Minikube driver.
2. Create the database credentials Secret (use the credentials configured for your MariaDB user):

   ```sh
   kubectl create secret generic temperature-db \
     --from-literal=username=temperature_app \
     --from-literal=password='your-db-password'
   ```

3. Apply the deployment:

   ```sh
   kubectl apply -f kubernetes/temperature-converter.yaml
   kubectl rollout status deployment/temperature-converter
   kubectl logs -f deployment/temperature-converter
   ```

The GUI needs an accessible X server, and the MariaDB user must allow connections from the Minikube network. The manifest does not create a database server or open a network Service because the application is a desktop GUI.

---

## 9. Author

**Luara Kelly Moreira da Silva**

Tieto- ja viestintätekniikka  
TVT25K-O  
Ohjelmistotuotantoprojekti 1  
TX00EY27-3012
