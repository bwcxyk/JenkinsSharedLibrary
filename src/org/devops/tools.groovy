package org.devops

// 格式化输出
def PrintMes(value,color){
    // 定义 ANSI 转义码常量
    def RESET = "\033[0m" // 重置所有样式和颜色
    def RED = "\033[1;31m" // 粗体，红色文本
    def GREEN = "\033[1;32m" // 粗体，绿色文本
    def BLUE = "\033[1;34m" // 粗体，蓝色文本
    def YELLOW = "\033[1;33m" // 粗体，黄色文本
    def MAGENTA = "\033[1;35m" // 粗体，品红色文本
    def CYAN = "\033[1;36m" // 粗体，青色文本
    def DEFAULT = "\033[1;30m" // 默认颜色（粗体，深灰色文本）

    // 定义颜色字典
    def colors = [
        'red'   : "${RED} >>>>>>>>>>>${value}<<<<<<<<<<< ${RESET}",
        'blue'  : "${BLUE} >>>>>>>>>>>${value}<<<<<<<<<<< ${RESET}",
        'green' : "${GREEN} >>>>>>>>>>>${value}<<<<<<<<<<< ${RESET}",
        'yellow': "${YELLOW} >>>>>>>>>>>${value}<<<<<<<<<<< ${RESET}",
    ]

    // 获取颜色字符串，如果颜色不存在，使用默认值
    def colorString = colors[color] ?: "${DEFAULT}${value}${RESET}"

    ansiColor('xterm') {
        echo colorString
    }
}


// 获取镜像版本
def createVersion() {
    // 定义一个版本号作为当次构建的版本，输出结果 20191210175842_69
    return new Date().format('yyyyMMddHHmmss') + "_${env.BUILD_ID}"
}


// 获取时间
def getTime() {
    // 定义一个版本号作为当次构建的版本，输出结果 20191210175842
    return new Date().format('yyyyMMddHHmmss')
}
