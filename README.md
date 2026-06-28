
## 项目结构

+ **src：** 所有源代码目录，包括主代码和测试代码。
+ **scripts：** 自定义构建或部署脚本，若对项目运行有实际作用则应提交。
+ **README.md：** 项目说明和开源协议，提升项目可读性和合规性。
+ **LICENSE：** 开源协议说明
+ **.gitignore：** 忽略规则文件，防止误提交无用或敏感文件。
+ **build.gradle.kts：** Gradle 构建脚本，定义依赖、插件、任务等。
+ **settings.gradle.kts：** 项目设置文件，声明子模块或项目名称。
+ **gradlew 和 gradlew.bat：** Gradle Wrapper 脚本，确保团队成员使用统一版本构建项目。
+ **gradle/wrapper/gradle-wrapper.jar：** Gradle Wrapper 的可执行核心包，由 Gradle 自动生成，负责下载、缓存并启动指定版本的 Gradle。
+ **gradle/wrapper/gradlew.properties：** Gradle Wrapper 配置文件，定义下载的 Gradle 版本。