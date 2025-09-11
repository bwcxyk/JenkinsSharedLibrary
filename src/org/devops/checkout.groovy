package org.devops

class Checkout implements Serializable {
    def call(){
        checkout scm
    }
    
    def checkoutCustom(){
        checkout([
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
        echo "===== Jenkins Info ====="
        echo "Jenkins Node Name: ${env.K8S_NODE_NAME}"  // 节点名称
        echo "Jenkins Node IP: ${env.K8S_NODE_IP}"      // 节点 IP
        echo "=============================="
    }
}
