def project = 'com.ssii.rdp'
def appName = 'demo-gradle'
def releaseVersion = '0.0.1'
def chartmuseum = 'chartmuseum.ssii.com'
// def feSvcName = "${appName}-frontend"
// def registry = 'containers.ssii.com'
// def imageTag = "${registry}/${project}/${appName}:${env.BRANCH_NAME}.${env.BUILD_NUMBER}"
// def build_tag = env.BRANCH_NAME

pipeline {
  agent any
  // triggers {
  //   cron('H */4 * * 1-5')
  // }
  options {
    // retry(3)
    // skipDefaultCheckout()
    timeout(time: 1, unit: 'HOURS') 
  }
  environment {
    build_tag = sh(returnStdout: true, script: 'git rev-parse --short HEAD').trim()
  }
  stages {
    // stage('Checkout') {
    //   steps {
    //     // checkout([$class: 'GitSCM',branches:[[name:'*/master']],doGenerateSubmoduleConfigurations:false,xtensions:[],submoduleCfg:[],userRemoteConfigs:[[credentialsId:'git:12c5e7bd0e763bbeffcbd5e1bcbc7e010014e7c083c3e78474e99fccbbe68237',url:'https://gitea.ssii.com/RDP/${appName}.git']]])
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
          sh("sed -i 's|version: */|version: ${build_tag}|g' ./build.gradle")
          if (env.BRANCH_NAME != 'staging' && env.BRANCH_NAME != 'master' && env.BRANCH_NAME != "${releaseVersion}") {
            build_tag = "${env.BRANCH_NAME}-${build_tag}"
            sh("sed -i 's|version: */|version: ${build_tag}-SNAPSHOT|g' ./build.gradle")
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
        //     sh("./gradlew sonarqube -Dsonar.projectKey=${appName} -Dsonar.host.url=https://sonar.ssii.com -Dsonar.login=05e82e5b6bd6a9503972de695897d701b2965546")
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
            sh("sed -i 's|tag: */|tag: ${build_tag}|g' ./charts/${appName}/values.yaml")
            sh("sed -i 's|version: */|version: ${releaseVersion}-${build_tag}|g' ./charts/${appName}/Chart.yaml")
            sh("sed -i 's|appVersion: */|appVersion: ${releaseVersion}-${build_tag}|g' ./charts/${appName}/Chart.yaml")
            sh("helm push -f ./charts/${appName} --version=${releaseVersion}-${build_tag} chartmuseum")
          }
        }
        stage('Push Helm chart - Staging') {
          when {
            branch 'staging'
          }
          steps {
            sh("sed -i 's|tag: */|tag: ${build_tag}|g' ./charts/${appName}/values.yaml")
            sh("sed -i 's|version: */|version: ${build_tag}|g' ./charts/${appName}/Chart.yaml")
            sh("sed -i 's|appVersion: */|appVersion: ${build_tag}|g' ./charts/${appName}/Chart.yaml")
            sh("helm push -f ./charts/${appName} --version=${build_tag} chartmuseum")
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
            sh("sed -i 's|tag: */|tag: ${build_tag}|g' ./charts/${appName}/values.yaml")
            sh("sed -i 's|prodReady: */|prodReady: true|g' ./charts/${appName}/values.yaml")
            sh("sed -i 's|version: */|version: ${build_tag}|g' ./charts/${appName}/Chart.yaml")
            sh("sed -i 's|appVersion: */|appVersion: ${build_tag}|g' ./charts/${appName}/Chart.yaml")
            sh("ls -a /home/jenkins/")
            sh("helm push -f ./charts/${appName} --version=${build_tag} chartmuseum")
            // sh("helm package ./charts/${appName} && curl --data-binary '@${appName}-${build_tag}.tgz' https://${chartmuseum}/api/charts")
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
            sh("helm upgrade --install ${appName} --version ${releaseVersion}-${build_tag} --namespace dev chartmuseum/${appName}")
          }
        }
        stage('Deploy - Testing') {
          when {
            branch 'testing'
          }
          steps {
            sh("helm upgrade --install ${appName} --version ${releaseVersion}-${build_tag} --namespace testing chartmuseum/${appName}")
          }
        }
        stage('Deploy - Staging') {
          when {
            branch 'staging'
          }
          input {
            message "确认要部署Staging环境吗？"
            id "staging-input"
          }
          steps {
            sh("helm upgrade --install ${appName} --version ${build_tag} --namespace staging chartmuseum/${appName}")
          }
        }
        stage('Deploy - Prod') {
          when {
            expression { BRANCH_NAME == /(master|"${releaseVersion}")/ }
            // anyOf {
            //     environment name: 'DEPLOY_TO', value: 'production'
            //     environment name: 'DEPLOY_TO', value: 'release'
            // }
          }
          input {
            message "确认要部署Prod环境吗？"
            id "prod-input"
          }
          steps {
            sh("helm upgrade --install ${appName} --version ${build_tag} --namespace production chartmuseum/${appName}")
          }
        }
      }
    }
  }
}
