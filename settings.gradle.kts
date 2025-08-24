rootProject.name = "BanManager"

include(":BanManagerCommon")
include(":BanManagerLibs")
include(":BanManagerBukkit")
include(":BanManagerVelocity")
include(":BanManagerBungee")
include(":AdminGUI")

if (startParameter.projectProperties["includeSponge"]?.toBoolean() == true) {
  include(":BanManagerSponge")
}
if (startParameter.projectProperties["includeFabric"]?.toBoolean() == true) {
  include(":BanManagerFabric")
}

project(":BanManagerCommon").projectDir = file("common")
project(":BanManagerLibs").projectDir = file("libs")
project(":BanManagerBukkit").projectDir = file("bukkit")
project(":BanManagerVelocity").projectDir = file("velocity")
project(":BanManagerBungee").projectDir = file("bungee")
project(":AdminGUI").projectDir = file("admingui")
if (rootProject.children.any { it.name == "BanManagerSponge" }) {
  project(":BanManagerSponge").projectDir = file("sponge")
}
if (rootProject.children.any { it.name == "BanManagerFabric" }) {
  project(":BanManagerFabric").projectDir = file("fabric")
}
