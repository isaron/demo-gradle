pipeline {
  agent {
    node {
      label 'slave01-gradle-springboot'
    }

  }
  stages {
    stage('Build') {
      steps {
        sh './gradlew build'
      }
    }
    stage('Test') {
      steps {
        sh '''./gradlew sonarqube 
  -Dsonar.host.url=https://sonar.ssii.com 
  -Dsonar.login=78e4c996818567e429196c8076dee35166351f1e'''
        waitForQualityGate true
      }
    }
    stage('Publish') {
      agent {
        dockerfile {
          filename 'Dockerfile'
        }

      }
      steps {
        sh './gradlew publish'
        sh './gradlew docker'
      }
    }
    stage('Deploy') {
      steps {
        sh './gradle deploy'
      }
    }
  }
}