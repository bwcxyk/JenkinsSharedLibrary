/**
@Library('jenkinslibrary@main') _
def docker = dockerHelper(this)

pipeline {
    agent any

    environment {
        repo = "public"
    }

    parameters {
    choice choices: ['local', 'aliyun', 'huaweicloud'], description: '镜像仓库', name: 'REGISTRY'
    // string defaultValue: '1.0', description: '版本', name: 'TAG'
    }
    stages{
        stage ('Example') {
            steps {
                script {
                    docker.init(params)
                    docker.build(project: "demo").push()
                }
            }
        }
    }
}
**/

package org.devops

class Docker implements Serializable {

    def script  // pipeline上下文

    def credentialsId = ""
    def registryUrl = ""
    def tag = ""
    def islogin = false
    def image = ""
    def project = ""

    // 构造函数传入 script 
    Docker(script) {
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
            credentialsId = registry
            registryUrl = registryMap[registry]
        } else {
            script.error "Unsupported registry: ${registry}"
        }

        def defaultTag = "${new Date().format('yyyyMMddHHmmss')}_${script.env.BUILD_ID}"
        tag = params.TAG ?: defaultTag
        islogin = false

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
            script.echo "\u001B[1;35m🔧 Building project: ${project} \u001B[0m"
        }

        try {
            script.sh "docker build -t ${image} -f ${path}/${dockerfile} ${path}"
        } catch (Exception e) {
            script.error "Docker build failed: ${e.message}"
        }

        return this
    }

    def push() {
        login()
        if (islogin) {
            try {
                script.sh "docker push ${image}"

                script.currentBuild.description = "docker tag: ${tag}"

                def imageInfo = [
                    project: project,
                    image: image
                ]
                script.writeJSON file: "${project}-image.json", json: imageInfo
                script.archiveArtifacts artifacts: "${project}-image.json"

                script.ansiColor('xterm') {
                    script.echo "\u001B[1;32m📦 Image is: ${image}\u001B[m"
                }
            } catch (Exception e) {
                script.error "Failed to push image: ${e.message}"
            }
        } else {
            script.error "Login failed, cannot push image."
        }

        rmi()
        return this
    }

    def rmi() {
        try {
            script.sh "docker rmi ${image}"
        } catch (Exception e) {
            script.error "Docker rmi failed: ${e.message}"
        }
        return this
    }

    def login() {
        if (islogin || credentialsId == "") {
            return this
        }

        script.echo "Using credentialsId: ${credentialsId}"

        script.withCredentials([script.usernamePassword(credentialsId: credentialsId,
                                                      usernameVariable: 'USERNAME',
                                                      passwordVariable: 'PASSWORD')]) {
            try {
                script.sh "echo \$PASSWORD | docker login -u \$USERNAME --password-stdin ${registryUrl}"
                script.echo "✅ Docker login successful."
                islogin = true
            } catch (Exception e) {
                script.echo "Docker login error: ${e.message}"
            }
        }

        return this
    }
}
