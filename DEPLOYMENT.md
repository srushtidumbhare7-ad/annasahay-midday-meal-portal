# AnnaSahay – Complete Deployment & GitHub Guide

This guide provides step-by-step instructions for:
1. **Pushing the project code to GitHub** (Version Control & Portfolio Showcase).
2. **Deploying the live web application on the cloud** (Free hosting on Render / Railway).
3. **Running locally or on any server with Docker Compose** (Zero-configuration 1-command startup).

---

## Architecture Overview

- **Backend**: Java 17, Spring Boot 3.3.2 (Spring Data JPA, Spring Web, Validation)
- **Frontend**: HTML5, Modern Responsive CSS3 (Glassmorphism & Executive Theme), Vanilla JS (ES6+), Chart.js
- **Database**: MySQL 8.0 with automatic schema migration and seed data (`data.sql`)
- **Containerization**: Multi-stage `Dockerfile` and `docker-compose.yml`

> **Note**: GitHub Pages only hosts static client-side files (HTML/CSS/JS). Because AnnaSahay requires a live **Java 17 runtime (JVM)** and **MySQL Database**, you need a backend cloud platform (like Render or Railway) to host it live on the web.

---

## Phase 1: Pushing Code to GitHub

Your local workspace is already initialized with Git, configured with a comprehensive `.gitignore` (which protects against committing raw database binaries or target folders), and has an initial commit ready on branch `main`.

### Step 1: Create a Repository on GitHub.com
1. Open your browser and go to [https://github.com](https://github.com).
2. Log in to your GitHub account (`srushtidumbhare7-ad`).
3. Click the **`+`** icon in the top-right corner and select **New repository** (or visit [https://github.com/new](https://github.com/new)).
4. Enter the repository details:
   - **Repository name**: `AnnaSahay` (or `annasahay-midday-meal-portal`)
   - **Description**: `Smart Mid-Day Meal Monitoring & Management System (PM POSHAN Scheme) built with Java Spring Boot, MySQL, and Vanilla CSS.`
   - **Visibility**: Select **Public** (recommended for resume/portfolio visibility).
   - **IMPORTANT**: Under **"Initialize this repository with"**:
     - ❌ **Do NOT check** "Add a README file"
     - ❌ **Do NOT check** "Add .gitignore"
     - ❌ **Do NOT choose** a license
     *(Checking these will cause a git merge conflict because these files are already committed locally).*
5. Click **Create repository**.

---

### Step 2: Link Remote & Push from Local Machine
1. Open **PowerShell** or **Command Prompt** in the project folder:
   ```powershell
   cd "c:\Users\srush\OneDrive\Desktop\Folders\Java Full Stack projects\AnnaSahay_Resume"
   ```
2. Link your new GitHub repository as the remote origin:
   ```powershell
   git remote add origin https://github.com/srushtidumbhare7-ad/AnnaSahay.git
   ```
   *(If your GitHub repository has a different name, replace `AnnaSahay` with your chosen repository name).*

3. Push your code to GitHub:
   ```powershell
   git push -u origin main
   ```

4. If prompted for credentials:
   - Choose **Sign in with your browser**, OR
   - Enter your GitHub username (`srushtidumbhare7-ad`) and your **Personal Access Token (PAT)** as the password.
     *(To generate a PAT: GitHub Settings → Developer Settings → Personal Access Tokens → Tokens (classic) → Generate new token with `repo` permissions).*

---

### Step 3: Making Future Updates
Whenever you make changes to the code in the future, push updates using these three commands:
```powershell
git add .
git commit -m "Describe what changes you made"
git push
```

---

## Phase 2: Live Cloud Deployment (Free on Render.com)

Once your code is pushed to GitHub, you can host the running application live on the web with a public URL in minutes.

### Step 1: Set Up a Free MySQL Database
Because Spring Boot needs a MySQL database to connect to:
1. Go to **[Aiven.io](https://aiven.io/)** (Free tier MySQL) or **[Railway.app](https://railway.app/)**.
2. Sign up and click **Create Service** → **MySQL**.
3. Choose the **Free** tier.
4. Copy the connection details:
   - **Host** (e.g., `mysql-xxxx.aivencloud.com`)
   - **Port** (e.g., `12345` or `3306`)
   - **Database Name** (e.g., `defaultdb` or `annasahay`)
   - **Username** (e.g., `avnadmin` or `root`)
   - **Password** (e.g., your generated password)

---

### Step 2: Deploy AnnaSahay on Render
1. Visit **[Render.com](https://render.com/)** and log in with your **GitHub** account.
2. In the dashboard, click **New +** → **Web Service**.
3. Choose **Build and deploy from a Git repository**.
4. Select your **`AnnaSahay`** repository.
5. Fill in the service configuration:
   - **Name**: `annasahay` (or `annasahay-portal`)
   - **Region**: Nearest region (e.g., Singapore, Frankfurt, or Oregon)
   - **Branch**: `main`
   - **Runtime**: **Docker** *(Render automatically detects the provided `Dockerfile`)*
   - **Instance Type**: **Free**
6. Scroll down to **Environment Variables** and add the following 3 variables:

| Key | Value Example |
| :--- | :--- |
| `SPRING_DATASOURCE_URL` | `jdbc:mysql://<your-db-host>:<port>/<db_name>?createDatabaseIfNotExist=true&useSSL=false&serverTimezone=UTC` |
| `SPRING_DATASOURCE_USERNAME` | `<your-db-username>` |
| `SPRING_DATASOURCE_PASSWORD` | `<your-db-password>` |

7. Click **Create Web Service**.
8. Render will automatically:
   - Build the Docker container.
   - Run Maven build (`mvn clean package -DskipTests`).
   - Launch the Spring Boot JAR (`target/anna-sahay-0.0.1-SNAPSHOT.jar`).
   - Run Hibernate schema creation and seed tables using `data.sql`.
9. Once the logs display `Started AnnaSahayApplication in ... seconds`, click the public URL provided by Render (e.g., `https://annasahay.onrender.com`).

---

## Phase 3: One-Command Deployment with Docker Compose

If you or a recruiter wants to run the entire application stack (MySQL 8 + Spring Boot + UI) on any computer or cloud VM without installing Java or MySQL locally:

### Prerequisites:
- Install **Docker Desktop** on your machine.

### Run the Stack:
Open your terminal in the project directory and execute:
```bash
docker-compose up --build
```

### What Happens Automatically:
1. Docker pulls the official `mysql:8.0` image and starts the database container with a persistent volume.
2. Docker runs the multi-stage `Dockerfile`: builds the Spring Boot JAR with Maven and packs it into a lightweight Alpine Linux JRE image.
3. Spring Boot connects to the MySQL container on the internal bridge network.
4. The application is served on **`http://localhost:8080`**.

### Stop the Containers:
```bash
docker-compose down
```

---

## Phase 4: Deploying on a Cloud Virtual Machine (AWS EC2 / DigitalOcean / Ubuntu VPS)

If you are deploying on an Ubuntu cloud server:

1. **Connect via SSH**:
   ```bash
   ssh -i your-key.pem ubuntu@your-server-ip
   ```

2. **Install Docker & Git**:
   ```bash
   sudo apt update && sudo apt install -y git docker.io docker-compose
   sudo usermod -aG docker ubuntu
   ```

3. **Clone Your Repository**:
   ```bash
   git clone https://github.com/srushtidumbhare7-ad/AnnaSahay.git
   cd AnnaSahay
   ```

4. **Launch Application Daemon**:
   ```bash
   docker-compose up -d --build
   ```

5. **Verify Running Containers**:
   ```bash
   docker ps
   ```
   Your app will be live on `http://your-server-ip:8080`.

---

## Troubleshooting & FAQ

### 1. `fatal: remote origin already exists`
If you made a typo when setting the remote origin, run:
```powershell
git remote remove origin
git remote add origin https://github.com/srushtidumbhare7-ad/AnnaSahay.git
```

### 2. Browser Shows Old UI Cached
If you made changes to CSS or JS and the browser shows older styles:
- Press `Ctrl + F5` (Windows) or `Cmd + Shift + R` (Mac) to bypass the browser cache.

### 3. Default Login Credentials
Upon initial deployment, default credentials seeded by `data.sql`:
- **Username**: `admin`
- **Password**: `admin123`
