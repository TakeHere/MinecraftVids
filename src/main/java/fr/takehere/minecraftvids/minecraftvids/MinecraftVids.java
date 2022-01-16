package fr.takehere.minecraftvids.minecraftvids;

import com.destroystokyo.paper.ParticleBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.plugin.java.JavaPlugin;
import org.jcodec.api.FrameGrab;
import org.jcodec.api.JCodecException;
import org.jcodec.common.DemuxerTrack;
import org.jcodec.common.io.NIOUtils;
import org.jcodec.common.model.Picture;
import org.jcodec.scale.AWTUtil;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class MinecraftVids extends JavaPlugin {

    Location spawnLocation;
    File video = new File(this.getDataFolder().getAbsolutePath() + File.separator + "video.mp4");
    int totalFrames = 0;
    List<Picture> pictures = new ArrayList<>();

    @Override
    public void onEnable() {
        spawnLocation = new Location(Bukkit.getWorld("world"),0,400, 0);

        Thread t = new Thread(() -> {
            try {
                FrameGrab grab = FrameGrab.createFrameGrab(NIOUtils.readableChannel(video));
                DemuxerTrack vt = grab.getVideoTrack();
                totalFrames = vt.getMeta().getTotalFrames();

                for (int i = 0; i < totalFrames; i++) {
                    Picture picture = FrameGrab.getFrameFromFile(video, i);
                    pictures.add(picture);
                    System.out.println(i + "/" + totalFrames);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JCodecException e) {
                e.printStackTrace();
            }
            System.out.println("Frames grabbing ended, start rendering");
            render();
        });
        t.start();
    }

    ParticleBuilder particleBuilder = new ParticleBuilder(Particle.REDSTONE);
    int currentImage = 0;

    public void render(){
        Bukkit.getScheduler().scheduleAsyncRepeatingTask(this, new Runnable() {
            @Override
            public void run() {
                BufferedImage bi = AWTUtil.toBufferedImage(pictures.get(currentImage));
                for (int x = 0; x < bi.getWidth(); x++) {
                    for (int y = 0; y < bi.getHeight(); y++) {
                        Location newLocation = new Location(spawnLocation.getWorld(), spawnLocation.getX() + x, spawnLocation.getY() - y, spawnLocation.getZ());
                        particleBuilder.color(getColorAt(bi, x, y), 10f).location(newLocation).spawn();
                    }
                }

                currentImage++;
            }
        },0,3);
    }

    public Color getColorAt(BufferedImage image, int x, int y){
        int clr = image.getRGB(x, y);
        int red =   (clr & 0x00ff0000) >> 16;
        int green = (clr & 0x0000ff00) >> 8;
        int blue =   clr & 0x000000ff;

        return Color.fromRGB(red, green, blue);
    }
}
