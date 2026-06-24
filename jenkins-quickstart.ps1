#!/usr/bin/env powershell
<#
.SYNOPSIS
    Quick-start script to launch Jenkins pipeline for bank-customer-onboarding-service
.DESCRIPTION
    Sets up and starts Docker containers, initializes Jenkins, and displays instructions
.EXAMPLE
    .\jenkins-quickstart.ps1
#>

param(
    [switch]$NoLogs,
    [switch]$Clean
)

$ErrorActionPreference = "Stop"
$colors = @{
    Success = 'Green'
    Info = 'Cyan'
    Warning = 'Yellow'
    Error = 'Red'
}

function Write-Status {
    param([string]$Message, [string]$Type = 'Info')
    $color = $colors[$Type]
    Write-Host "[$Type] $Message" -ForegroundColor $color
}

function Test-DockerRunning {
    try {
        docker ps -q | Out-Null
        return $true
    }
    catch {
        return $false
    }
}

# Main script
Write-Status "Starting Jenkins Pipeline Setup" Info

# Check Docker
Write-Status "Checking Docker installation..." Info
if (-not (Test-DockerRunning)) {
    Write-Status "Docker is not running or not installed!" Error
    Write-Status "Please install Docker and Docker Compose from https://www.docker.com/products/docker-desktop" Warning
    exit 1
}
Write-Status "Docker is ready" Success

# Set working directory
$projectPath = Split-Path -Parent $MyInvocation.MyCommand.Definition
Set-Location $projectPath

# Clean option
if ($Clean) {
    Write-Status "Cleaning up existing containers..." Warning
    docker compose -f docker-compose-jenkins.yml down -v 2>$null
    Write-Status "Containers removed" Success
}

# Start containers
Write-Status "Starting Jenkins and PostgreSQL..." Info
docker compose -f docker-compose-jenkins.yml up -d

# Wait for Jenkins
Write-Status "Waiting for Jenkins to be ready (this may take 1-2 minutes)..." Info
$maxAttempts = 120
$attempt = 0

while ($attempt -lt $maxAttempts) {
    try {
        $response = Invoke-WebRequest -Uri "http://localhost:8080" -UseBasicParsing -TimeoutSec 5 -ErrorAction SilentlyContinue
        if ($response.StatusCode -eq 200) {
            Write-Status "Jenkins is ready!" Success
            break
        }
    }
    catch {}

    $attempt++
    if ($attempt % 10 -eq 0) {
        Write-Status "Still waiting... ($attempt/120)" Info
    }
    Start-Sleep -Seconds 1
}

if ($attempt -eq $maxAttempts) {
    Write-Status "Jenkins took too long to start. Showing logs..." Warning
    docker logs jenkins-pipeline
    exit 1
}

# Get initial password
Write-Status "Retrieving initial admin password..." Info
$initialPassword = docker exec jenkins-pipeline cat /var/jenkins_home/secrets/initialAdminPassword 2>$null

if (-not $initialPassword) {
    Write-Status "Could not retrieve password. Check logs:" Warning
    docker logs jenkins-pipeline
    exit 1
}

# Display information
Write-Host ""
Write-Host "╔════════════════════════════════════════════════════════════════╗" -ForegroundColor Cyan
Write-Host "║          JENKINS PIPELINE READY FOR SETUP                      ║" -ForegroundColor Cyan
Write-Host "╚════════════════════════════════════════════════════════════════╝" -ForegroundColor Cyan
Write-Host ""
Write-Status "Jenkins URL: http://localhost:8080" Info
Write-Status "Initial Admin Password:" Info
Write-Host "  $initialPassword" -ForegroundColor Yellow
Write-Host ""
Write-Status "PostgreSQL Connection:" Info
Write-Host "  Host: localhost" -ForegroundColor Gray
Write-Host "  Port: 5432" -ForegroundColor Gray
Write-Host "  Database: bank_onboarding" -ForegroundColor Gray
Write-Host "  User: bank_user" -ForegroundColor Gray
Write-Host "  Password: bank_password" -ForegroundColor Gray
Write-Host ""
Write-Status "Next Steps:" Info
Write-Host "  1. Open http://localhost:8080 in your browser" -ForegroundColor Gray
Write-Host "  2. Paste the password above to unlock Jenkins" -ForegroundColor Gray
Write-Host "  3. Follow instructions in JENKINS_SETUP.md (Steps 3-6)" -ForegroundColor Gray
Write-Host "  4. Create a new Pipeline job pointing to Jenkinsfile" -ForegroundColor Gray
Write-Host "  5. Run 'Build Now' to execute the pipeline" -ForegroundColor Gray
Write-Host ""
Write-Status "View detailed setup guide:" Info
Write-Host "  More details at: .\JENKINS_SETUP.md" -ForegroundColor Gray
Write-Host ""

# Optional: Show logs
if (-not $NoLogs) {
    Write-Status "Showing Jenkins logs (Ctrl+C to stop)..." Info
    docker logs jenkins-pipeline -f
}

Write-Status "Setup complete!" Success

