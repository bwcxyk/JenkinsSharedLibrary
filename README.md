# JenkinsLibrary

## 使用说明
#### 配置共享库
- 系统管理 -> 系统设置 -> Global Pipeline Libraries 

#### 替换信息
- Maven配置 -> [resources/config/settings.xml]
- Docker仓库 -> [src/org/devops/docker.groovy]
  

## 目录结构

共享库中存储的每个文件都是一个groovy的类，每个文件（类）中包含一个或多个方法，每个方法包含groovy语句块。
Shared Library遵循固定的代码目录结构：

```
+- src                     # Groovy source files
|   +- org
|       +- foo
|           +- Bar.groovy  # for org.foo.Bar class
+- vars
|   +- foo.groovy          # for global 'foo' variable
|   +- foo.txt             # help for 'foo' variable
+- resources               # resource files (external libraries only)
|   +- org
|       +- foo
|           +- bar.json    # static helper data for org.foo.Bar
```

src目录：

- 标准的Java源目录结构，存放编写的groovy类，执行流水线时，此目录将添加到类路径
- 存放一些特定的功能实现，文件格式为.groovy

vars目录：

- 存放可从Pipeline访问的全局脚本(标准化脚本)，这些脚本文件在流水线中作为变量公开
- 使用驼峰（camelCased）命名方式，文件格式为.groovy
- 在.groovy文件中，可以通过import的方式，引入src目录的类库

resources目录：

- 从外部库中使用步骤来加载相关联的非Groovy文件

doc目录：

- 存放pipeline的相关文档说明
- 一般包含ReadMe.md文件
