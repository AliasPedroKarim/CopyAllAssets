package me.ascencia.copyallassets;

import me.ascencia.copyallassets.commands.CopyCommand;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.minecraft.server.command.CommandManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class CopyAllAssets implements ModInitializer {
	public static CopyAllAssets INSTANCE;
	public static String MODID = "copyallassets";

	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LogManager.getLogger(CopyAllAssets.MODID);

	@Override
	public void onInitialize() {
		LOGGER.info(CopyAllAssets.MODID + " is now initialised");

		INSTANCE = this;

		CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> {
			dispatcher.register(CommandManager.literal("copy").executes(new CopyCommand()));
		});
	}
}
