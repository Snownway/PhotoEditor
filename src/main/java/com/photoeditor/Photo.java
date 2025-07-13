package com.photoeditor;

import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.stage.FileChooser;
import net.coobird.thumbnailator.Thumbnails;
import net.coobird.thumbnailator.resizers.configurations.Antialiasing;
import net.coobird.thumbnailator.resizers.configurations.Rendering;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Photo {
    private Image displayedImage;
    private Image currentImage;
    private Image originalImage;
    private final List<Line> drawLines = new ArrayList<>();

    public Image getDisplayedImage() {
        return displayedImage;
    }

    public Image getCurrentImage() {
        return currentImage;
    }

    public Image getOriginalImage() {
        return  originalImage;
    }

    public void setCurrentImage(Image image) {
        currentImage = image;
    }

    public void setDisplayedImage(Image image) {
        displayedImage = image;
    }

    public void setOriginalImage(Image image) {
        originalImage = image;
    }


    void openImage() throws IOException {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Image File");
        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg"));

        File selectedFile = fileChooser.showOpenDialog(App.getPrimaryStage());

        if (selectedFile != null) {
            originalImage = new Image(selectedFile.toURI().toString());
            currentImage = new Image(selectedFile.toURI().toString());
            if (originalImage.getWidth() > 1920 && originalImage.getHeight() > 1080) {
                compressImage(selectedFile);
            }
            displayedImage = currentImage;
        }
        drawLines.clear();
    }

    void saveImage() {
        if (displayedImage == null) {
            return;
        }

        originalImage = Controller.applyDrawToImage(originalImage, drawLines);

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Сохранить фото");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("JPG", "*.jpg"),
                new FileChooser.ExtensionFilter("JPEG", "*.jpeg"),
                new FileChooser.ExtensionFilter("PNG", "*.png")
        );

        File file = fileChooser.showSaveDialog(null);
        if (file != null) {
            try {
                BufferedImage bufferedImage = convertFXImageToBufferedImage(originalImage, file.getName().substring(file.getName().lastIndexOf('.') + 1));
                ImageIO.write(bufferedImage, file.getName().substring(file.getName().lastIndexOf('.') + 1), file);
            } catch (IOException e) {
                System.out.println("Произошла ошибка");
            }
        }
    }

    private BufferedImage convertFXImageToBufferedImage(Image image, String format) {
        int width = (int) image.getWidth();
        int height = (int) image.getHeight();
        BufferedImage bufferedImage;

        if ("jpg".equalsIgnoreCase(format) || "jpeg".equalsIgnoreCase(format)) {
            bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        } else {
            bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        }

        PixelReader pixelReader = image.getPixelReader();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int argb = pixelReader.getArgb(x, y);
                bufferedImage.setRGB(x, y, argb);
            }
        }

        return bufferedImage;
    }

    private void compressImage(File selectedFile) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Thumbnails.of(selectedFile)
                .size(1920, 1080)
                .outputQuality(1.0)
                .outputFormat("jpg")
                .rendering(Rendering.QUALITY)
                .antialiasing(Antialiasing.ON)
                .toOutputStream(outputStream);

        ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());
        currentImage = new Image(inputStream);
    }

    void copyDrawLines(List<Line> lines) {
        this.drawLines.addAll(lines);
    }
}
