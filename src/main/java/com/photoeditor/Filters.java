package com.photoeditor;

import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;


public class Filters {
    static Image setBrightnessImage(int brightnessValue, Image changeImage) {
        double brightnessFactor = 1.0 + 0.7 * ((double) brightnessValue / 100);

        int width = (int) changeImage.getWidth();
        int height = (int) changeImage.getHeight();

        WritableImage writableImage = new WritableImage(width, height);
        PixelWriter pixelWriter = writableImage.getPixelWriter();

        PixelReader pixelReader = changeImage.getPixelReader();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int pixel = pixelReader.getArgb(x,y);
                int alpha = (pixel >> 24) & 0xff;
                int red = Math.min(255, Math.max(0, (int) (((pixel >> 16) & 0xff) * brightnessFactor)));
                int green = Math.min(255, Math.max(0, (int) (((pixel >> 8) & 0xff) * brightnessFactor)));
                int blue = Math.min(255, Math.max(0, (int) ((pixel & 0xff) * brightnessFactor)));

                pixelWriter.setArgb(x, y, (alpha << 24) | (red << 16) | (green << 8) | blue);
            }
        }
        return writableImage;
    }

    static Image setContrastImage(int contrastValue, Image changeImage) {
        double contrastFactor = 1.0 + 0.7 * (double) contrastValue / 100;

        int width = (int) changeImage.getWidth();
        int height = (int) changeImage.getHeight();

        WritableImage writableImage = new WritableImage(width, height);
        PixelWriter pixelWriter = writableImage.getPixelWriter();

        PixelReader pixelReader = changeImage.getPixelReader();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int pixel = pixelReader.getArgb(x,y);
                int alpha = (pixel >> 24) & 0xff;
                int red = Math.min(255, Math.max(0, (int) ((((pixel >> 16) & 0xff) - 128) * contrastFactor + 128)));
                int green = Math.min(255, Math.max(0, (int) ((((pixel >> 8) & 0xff) - 128) * contrastFactor + 128)));
                int blue = Math.min(255, Math.max(0, (int) (((pixel & 0xff) - 128) * contrastFactor + 128)));

                pixelWriter.setArgb(x, y, (alpha << 24) | (red << 16) | (green << 8) | blue);
            }
        }
        return writableImage;
    }

    static Image setSaturationImage(int saturationValue, Image changeImage) {
        double saturationFactor = 1.0 + 0.7 * (double) saturationValue / 100;

        int width = (int) changeImage.getWidth();
        int height = (int) changeImage.getHeight();

        WritableImage writableImage = new WritableImage(width, height);
        PixelWriter pixelWriter = writableImage.getPixelWriter();

        PixelReader pixelReader = changeImage.getPixelReader();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int pixel = pixelReader.getArgb(x, y);
                int alpha = (pixel >> 24) & 0xff;
                int red = (pixel >> 16) & 0xff;
                int green = (pixel >> 8) & 0xff;
                int blue = pixel & 0xff;

                double[] hsl = rgbToHsl(red, green, blue);
                hsl[1] = Math.min(1.0, Math.max(0.0, hsl[1] * saturationFactor));
                int[] rgb = hslToRgb(hsl[0], hsl[1], hsl[2]);

                pixelWriter.setArgb(x, y, (alpha << 24) | (rgb[0] << 16) | (rgb[1] << 8) | rgb[2]);
            }
        }
        return writableImage;
    }

    static Image setTemperatureImage(int temperatureValue, Image changeImage) {
        double temperatureFactor = 1.0 + (double) temperatureValue / 100;

        int width = (int) changeImage.getWidth();
        int height = (int) changeImage.getHeight();

        WritableImage writableImage = new WritableImage(width, height);
        PixelWriter pixelWriter = writableImage.getPixelWriter();

        PixelReader pixelReader = changeImage.getPixelReader();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int pixel = pixelReader.getArgb(x, y);
                int alpha = (pixel >> 24) & 0xff;
                int red = Math.min(255, Math.max(0, (int) (((pixel >> 16) & 0xff) * temperatureFactor)));
                int blue = Math.min(255, Math.max(0, (int) ((pixel & 0xff) / temperatureFactor)));
                pixelWriter.setArgb(x, y, (alpha << 24) | (red << 16) | (((pixel >> 8) & 0xff) << 8) | blue);
            }
        }
        return writableImage;
    }

    static Image setSharpenImage(int sharpnessValue, Image changeImage) {
        double sharpenFactor = 1.0 + 4.0 * (double) sharpnessValue / 100;

        int width = (int) changeImage.getWidth();
        int height = (int) changeImage.getHeight();

        WritableImage writableImage = new WritableImage(width, height);
        PixelWriter pixelWriter = writableImage.getPixelWriter();

        PixelReader pixelReader = changeImage.getPixelReader();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int pixel = pixelReader.getArgb(x, y);
                int originalRed = (pixel >> 16) & 0xff;
                int originalGreen = (pixel >> 8) & 0xff;
                int originalBlue = pixel & 0xff;

                int totalRed = 0;
                int totalGreen = 0;
                int totalBlue = 0;
                int count = 0;

                double currentWidth, currentHeight;
                currentWidth = changeImage.getWidth();
                currentHeight = changeImage.getHeight();

                for (int ky = -1; ky <= 1; ky++) {
                    for (int kx = -1; kx <= 1; kx++) {
                        int nx = x + kx;
                        int ny = y + ky;

                        if (nx >= 0 && nx < currentWidth && ny >= 0 && ny < currentHeight) {
                            pixel = pixelReader.getArgb(nx, ny);
                            int red = (pixel >> 16) & 0xff;
                            int green = (pixel >> 8) & 0xff;
                            int blue = pixel & 0xff;

                            totalRed += red;
                            totalGreen += green;
                            totalBlue += blue;
                            count++;
                        }
                    }
                }

                int avgRed = totalRed / count;
                int avgGreen = totalGreen / count;
                int avgBlue = totalBlue / count;

                int sharpenedRed = Math.min(255, Math.max(0, (int) (originalRed + sharpenFactor * (originalRed - avgRed))));
                int sharpenedGreen = Math.min(255, Math.max(0, (int) (originalGreen + sharpenFactor * (originalGreen - avgGreen))));
                int sharpenedBlue = Math.min(255, Math.max(0, (int) (originalBlue + sharpenFactor * (originalBlue - avgBlue))));

                pixelWriter.setArgb(x, y, (0xff << 24) | (sharpenedRed << 16) | (sharpenedGreen << 8) | sharpenedBlue);
            }
        }
        return writableImage;
    }

    private static double[] rgbToHsl(int r, int g, int b) {
        double rf = r / 255.0;
        double gf = g / 255.0;
        double bf = b / 255.0;

        double max = Math.max(rf, Math.max(gf, bf));
        double min = Math.min(rf, Math.min(gf, bf));

        double h, s, l = (max + min) / 2.0;

        if (max == min) {
            h = s = 0.0; // achromatic
        } else {
            double d = max - min;
            s = l > 0.5 ? d / (2.0 - max - min) : d / (max + min);
            if (max == rf) {
                h = (gf - bf) / d + (gf < bf ? 6.0 : 0.0);
            } else if (max == gf) {
                h = (bf - rf) / d + 2.0;
            } else {
                h = (rf - gf) / d + 4.0;
            }
            h /= 6.0;
        }

        return new double[]{h, s, l};
    }

    private static int[] hslToRgb(double h, double s, double l) {
        double r, g, b;

        if (s == 0.0) {
            r = g = b = l; // achromatic
        } else {
            double q = l < 0.5 ? l * (1 + s) : l + s - l * s;
            double p = 2 * l - q;
            r = hueToRgb(p, q, h + 1.0 / 3.0);
            g = hueToRgb(p, q, h);
            b = hueToRgb(p, q, h - 1.0 / 3.0);
        }

        return new int[]{(int) (r * 255), (int) (g * 255), (int) (b * 255)};
    }

    private static double hueToRgb(double p, double q, double t) {
        if (t < 0) t += 1;
        if (t > 1) t -= 1;
        if (t < 1.0 / 6.0) return p + (q - p) * 6 * t;
        if (t < 1.0 / 2.0) return q;
        if (t < 2.0 / 3.0) return p + (q - p) * (2.0 / 3.0 - t) * 6;
        return p;
    }

    static Image setBinaryImage(int blackWhiteValue, Image changeImage) {
        int blackWhiteFactor = 128 + blackWhiteValue;

        int width = (int) changeImage.getWidth();
        int height = (int) changeImage.getHeight();

        WritableImage writableImage = new WritableImage(width, height);
        PixelWriter pixelWriter = writableImage.getPixelWriter();

        PixelReader pixelReader = changeImage.getPixelReader();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int pixel = pixelReader.getArgb(x, y);

                int binaryPixel = getBinaryPixel(pixel, blackWhiteFactor);

                pixelWriter.setArgb(x, y, binaryPixel);
            }
        }
        return writableImage;
    }

    private static int getBinaryPixel(int pixel, int blackWhiteFactor) {
        int alpha = (pixel >> 24) & 0xff;
        int red = (pixel >> 16) & 0xff;
        int green = (pixel >> 8) & 0xff;
        int blue = pixel & 0xff;

        // Преобразование в черно-белый с использованием порогового значения
        int gray = (int) (0.299 * red + 0.587 * green + 0.114 * blue);
        int binaryPixel;

        if (gray >= blackWhiteFactor) {
            binaryPixel = (alpha << 24) | (0xff << 16) | (0xff << 8) | 0xff; // Белый
        } else {
            binaryPixel = (alpha << 24) | (0); // Черный
        }
        return binaryPixel;
    }

    static Image setBlurImage(int blurValue, Image changeImage) {
        int blurRadius = blurValue / 25; // Задаем радиус размытия внутри функции

        int width = (int) changeImage.getWidth();
        int height = (int) changeImage.getHeight();

        WritableImage writableImage = new WritableImage(width, height);
        PixelWriter pixelWriter = writableImage.getPixelWriter();

        PixelReader pixelReader = changeImage.getPixelReader();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int totalRed = 0;
                int totalGreen = 0;
                int totalBlue = 0;
                int count = 0;

                for (int ky = -blurRadius; ky <= blurRadius; ky++) {
                    for (int kx = -blurRadius; kx <= blurRadius; kx++) {
                        int nx = x + kx;
                        int ny = y + ky;

                        if (nx >= 0 && nx < width && ny >= 0 && ny < height) {
                            int pixel = pixelReader.getArgb(nx, ny);
                            int red = (pixel >> 16) & 0xff;
                            int green = (pixel >> 8) & 0xff;
                            int blue = pixel & 0xff;

                            totalRed += red;
                            totalGreen += green;
                            totalBlue += blue;
                            count++;
                        }
                    }
                }

                int blurredRed = totalRed / count;
                int blurredGreen = totalGreen / count;
                int blurredBlue = totalBlue / count;

                int blurredPixel = (0xff << 24) | (blurredRed << 16) | (blurredGreen << 8) | blurredBlue;

                pixelWriter.setArgb(x, y, blurredPixel);
            }
        }
        return writableImage;
    }
}