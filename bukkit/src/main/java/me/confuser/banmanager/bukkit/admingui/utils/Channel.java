package me.confuser.banmanager.bukkit.admingui.utils;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import me.confuser.banmanager.bukkit.admingui.AdminGuiIntegration;
import org.bukkit.Bukkit;

public class Channel {

	public static void send(String sender, String subchannel, String... data) {

		if (AdminGuiIntegration.getInstance().getConf().getBoolean("bungeecord_enabled", false)) {
			ByteArrayDataOutput output = ByteStreams.newDataOutput();
			output.writeUTF(sender);
			output.writeUTF(subchannel);
			for (String da : data) output.writeUTF(da);
			Bukkit.getServer().sendPluginMessage(AdminGuiIntegration.getInstance().getBanManagerPlugin(), "my:admingui", output.toByteArray());
		}
	}

}
