/**
@Library('jenkinslibrary@main') _
def gradle = gradleHelper(this)

pipeline {
    agent any
    
    parameters {
    string(name: 'BUILD_ARGS', defaultValue: '', description: '可选的构建工具参数')
    }
    
    stages {
        stage ('Example') {
            steps {
                script {
                    gradle.build(params.BUILD_ARGS ?: '')
                }
            }
        }
    }
}
**/

package org.devops

class Gradle implements Serializable {
    def script  // Pipeline 上下文

    Gradle(script) {
        this.script = script
    }

    /**
     * 打印 Gradle 版本
     */
    private def printGradleVersion() {
        script.echo "Gradle version:"
        script.sh 'gradle --version'
    }

    def test(additionalArgs = "") {
        printGradleVersion()

        def gradleCommand = "gradle test ${additionalArgs}"
        def exitCode = script.sh(script: gradleCommand, returnStatus: true)
    
        if (exitCode != 0) {
            script.error "Gradle test failed with exit code: $exitCode"
        }
    }
    
    def build(additionalArgs = "") {
        printGradleVersion()
        
        def gradleCommand = "gradle build -x test ${additionalArgs}"
        def exitCode = script.sh(script: gradleCommand, returnStatus: true)
    
        if (exitCode != 0) {
            script.error "Gradle build failed with exit code: $exitCode"
        }
    }
}
