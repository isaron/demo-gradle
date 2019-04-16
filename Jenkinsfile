pipeline {
  agent any
  stages {
    // stage('Checkout') {
    //   steps {
    //     // checkout([$class: 'GitSCM',branches:[[name:'*/master']],doGenerateSubmoduleConfigurations:false,xtensions:[],submoduleCfg:[],userRemoteConfigs:[[credentialsId:'git:12c5e7bd0e763bbeffcbd5e1bcbc7e010014e7c083c3e78474e99fccbbe68237',url:'https://gitea.ssii.com/RDP/demo-gradle.git']]])
    //     checkout scm
    //   }
    // }
    stage('Clean') {
      steps {
        sh 'chmod +x ./gradlew'
        gradlew('clean')
      }
    }
    stage('Test') {
      // parallel {
        // stage('Integration Tests') {
          steps {
            gradlew('test')
          }
        // }
        // stage('Code Analysis') {
        //   steps {
        //     sh './gradlew sonarqube -Dsonar.projectKey=demo-gradle -Dsonar.host.url=https://sonar.ssii.com -Dsonar.login=7dde01fab5c7cf5f2d8c6d955d0b57951b1a7b07'
        //     waitForQualityGate true
        //   }
        // }
      // }
    }
    stage('Build') {
      steps {
        gradlew('bootJar')
      }
    }
    stage('Publish') {
      // agent {
      //   dockerfile {
      //     filename 'Dockerfile'
      //   }
      // }
      parallel {
        stage('Publish Jar') {
          steps {
            gradlew('publish')
          }
        }
        stage('Publish Docker image') {
          steps {
            gradlew('jib')
          }
        }
      }
    }
    // stage('Deploy') {
    //   steps {
    //     sh './gradle deploy'
    //   }
    // }
  }
}

def gradlew(String... args) {
  sh "./gradlew ${args.join(' ')} -s"
}