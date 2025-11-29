pluginManagement {
  repositories {
    gradlePluginPortal()
    google()
    mavenCentral()
    // 添加阿里云镜像
    maven { url = uri("https://maven.aliyun.com/repository/gradle-plugin") }
  }
}

dependencyResolutionManagement {
  // 🔽 修改此处：允许项目级仓库或完全在settings中配置 🔽
  repositoriesMode.set(RepositoriesMode.PREFER_SETTINGS) // 改为PREFER_SETTINGS
  
  repositories {
    // 阿里云镜像 (必须添加)
    maven { url = uri("https://maven.aliyun.com/repository/public") }
    maven { url = uri("https://maven.aliyun.com/repository/google") }
    
    // 标准仓库
    google()
    mavenCentral()
    
    // PhotoView 所需仓库
    maven { url = uri("https://jitpack.io") } // 必须添加此项
  }
}

rootProject.name = "BBQ"
include(":app")
