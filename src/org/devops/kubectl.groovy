/**
@Library('jenkinslibrary@master') _

pipeline {
    agent any

    environment {
        repo = "public"
        k8s_namespace = "default"
    }

    parameters {
    choice choices: ['dev', 'stg', 'prod'], description: '构建环境', name: 'BUILD_ENV'
    }
    stages{
        stage ('Example') {
            steps {
                script {
                    kubectl.init()
                    kubectl.deploy(project: "demo")
                }
            }
        }
    }
}
**/

def init() {
    def defaultBuildenv = "dev"
    
    if (!params.BUILD_ENV) {
        echo "⚠️ BUILD_ENV is not set. Using default: ${defaultBuildenv}"
    }
    
    def buildEnv = params.BUILD_ENV ?: defaultBuildenv
    def credentialsId = "k8s_${buildEnv}"

    try {
        withCredentials([file(credentialsId: credentialsId, variable: 'KUBECONFIG')]) {
            // 生成临时配置文件
            sh """
                mkdir -p ~/.kube
                cp \${KUBECONFIG} ~/.kube/config
                chmod 600 ~/.kube/config
            """
        }
    } catch (Exception e) {
        error "❌ Credentials file not found. Error: ${e.message}"
    }

    // 验证集群连接
    try {
        sh "kubectl version --short --output=json --request-timeout=3s >/dev/null 2>&1"
        echo "✅ Kubernetes cluster connection successful."
    } catch (Exception e) {
        error "❌ Kubernetes cluster connection error. Error: ${e.message}"
    }
}

def deploy(Map params) {
    // 参数校验和初始化
    String projectName = params.get('project', "")
    String containerName = params.get('container', projectName)

    if (!projectName) {
        error "必须提供 project 参数"
    }

    // 命名空间处理
    String namespace = env.k8s_namespace ?: error("未设置 k8s_namespace 环境变量")

    try {
        // 1. 读取镜像信息
        def imageInfo = readJSON(file: "${projectName}-image.json").with {
            it ?: error ("镜像信息文件 ${projectName}-image.json 读取失败")
        }

        ansiColor('xterm') {
            echo "\u001B[1;36m🚀 开始部署 ${projectName} (容器: ${containerName})"
            echo "\u001B[1;36m📦 镜像: ${imageInfo.image}"
            echo "\u001B[1;36m🏷️ 命名空间: ${namespace}\u001B[0m"
        }

        // 2. 执行部署
        sh """
            kubectl set image deployment/${projectName} \
            ${containerName}=${imageInfo.image} \
            -n ${namespace}
        """

    } catch (Exception e) {
        // 错误处理
        def errorDetails = sh(
            script: """
                kubectl describe deployment/${projectName} -n ${namespace} && 
                kubectl get pods -n ${namespace} -l app=${projectName}
            """,
            returnStdout: true
        ).trim()

        ansiColor('xterm') {
            echo "\u001B[1;31m❌ 部署失败: ${projectName}"
            echo "\u001B[1;31m💥 错误信息: ${e.message}"
            echo "\u001B[1;33m🔍 诊断信息:\n${errorDetails}\u001B[0m"
        }
        error "部署 ${projectName} 失败"
    }
}
