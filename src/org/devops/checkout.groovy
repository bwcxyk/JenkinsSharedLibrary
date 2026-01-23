package org.devops

def call(){
    checkout scm
}

def checkoutCustom(){
    // 打印节点信息
    printNodeInfo()
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
    // 只有 JNLP Container 可以传递系统环境变量到 Jenkins
    // 其他 Container 使用无法获取到
    echo "===== Jenkins Info ====="
    echo "Jenkins Node Name: ${env.K8S_NODE_NAME}"  // 节点名称
    echo "Jenkins Node IP: ${env.K8S_NODE_IP}"      // 节点 IP
    echo "=============================="
}
