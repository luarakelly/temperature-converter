pipeline {
    agent any

    tools {
        maven 'Maven'
        jdk 'JDK21'
    }

    environment {
        DOCKERHUB_CREDENTIALS_ID = 'Docker_Hub'
        DOCKERHUB_REPO = 'luaram/temperature-converter'
        DOCKER_IMAGE_TAG = 'latest'
    }

    stages {

        stage('Checkout') {
            steps {
                git branch: 'main',
                    url: 'https://github.com/luarakelly/temperature-converter'
            }
        }

        stage('Build and Test') {
            steps {
                sh 'mvn clean verify'
            }
            post {
                always {
                    junit(
                        testResults: '**/target/surefire-reports/*.xml',
                        allowEmptyResults: false
                    )
                }
            }
        }

        stage('Publish Coverage Report') {
            steps {
                archiveArtifacts(
                    artifacts: 'target/site/jacoco/**',
                    fingerprint: true
                )

                jacoco()
            }
        }

        stage('Build Docker Image') {
            steps {
                sh 'docker build -t ${DOCKERHUB_REPO}:${DOCKER_IMAGE_TAG} .'
            }
        }

        stage('Push Docker Image to Docker Hub') {
            steps {
                script {
                    docker.withRegistry(
                        'https://index.docker.io/v1/',
                        DOCKERHUB_CREDENTIALS_ID
                    ) {
                        docker.image(
                            "${DOCKERHUB_REPO}:${DOCKER_IMAGE_TAG}"
                        ).push()
                    }
                }
            }
        }
    }

    post {
        success {
            echo 'Build, tests, coverage, and Docker publishing succeeded.'
        }

        failure {
            echo 'The pipeline failed. Check the stage logs.'
        }
    }
}

