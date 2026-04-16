/**
@Library('jenkinslibrary@main') _
def nodejs = nodejsHelper(this)

pipeline {
    agent any
    
    parameters {
    choice choices: ['dev', 'prod'], description: '打包环境', name: 'BUILD_ENV'
    }
    
    stages {
        stage ('Example') {
            steps {
                script {
                    nodejs.install("npm").build()
                }
            }
        }
    }
}
**/

package org.devops

class Nodejs implements Serializable {
    def script  // Pipeline 上下文

    Nodejs(script) {
        this.script = script
    }

    def install(pkgManager = "npm", installCmd = "ci") {
        // 打印 Node.js 版本
        script.echo "=== Checking Node.js version ==="
        script.sh "node -v"
    
        String installCommand = "${pkgManager} ${installCmd}"
        script.echo "Executing: ${installCommand}"
    
        def exitCode = script.sh(script: installCommand, returnStatus: true)
        if (exitCode != 0) {
            script.error "${installCommand} failed with exit code: ${exitCode}"
        }
    
        return this  // 支持链式调用
    }
    
    def build() {
        def buildEnv = script.params?.BUILD_ENV

        if (buildEnv) {
            script.echo "BUILD_ENV detected: ${buildEnv}"
        } else {
            script.echo "No BUILD_ENV provided, using default build"
        }

        String buildCommand = buildEnv ? "npm run build:${buildEnv}" : "npm run build"

        def exitCode = script.sh(script: buildCommand, returnStatus: true)
        if (exitCode != 0) {
            script.error "${buildCommand} failed with exit code: ${exitCode}"
        }

        return this
    }

}
