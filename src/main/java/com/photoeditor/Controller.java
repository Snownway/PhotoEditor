package com.photoeditor;

import javafx.animation.PauseTransition;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.util.Duration;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import static com.photoeditor.App.getPrimaryStage;
import static com.photoeditor.Filters.setBinaryImage;
import static com.photoeditor.Filters.setBlurImage;
import static com.photoeditor.Filters.setBrightnessImage;
import static com.photoeditor.Filters.setContrastImage;
import static com.photoeditor.Filters.setSaturationImage;
import static com.photoeditor.Filters.setSharpenImage;
import static com.photoeditor.Filters.setTemperatureImage;

public class Controller {

    @FXML
    private StackPane stackPaneToDraw;

    @FXML
    private AnchorPane anchorPane;

    @FXML
    private ScrollPane filterScrollPane,
            imageViewScrollPane;

    @FXML
    private VBox vBoxFilterScrollPane,
            vBoxImageViewScrollPane,
            vBoxToDraw,
            vBoxCanvas;

    @FXML
    private HBox hBoxLeftTools,
            hBoxRightTools;

    @FXML
    private ImageView imageView;

    @FXML
    private ImageView drawImageView;

    @FXML
    private MenuItem menuItemSaveImage,
            menuItemOpenImage;

    @FXML
    private Canvas canvas;

    @FXML
    private Slider brightSlider,
            contrastSlider,
            saturationSlider,
            temperatureSlider,
            sharpnessSlider,
            zoomSlider,
            blackWhiteSlider,
            blurSlider,
            lineThicknessSlider;

    @FXML
    private Label percentOpenLabel,
            brightnessLabel,
            contrastLabel,
            saturationLabel,
            temperatureLabel,
            sharpnessLabel,
            blackWhiteLabel,
            blurLabel,
            widthLine;

    @FXML
    private Button buttonChangeImage,
            zoomInButton,
            zoomOutButton,
            buttonDraw,
            applyFilter;

    @FXML
    private ColorPicker colorPicker;

    private final Photo photo = new Photo();
    private double percentOpenImage;
    private ArrayList<Label> labelSliders;
    private ArrayList<Slider> sliders;

    @FXML
    private void initialize(){
        labelSliders = new ArrayList<>(Arrays.asList(brightnessLabel, contrastLabel, saturationLabel, temperatureLabel, sharpnessLabel, blackWhiteLabel, blurLabel));
        sliders = new ArrayList<>(Arrays.asList(brightSlider, contrastSlider, saturationSlider, temperatureSlider, sharpnessSlider, blurSlider, blackWhiteSlider));
    }

    private boolean settingStartingParameters = false;
    private void settingStartingParameters() {
        buttonChangeImage.setVisible(true);
        buttonDraw.setVisible(true);
        zoomOutButton.setVisible(true);
        zoomSlider.setVisible(true);
        zoomInButton.setVisible(true);
        menuItemSaveImage.setDisable(false);
        settingStartingParameters = true;

        getPrimaryStage().getScene().widthProperty().addListener((_) -> updateInterfaceDisplayedImage());
        getPrimaryStage().getScene().heightProperty().addListener((_) -> updateInterfaceDisplayedImage());
        zoomSlider.valueProperty().addListener((_) -> applyZoom(percentOpenImage, zoomSlider.getValue()));
    }

    @FXML
    private void actionOpenImage() throws IOException {
        photo.openImage();
        if (photo.getOriginalImage() == null){
            return;
        }
        imageView.setImage(photo.getDisplayedImage());
        drawImageView.setImage(photo.getDisplayedImage());
        if (!settingStartingParameters) {
            settingStartingParameters();
        }
        resetSlidersAndLabels();
        updateInterfaceDisplayedImage();
    }

    @FXML
    private void actionSaveImage() {
        photo.saveImage();
    }

    @FXML
    private void actionZoomIn() {
        double newPercentOpenImage = percentOpenImage + 0.1 * percentOpenImage;
        if (percentOpenImage < 100 && percentOpenImage > 88) {
            newPercentOpenImage = 100;
        }
        if (newPercentOpenImage > 800) {
            newPercentOpenImage = 800;
        }
        applyZoom(percentOpenImage, newPercentOpenImage);
    }

    @FXML
    private void actionZoomOut() {
        double newPercentOpenImage = percentOpenImage - 0.1 * percentOpenImage;
        if (percentOpenImage > 100 && percentOpenImage <= 110) {
            newPercentOpenImage = 100;
        }
        if (newPercentOpenImage < 1) {
            newPercentOpenImage = 1;
        }
        applyZoom(percentOpenImage, newPercentOpenImage);
    }

    private void applyZoom(double oldPercent, double newPercent) {
        imageView.setFitWidth(imageView.getFitWidth() * newPercent / oldPercent);
        imageView.setFitHeight(imageView.getFitHeight() * newPercent / oldPercent);

        updateLabelPercentOpenImageAndZoomSlider(newPercent);
    }

    private void updateInterfaceDisplayedImage() {
        if (isEditingImage) {
            imageViewScrollPane.setPrefWidth(anchorPane.getWidth() - filterScrollPane.getPrefWidth());
        } else {
            imageViewScrollPane.setPrefWidth(anchorPane.getWidth());
        }
        vBoxImageViewScrollPane.setPrefWidth(imageViewScrollPane.getPrefWidth() - 2);
        vBoxImageViewScrollPane.setPrefHeight(anchorPane.getHeight() - 52);
        imageView.setFitWidth(vBoxImageViewScrollPane.getPrefWidth());
        imageView.setFitHeight(vBoxImageViewScrollPane.getPrefHeight());
        updateLabelPercentOpenImageAndZoomSlider(percentOpenImage = calculatedPercentOpenImage(photo.getDisplayedImage()));
        updateInterfaceDrawImage();
    }

    private void updateInterfaceDrawImage() {
        stackPaneToDraw.setPrefWidth(anchorPane.getWidth());
        stackPaneToDraw.setPrefHeight(anchorPane.getHeight() - 100);
        drawImageView.setFitWidth(stackPaneToDraw.getPrefWidth());
        oldWidthCanvas = canvas.getWidth();
        oldHeightCanvas = canvas.getHeight();
        drawImageView.setFitHeight(stackPaneToDraw.getPrefHeight());
        canvas.setWidth(drawImageView.getBoundsInLocal().getWidth());
        canvas.setHeight(drawImageView.getBoundsInLocal().getHeight());
        if (isDraw) {
            GraphicsContext gc = canvas.getGraphicsContext2D();
            gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
            redrawLines(gc);
        }
    }

    private void redrawLines(GraphicsContext gc) {
        double scaleX = drawImageView.getBoundsInLocal().getWidth() / oldWidthCanvas;
        double scaleY = drawImageView.getBoundsInLocal().getHeight() / oldHeightCanvas;
        List<Line> newLines = new ArrayList<>();

        for (Line line : lines) {
            gc.setStroke(line.getColor());
            gc.beginPath();
            List<Line.Point> points = line.getPoints();

            Line newLine = new Line(line.getColor(), line.getWidth());
            newLines.add(newLine);
            int sizeListPoints = points.size();

            for (int i = 0; i < sizeListPoints; i++) {
                Line.Point point = points.get(i);
                double newX = point.x() * scaleX;
                double newY = point.y() * scaleY;

                if (i == 0) {
                    gc.moveTo(newX, newY);
                } else {
                    gc.lineTo(newX, newY);
                }
                newLine.addPoint(newX, newY);
            }
            gc.stroke();
            gc.closePath();
            lines = newLines;
        }
    }

    private int calculatedPercentOpenImage(Image image) {
        double widthRatio = (imageView.getBoundsInLocal().getWidth() / (image.getWidth() / 2));
        double heightRatio = (imageView.getBoundsInLocal().getHeight() / (image.getHeight() / 2));
        double percentOpenImage = Math.min(widthRatio, heightRatio) * 100;

        if (percentOpenImage >= 100) {
            return 100;
        }
        return (int) Math.round(percentOpenImage);
    }

    private void updateLabelPercentOpenImageAndZoomSlider(double newValuePercent) {
        percentOpenImage = newValuePercent;
        percentOpenLabel.setText((int) newValuePercent + "%");
        zoomSlider.setValue(newValuePercent);
    }

    private boolean isEditingImage = false;

    @FXML
    private void actionChangeImage() {
        if (!isEditingImage) {
            buttonChangeImage.setText("Отмена");
            updateInterfaceOnActionChangeImage(true);
            initializeListenerFilters();
            isEditingImage = true;
        } else {
            buttonChangeImage.setText("Изменить");
            updateInterfaceOnActionChangeImage(false);
            resetSlidersAndLabels();
            isEditingImage = false;
        }
        updateInterfaceDisplayedImage();
    }

    private void updateInterfaceOnActionChangeImage(boolean bool) {
        menuItemSaveImage.setDisable(bool);
        filterScrollPane.setVisible(bool);
        buttonDraw.setDisable(bool);
    }

    private void initializeListenerFilters() {
        PauseTransition pause = new PauseTransition(Duration.millis(10));
        addSliderListener(brightSlider, brightnessLabel, "brightness", pause);
        addSliderListener(contrastSlider, contrastLabel, "contrast", pause);
        addSliderListener(saturationSlider, saturationLabel, "saturation", pause);
        addSliderListener(temperatureSlider, temperatureLabel, "temperature", pause);
        addSliderListener(sharpnessSlider, sharpnessLabel, "sharpness", pause);
        addSliderListener(blackWhiteSlider, blackWhiteLabel, "blackWhite", pause);
        addSliderListener(blurSlider, blurLabel, "blur", pause);
    }

    private void addSliderListener(Slider slider, Label label, String filterName, PauseTransition pause) {
        slider.valueProperty().addListener(_ -> {
            label.setText(String.valueOf((int) slider.getValue()));
            pause.stop();
            pause.setOnFinished(_ -> {
                updateAppliedFilters(filterName, (int) slider.getValue());
                photo.setDisplayedImage(photo.getCurrentImage());
                photo.setDisplayedImage(processAppliedFilters(photo.getDisplayedImage()));
                imageView.setImage(photo.getDisplayedImage());
            });
            pause.play();
        });
    }

    private final List<String> appliedFilters = new LinkedList<>();

    private void updateAppliedFilters(String filterName, int value) {
        if (value != 0) {
            if (!appliedFilters.contains(filterName)) {
                appliedFilters.add(filterName);
            }
        } else {
            appliedFilters.remove(filterName);
        }
    }

    private Image processAppliedFilters(Image changeImage) {
        for (String filterName : appliedFilters) {
            switch (filterName) {
                case "brightness" -> changeImage = setBrightnessImage((int) brightSlider.getValue(), changeImage);
                case "contrast" -> changeImage = setContrastImage((int) contrastSlider.getValue(), changeImage);
                case "saturation" -> changeImage = setSaturationImage((int) saturationSlider.getValue(), changeImage);
                case "temperature" -> changeImage = setTemperatureImage((int) temperatureSlider.getValue(), changeImage);
                case "sharpness" -> changeImage = setSharpenImage((int) sharpnessSlider.getValue(), changeImage);
                case "blackWhite" -> changeImage = setBinaryImage((int) blackWhiteSlider.getValue(), changeImage);
                case "blur" -> changeImage = setBlurImage((int) blurSlider.getValue(), changeImage);
            }
        }
        return changeImage;
    }

    private void resetSlidersAndLabels() {
        for (var slider : sliders) {
            slider.setValue(0);
        }
        for (var label : labelSliders) {
            label.setText("0");
        }
    }

    @FXML
    private void actionApplyFilterOnImage() {
        photo.setOriginalImage(processAppliedFilters(photo.getOriginalImage()));
        photo.setCurrentImage(photo.getDisplayedImage());
        actionChangeImage();
    }

    private static double oldWidthCanvas;
    private static double oldHeightCanvas;
    private static List<Line> lines = new ArrayList<>();
    private boolean isDraw = false;

    @FXML
    private void actionDrawImage() {
        if (!isDraw) {
            drawImageView.setImage(photo.getDisplayedImage());
            buttonDraw.setText("Отмена");
            updateInterfaceOnActionDrawImage(true);

            canvas.setOnMousePressed(this::startDrawing);
            canvas.setOnMouseDragged(this::continueDrawing);
            canvas.setOnMouseReleased(this::stopDrawing);

            isDraw = true;
        } else {
            buttonDraw.setText("Рисовать");
            updateInterfaceOnActionDrawImage(false);

            GraphicsContext gc = canvas.getGraphicsContext2D();
            gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());

            if (!isApplyDrawOnImage) {
                lines.clear();
            }

            canvas.setOnMousePressed(null);
            canvas.setOnMouseDragged(null);
            canvas.setOnMouseReleased(null);

            isDraw = false;
        }
    }

    private void updateInterfaceOnActionDrawImage(boolean bool) {
        buttonChangeImage.setDisable(bool);
        stackPaneToDraw.setVisible(bool);
        imageViewScrollPane.setVisible(!bool);
        hBoxLeftTools.setVisible(bool);
        hBoxRightTools.setVisible(bool);
        zoomSlider.setDisable(bool);
        zoomInButton.setDisable(bool);
        zoomOutButton.setDisable(bool);
    }

    private void startDrawing(MouseEvent event) {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.setStroke(colorPicker.getValue());
        gc.setLineWidth(lineThicknessSlider.getValue());
        gc.beginPath();
        gc.moveTo(event.getX(), event.getY());
        gc.stroke();
        Line newLine = new Line(colorPicker.getValue(), lineThicknessSlider.getValue());
        newLine.addPoint(event.getX(), event.getY());
        lines.add(newLine);
    }

    private void continueDrawing(MouseEvent event) {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.lineTo(event.getX(), event.getY());
        gc.stroke();
        lines.getLast().addPoint(event.getX(), event.getY());
    }

    private void stopDrawing(MouseEvent event) {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.lineTo(event.getX(), event.getY());
        gc.stroke();
        gc.closePath();
        lines.getLast().addPoint(event.getX(), event.getY());
    }

    private boolean isApplyDrawOnImage = false;
    private static double curWidthCanvas, curHeightCanvas;

    @FXML
    private void applyDrawImage() {
        isApplyDrawOnImage = true;
        actionDrawImage();

        curWidthCanvas = canvas.getWidth();
        curHeightCanvas = canvas.getHeight();

        photo.copyDrawLines(lines);
        photo.setDisplayedImage(applyDrawToImage(photo.getDisplayedImage(), lines));
        imageView.setImage(photo.getDisplayedImage());
        photo.setCurrentImage(photo.getDisplayedImage());

        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());

        isApplyDrawOnImage = false;
    }

    public static Image applyDrawToImage(Image image, List<Line> lines) {
        double newWidth = image.getWidth();
        double newHeight = image.getHeight();

        double scaleX = newWidth / curWidthCanvas;
        double scaleY = newHeight / curHeightCanvas;

        WritableImage writableImage = new WritableImage((int) newWidth, (int) newHeight);
        PixelReader pixelReader = image.getPixelReader();
        PixelWriter pixelWriter = writableImage.getPixelWriter();

        for (int y = 0; y < newHeight; y++) {
            for (int x = 0; x < newWidth; x++) {
                Color color = pixelReader.getColor(x, y);
                pixelWriter.setColor(x, y, color);
            }
        }

        for (Line line : lines) {
            Color lineColor = line.getColor();
            List<Line.Point> points = line.getPoints();

            for (int i = 0; i < points.size(); i++) {
                Line.Point point = points.get(i);

                double newX = point.x() * scaleX;
                double newY = point.y() * scaleY;

                if (i == 0) {
                    pixelWriter.setColor((int) newX, (int) newY, lineColor);
                } else {
                    Line.Point prevPoint = points.get(i - 1);
                    double prevX = prevPoint.x() * scaleX;
                    double prevY = prevPoint.y() * scaleY;

                    double scaleSize = Math.min(image.getWidth() / curWidthCanvas, image.getHeight() / curHeightCanvas);
                    int thickness;
                    if (scaleSize > 2) {
                        thickness = (int) Math.round(line.getWidth() * Math.min(image.getWidth() / curWidthCanvas, image.getHeight() / curHeightCanvas));
                    }
                    else {
                        thickness = (int) Math.round(line.getWidth() + 1);
                    }
                    drawLine(pixelWriter, (int) prevX, (int) prevY, (int) newX, (int) newY, lineColor, thickness, (int) newWidth, (int) newHeight);
                }
            }
        }
        lines.clear();
        return writableImage;
    }

    private static void drawLine(PixelWriter pixelWriter, int x1, int y1, int x2, int y2, Color color, int thickness, int imageWidth, int imageHeight) {
        int dx = Math.abs(x2 - x1);
        int dy = Math.abs(y2 - y1);
        int sx = x1 < x2 ? 1 : -1;
        int sy = y1 < y2 ? 1 : -1;
        int err = dx - dy;

        while (true) {
            drawPoint(pixelWriter, x1, y1, color, thickness, imageWidth, imageHeight);
            if (x1 == x2 && y1 == y2) break;
            int e2 = 2 * err;
            if (e2 > -dy) {
                err -= dy;
                x1 += sx;
            }
            if (e2 < dx) {
                err += dx;
                y1 += sy;
            }
        }
    }

    private static void drawPoint(PixelWriter pixelWriter, int x, int y, Color color, int thickness, int imageWidth, int imageHeight) {
        for (int i = -thickness / 2; i <= thickness / 2; i++) {
            for (int j = -thickness / 2; j <= thickness / 2; j++) {
                int px = x + i;
                int py = y + j;
                if (px >= 0 && px < imageWidth && py >= 0 && py < imageHeight) {
                    pixelWriter.setColor(px, py, color);
                }
            }
        }
    }

    @FXML
    private void updateLabelWidthLine() {
        widthLine.setText(String.valueOf((int) lineThicknessSlider.getValue()));
    }
}