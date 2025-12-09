rootProject.name = "BanManager"

include("common")
project(":common").name = "BanManagerCommon"

include("bukkit")
include("bungee")
include("velocity")
include("sponge")
include("fabric")

include("libs")
project(":libs").name = "BanManagerLibs"

include("admingui")
project(":admingui").name = "AdminGUI"






