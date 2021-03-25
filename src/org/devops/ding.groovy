def message(String source, String add){
    if(!source){
        source = ""
    }
    env.BUILD_TASKS = source + add + "\n&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;"
    return env.BUILD_TASKS
}