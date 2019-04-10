pipeline {
  agent any
  stages {
    stage('Checkout') {
      steps {
        // checkout([$class: 'GitSCM',branches:[[name:'*/master']],doGenerateSubmoduleConfigurations:false,xtensions:[],submoduleCfg:[],userRemoteConfigs:[[credentialsId:'git:12c5e7bd0e763bbeffcbd5e1bcbc7e010014e7c083c3e78474e99fccbbe68237',url:'https://gitea.ssii.com/RDP/demo-gradle.git']]])
        checkout scm
      }
    }
    stage('Clean') {
      steps {
        sh 'chmod +x ./gradlew'
        gradlew('clean')
      }
    }
    stage('Test') {
      parallel {
        stage('Integration Tests') {
          steps {
            gradlew('test')
          }
        }
        stage('Code Analysis') {
          steps {
            sh './gradlew sonarqube -Dsonar.host.url=https://sonar.ssii.com -Dsonar.login=78e4c996818567e429196c8076dee35166351f1e'
            waitForQualityGate true
          }
        }
      }
    }
    stage('Build') {
      steps {
        gradlew('build')
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