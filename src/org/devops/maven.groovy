/**
@Library('jenkinslibrary@master') _

pipeline {
    agent any
    stages {
        stage ('Example') {
            steps {
                script {
                    maven.build(BUILD_ARGS)
                }
            }
        }
    }
}
**/

package org.devops

def readSettingsXml() {
    // 从共享库的 resources 目录中读取 settings.xml 的内容
    def settingsContent = libraryResource 'config/settings.xml'
    
    // 将 settings.xml 内容写入到当前工作区
    writeFile file: 'settings.xml', text: settingsContent
}

def test(additionalMavenArgs = "") {
    readSettingsXml()

    def mvnCommand = "mvn -s settings.xml test ${additionalMavenArgs}"
    def exitCode = sh(script: mvnCommand, returnStatus: true)

    if (exitCode != 0) {
        error "Maven test failed with exit code: $exitCode"
    }
}

def build(additionalMavenArgs = "") {
    readSettingsXml()

    def mvnCommand = "mvn -s settings.xml clean package -Dmaven.test.skip=true ${additionalMavenArgs}"
    def exitCode = sh(script: mvnCommand, returnStatus: true)

    if (exitCode != 0) {
        error "Maven build failed with exit code: $exitCode"
    }
}

def deploy(additionalMavenArgs = "") {
    readSettingsXml()

    def mvnCommand = "mvn -s settings.xml clean deploy -Dmaven.test.skip=true ${additionalMavenArgs}"
    def exitCode = sh(script: mvnCommand, returnStatus: true)

    if (exitCode != 0) {
        error "Maven deploy failed with exit code: $exitCode"
    }
}
