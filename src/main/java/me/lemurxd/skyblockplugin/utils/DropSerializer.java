package me.lemurxd.skyblockplugin.utils;

import me.lemurxd.skyblockplugin.constructors.DropEntry;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

public class DropSerializer {

    public static String serialize(List<DropEntry> drops) {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream);

            dataOutput.writeInt(drops.size());

            for (DropEntry entry : drops) {
                dataOutput.writeObject(entry.dropItem()); // Zapisuje ItemStack
                dataOutput.writeDouble(entry.chance());
                dataOutput.writeBoolean(entry.enabled());
            }

            dataOutput.close();
            return Base64Coder.encodeLines(outputStream.toByteArray());
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    public static List<DropEntry> deserialize(String data) {
        List<DropEntry> drops = new ArrayList<>();
        if (data == null || data.isEmpty()) return drops;

        try {
            ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64Coder.decodeLines(data));
            BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream);

            int size = dataInput.readInt();

            for (int i = 0; i < size; i++) {
                org.bukkit.inventory.ItemStack item = (org.bukkit.inventory.ItemStack) dataInput.readObject();
                double chance = dataInput.readDouble();
                boolean enabled = dataInput.readBoolean();

                drops.add(new DropEntry(item, chance, enabled));
            }

            dataInput.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return drops;
    }
}
