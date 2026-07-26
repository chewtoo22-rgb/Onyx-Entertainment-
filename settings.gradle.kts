pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "onyx-av-hub"

include(
    ":app",
    ":core-common",
    ":core-data",
    ":core-media",
    ":core-video-gl",
    ":core-dsp",
    ":feature-audio-hub",
    ":feature-player",
)
