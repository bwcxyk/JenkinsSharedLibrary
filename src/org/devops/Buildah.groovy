/**
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
**/

package org.devops

class Buildah implements Serializable {

    def steps
    def credentialsId = ""
    def registryUrl = ""
    def tag = ""
    def image = ""
    def project = ""
    def isLogin = false

    Buildah(steps) {
        this.steps = steps
    }

    def init(Map params = [:]) {
        steps.echo "Params: ${params}"
        def defaultRegistry = "local"

        def registry = params.REGISTRY ?: defaultRegistry
        def registryMap = [
            'local'       : "192.168.1.60",
            'aliyun'      : "registry.cn-shanghai.aliyuncs.com",
            'huaweicloud' : "swr.cn-east-2.myhuaweicloud.com"
        ]

        if (!registryMap.containsKey(registry)) {
            steps.error "Unsupported registry: ${registry}"
        }

        credentialsId = registry
        registryUrl = registryMap[registry]

        if (!steps.env.repo) {
            steps.error "❗Repository is not set. Please define the 'env.repo' environment variable."
        }

        def defaultTag = "${new Date().format('yyyyMMddHHmmss')}_${steps.env.BUILD_ID}"
        tag = params.TAG ?: defaultTag
        isLogin = false

        return this
    }

    def build(Map params) {
        String dockerfile = params.get('Dockerfile', "Dockerfile")
        String context = params.get('path', ".")
        project = params.project

        if (!project) {
            steps.error "project is required"
        }

        image = "${registryUrl}/${steps.env.repo}/${project}:${tag}"

        steps.ansiColor('xterm') {
            steps.echo "\u001B[1;35m🔧 Building project: ${project} with Buildah \u001B[0m"
        }

        try {
            // 使用 Buildah bud 命令构建镜像
            steps.sh """
                buildah bud --format=docker --file=${context}/${dockerfile} --tag=${image} ${context}
            """
        } catch (Exception e) {
            steps.error "Buildah build failed: ${e.message}"
        }

        return this
    }

    def login() {
        if (isLogin || credentialsId == "") {
            return this
        }

        steps.echo "Using credentialsId: ${credentialsId}"

        steps.withCredentials([steps.usernamePassword(credentialsId: credentialsId,
                                                      usernameVariable: 'USERNAME',
                                                      passwordVariable: 'PASSWORD')]) {
            try {
                steps.sh """
                    buildah login -u \$USERNAME -p \$PASSWORD ${registryUrl}
                """
                steps.echo "✅ Buildah login successful."
                isLogin = true
            } catch (Exception e) {
                steps.error "Buildah login failed: ${e.message}"
            }
        }

        return this
    }

    def push() {
        login()
        if (!isLogin) {
            steps.error "Login failed, cannot push image."
        }

        try {
            steps.sh "buildah push ${image}"
            steps.currentBuild.description = "Image tag: ${tag}"

            def imageInfo = [
                project: project,
                image: image
            ]
            steps.writeJSON file: "${project}-image.json", json: imageInfo
            steps.archiveArtifacts artifacts: "${project}-image.json"

            steps.ansiColor('xterm') {
                steps.echo "\u001B[1;32m📦 Image pushed: ${image}\u001B[0m"
            }
        } catch (Exception e) {
            steps.error "Buildah push failed: ${e.message}"
        }

        return this
    }

    def rmi() {
        try {
            steps.sh "buildah rmi ${image}"
        } catch (Exception e) {
            steps.echo "Buildah rmi failed: ${e.message}"
        }
        return this
    }
}
