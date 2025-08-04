/**
@Library('jenkinslibrary@main') _
def kaniko = kanikoHelper(this)

pipeline {
    stages{
        stage("BuildImages"){
            steps{
                container('kaniko'){
                    script{
                        tools.PrintMes("构建镜像","green")
                        kaniko.init(params)
                        kaniko.build(project: "demo")
                    }
                }
            }
        }
    }
}
**/

package org.devops

class Kaniko implements Serializable {

    def script  // pipeline上下文

    def registryUrl = ""
    def tag = ""
    def image = ""
    def project = ""

    // 构造函数传入 script
    Kaniko(script) {
        this.script = script
    }

    def init(Map params = [:]) {
        script.echo "Params: ${params}"
        def defaultRegistry = "local"

        if (!params.REGISTRY) {
            script.echo "❗Registry is not set. Please define the 'REGISTRY' parameter. Using default: ${defaultRegistry}"
        }
        def registry = params.REGISTRY ?: defaultRegistry

        if (!script.env.repo) {
            script.error "❗Repository is not set. Please define the 'env.repo' environment variable."
        }

        def registryMap = [
            'local'       : "192.168.1.60",
            'aliyun'      : "registry.cn-shanghai.aliyuncs.com",
            'huaweicloud' : "swr.cn-east-2.myhuaweicloud.com"
        ]

        if (registryMap.containsKey(registry)) {
            registryUrl = registryMap[registry]
        } else {
            script.error "Unsupported registry: ${registry}"
        }

        def defaultTag = "${new Date().format('yyyyMMddHHmmss')}_${script.env.BUILD_ID}"
        tag = params.TAG ?: defaultTag

        return this
    }

    def build(Map params) {
        String dockerfile = params.get('Dockerfile', "Dockerfile")
        String path = params.get('path', ".")
        project = params.get('project')

        if (!project) {
            script.error "project is required"
        }

        image = "${registryUrl}/${script.env.repo}/${project}:${tag}"

        script.ansiColor('xterm') {
            script.echo "\u001B[1;35m🔧 Building project with Kaniko: ${project} \u001B[0m"
        }

        try {
            script.sh """
                /kaniko/executor \
                --dockerfile=${path}/${dockerfile} \
                --context=${path} \
                --destination=${image} \
                --cleanup
            """
        } catch (Exception e) {
            script.error "Kaniko build failed: ${e.message}"
        }

        script.currentBuild.description = "Image tag: ${tag}"

        def imageInfo = [
            project: project,
            image: image
        ]
        script.writeJSON file: "${project}-image.json", json: imageInfo
        script.archiveArtifacts artifacts: "${project}-image.json"

        script.ansiColor('xterm') {
            script.echo "\u001B[1;32m📦 Image is: ${image}\u001B[m"
        }

        return this
    }
}
