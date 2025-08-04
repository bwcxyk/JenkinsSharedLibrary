/**
@Library('jenkinslibrary@main') _
def buildah = buildahHelper(this)

pipeline {
    stages{
        stage("BuildImages"){
            steps{
                container('buildah'){
                    script{
                        tools.PrintMes("构建镜像","green")
                        buildah.init(params)
                        buildah.build(project: "demo").push()
                    }
                }
            }
        }
    }
}
**/

package org.devops

class Buildah implements Serializable {

    def script
    def credentialsId = ""
    def registryUrl = ""
    def tag = ""
    def image = ""
    def project = ""
    def isLogin = false

    Buildah(script) {
        this.script = script
    }

    def init(Map params = [:]) {
        script.echo "Params: ${params}"
        def defaultRegistry = "local"

        def registry = params.REGISTRY ?: defaultRegistry
        def registryMap = [
            'local'       : "192.168.1.60",
            'aliyun'      : "registry.cn-shanghai.aliyuncs.com",
            'huaweicloud' : "swr.cn-east-2.myhuaweicloud.com"
        ]

        if (!registryMap.containsKey(registry)) {
            script.error "Unsupported registry: ${registry}"
        }

        credentialsId = registry
        registryUrl = registryMap[registry]

        if (!script.env.repo) {
            script.error "❗Repository is not set. Please define the 'env.repo' environment variable."
        }

        def defaultTag = "${new Date().format('yyyyMMddHHmmss')}_${script.env.BUILD_ID}"
        tag = params.TAG ?: defaultTag
        isLogin = false

        return this
    }

    def build(Map params) {
        String dockerfile = params.get('Dockerfile', "Dockerfile")
        String context = params.get('path', ".")
        project = params.get('project')

        if (!project) {
            script.error "project is required"
        }

        image = "${registryUrl}/${script.env.repo}/${project}:${tag}"

        script.ansiColor('xterm') {
            script.echo "\u001B[1;35m🔧 Building project: ${project} with Buildah \u001B[0m"
        }

        try {
            // 使用 Buildah bud 命令构建镜像
            script.sh """
                buildah bud --format=docker --file=${context}/${dockerfile} --tag=${image} ${context}
            """
        } catch (Exception e) {
            script.error "Buildah build failed: ${e.message}"
        }

        return this
    }

    def login() {
        if (isLogin || credentialsId == "") {
            return this
        }

        script.echo "Using credentialsId: ${credentialsId}"

        script.withCredentials([script.usernamePassword(credentialsId: credentialsId,
                                                      usernameVariable: 'USERNAME',
                                                      passwordVariable: 'PASSWORD')]) {
            try {
                script.sh """
                    buildah login -u \$USERNAME -p \$PASSWORD ${registryUrl}
                """
                script.echo "✅ Buildah login successful."
                isLogin = true
            } catch (Exception e) {
                script.error "Buildah login failed: ${e.message}"
            }
        }

        return this
    }

    def push() {
        login()
        if (!isLogin) {
            script.error "Login failed, cannot push image."
        }

        try {
            script.sh "buildah push ${image}"
            script.currentBuild.description = "Image tag: ${tag}"

            def imageInfo = [
                project: project,
                image: image
            ]
            script.writeJSON file: "${project}-image.json", json: imageInfo
            script.archiveArtifacts artifacts: "${project}-image.json"

            script.ansiColor('xterm') {
                script.echo "\u001B[1;32m📦 Image pushed: ${image}\u001B[0m"
            }
        } catch (Exception e) {
            script.error "Buildah push failed: ${e.message}"
        }

        return this
    }

    def rmi() {
        try {
            script.sh "buildah rmi ${image}"
        } catch (Exception e) {
            script.echo "Buildah rmi failed: ${e.message}"
        }
        return this
    }
}
