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
    def nodeName = sh(
        script: 'set +x; printf "%s" "$K8S_NODE_NAME"',
        returnStdout: true
    ).trim()

    def nodeIp = sh(
        script: 'set +x; printf "%s" "$K8S_NODE_IP"',
        returnStdout: true
    ).trim()

    echo "===== Jenkins Info ====="
    echo "Jenkins Node Name: ${nodeName}"   // 节点名称
    echo "Jenkins Node IP: ${nodeIp}"       // 节点 IP
    echo "=============================="
}
