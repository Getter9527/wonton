plugins {
    id("java")
    // application 是 Gradle 的一个核心插件（Core Plugin）
    // 目的是："把一个 JVM 项目变成一个可运行的应用程序"
    application
}

group = "com.sample"
version = "0.1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

application {
    // 设置 主程序入口
    mainClass.set("com.wonton.Application")
}

tasks.named<JavaExec>("run") {
    // 把标准输入转发给子进程，否则无法获取用户输入
    standardInput = System.`in`
}

tasks.jar {
    // 设置 jar包文件名称
    // 固定 wonton.jar 文件名，是为了让后面 jpackage 的参数稳定
    archiveFileName.set("wonton.jar")
}

val javaToolchains = the<org.gradle.jvm.toolchain.JavaToolchainService>()

tasks.register<Exec>("jpackage") {
    group = "distribution"
    description = "使用 jpackage 打包原生 exe（内嵌运行时，目标机器无需安装 Java）"
    dependsOn(tasks.jar)

    doFirst {
        // 通过工具链定位构建所用 JDK，无需 jpackage 在 PATH 中
        // launcherFor 返回 Provider<JavaLauncher>，需 get() 解开后才能访问 metadata
        val launcher = javaToolchains.launcherFor(java.toolchain).get()
        val jPackageExe = launcher.metadata.installationPath.file("bin/jpackage.exe").asFile.absolutePath
        val exeDest = layout.buildDirectory.dir("exe").get().asFile
        val iconFile = rootProject.file("icons/logo.ico")  // 图标路径
        // 先清理旧目录中的产物
        exeDest.deleteRecursively()
        val commandArgs = buildList {
            add(jPackageExe)
            // 只生成目录形式的可执行程序，不做安装器（安装器需要 WiX 工具）
            add("--type")
            add("app-image")

            add("--name")
            add("wonton")

            add("--input")
            add(tasks.jar.get().destinationDirectory.get().asFile.absolutePath)

            add("--main-jar")
            add(tasks.jar.get().archiveFileName.get())

            add("--main-class")
            add(application.mainClass.get())

            add("--dest")
            add(exeDest.absolutePath)
            // 生成控制台子系统启动器，print 的输出才能在终端显示（不加会变成窗口子系统，看不到输出）
            add("--win-console")

            // 如果图标文件存在，则添加图标参数
            if (iconFile.exists()) {
                add("--icon")
                add(iconFile.absolutePath)
            } else {
                logger.warn("由于 LOGO 图标文件未找到：${iconFile.absolutePath}，将使用默认图标")
            }
        }
        commandLine(commandArgs)
    }
}

tasks.test {
    useJUnitPlatform()
}