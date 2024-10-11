/**
@Library('jenkinslibrary@master') _

pipeline {
    agent any

    environment {
        repo = "public"
    }

    parameters {
    choice choices: ['local', 'aliyun', 'huaweicloud'], description: '镜像仓库', name: 'registry'
    }
    stages{
        stage ('Example') {
            steps {
                script {
                    docker.docker("demo")
                    docker.build()
                    docker.push()
                }
            }
        }
    }
}
**/

def docker() {

    // 检查registry和env.repo是否已经设置
    if (!registry) {
        error "Registry is not set. Please define the 'registry' parameter."
    }
    if (!env.repo) {
        error "Repository is not set. Please define the 'env.repo' environment variable."
    }

    def registryMap = [
        'local'        : "192.168.1.60",
        'aliyun'       : "registry.cn-shanghai.aliyuncs.com",
        'huaweicloud'  : "swr.cn-east-2.myhuaweicloud.com"
    ]

    if (registryMap.containsKey(registry)) {
        credentialsId = registry
        registryUrl = registryMap[registry]
    } else {
        error "Unsupported registry: ${registry}"
    }

    tag = "${new Date().format('yyyyMMddHHmmss')}_${env.BUILD_ID}"
    islogin = false
    return this
}

def build(String directory = null, String project) {
    image = "${registryUrl}/${env.repo}/${project}:${tag}"
    def msg = ""
    Boolean isdockerbuild = false

    try {
        if (directory) {
            // 如果传入了目录参数，则切换到该目录并构建
            sh """
                cd ${directory}
                docker build -t ${image} .
            """
        } else {
            // 如果没有传入目录参数，则在当前目录执行构建
            sh "docker build -t ${image} ."
        }

        isdockerbuild = true
        env.CURRENT_IMAGE = image
    } catch (Exception e) {
        msg = e.toString()
        error "Docker build failed: ${msg}"
    }

    echo "++++++++++++++++++++++++++++++++++++++++++++++"
    echo "Docker image built successfully: ${env.CURRENT_IMAGE}"

    return this
}

def push() {
    login()
    if (islogin) {
        try {
            // 执行docker push命令
            sh "docker push ${image}"
            
            // 如果push成功，则设置构建描述
            currentBuild.description = "docker tag: ${tag}"
            echo "Image pushed successfully and build description set."
        } catch (Exception e) {
            // 如果push失败，则打印错误信息
            error "Failed to push image: ${e.message}"
        }
    } else {
        error "Login failed, cannot push image."
    }
}

def login() {
    if (islogin || credentialsId == "") {
        return this
    }

    echo "Using credentialsId: ${credentialsId}"

    withCredentials([usernamePassword(credentialsId: credentialsId, 
                                       usernameVariable: 'USERNAME', 
                                       passwordVariable: 'PASSWORD')]) {
        try {
            sh "docker login -u ${USERNAME} -p ${PASSWORD} ${registryUrl}"
            echo "Docker login successful."
            islogin = true
        } catch (Exception e) {
            echo "Docker login error: ${e.message}"
        }
    }

    return this
}
