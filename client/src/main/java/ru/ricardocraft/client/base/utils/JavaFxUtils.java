package ru.ricardocraft.client.base.utils;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Region;
import javafx.scene.shape.Rectangle;
import ru.ricardocraft.client.base.helper.EnFSHelper;
import ru.ricardocraft.client.service.launch.SkinManager;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.nio.file.NoSuchFileException;

public class JavaFxUtils {
    private JavaFxUtils() {

    }

    public static void putAvatarToImageView(SkinManager skinManager, String username, ImageView imageView) {
        int width = (int) imageView.getFitWidth();
        int height = (int) imageView.getFitHeight();
        Image head = skinManager.getScaledFxSkinHead(username, width, height);
        if (head == null) return;
        imageView.setImage(head);
    }

    public static void setRadius(Region node, double radius) {
        setRadius(node, radius, radius);
    }

    public static void setRadius(Region node, double width, double height) {
        Rectangle r = new Rectangle(30, 30);
        r.setArcWidth(width);
        r.setArcHeight(height);
        node.setClip(r); // Or setShape (?)
        node.widthProperty().addListener(p -> r.setWidth(node.getWidth()));
        node.heightProperty().addListener(p -> r.setHeight(node.getHeight()));
    }

    public static void setStaticRadius(ImageView node, double radius) {
        setStaticRadius(node, radius, radius);
    }

    public static void setStaticRadius(ImageView node, double width, double height) {
        Rectangle r = new Rectangle(node.getFitWidth(), node.getFitHeight());
        r.setArcWidth(width);
        r.setArcHeight(height);
        node.setClip(r);
    }

    public static URL getStyleUrl(String url) throws IOException {
        URL globalCss;
        try {
            globalCss = EnFSHelper.getResourceURL(url + ".bss");
        } catch (FileNotFoundException | NoSuchFileException e) {
            globalCss = EnFSHelper.getResourceURL(url + ".css");
        }
        return globalCss;
    }
}
