package fr.takehere.minecraftvids.minecraftvids;

import com.destroystokyo.paper.ParticleBuilder;
import it.unimi.dsi.fastutil.Hash;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.plugin.java.JavaPlugin;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class MinecraftVids extends JavaPlugin {

    int currentImage = 0;

    @Override
    public void onEnable() {
        Location spawnLocation = new Location(Bukkit.getWorld("world"),0,400, 0);
        List<Image> images = new ArrayList<>();

        new Thread(() -> {
            try {
                File dir = new File(this.getDataFolder().getAbsolutePath());
                File[] files = dir.listFiles();

                for (int i = 0; i < files.length; i++) {
                    System.out.println(i + "/" + files.length);

                    File file = files[i];

                    BufferedImage bi = ImageIO.read(file);
                    Image image = new Image();

                    for (int x = 0; x < bi.getWidth(); x++) {
                        for (int y = 0; y < bi.getHeight(); y++) {
                            int clr = bi.getRGB(x, y);
                            int red =   (clr & 0x00ff0000) >> 16;
                            int green = (clr & 0x0000ff00) >> 8;
                            int blue =   clr & 0x000000ff;

                            Color pixelColor = Color.fromRGB(red, green, blue);
                            image.pixels.put(new Vector2(x, y), pixelColor);
                        }
                    }

                    images.add(image);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).run();

        ParticleBuilder particleBuilder = new ParticleBuilder(Particle.REDSTONE);

        Bukkit.getScheduler().scheduleAsyncRepeatingTask(this, new Runnable() {
            @Override
            public void run() {
                Image image = images.get(currentImage);
                for (Map.Entry<Vector2, Color> vector2ColorEntry : image.pixels.entrySet()) {
                    Color color = vector2ColorEntry.getValue();
                    Vector2 vector2 = vector2ColorEntry.getKey();
                    Location newLocation = new Location(spawnLocation.getWorld(), spawnLocation.getX() + (vector2.x), spawnLocation.getY() - vector2.y, spawnLocation.getZ());

                    particleBuilder.color(color, 10f).location(newLocation).spawn();
                }

                currentImage++;
            }
        },0,5);
    }

    @Override
    public void onDisable() {

    }
}
