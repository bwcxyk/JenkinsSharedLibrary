import com.sun.xml.internal.bind.v2.TODO
import org.yaml.snakeyaml.Yaml
//部署k8s服务
def getObject(String resourcePath, Boolean wtach=true, String workloadFilePath) {
    this.wtach = wtach
    this.workloadFilePath = workloadFilePath
    echo "我开始执行deploy对象+++++++++++++++++++++++++++++++++++"
    this.resourcePath = resourcePath
    //判断传出的更改路径是否为空,workloadFilePath
    if (!resourcePath && !workloadFilePath){
        throw Exception('illegal resource path')
    }
    this.msg = new Ding()
    echo "直接结束拿到deploy对象+++++++++++++++++++++++++++++++++++"
    return this
}

//替换deploy.yaml文件里的镜像地址
def start() {
    try {
        //env.CURRENT_IMAGE用来存储当前构建的镜像地址，需要在Docker.groovy中设置值
        sh "sed -i 's#{{IMAGE_URL}}#${env.CURRENT_IMAGE}#g' ${this.resourcePath}/*"
        //部署pod
        sh "kubectl apply -f ${this.resourcePath}"
    } catch (Exception e) {
        updateGitlabCommitStatus(name: env.STAGE_NAME, state: 'failed')
        this.msg.message(env.BUILD_TASKS, "${env.stage_name} NO...  x")
        throw exc
    }

    //判断如果this.wtach等于true代表pod检测成功
    if (this.wtach){
        //TODO
        updateGitlabCommitStatus(name: env.STAGE_NAME, state: 'success')
        this.msg.message(env.BUILD_TASKS, "${env.stage_name} OK...  √")
    }else {
    }
}

//验证部署是否成功pod是否为runing
def read_yaml(){
    try {
        //读取yaml文件
        def content = readFile this.workloadFilePath

        //new一个yaml对象
        Yaml parser = new Yaml()
        //调用yaml对象的load方法读取文件
        def data = parser.load(content)
        def kind = data['kind']
        if(!kind){
            throw Exception('')
        }
    }catch(Exception e){

    }
}


