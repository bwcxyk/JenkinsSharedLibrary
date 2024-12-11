/**
@Library('jenkinslibrary@master') _

pipeline {
    agent any
    
    parameters {
    string(name: 'buildCommand', defaultValue: 'npm run build', description: '构建命令')
    }
    
    stages {
        stage ('Example') {
            steps {
                script {
                    npm.install(npm).build(params.buildCommand ?: '')
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

def build(buildCommand = "npm run build") {
    def exitCode = sh(script: buildCommand, returnStatus: true)

    if (exitCode != 0) {
        error "Build failed with exit code: $exitCode"
    }

    return this // 支持链式调用
}
