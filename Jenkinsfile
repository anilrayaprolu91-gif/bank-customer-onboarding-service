pipeline {
    agent { label 'linux && docker' }

    tools {
        jdk 'jdk21'
    }

    options {
        timestamps()
        ansiColor('xterm')
        disableConcurrentBuilds()
        buildDiscarder(logRotator(numToKeepStr: '30', artifactNumToKeepStr: '15'))
        timeout(time: 60, unit: 'MINUTES')
    }

    parameters {
        booleanParam(name: 'RUN_SONAR', defaultValue: true, description: 'Run SonarQube analysis stage')
        booleanParam(name: 'RUN_SPOTBUGS', defaultValue: true, description: 'Run SpotBugs stage')
        booleanParam(name: 'RUN_CONTRACT_TESTS', defaultValue: true, description: 'Run Spring Cloud Contract verification stage')
        booleanParam(name: 'RUN_CONSUMER_CONTRACT_TESTS', defaultValue: true, description: 'Run Pact consumer contract tests')
        booleanParam(name: 'PUBLISH_CONTRACT_STUBS', defaultValue: true, description: 'Publish contract stubs artifact to Maven repository (only runs on main/release branches)')
        string(name: 'STUBS_CREDENTIALS_ID', defaultValue: 'nexus-contract-stubs-creds', description: 'Jenkins credentialsId (username/password) for the stubs Maven repository')
        string(name: 'STUBS_REPO_URL', defaultValue: '', description: 'Maven repository URL for contract stubs, e.g. https://nexus.bank.internal/repository/contract-stubs')
        string(name: 'SONARQUBE_ENV', defaultValue: 'sonarqube', description: 'Jenkins SonarQube server name')
        string(name: 'DOCKER_IMAGE', defaultValue: 'bank-customer-onboarding-service', description: 'Docker image repository/name')
        string(name: 'DOCKER_TAG', defaultValue: '', description: 'Docker tag override (defaults to build number)')
        booleanParam(name: 'RUN_DOCKER_PUSH', defaultValue: false, description: 'Push built image to registry')
        booleanParam(name: 'REQUIRE_RELEASE_APPROVAL', defaultValue: true, description: 'Require manual approval before Docker push on release/* branches')
        string(name: 'DOCKER_REGISTRY', defaultValue: '', description: 'Registry host prefix, e.g. registry.bank.internal')
        string(name: 'DOCKER_CREDENTIALS_ID', defaultValue: 'docker-registry-creds', description: 'Jenkins credentialsId for registry username/password')
        string(name: 'RELEASE_APPROVERS', defaultValue: 'release-managers,devsecops', description: 'Comma-separated Jenkins users/groups allowed to approve release push')
    }

    environment {
        GRADLE_OPTS = '-Dorg.gradle.daemon=false -Dorg.gradle.console=plain'
        UNIT_TEST_PASSED             = 'false'
        COMPONENT_TEST_PASSED        = 'false'
        CONTRACT_TEST_PASSED         = 'false'
        CONSUMER_CONTRACT_TEST_PASSED = 'false'
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
                script {
                    env.EFFECTIVE_DOCKER_TAG = params.DOCKER_TAG?.trim() ? params.DOCKER_TAG.trim() : "${env.BUILD_NUMBER}"
                    String normalizedRegistry = params.DOCKER_REGISTRY?.trim()
                    if (normalizedRegistry?.endsWith('/')) {
                        normalizedRegistry = normalizedRegistry.replaceAll('/+$', '')
                    }
                    env.EFFECTIVE_DOCKER_IMAGE = normalizedRegistry ? "${normalizedRegistry}/${params.DOCKER_IMAGE}" : "${params.DOCKER_IMAGE}"
                    env.EFFECTIVE_DOCKER_IMAGE_TAG = "${env.EFFECTIVE_DOCKER_IMAGE}:${env.EFFECTIVE_DOCKER_TAG}"
                }
            }
        }

        stage('Build') {
            steps {
                sh 'chmod +x gradlew || true'
                sh './gradlew clean assemble -x test --no-daemon'
            }
        }

        stage('Test Execution') {
            failFast true
            parallel {
                stage('Unit Test') {
                    steps {
                        catchError(buildResult: 'FAILURE', stageResult: 'FAILURE') {
                            sh './gradlew test --no-daemon'
                            script {
                                env.UNIT_TEST_PASSED = 'true'
                            }
                        }
                    }
                    post {
                        always {
                            junit testResults: 'build/test-results/test/*.xml', allowEmptyResults: true, keepLongStdio: true
                        }
                    }
                }

                stage('Component Test') {
                    steps {
                        catchError(buildResult: 'FAILURE', stageResult: 'FAILURE') {
                            sh './gradlew componentTest --no-daemon'
                            script {
                                env.COMPONENT_TEST_PASSED = 'true'
                            }
                        }
                    }
                    post {
                        always {
                            junit testResults: 'build/test-results/componentTest/*.xml', allowEmptyResults: true, keepLongStdio: true
                        }
                    }
                }

                stage('Blackbox Test') {
                    options {
                        timeout(time: 30, unit: 'MINUTES')
                    }
                    steps {
                        catchError(buildResult: 'UNSTABLE', stageResult: 'UNSTABLE') {
                            sh './gradlew blackboxTest --no-daemon'
                        }
                    }
                    post {
                        always {
                            junit testResults: 'build/test-results/blackboxTest/*.xml', allowEmptyResults: true, keepLongStdio: true
                        }
                        unsuccessful {
                            echo 'Blackbox Test failed and was marked UNSTABLE (non-blocking by design).'
                        }
                    }
                }
            }
        }

        stage('Contract Test') {
            when {
                expression { return params.RUN_CONTRACT_TESTS && env.UNIT_TEST_PASSED == 'true' && env.COMPONENT_TEST_PASSED == 'true' }
            }
            steps {
                catchError(buildResult: 'FAILURE', stageResult: 'FAILURE') {
                    sh './gradlew contractTest --no-daemon'
                    script {
                        env.CONTRACT_TEST_PASSED = 'true'
                    }
                }
            }
            post {
                always {
                    junit testResults: 'build/test-results/contractTest/*.xml', allowEmptyResults: true, keepLongStdio: true
                }
            }
        }

        stage('Consumer Contract Test') {
            when {
                expression { return params.RUN_CONSUMER_CONTRACT_TESTS }
            }
            steps {
                catchError(buildResult: 'FAILURE', stageResult: 'FAILURE') {
                    sh './gradlew consumerContractTest --no-daemon'
                    script {
                        env.CONSUMER_CONTRACT_TEST_PASSED = 'true'
                    }
                }
            }
            post {
                always {
                    junit testResults: 'build/test-results/consumerContractTest/*.xml', allowEmptyResults: true, keepLongStdio: true
                    archiveArtifacts artifacts: 'build/pacts/**/*.json', allowEmptyArchive: true, fingerprint: true
                }
            }
        }

        stage('Publish Contract Stubs') {
            when {
                allOf {
                    expression { return params.PUBLISH_CONTRACT_STUBS }
                    expression { return env.CONTRACT_TEST_PASSED == 'true' }
                    not { changeRequest() }
                    anyOf {
                        branch 'main'
                        branch pattern: 'release/.+', comparator: 'REGEXP'
                    }
                }
            }
            steps {
                script {
                    String stubsRepoUrl = params.STUBS_REPO_URL?.trim()
                    if (stubsRepoUrl) {
                        withCredentials([usernamePassword(
                                credentialsId: params.STUBS_CREDENTIALS_ID,
                                usernameVariable: 'STUBS_USERNAME',
                                passwordVariable: 'STUBS_PASSWORD')]) {
                            sh """
                                ./gradlew publishContractStubsToRepository --no-daemon \\
                                    -DSTUBS_REPO_URL=${stubsRepoUrl} \\
                                    -DSTUBS_REPO_USERNAME=\${STUBS_USERNAME} \\
                                    -DSTUBS_REPO_PASSWORD=\${STUBS_PASSWORD}
                            """
                        }
                        echo "Contract stubs published to ${stubsRepoUrl}"
                    } else {
                        echo "STUBS_REPO_URL not configured — publishing to local Maven cache only."
                        sh './gradlew publishContractStubs --no-daemon'
                    }
                }
            }
            post {
                success {
                    echo "Stubs artifact: ${env.JOB_NAME}:${env.BUILD_NUMBER} — ${env.GIT_BRANCH}"
                }
            }
        }

        stage('SonarQube') {
            when {
                allOf {
                    expression { return params.RUN_SONAR }
                    expression { return env.UNIT_TEST_PASSED == 'true' && env.COMPONENT_TEST_PASSED == 'true' && env.CONTRACT_TEST_PASSED == 'true' }
                    anyOf {
                        changeRequest()
                        branch 'main'
                        branch 'develop'
                        branch pattern: 'release/.+', comparator: 'REGEXP'
                    }
                }
            }
            steps {
                withSonarQubeEnv("${params.SONARQUBE_ENV}") {
                    sh './gradlew sonar --no-daemon'
                }
            }
        }

        stage('SonarQube Quality Gate') {
            when {
                allOf {
                    expression { return params.RUN_SONAR }
                    expression { return env.UNIT_TEST_PASSED == 'true' && env.COMPONENT_TEST_PASSED == 'true' && env.CONTRACT_TEST_PASSED == 'true' }
                    anyOf {
                        changeRequest()
                        branch 'main'
                        branch 'develop'
                        branch pattern: 'release/.+', comparator: 'REGEXP'
                    }
                }
            }
            steps {
                timeout(time: 15, unit: 'MINUTES') {
                    waitForQualityGate abortPipeline: true
                }
            }
        }

        stage('SpotBugs') {
            when {
                expression { return params.RUN_SPOTBUGS && env.UNIT_TEST_PASSED == 'true' && env.COMPONENT_TEST_PASSED == 'true' && env.CONTRACT_TEST_PASSED == 'true' }
            }
            steps {
                sh './gradlew spotbugsMain spotbugsTest spotbugsComponentTest spotbugsBlackboxTest --no-daemon'
            }
        }

        stage('Jacoco') {
            when {
                expression { return env.UNIT_TEST_PASSED == 'true' && env.COMPONENT_TEST_PASSED == 'true' && env.CONTRACT_TEST_PASSED == 'true' }
            }
            steps {
                sh './gradlew jacocoTestReport --no-daemon'
            }
        }

        stage('Docker Build') {
            when {
                allOf {
                    not { changeRequest() }
                    anyOf {
                        branch 'main'
                        branch pattern: 'release/.+', comparator: 'REGEXP'
                        buildingTag()
                    }
                }
            }
            steps {
                sh "docker build -t ${env.EFFECTIVE_DOCKER_IMAGE_TAG} ."
            }
        }

        stage('Release Approval Gate') {
            when {
                allOf {
                    expression { return params.RUN_DOCKER_PUSH && params.REQUIRE_RELEASE_APPROVAL }
                    not { changeRequest() }
                    branch pattern: 'release/.+', comparator: 'REGEXP'
                }
            }
            steps {
                timeout(time: 20, unit: 'MINUTES') {
                    input(
                        message: "Approve Docker push for ${env.EFFECTIVE_DOCKER_IMAGE_TAG}?",
                        ok: 'Approve Push',
                        submitter: params.RELEASE_APPROVERS
                    )
                }
            }
        }

        stage('Docker Push') {
            when {
                allOf {
                    expression { return params.RUN_DOCKER_PUSH }
                    not { changeRequest() }
                    anyOf {
                        branch 'main'
                        branch pattern: 'release/.+', comparator: 'REGEXP'
                        buildingTag()
                    }
                }
            }
            steps {
                script {
                    String loginTarget = params.DOCKER_REGISTRY?.trim() ?: ''
                    if (params.DOCKER_CREDENTIALS_ID?.trim()) {
                        withCredentials([usernamePassword(credentialsId: params.DOCKER_CREDENTIALS_ID, usernameVariable: 'DOCKER_USERNAME', passwordVariable: 'DOCKER_PASSWORD')]) {
                            if (loginTarget) {
                                sh 'echo "$DOCKER_PASSWORD" | docker login "$DOCKER_REGISTRY" -u "$DOCKER_USERNAME" --password-stdin'
                            } else {
                                sh 'echo "$DOCKER_PASSWORD" | docker login -u "$DOCKER_USERNAME" --password-stdin'
                            }
                        }
                    }
                    sh "docker push ${env.EFFECTIVE_DOCKER_IMAGE_TAG}"
                }
            }
            post {
                always {
                    script {
                        if (params.DOCKER_REGISTRY?.trim()) {
                            sh "docker logout ${params.DOCKER_REGISTRY.trim()} || true"
                        }
                    }
                }
            }
        }

        stage('Quality Gate Summary') {
            steps {
                echo "Gate flags -> UNIT_TEST_PASSED=${env.UNIT_TEST_PASSED}, COMPONENT_TEST_PASSED=${env.COMPONENT_TEST_PASSED}, CONTRACT_TEST_PASSED=${env.CONTRACT_TEST_PASSED}, CONSUMER_CONTRACT_TEST_PASSED=${env.CONSUMER_CONTRACT_TEST_PASSED}, RUN_SONAR=${params.RUN_SONAR}, RUN_SPOTBUGS=${params.RUN_SPOTBUGS}"
            }
        }

        stage('Publish Reports') {
            steps {
                sh './gradlew testLayersReport --no-daemon || true'
                archiveArtifacts artifacts: 'build/reports/**, build/test-results/**, build/allure-results/**, build/libs/*.jar, build/pacts/**/*.json', allowEmptyArchive: true, fingerprint: true
            }
        }
    }

    post {
        always {
            echo "Pipeline finished with status: ${currentBuild.currentResult}"
        }
        success {
            echo "Build, test, quality scans, and reporting completed successfully."
        }
        failure {
            echo 'Pipeline failed. Inspect stage logs and published reports for details.'
        }
    }
}

