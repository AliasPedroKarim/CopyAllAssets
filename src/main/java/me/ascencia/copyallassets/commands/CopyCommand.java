package me.ascencia.copyallassets.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.ascencia.copyallassets.CopyAllAssets;
import me.ascencia.copyallassets.utils.Utils;
import net.fabricmc.loader.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.resource.ResourcePack;
import net.minecraft.resource.ZipResourcePack;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.LiteralText;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Paths;
import java.util.Enumeration;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class CopyCommand implements Command<ServerCommandSource> {

    public static File newFile(File destinationDir, ZipEntry zipEntry) throws IOException {
        File destFile = new File(destinationDir, zipEntry.getName());

        String destDirPath = destinationDir.getCanonicalPath();
        String destFilePath = destFile.getCanonicalPath();

        if (!destFilePath.startsWith(destDirPath + File.separator)) {
            throw new IOException("Entry is outside of the target dir: " + zipEntry.getName());
        }

        return destFile;
    }

    @Override
    public int run(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        context.getSource().getPlayer().sendMessage(new LiteralText("info : " + MinecraftClient.getInstance().fpsDebugString), false);

        byte[] buffer = new byte[1024];

        Stream<ResourcePack> b = MinecraftClient.getInstance().getResourceManager().streamResourcePacks();

        b.forEach(resourcePack -> {
            if(resourcePack instanceof ZipResourcePack) {
                ZipResourcePack zipResourcePack = (ZipResourcePack) resourcePack;

                try {
                    Field reqField = Utils.checkFieldExist(ZipResourcePack.class, "file") ? ZipResourcePack.class.getDeclaredField("file") : null;

                    ZipFile zipFile = null;

                    if (reqField != null) {
                        reqField.setAccessible(true);
                        try {
                            zipFile = (ZipFile) reqField.get(zipResourcePack);

                            String nameFile = Paths.get(zipFile.getName()).getFileName().toString().split("\\.")[0];
                            File dirOutput = new File(String.join(File.separator, FabricLoader.INSTANCE.getGameDir().toString(), CopyAllAssets.MODID, nameFile));

                            if (zipFile != null) {
                                // create dir output if don't exist
                                if (!dirOutput.exists()) {
                                    dirOutput.mkdirs();
                                }

                                /*try {
                                    new net.lingala.zip4j.core.ZipFile(new File(zipFile.getName()))
                                            .extractAll(dirOutput.toPath().toString());
                                } catch (ZipException e) {
                                    e.printStackTrace();
                                }*/

                                Enumeration<? extends ZipEntry> entries = zipFile.entries();
                                while(entries.hasMoreElements()) {
                                    ZipEntry zipEntry = (ZipEntry)entries.nextElement();

                                    System.out.println("File found > name : " + zipEntry.getName() + " <> size : " + zipEntry.getSize() + " <> CompressedSize : " + zipEntry.getCompressedSize());

                                    // Filter
                                    // if(!zipEntry.getName().endsWith(".ogg") & !zipEntry.getName().endsWith(".png")) {}

                                    File newFile = newFile(dirOutput, zipEntry);
                                    if (zipEntry.isDirectory()) {
                                        if (!newFile.isDirectory() && !newFile.mkdirs()) {
                                            throw new IOException("Failed to create directory " + newFile);
                                        }
                                    } else {
                                        // fix for Windows-created archives
                                        File parent = newFile.getParentFile();
                                        if (!parent.isDirectory() && !parent.mkdirs()) {
                                            throw new IOException("Failed to create directory " + parent);
                                        }

                                        // write file content
                                        FileOutputStream fos = new FileOutputStream(newFile);
                                        int len;
                                        while ((len = zipFile.getInputStream(zipEntry).read(buffer)) > 0) {
                                            fos.write(buffer, 0, len);
                                        }
                                        fos.close();
                                    }
                                }
                            }
                        } catch (IllegalAccessException | IOException e) {
                            e.printStackTrace();
                        }
                    }
                } catch (NoSuchFieldException e) {
                    e.printStackTrace();
                }
            }
        });

        return 1;
    }

}
