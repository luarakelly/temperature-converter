pipeline {
    agent any

    stages {

        stage('Checkout') {
            steps {
                git branch: 'main',
                    url: 'https://github.com/luarakelly/temperature-converter'
            }
        }

        stage('Build') {
            steps {
                bat 'mvn clean install'
            }
        }

        stage('Test') {
            steps {
                bat 'mvn test'
            }
        }

        stage('Code Coverage') {
            steps {
                bat 'mvn jacoco:report'
            }
        }

        stage('Publish Test Results') {
            steps {
                junit '**/target/surefire-reports/*.xml'
            }
        }

        stage('Publish Coverage Report') {
            steps {
                jacoco()
            }
        }

        stage('Docker Build') {
            steps {
                bat 'docker build -t luaram/temperature-converter:latest .'
            }
        }

        stage('Docker Run') {
            steps {
                bat 'docker run --rm luaram/temperature-converter:latest'
            }
        }

        stage('Docker Push') {
            steps {
                bat 'docker push luaram/temperature-converter:latest'
            }
        }
    }
}

