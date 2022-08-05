//docker对象
def docker(String repo, String tag, String credentialsId, String Dockerfile = "Dockerfile", String path = ".") {
    this.image = repo + ":" + tag
    this.repo = tag
    this.Dockerfile = Dockerfile
    this.path = path
    this.credentialsId = credentialsId
    this.islogin = false
    return this
}

//构建镜像
def  build() {
        msg = ""
        Boolean isdockerbuild = false
        //构建信息
        stage = env.STAGE_NAME + '-build'

        retry(3) {
            try {
                sh "docker build -t ${this.image} -f ${this.Dockerfile} ${this.path}"
                isdockerbuild = true
                env.CURRENT_IMAGE = this.image
            } catch (Exception e) {
                //抛出异常打印错误
                msg = e.toString()
            }
        }

        stage = env.STAGE_NAME + '-build'
    echo "++++++++++++++++++++++++++++++++++++++++++++++"

    if (isdockerbuild) {
        //通知gitlab构建成功,也用于后期发送钉钉通知
        try {
            updateGitlabCommitStatus(name: '${stage}', state: 'success')
        }catch (Exception e){
            echo "构建成功发送错误!"
        }
        //new一个Ding对象
       // new Ding().message(env.BUILD_TASKS, "${stage} success...   √")
        echo "docker build success"
    } else {
        //通知gitlab构建失败
        try{
            updateGitlabCommitStatus(name: '${stage}', state: 'failed')
        }catch(Exception e){
            echo "构建失败发送错误!"
        }
       // new Ding().message(env.BUILD_TASKS, "${stage} Failed...  x")
        echo "docker build error:" + $ { msg }
    }
    return this
}


//上传镜像
def push() {
    this.login()
    sh "docker push ${this.image}"
}


//登录仓库
def login() {
    if (this.islogin || this.credentialsId == "") {
        return this
    }
    withCredentials([usernamePassword(credentialsId: this.credentialsId, usernameVariable: 'USERNAME', passwordVariable: 'PASSWORD')])

    //获取处理后的登录地址
    String Registry = this.getRegistry()

    retry(3) {
        //抛出异常
        try {
            sh "docker login ${Registry} -u $USERNAME $PASSWORD"
            this.islogin = true
        } catch (Exception e) {
            echo "docker login error:" + ignored.toString()
        }
        return this
    }
}

//接收登录的地址
def getRegistry() {
    def login_list = this.image.split('/')
    if (login_list.size() > 0) {
        return login_list[0]
    }
}