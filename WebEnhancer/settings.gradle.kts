rootProject.name = "BanManagerWebEnhancer"

include(":BanManagerWebEnhancerCommon")
include(":BanManagerWebEnhancerBukkit")
include(":BanManagerWebEnhancerLibs")

project(":BanManagerWebEnhancerCommon").projectDir = file("common")
project(":BanManagerWebEnhancerBukkit").projectDir = file("bukkit")
project(":BanManagerWebEnhancerLibs").projectDir = file("libs")
