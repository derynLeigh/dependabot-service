# PHASE 1
## Initial Setup
### Step 1: Install Prerequisites
#### Install Java 21
#### Download from: https://adoptium.net/

#### Install Gradle (if not already installed)
**macOS**
`brew install gradle`

### Install Gauge
**macOS**
`brew install gauge`

#### Verify installations
`java -version`    ### Should show 21.x.x

`gradle -v`        ### Should show 8.x

`gauge version`    ### Should show gauge version

## Step 2: Create Project Directory
#### Create project root
`mkdir dependabot-service`

`cd dependabot-service`

#### Initialize as Git repository
`git init`


#### Create .gitignore
`cat > .gitignore << 'EOF'`
```
# Gradle
.gradle/
build/
!gradle/wrapper/gradle-wrapper.jar
!**/src/main/**/build/
!**/src/test/**/build/

# IDE
.idea/
*.iml
.vscode/
*.code-workspace

# OS
.DS_Store
Thumbs.db

# Logs
*.log
logs/

# Environment
.env
.env.local

# Gauge
reports/
logs/

# Spring Boot
!.mvn/wrapper/maven-wrapper.jar
!**/src/main/**/target/
!**/src/test/**/target/
```
`EOF`

## Step 3: Create Gradle Configuration Files
Create these files in your project root:

`build.gradle` - Main build configuration (from artifact above)

`settings.gradle` - Project settings (from artifact above)

`gradle.properties` - Gradle properties (from artifact above)

## Step 4: Create Directory Structure

#### Create main source directories
`mkdir -p src/main/java/com/dependabot`

`mkdir -p src/main/resources`

#### Create test directories
`mkdir -p src/test/java/com/dependabot`

`mkdir -p src/test/resources`

#### Create Gauge directories
`mkdir -p specs`

`mkdir -p src/test/java/com/dependabot/gauge`

## Step 5: Verify Gradle Setup

#### Download dependencies and verify build

`./gradlew build`

#### Run tests (should pass - nothing to test yet)

`./gradlew test`

#### Check dependencies

`./gradlew dependencies`

## Step 6: Initialize Gauge
#### Initialise Gauge project (if not using gradle task)

`gauge init java`

This creates:
 - specs/ directory for specification files
 - env/ directory for environment configs
 - manifest.json for Gauge configuration