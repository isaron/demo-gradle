def projectGroup = 'rdp'
def projectName = 'demo-gradle'
def releaseVersion = '0.0.1'
// def chartmuseum = 'chartmuseum.ssii.com'
// def feSvcName = "${projectName}-frontend"
def registry = 'containers.ssii.com'
def imageRepo = "${registry}/${projectGroup}/${projectName}"
// def build_tag = env.BRANCH_NAME

pipeline {
  agent any
  // triggers {
  //   cron('H.*/4.*.* 1-5')
  // }
  options {
    // retry(3)
    // skipDefaultCheckout()
    timeout(time: 1, unit: 'HOURS') 
  }
  environment {
    build_tag = sh(returnStdout: true, script: 'git rev-parse --short HEAD').trim()
    release_tag = "${build_tag}"
  }
  stages {
    // stage('Checkout') {
    //   steps {
    //     // checkout([$class: 'GitSCM',branches:[[name:'*/master']],doGenerateSubmoduleConfigurations:false,xtensions:[],submoduleCfg:[],userRemoteConfigs:[[credentialsId:'git:12c5e7bd0e763bbeffcbd5e1bcbc7e010014e7c083c3e78474e99fccbbe68237',url:'https://gitea.ssii.com/RDP/${projectName}.git']]])
    //     checkout scm
    //   }
    // }
    stage('Prepare') {
      steps {
        script {
          build_tag = sh(returnStdout: true, script: 'git rev-parse --short HEAD').trim()
          if (env.BRANCH_NAME == "${releaseVersion}") {
            build_tag = "${env.BRANCH_NAME}"
          }
          if (env.BRANCH_NAME == 'master' || env.BRANCH_NAME == 'staging') {
            build_tag = "${releaseVersion}-${env.BRANCH_NAME}"
          }
          release_tag = "${build_tag}"
          sh("sed -i 's#projectVersion=.*#projectVersion=${release_tag}#' ./gradle.properties")
          if (env.BRANCH_NAME != 'staging' && env.BRANCH_NAME != 'master' && env.BRANCH_NAME != "${releaseVersion}") {
            build_tag = "${env.BRANCH_NAME}-${build_tag}"
            release_tag = "${releaseVersion}-${build_tag}"
            sh("sed -i 's#projectVersion=.*#projectVersion=${release_tag}-SNAPSHOT#' ./gradle.properties")
          }
          sh("sed -i 's#projectGroup=.*#projectGroup=${projectGroup}#' ./gradle.properties")
          sh("sed -i 's#projectName=.*#projectName=${projectName}#' ./gradle.properties")
        }
        sh("helm repo add --username admin --password admin123 chartmuseum https://chartmuseum.ssii.com && helm repo add mirror http://172.30.80.33:8080/ && helm repo update")
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
        //     sh("./gradlew sonarqube -Dsonar.projectKey=${projectName} -Dsonar.host.url=https://sonar.ssii.com -Dsonar.login=05e82e5b6bd6a9503972de695897d701b2965546")
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
      when {
        expression {
          currentBuild.result == null || currentBuild.result == 'SUCCESS' // 判断是否发生测试失败
        }
      }
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
        stage('Push Helm chart - Dev/Testing/Feature/Bugfix') {
          when {
            not {
              anyOf {
                branch 'master'
                branch 'staging'
                branch "${releaseVersion}"
              }
            }
          }
          steps {
            sh("sed -i 's#repository:.*#repository: ${imageRepo}#' ./charts/${projectName}/values.yaml")
            sh("sed -i 's#tag:.*#tag: ${release_tag}#' ./charts/${projectName}/values.yaml")
            sh("sed -i 's#UriPrefix:.*#UriPrefix: /${release_tag}#' ./charts/${projectName}/values.yaml")
            sh("sed -i 's#name:.*#name: ${projectName}#' ./charts/${projectName}/Chart.yaml")
            sh("sed -i 's#version:.*#version: ${release_tag}#' ./charts/${projectName}/Chart.yaml")
            sh("sed -i 's#appVersion:.*#appVersion: ${release_tag}#' ./charts/${projectName}/Chart.yaml")
            sh("helm push -f ./charts/${projectName} --version=${release_tag} chartmuseum")
          }
        }
        stage('Push Helm chart - Staging') {
          when {
            branch 'staging'
          }
          steps {
            sh("sed -i 's#repository:.*#repository: ${imageRepo}#' ./charts/${projectName}/values.yaml")
            sh("sed -i 's#tag:.*#tag: ${release_tag}#' ./charts/${projectName}/values.yaml")
            sh("sed -i 's#UriPrefix:.*#UriPrefix: /${release_tag}#' ./charts/${projectName}/values.yaml")
            sh("sed -i 's#name:.*#name: ${projectName}#' ./charts/${projectName}/Chart.yaml")
            sh("sed -i 's#version:.*#version: ${release_tag}#' ./charts/${projectName}/Chart.yaml")
            sh("sed -i 's#appVersion:.*#appVersion: ${release_tag}#' ./charts/${projectName}/Chart.yaml")
            sh("helm push -f ./charts/${projectName} --version=${release_tag} chartmuseum")
          }
        }
        stage('Push Helm chart - Prod') {
          when {
            anyOf {
              branch 'master'
              branch "${releaseVersion}"
            }
          }
          steps {
            sh("sed -i 's#repository:.*#repository: ${imageRepo}#' ./charts/${projectName}/values.yaml")
            sh("sed -i 's#tag:.*#tag: ${release_tag}#' ./charts/${projectName}/values.yaml")
            sh("sed -i 's#prodReady:.*#prodReady: true#' ./charts/${projectName}/values.yaml")
            sh("sed -i 's#name:.*#name: ${projectName}#' ./charts/${projectName}/Chart.yaml")
            sh("sed -i 's#version:.*#version: ${release_tag}#' ./charts/${projectName}/Chart.yaml")
            sh("sed -i 's#appVersion:.*#appVersion: ${release_tag}#' ./charts/${projectName}/Chart.yaml")
            sh("helm push -f ./charts/${projectName} --version=${release_tag} chartmuseum")
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
          when {
            branch 'dev'
          }
          steps {
            sh("helm repo update")
            sh("helm upgrade --install ${projectName} --version ${release_tag} --namespace dev chartmuseum/${projectName}")
          }
        }
        stage('Deploy - Testing') {
          when {
            branch 'testing'
          }
          steps {
            sh("helm repo update")
            sh("helm upgrade --install ${projectName} --version ${release_tag} --namespace testing chartmuseum/${projectName}")
          }
        }
        stage('Deploy - Staging') {
          when {
            branch 'staging'
          }
          // input {
          //   message "确认部署Staging环境吗？"
          //   id "staging-input"
          //   ok "确认部署"
          // }
          steps {
            sh("helm repo update")
            sh("helm upgrade --install ${projectName} --version ${release_tag} --namespace staging chartmuseum/${projectName}")
          }
        }
        stage('Deploy - Prod') {
          when {
            anyOf {
              branch 'master'
              branch "${releaseVersion}"
            }
            // expression { BRANCH_NAME == /(master#"${releaseVersion}")/ }
            // anyOf {
            //     environment name: 'DEPLOY_TO', value: 'production'
            //     environment name: 'DEPLOY_TO', value: 'release'
            // }
          }
          input {
            message "确认部署Prod环境吗？"
            id "prod-input"
            ok "确认部署"
          }
          steps {
            sh("helm upgrade --install ${projectName} --version ${release_tag} --namespace production chartmuseum/${projectName}")
          }
        }
      }
    }
  }
}
