/**
@Library('jenkinslibrary@main') _
def maven = mavenHelper(this)

pipeline {
    agent any
    
    parameters {
        // Maven 构建参数
        string(
            name: 'BUILD_ARGS',
            defaultValue: '',
            description: '可选的 Maven 构建参数'
        )
        booleanParam(
            name: 'PURGE_LOCAL_REPOSITORY',
            defaultValue: false,
            description: '构建前清理本地 Maven 依赖缓存'
        )

        string(
            name: 'PURGE_ARTIFACTS',
            defaultValue: '',
            description: '留空表示清理当前项目所有依赖；填写依赖坐标（groupId:artifactId），多个依赖用逗号分隔'
        )
    }
    
    stages {
        stage("Build") {
            steps {
                container('Example') {
                    script {
                        tools.PrintMes("执行打包", "green")
                        if (params.PURGE_LOCAL_REPOSITORY) {
                            tools.PrintMes("清理本地 Maven 依赖缓存", "yellow")
                            maven.purgeLocalRepository(params.PURGE_ARTIFACTS ?: "")
                        }
                        maven.deploy(params.BUILD_ARGS ?: "")
                    }
                }
            }
        }
    }
}
**/

package org.devops

class Maven implements Serializable {
    private final def script  // Pipeline 上下文

    Maven(script) {
        this.script = script
    }

    private def readSettingsXml() {
        def settingsContent = script.libraryResource('config/settings.xml')
        script.writeFile file: 'settings.xml', text: settingsContent
    }

    /**
     * 打印 Maven 版本
     */
    private def printMavenVersion() {
        script.echo "Maven version:"
        script.sh 'mvn -version'
    }

    def purgeLocalRepository(String artifacts = "") {
        readSettingsXml()
        printMavenVersion()

        artifacts = artifacts?.trim()

        if (artifacts) {
            script.echo "Purging local Maven artifacts: ${artifacts}"
        } else {
            script.echo "Purging all project dependencies from local Maven repository"
        }

        def manualInclude = artifacts ? "-DmanualInclude=${artifacts}" : ""

        def mvnCommand = "mvn -s settings.xml dependency:purge-local-repository ${manualInclude}"

        def exitCode = script.sh(script: mvnCommand, returnStatus: true)

        if (exitCode != 0) {
            script.error "Maven purge local repository failed with exit code: $exitCode"
        }
    }

    def test(String additionalArgs = "") {
        readSettingsXml()
        printMavenVersion()

        def mvnCommand = "mvn -s settings.xml test ${additionalArgs}"
        def exitCode = script.sh(script: mvnCommand, returnStatus: true)

        if (exitCode != 0) {
            script.error "Maven test failed with exit code: $exitCode"
        }
    }

    def mavenPackage(String additionalArgs = "") {
        readSettingsXml()
        printMavenVersion()

        def mvnCommand = """
            mvn -B -s settings.xml \
                package \
                -T 1C \
                -Dmaven.test.skip=true \
                ${additionalArgs}
        """
        def exitCode = script.sh(script: mvnCommand, returnStatus: true)

        if (exitCode != 0) {
            script.error "Maven package failed with exit code: $exitCode"
        }
    }

    def deploy(String additionalArgs = "") {
        readSettingsXml()
        printMavenVersion()

        def mvnCommand = """
            mvn -B -s settings.xml \
                deploy \
                -T 1C \
                -Dmaven.test.skip=true \
                ${additionalArgs}
        """
        def exitCode = script.sh(script: mvnCommand, returnStatus: true)

        if (exitCode != 0) {
            script.error "Maven deploy failed with exit code: $exitCode"
        }
    }

    // deprecated
    def build(String additionalArgs = "") {
        script.ansiColor('xterm') {
            script.echo "\u001B[33m[DEPRECATED] build() is deprecated, please use mavenPackage()\u001B[0m"
        }
        mavenPackage(additionalArgs)
    }

}
