package com.eliaseeg.marketplace.utils;

import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import org.bson.Document;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Stolen from https://gist.github.com/graywolf336/8153678
 * Thank you everyone who posted!
 */
public class ItemStackSerializer {

    public static Document serialize(ItemStack item) {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream);
            dataOutput.writeObject(item);
            dataOutput.close();

            return new Document("serializedItem", Base64Coder.encodeLines(outputStream.toByteArray()));
        } catch (Exception e) {
            throw new RuntimeException("Unable to serialize ItemStack", e);
        }
    }

    public static ItemStack deserialize(Document doc) {
        try {
            ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64Coder.decodeLines(doc.getString("serializedItem")));
            BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream);
            
            ItemStack item = (ItemStack) dataInput.readObject();
            
            dataInput.close();
            return item;
        } catch (ClassNotFoundException | IOException e) {
            throw new RuntimeException("Unable to deserialize ItemStack", e);
        }
    }

}