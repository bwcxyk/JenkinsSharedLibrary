/**
@Library('jenkinslibrary@master') _
def docker = new org.devops.docker(this)

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
                    docker.docker(params)
                    docker.build(project: "demo").push()
                }
            }
        }
    }
}
**/

package org.devops

class docker implements Serializable {

    def steps  // pipeline上下文

    def credentialsId = ""
    def registryUrl = ""
    def tag = ""
    def islogin = false
    def image = ""
    def project = ""

    // 构造函数传入 steps
    docker(steps) {
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
            credentialsId = registry
            registryUrl = registryMap[registry]
        } else {
            steps.error "Unsupported registry: ${registry}"
        }

        def defaultTag = "${new Date().format('yyyyMMddHHmmss')}_${steps.env.BUILD_ID}"
        tag = params.TAG ?: defaultTag
        islogin = false

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
            steps.echo "\u001B[1;35m🔧 Building project: ${project} \u001B[0m"
        }

        try {
            steps.sh "docker build -t ${image} -f ${path}/${dockerfile} ${path}"
        } catch (Exception e) {
            steps.error "Docker build failed: ${e.message}"
        }

        return this
    }

    def push() {
        login()
        if (islogin) {
            try {
                steps.sh "docker push ${image}"

                steps.currentBuild.description = "docker tag: ${tag}"

                def imageInfo = [
                    project: project,
                    image: image
                ]
                steps.writeJSON file: "${project}-image.json", json: imageInfo
                steps.archiveArtifacts artifacts: "${project}-image.json"

                steps.ansiColor('xterm') {
                    steps.echo "\u001B[1;32m📦 Image is: ${image}\u001B[m"
                }
            } catch (Exception e) {
                steps.error "Failed to push image: ${e.message}"
            }
        } else {
            steps.error "Login failed, cannot push image."
        }

        rmi()
        return this
    }

    def rmi() {
        try {
            steps.sh "docker rmi ${image}"
        } catch (Exception e) {
            steps.error "Docker rmi failed: ${e.message}"
        }
        return this
    }

    def login() {
        if (islogin || credentialsId == "") {
            return this
        }

        steps.echo "Using credentialsId: ${credentialsId}"

        steps.withCredentials([steps.usernamePassword(credentialsId: credentialsId,
                                                      usernameVariable: 'USERNAME',
                                                      passwordVariable: 'PASSWORD')]) {
            try {
                steps.sh "echo \$PASSWORD | docker login -u \$USERNAME --password-stdin ${registryUrl}"
                steps.echo "✔ Docker login successful."
                islogin = true
            } catch (Exception e) {
                steps.echo "Docker login error: ${e.message}"
            }
        }

        return this
    }
}
