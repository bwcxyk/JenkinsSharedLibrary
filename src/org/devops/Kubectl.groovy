/**
@Library('jenkinslibrary@main') _
def kubectl = kubectlHelper()

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

package org.devops

class Kubectl implements Serializable {
    def script  // Pipeline 上下文

    Kubectl(script) {
        this.script = script
    }

    def init() {
        def defaultBuildenv = "dev"

        if (!script.params.BUILD_ENV) {
            script.echo "⚠️ BUILD_ENV is not set. Using default: ${defaultBuildenv}"
        }

        def buildEnv = script.params.BUILD_ENV ?: defaultBuildenv
        def credentialsId = "k8s_${buildEnv}"
    
        createKubeconfig(credentialsId)
    
        // 验证集群连接
        try {
            script.sh "kubectl version --short --output=json --request-timeout=3s >/dev/null 2>&1"
            script.echo "✅ Kubernetes cluster connection successful."
        } catch (Exception e) {
            script.error "❌ Kubernetes cluster connection error. Error: ${e.message}"
        }
    }

    def deploy(Map params) {
        // 参数校验和初始化
        String projectName = params.get('project', "")
        String containerName = params.get('container', projectName)

        if (!projectName) {
            script.error "必须提供 project 参数"
        }

        // 命名空间处理
        String namespace = script.env.k8s_namespace
        if (!namespace?.trim()) {
            script.error("未设置 k8s_namespace 环境变量")
        }

        try {
            // 1. 读取镜像信息
            def imageInfo = script.readJSON(file: "${projectName}-image.json").with {
                it ?: error ("镜像信息文件 ${projectName}-image.json 读取失败")
            }

            script.ansiColor('xterm') {
                script.echo "\u001B[1;36m🚀 开始部署 ${projectName} (容器: ${containerName})"
                script.echo "\u001B[1;36m📦 镜像: ${imageInfo.image}"
                script.echo "\u001B[1;36m🏷️ 命名空间: ${namespace}\u001B[0m"
            }

            // 2. 执行部署
            def result = script.sh(
                script: """kubectl set image deployment/${projectName} \\
                           ${containerName}=${imageInfo.image} -n ${namespace}""",
                returnStatus: true
            )

            if (result != 0) {
                throw new Exception("kubectl set image 执行失败")
            }
        } catch (Exception e) {
            // 错误处理
            def errorDetails = script.sh(
                script: """
                    kubectl describe deployment/${projectName} -n ${namespace} && 
                    kubectl get pods -n ${namespace} -l app=${projectName}
                """,
                returnStdout: true
            ).trim()

            script.ansiColor('xterm') {
                script.echo "\u001B[1;31m❌ 部署失败: ${projectName}"
                script.echo "\u001B[1;31m💥 错误信息: ${e.message}"
                script.echo "\u001B[1;33m🔍 诊断信息:\n${errorDetails}\u001B[0m"
            }
            script.error "部署 ${projectName} 失败"
        } finally {
            cleanupKubeconfig()
        }
    }

    def createKubeconfig(credentialsId) {
        script.withCredentials([script.file(credentialsId: credentialsId, variable: 'KUBECONFIG')]) {
            script.sh """
                mkdir -p ~/.kube
                cp \${KUBECONFIG} ~/.kube/config
                chmod 600 ~/.kube/config
            """
        }
    }

    def cleanupKubeconfig() {
        script.sh "rm -f ~/.kube/config"
    }

}
