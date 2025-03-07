/**
@Library('jenkinslibrary@master') _

pipeline {
    agent any
    
    parameters {
    choice choices: ['dev', 'prod'], description: '打包环境', name: 'BUILD_ENV'
    }
    
    stages {
        stage ('Example') {
            steps {
                script {
                    npm.install("cnpm").build
                }
            }
        }
    }
}
**/

package org.devops

def install(pkgManager = "npm") {
    def installCommand = "${pkgManager} install"

    def exitCode = sh(script: installCommand, returnStatus: true)
    if (exitCode != 0) {
        error "${installCommand} failed with exit code: $exitCode"
    }

    return this  // 支持链式调用
}

def build() {
    def defaultBuildenv = "prod"
    if (!params.BUILD_ENV) {
        echo "BUILD_ENV is not set. Using default: ${defaultBuildenv}"
    }
    def buildEnv = params.BUILD_ENV ?: defaultBuildenv
    try {
        echo "Starting build for environment: ${buildEnv}"
        sh "npm run build:${buildEnv}"
    } catch (Exception e) {
        // 捕获构建异常并抛出错误信息
        error "Build ${buildEnv} failed: ${e.message}"
    }

    return this // 支持链式调用
}
