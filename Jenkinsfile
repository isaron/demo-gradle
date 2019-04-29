def project = 'com.ssii.rdp'
def appName = 'demo-gradle'
def releaseVersion = '0.0.1'
// def feSvcName = "${appName}-frontend"
// def registry = 'containers.ssii.com'
// def imageTag = "${registry}/${project}/${appName}:${env.BRANCH_NAME}.${env.BUILD_NUMBER}"

pipeline {
  agent any
  stages {
    // stage('Checkout') {
    //   steps {
    //     // checkout([$class: 'GitSCM',branches:[[name:'*/master']],doGenerateSubmoduleConfigurations:false,xtensions:[],submoduleCfg:[],userRemoteConfigs:[[credentialsId:'git:12c5e7bd0e763bbeffcbd5e1bcbc7e010014e7c083c3e78474e99fccbbe68237',url:'https://gitea.ssii.com/RDP/demo-gradle.git']]])
    //     checkout scm
    //   }
    // }
    stage('Prepare') {
      steps {
        script {
          build_tag = sh(returnStdout: true, script: 'git rev-parse --short HEAD').trim()
          if (env.BRANCH_NAME == "$(releaseVersion)") {
            build_tag = "${env.BRANCH_NAME}"
          }
          sh("sed -i 's#version: */#version: ${build_tag}#' ./build.gradle")
          if (env.BRANCH_NAME != 'staging' && env.BRANCH_NAME != 'master' && env.BRANCH_NAME != "$(releaseVersion)") {
            build_tag = "${env.BRANCH_NAME}-${build_tag}"
            sh("sed -i 's#version: */#version: ${build_tag}-SNAPSHOT#' ./build.gradle")
          }
        }
        sh("chmod +x ./gradlew")
        sh("./gradlew clean")
      }
    }
    stage('Test') {
      // parallel {
        // stage('Integration Tests') {
          steps {
            sh("./gradlew test")
          }
        // }
        // stage('Code Analysis') {
        //   steps {
        //     sh("./gradlew sonarqube -Dsonar.projectKey=demo-gradle -Dsonar.host.url=https://sonar.ssii.com -Dsonar.login=05e82e5b6bd6a9503972de695897d701b2965546")
        //     waitForQualityGate true
        //   }
        // }
      // }
    }
    stage('Build') {
      steps {
        sh("./gradlew bootJar")
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
            sh("./gradlew publish")
          }
        }
        stage('Publish Docker image') {
          steps {
            sh("./gradlew jib")
          }
        }
        stage('Push Helm chart') {
          steps {
            script {
              if (env.BRANCH_NAME != 'staging' && env.BRANCH_NAME != 'master' && env.BRANCH_NAME != "$(releaseVersion)" && env.BRANCH_NAME == null) {
                sh("sed -i 's#tag: */#tag: ${build_tag}#' ./charts/demo-gradle/values.yaml")
                sh("sed -i 's#appVersion: */#appVersion: ${build_tag}#' ./charts/demo-gradle/Chart.yaml")
                sh("helm push ./charts/demo-gradle --version='${build_tag}' chartmuseum")
              }
              if (env.BRANCH_NAME == 'staging' || env.BRANCH_NAME == null) {
                sh("sed -i 's#tag: */#tag: ${build_tag}#' ./charts/demo-gradle/values.yaml")
                sh("sed -i 's#appVersion: */#appVersion: ${build_tag}#' ./charts/demo-gradle/Chart.yaml")
                sh("helm push ./charts/demo-gradle --version='${build_tag}-staging' chartmuseum")
              }
              if (env.BRANCH_NAME == 'master' || env.BRANCH_NAME == "$(releaseVersion)" || env.BRANCH_NAME == null) {
                sh("sed -i 's#tag: */#tag: ${build_tag}#' ./charts/demo-gradle/values.yaml")
                sh("sed -i 's#prodReady: */#prodReady: true#' ./charts/demo-gradle/values.yaml")
                sh("sed -i 's#appVersion: */#appVersion: ${build_tag}#' ./charts/demo-gradle/Chart.yaml")
                sh("helm push -f ./charts/demo-gradle --version='${build_tag}' chartmuseum")
              }
            }
          }
        }
      }
    }
    stage('Deploy') {
      when {
        expression {
          currentBuild.result == null || currentBuild.result == 'SUCCESS' // 判断是否发生测试失败
        }
      }
      parallel {
        stage('Deploy - Dev') {
          steps {
            script {
              if (env.BRANCH_NAME == 'dev' || env.BRANCH_NAME == null) {
                sh("helm upgrade --install demo-gradle --version ${build_tag} --namespace dev chartmuseum/demo-gradle")
              }
            }
          }
        }
        stage('Deploy - Testing') {
          steps {
            script {
              if (env.BRANCH_NAME == 'testing' || env.BRANCH_NAME == null) {
                sh("helm upgrade --install demo-gradle --version ${build_tag} --namespace testing chartmuseum/demo-gradle")
              }
            }
          }
        }
        stage('Deploy - Staging') {
          steps {
            script {
              if (env.BRANCH_NAME == 'staging' || env.BRANCH_NAME == null) {
                timeout(time: 10, unit: 'MINUTES') {
                  input '确认要部署Staging环境吗？'
                }
              }
              sh("helm upgrade --install demo-gradle --version ${build_tag}-staging --namespace staging chartmuseum/demo-gradle")
            }
          }
        }
        stage('Deploy - Prod') {
          steps {
            script {
              if (env.BRANCH_NAME == 'master' || env.BRANCH_NAME == "$(releaseVersion)" || env.BRANCH_NAME == null) {
                timeout(time: 10, unit: 'MINUTES') {
                  input '确认要部署Prod环境吗？'
                }
              }
              sh("helm upgrade --install demo-gradle --version='${build_tag}' --namespace production chartmuseum/demo-gradl")
            }
          }
        }
      }
    }
  }
}
