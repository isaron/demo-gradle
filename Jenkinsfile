pipeline {
  agent {
    node {
      label 'slave01-gradle-springboot'
    }

  }
  stages {
    stage('Checkout') {
      steps {
        // checkout([$class: 'GitSCM',branches:[[name:'*/master']],doGenerateSubmoduleConfigurations:false,xtensions:[],submoduleCfg:[],userRemoteConfigs:[[credentialsId:'git:12c5e7bd0e763bbeffcbd5e1bcbc7e010014e7c083c3e78474e99fccbbe68237',url:'https://gitea.ssii.com/RDP/demo-gradle.git']]])
        checkout scm
        
      }
    }
    stage('Test') {
      steps {
        sh './gradlew sonarqube -Dsonar.host.url=https://sonar.ssii.com -Dsonar.login=78e4c996818567e429196c8076dee35166351f1e'
        waitForQualityGate true
      }
    }
    stage('Build') {
      steps {
        sh './gradlew build'
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
    // stage('Deploy') {
    //   steps {
    //     sh './gradle deploy'
    //   }
    // }
  }
}