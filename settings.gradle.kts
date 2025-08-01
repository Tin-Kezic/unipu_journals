rootProject.name = "journals"
include("data", "domain", "feature", "data-account")

dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            from(files("libs.versions.toml"))
        }
    }
    repositories {
        mavenCentral()
    }
}