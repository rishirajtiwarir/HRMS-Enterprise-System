$JdkVersion = "21.0.2+13"
$JdkDirName = "jdk-21.0.2+13"
$JdkDir = "$PSScriptRoot\.jdk"
$JavaHomePath = "$JdkDir\$JdkDirName"
$JavaExe = "$JavaHomePath\bin\java.exe"

$MavenVersion = "3.9.6"
$MavenDir = "$PSScriptRoot\.maven"
$MvnPath = "$MavenDir\apache-maven-$MavenVersion\bin\mvn.cmd"

# ===================================================================
# 1. DOWNLOAD PORTABLE JAVA 21 JDK IF NOT PRESENT
# ===================================================================
if (-not (Test-Path $JavaExe)) {
    Write-Host "--------------------------------------------------------" -ForegroundColor Cyan
    Write-Host "Portable Java 21 JDK not found. Downloading JDK v$JdkVersion..." -ForegroundColor Cyan
    Write-Host "--------------------------------------------------------" -ForegroundColor Cyan
    
    # Create target directory
    New-Item -ItemType Directory -Force -Path $JdkDir | Out-Null
    
    $JdkZipPath = "$JdkDir\jdk.zip"
    # Download official Eclipse Temurin JDK 21 zip for Windows x64
    $JdkUrl = "https://github.com/adoptium/temurin21-binaries/releases/download/jdk-21.0.2%2B13/OpenJDK21U-jdk_x64_windows_hotspot_21.0.2_13.zip"
    
    Write-Host "Downloading (approx. 190MB, please wait)..." -ForegroundColor Yellow
    Invoke-WebRequest -Uri $JdkUrl -OutFile $JdkZipPath
    
    Write-Host "Extracting JDK package..." -ForegroundColor Cyan
    Expand-Archive -Path $JdkZipPath -DestinationPath $JdkDir -Force
    
    # Clean up zip
    Remove-Item $JdkZipPath
    Write-Host "Java 21 JDK configured successfully in local folder!" -ForegroundColor Green
}

# ===================================================================
# 2. DOWNLOAD PORTABLE MAVEN IF NOT PRESENT
# ===================================================================
if (-not (Test-Path $MvnPath)) {
    Write-Host "--------------------------------------------------------" -ForegroundColor Cyan
    Write-Host "Portable Maven not found. Downloading Apache Maven v$MavenVersion..." -ForegroundColor Cyan
    Write-Host "--------------------------------------------------------" -ForegroundColor Cyan
    
    New-Item -ItemType Directory -Force -Path $MavenDir | Out-Null
    
    $MvnZipPath = "$MavenDir\maven.zip"
    $MvnUrl = "https://archive.apache.org/dist/maven/maven-3/$MavenVersion/binaries/apache-maven-$MavenVersion-bin.zip"
    
    Write-Host "Downloading Maven..." -ForegroundColor Yellow
    Invoke-WebRequest -Uri $MvnUrl -OutFile $MvnZipPath
    
    Write-Host "Extracting Maven package..." -ForegroundColor Cyan
    Expand-Archive -Path $MvnZipPath -DestinationPath $MavenDir -Force
    
    Remove-Item $MvnZipPath
    Write-Host "Maven configured successfully in local folder!" -ForegroundColor Green
}

# ===================================================================
# 3. CONFIGURE ENVIRONMENT VARIABLES LOCALLY FOR THIS SESSION
# ===================================================================
Write-Host "Setting environment variables locally..." -ForegroundColor Cyan
$env:JAVA_HOME = $JavaHomePath
$env:PATH = "$JavaHomePath\bin;$MavenDir\apache-maven-$MavenVersion\bin;" + $env:PATH

# Double check versions being used
Write-Host "Java version being used:" -ForegroundColor Yellow
java -version
Write-Host "Maven version being used:" -ForegroundColor Yellow
mvn -version

# ===================================================================
# 4. RUN SPRING BOOT BACKEND
# ===================================================================
Write-Host "--------------------------------------------------------" -ForegroundColor Green
Write-Host "Starting Spring Boot Application using Java 21 & Maven..." -ForegroundColor Green
Write-Host "--------------------------------------------------------" -ForegroundColor Green

mvn clean spring-boot:run
