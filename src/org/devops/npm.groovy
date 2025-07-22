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
                    npm.install("cnpm").build()
                }
            }
        }
    }
}
**/

package org.devops

def install(pkgManager = "npm") {
    // 打印 Node.js 版本
    echo "=== Checking Node.js version ==="
    sh "node -v"

    // String installCommand = "${pkgManager} install"
    String installCommand = "${pkgManager} ci"
    echo "Executing: ${installCommand}"

    def exitCode = sh(script: installCommand, returnStatus: true)
    if (exitCode != 0) {
        error "${installCommand} failed with exit code: ${exitCode}"
    }

    return this  // 支持链式调用
}

def build() {
    def defaultBuildenv = "prod"
    if (!params.BUILD_ENV) {
        echo "BUILD_ENV is not set. Using default: ${defaultBuildenv}"
    }
    def buildEnv = params.BUILD_ENV ?: defaultBuildenv

    String buildCommand = "npm run build:${buildEnv}"
    echo "Executing: ${buildCommand}"

    def exitCode = sh(script: buildCommand, returnStatus: true)
    if (exitCode != 0) {
        error "${buildCommand} failed with exit code: ${exitCode}"
    }

    return this // 支持链式调用
}

