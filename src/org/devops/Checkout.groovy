package org.devops

class Checkout implements Serializable {
    def script  // 保存 pipeline 上下文

    Checkout(script) {
        this.script = script
    }

    def call(){
        script.checkout script.scm
    }
    
    def checkoutCustom(){
        script.checkout([
            $class: 'GitSCM',
            branches: scm.branches,
            doGenerateSubmoduleConfigurations: scm.doGenerateSubmoduleConfigurations,
            extensions: [
                // 设置 clone 参数
                [$class: 'CloneOption', depth: 1, noTags: false],
                // 添加 GitLFSPull 插件
                [$class: 'GitLFSPull']
            ],
            userRemoteConfigs: scm.userRemoteConfigs
        ])
    }

    def printNodeInfo(){
        script.echo "===== Jenkins Info ====="
        script.echo "Jenkins Node Name: ${env.K8S_NODE_NAME}"  // 节点名称
        script.echo "Jenkins Node IP: ${env.K8S_NODE_IP}"      // 节点 IP
        script.echo "=============================="
    }
}
