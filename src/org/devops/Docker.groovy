def docker(String project) {
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
    image = "${registryUrl}/${env.repo}/${project}:${tag}"
    islogin = false
    return this
}

def build() {
    def msg = ""
    Boolean isdockerbuild = false
    
    try {
        sh "docker build -t ${image} ."
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
            currentBuild.description = "docker tag: ${image}"
            echo "Image pushed successfully and build description set."
        } catch (Exception e) {
            // 如果push失败，则打印错误信息
            echo "Failed to push image: ${e.message}"
        }
    } else {
        echo "Login failed, cannot push image."
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
