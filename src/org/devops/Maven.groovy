/**
@Library('jenkinslibrary@main') _
def maven = mavenHelper(this)

pipeline {
    agent any
    
    parameters {
    string(name: 'BUILD_ARGS', defaultValue: '', description: '可选的构建工具参数')
    }
    
    stages {
        stage ('Example') {
            steps {
                script {
                    maven.build(params.BUILD_ARGS ?: '')
                }
            }
        }
    }
}
**/

package org.devops

class Maven implements Serializable {
    def script  // Pipeline 上下文

    Maven(script) {
        this.script = script
    }

    def readSettingsXml() {
        def settingsContent = script.libraryResource('config/settings.xml')
        script.writeFile file: 'settings.xml', text: settingsContent
    }

    def test(String additionalArgs = "") {
        readSettingsXml()
        def mvnCommand = "mvn -s settings.xml test ${additionalArgs}"
        def exitCode = script.sh(script: mvnCommand, returnStatus: true)

        if (exitCode != 0) {
            script.error "Maven test failed with exit code: $exitCode"
        }
    }

    def build(String additionalArgs = "") {
        readSettingsXml()
        def mvnCommand = "mvn -s settings.xml clean package -Dmaven.test.skip=true ${additionalArgs}"
        def exitCode = script.sh(script: mvnCommand, returnStatus: true)

        if (exitCode != 0) {
            script.error "Maven build failed with exit code: $exitCode"
        }
    }

    def deploy(String additionalArgs = "") {
        readSettingsXml()
        def mvnCommand = "mvn -s settings.xml clean deploy -Dmaven.test.skip=true ${additionalArgs}"
        def exitCode = script.sh(script: mvnCommand, returnStatus: true)

        if (exitCode != 0) {
            script.error "Maven deploy failed with exit code: $exitCode"
        }
    }
}
