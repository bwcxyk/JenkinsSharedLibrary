/**
@Library('jenkinslibrary@master') _

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
                    docker.docker()
                    docker.build(project: "demo").push()
                }
            }
        }
    }
}
**/

def docker() {
    echo "Params: ${params}"
    // 检查 params.REGISTRY 是否已经设置
    def defaultRegistry = "local"
    if (!params.REGISTRY) {
        echo "Registry is not set. Please define the 'REGISTRY' parameter. Using default: ${defaultRegistry}"
    }
    def registry = params.REGISTRY ?: defaultRegistry

    // 检查 env.repo 是否已经设置
    if (!env.repo) {
        error "Repository is not set. Please define the 'env.repo' environment variable."
    }

    // 定义不同 registry 的 URL 映射
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

    // 设置默认的镜像标签，格式为日期时间加构建ID
    def defaultTag = "${new Date().format('yyyyMMddHHmmss')}_${env.BUILD_ID}"
    tag = params.TAG ?: defaultTag
    islogin = false
    return this
}

def build(Map params) {
    String dockerfile = params.get('Dockerfile', "Dockerfile")
    String path = params.get('path', ".")
    String project = params.project

    if (!project) {
        error "project is required"
    }

    // 构建镜像名称
    image = "${registryUrl}/${env.repo}/${project}:${tag}"
    def msg = ""
    ansiColor('xterm') {
        echo "\u001B[33m Building project: ${project} \u001B[0m"
    }

    try {
        // 执行 Docker 构建命令
        sh "docker build -t ${image} -f ${path}/${dockerfile} ${path}"
    } catch (Exception e) {
        // 如果构建失败，捕获异常并记录错误信息
        msg = e.toString()
        error "Docker build failed: ${msg}"
    }

    // 输出成功信息
    echo "Docker image built successfully: ${image}"

    // 返回当前对象以支持链式调用
    return this
}

def push() {
    login()
    if (islogin) {
        try {
            // 执行docker push命令
            sh "docker push ${image}"
            
            // 如果 push 成功，则设置构建描述
            currentBuild.description = "docker tag: ${tag}"
            // 使用 ANSI 颜色代码打印绿色文本
            ansiColor('xterm') {
                echo "\u001B[1;32m Image is: ${image}\u001B[m"
            }
            echo "Image pushed successfully and build description set."
        } catch (Exception e) {
            // 如果 push 失败，则打印错误信息
            error "Failed to push image: ${e.message}"
        }
    } else {
        error "Login failed, cannot push image."
    }
    // 调用删除方法
    rmi()
}

def rmi() {
    try {
        // 执行 Docker 镜像删除命令
        sh "docker rmi ${image}"
    } catch (Exception e) {
        // 如果删除失败，捕获异常并记录错误信息
        msg = e.toString()
        error "Docker rmi failed: ${msg}"
    }
    // 返回当前对象以支持链式调用
    return this
}

def login() {
    // 如果已经登录或者 credentialsId 为空，则直接返回
    if (islogin || credentialsId == "") {
        return this
    }

    echo "Using credentialsId: ${credentialsId}"

    // 使用 Jenkins 的 withCredentials 方法获取凭据
    withCredentials([usernamePassword(credentialsId: credentialsId, 
                                       usernameVariable: 'USERNAME', 
                                       passwordVariable: 'PASSWORD')]) {
        try {
            // 安全性：使用 \ 转义 $ 符号可以防止 Groovy 立即展开变量，而是让 shell 在执行命令时再进行变量替换
            sh "echo \$PASSWORD | docker login -u \$USERNAME --password-stdin ${registryUrl}"
            echo "Docker login successful."
            islogin = true
        } catch (Exception e) {
            echo "Docker login error: ${e.message}"
        }
    }

    return islogin
}
