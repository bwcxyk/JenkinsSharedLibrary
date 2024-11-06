/**
@Library('jenkinslibrary@master') _

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

def readSettingsXml() {
    // 从共享库的 resources 目录中读取 settings.xml 的内容
    def settingsContent = libraryResource 'config/settings.xml'
    
    // 将 settings.xml 内容写入到当前工作区
    writeFile file: 'settings.xml', text: settingsContent
}

def test(additionalArgs = "") {
    readSettingsXml()

    def mvnCommand = "mvn -s settings.xml test ${additionalArgs}"
    def exitCode = sh(script: mvnCommand, returnStatus: true)

    if (exitCode != 0) {
        error "Maven test failed with exit code: $exitCode"
    }
}

def build(additionalArgs = "") {
    readSettingsXml()

    def mvnCommand = "mvn -s settings.xml clean package -Dmaven.test.skip=true ${additionalArgs}"
    def exitCode = sh(script: mvnCommand, returnStatus: true)

    if (exitCode != 0) {
        error "Maven build failed with exit code: $exitCode"
    }
}

def deploy(additionalArgs = "") {
    readSettingsXml()

    def mvnCommand = "mvn -s settings.xml clean deploy -Dmaven.test.skip=true ${additionalArgs}"
    def exitCode = sh(script: mvnCommand, returnStatus: true)

    if (exitCode != 0) {
        error "Maven deploy failed with exit code: $exitCode"
    }
}
