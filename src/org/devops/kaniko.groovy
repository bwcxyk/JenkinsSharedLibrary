/**
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
**/

package org.devops

class kaniko implements Serializable {

    def steps  // pipeline上下文

    def registryUrl = ""
    def tag = ""
    def image = ""
    def project = ""

    // 构造函数传入 steps
    kaniko(steps) {
        this.steps = steps
    }

    def init(Map params = [:]) {
        steps.echo "Params: ${params}"
        def defaultRegistry = "local"

        if (!params.REGISTRY) {
            steps.echo "❗Registry is not set. Please define the 'REGISTRY' parameter. Using default: ${defaultRegistry}"
        }
        def registry = params.REGISTRY ?: defaultRegistry

        if (!steps.env.repo) {
            steps.error "❗Repository is not set. Please define the 'env.repo' environment variable."
        }

        def registryMap = [
            'local'       : "192.168.1.60",
            'aliyun'      : "registry.cn-shanghai.aliyuncs.com",
            'huaweicloud' : "swr.cn-east-2.myhuaweicloud.com"
        ]

        if (registryMap.containsKey(registry)) {
            registryUrl = registryMap[registry]
        } else {
            steps.error "Unsupported registry: ${registry}"
        }

        def defaultTag = "${new Date().format('yyyyMMddHHmmss')}_${steps.env.BUILD_ID}"
        tag = params.TAG ?: defaultTag

        return this
    }

    def build(Map params) {
        String dockerfile = params.get('Dockerfile', "Dockerfile")
        String path = params.get('path', ".")
        project = params.project

        if (!project) {
            steps.error "project is required"
        }

        image = "${registryUrl}/${steps.env.repo}/${project}:${tag}"

        steps.ansiColor('xterm') {
            steps.echo "\u001B[1;35m🔧 Building project with Kaniko: ${project} \u001B[0m"
        }

        try {
            steps.sh """
                /kaniko/executor \
                --dockerfile=${path}/${dockerfile} \
                --context=${path} \
                --destination=${image} \
                --cleanup
            """
        } catch (Exception e) {
            steps.error "Kaniko build failed: ${e.message}"
        }

        steps.currentBuild.description = "Image tag: ${tag}"

        def imageInfo = [
            project: project,
            image: image
        ]
        steps.writeJSON file: "${project}-image.json", json: imageInfo
        steps.archiveArtifacts artifacts: "${project}-image.json"

        steps.ansiColor('xterm') {
            steps.echo "\u001B[1;32m📦 Image is: ${image}\u001B[m"
        }

        return this
    }
}
