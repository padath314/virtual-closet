package com.virtualcloset.app.ui;

import com.virtualcloset.app.model.CategoryType;
import com.virtualcloset.app.model.ClothingItem;
import com.virtualcloset.app.model.Outfit;
import com.virtualcloset.app.service.ClosetService;
import com.virtualcloset.app.service.ResourceUtils;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.io.InputStream;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class MainView {
    private final ClosetService closetService;
    private final StackPane previewPane = new StackPane();
    private final Label selectionSummary = new Label();
    private final TextField outfitNameField = new TextField();
    private final CheckBox favoriteCheckBox = new CheckBox("Mark as favorite");
    private final ListView<Outfit> outfitListView = new ListView<>();
    private final Map<CategoryType, FlowPane> categoryPanes = new EnumMap<>(CategoryType.class);
    private final Map<String, Image> imageCache = new HashMap<>();
    private double baseLayerDisplayW = 0;
    private double baseLayerDisplayH = 0;
    private double baseRawW = 0;
    private double baseRawH = 0;

    public MainView(ClosetService closetService) {
        this.closetService = closetService;
    }

    public Parent createContent() {
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(18));
        root.setBackground(new Background(new BackgroundFill(Color.web("#12111a"), CornerRadii.EMPTY, Insets.EMPTY)));
        root.getStylesheets().add(getClass().getResource("/dark-theme.css").toExternalForm());

        Label title = new Label("Virtual Closet");
        title.setFont(Font.font("System", FontWeight.BOLD, 26));
        title.setTextFill(Color.web("#f0eeff"));

        Label subtitle = new Label("Layer outfits, mix accessories, search by tags/colors, and save favorites.");
        subtitle.setTextFill(Color.web("#a89ec5"));

        VBox header = new VBox(4, title, subtitle);
        header.setPadding(new Insets(0, 0, 16, 0));
        root.setTop(header);

        SplitPane splitPane = new SplitPane();
        splitPane.setDividerPositions(0.38, 0.72);
        categoryPanes.clear();
        splitPane.getItems().addAll(buildSelectionPanel(), buildPreviewPanel(), buildSavedPanel());
        root.setCenter(splitPane);
        refreshPreview();
        refreshSavedOutfits();
        return root;
    }

    private Parent buildSelectionPanel() {
        VBox container = new VBox(14);
        container.setPadding(new Insets(16));
        container.setPrefWidth(470);
        container.setBackground(cardBackground());
        container.setBorder(cardBorder());

        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        tabPane.setStyle("-fx-background-color: #1e1c2b; -fx-tab-header-background: #1e1c2b;");
        for (CategoryType categoryType : CategoryType.values()) {
            FlowPane itemsPane = new FlowPane();
            itemsPane.setHgap(10);
            itemsPane.setVgap(10);
            categoryPanes.put(categoryType, itemsPane);
            populateItemsPane(itemsPane, closetService.getItemsForCategory(categoryType));

            ScrollPane tabScroll = new ScrollPane(itemsPane);
            tabScroll.setFitToWidth(true);
            tabScroll.setStyle("-fx-background-color: #1e1c2b;");
            VBox.setVgrow(tabScroll, Priority.ALWAYS);

            TextField categorySearch = new TextField();
            categorySearch.setPromptText("Search " + categoryType.getDisplayName().toLowerCase() + " by name, color, tag…");
            categorySearch.setStyle("-fx-background-color: #27243a; -fx-text-fill: #f0eeff; -fx-prompt-text-fill: #a89ec5; -fx-background-radius: 8;");
            categorySearch.textProperty().addListener((obs, oldVal, newVal) -> {
                List<ClothingItem> filtered = closetService.getItemsForCategory(categoryType).stream()
                        .filter(item -> item.matchesQuery(newVal))
                        .toList();
                populateItemsPane(itemsPane, filtered);
            });

            VBox tabContent = new VBox(8, tabScroll, categorySearch);
            tabContent.setPadding(new Insets(8, 4, 4, 4));
            VBox.setVgrow(tabScroll, Priority.ALWAYS);

            String tabLabel = categoryType.getDisplayName() + (categoryType.isMultiSelect() ? " ✦" : "");
            Tab tab = new Tab(tabLabel, tabContent);
            tabPane.getTabs().add(tab);
        }

        Button clearButton = new Button("Clear outfit");
        clearButton.setStyle("-fx-background-color: #3a3550; -fx-text-fill: #f0eeff; -fx-background-radius: 8; -fx-cursor: hand;");
        clearButton.setOnAction(event -> {
            closetService.clearSelections();
            refreshPreview();
            refreshAllItemPanes();
        });

        container.getChildren().addAll(sectionTitle("Wardrobe"), tabPane, clearButton);
        VBox.setVgrow(tabPane, Priority.ALWAYS);
        return container;
    }

    private Parent buildPreviewPanel() {
        VBox container = new VBox(14);
        container.setPadding(new Insets(16));
        container.setBackground(cardBackground());
        container.setBorder(cardBorder());
        container.setAlignment(Pos.TOP_CENTER);

        Label title = sectionTitle("Outfit Builder");
        previewPane.setMinSize(200, 300);
        previewPane.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        // Clip so clothing layers never visually overflow the canvas box
        javafx.scene.shape.Rectangle clip = new javafx.scene.shape.Rectangle();
        clip.widthProperty().bind(previewPane.widthProperty());
        clip.heightProperty().bind(previewPane.heightProperty());
        previewPane.setClip(clip);
        previewPane.setBackground(new Background(new BackgroundFill(Color.web("#1a1826"), new CornerRadii(18), Insets.EMPTY)));
        previewPane.setBorder(new Border(new BorderStroke(Color.web("#3a3550"), BorderStrokeStyle.SOLID, new CornerRadii(18), new BorderWidths(1.2))));

        outfitNameField.setPromptText("Outfit name");
        outfitNameField.setStyle("-fx-background-color: #27243a; -fx-text-fill: #f0eeff; -fx-prompt-text-fill: #a89ec5; -fx-background-radius: 8;");
        favoriteCheckBox.setStyle("-fx-text-fill: #f0eeff;");
        Button saveButton = new Button("Save outfit");
        saveButton.setStyle("-fx-background-color: #7a5cff; -fx-text-fill: #ffffff; -fx-background-radius: 8; -fx-cursor: hand;");
        saveButton.setOnAction(event -> saveCurrentOutfit());
        HBox saveRow = new HBox(10, outfitNameField, favoriteCheckBox, saveButton);
        saveRow.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(outfitNameField, Priority.ALWAYS);

        container.getChildren().addAll(title, previewPane, saveRow);
        VBox.setVgrow(previewPane, Priority.ALWAYS);
        return container;
    }

    private Parent buildSavedPanel() {
        VBox container = new VBox(14);
        container.setPadding(new Insets(16));
        container.setPrefWidth(320);
        container.setBackground(cardBackground());
        container.setBorder(cardBorder());

        outfitListView.setCellFactory(list -> new ListCell<>() {
            @Override
            protected void updateItem(Outfit outfit, boolean empty) {
                super.updateItem(outfit, empty);
                if (empty || outfit == null) {
                    setGraphic(null);
                    return;
                }
                Label name = new Label(outfit.getName() + (outfit.isFavorite() ? " ★" : ""));
                name.setFont(Font.font("System", FontWeight.SEMI_BOLD, 14));
                name.setTextFill(Color.web("#f0eeff"));
                Label meta = new Label(outfit.getItemIds().size() + " items");
                meta.setTextFill(Color.web("#a89ec5"));
                Button load = new Button("Load");
                load.setStyle("-fx-background-color: #7a5cff; -fx-text-fill: #ffffff; -fx-background-radius: 6; -fx-cursor: hand;");
                load.setOnAction(event -> {
                    closetService.loadOutfit(outfit);
                    refreshPreview();
                    refreshAllItemPanes();
                });
                Button favorite = new Button(outfit.isFavorite() ? "Unfavorite" : "Favorite");
                favorite.setStyle("-fx-background-color: #3a3550; -fx-text-fill: #f0eeff; -fx-background-radius: 6; -fx-cursor: hand;");
                favorite.setOnAction(event -> {
                    closetService.toggleFavorite(outfit);
                    refreshSavedOutfits();
                });
                Button delete = new Button("Delete");
                delete.setStyle("-fx-background-color: #3a3550; -fx-text-fill: #f0eeff; -fx-background-radius: 6; -fx-cursor: hand;");
                delete.setOnAction(event -> {
                    closetService.deleteOutfit(outfit);
                    refreshSavedOutfits();
                });
                Region spacer = new Region();
                HBox.setHgrow(spacer, Priority.ALWAYS);
                VBox textBox = new VBox(2, name, meta);
                HBox actions = new HBox(6, load, favorite, delete);
                VBox cell = new VBox(8, new HBox(8, textBox, spacer), actions);
                cell.setPadding(new Insets(10));
                cell.setBackground(new Background(new BackgroundFill(Color.web("#27243a"), new CornerRadii(12), Insets.EMPTY)));
                cell.setBorder(new Border(new BorderStroke(Color.web("#3a3550"), BorderStrokeStyle.SOLID, new CornerRadii(12), new BorderWidths(1))));
                setGraphic(cell);
            }
        });

        container.getChildren().addAll(sectionTitle("Saved Outfits"), outfitListView);
        VBox.setVgrow(outfitListView, Priority.ALWAYS);
        return container;
    }

    private void populateItemsPane(FlowPane pane, List<ClothingItem> items) {
        pane.getChildren().clear();
        for (ClothingItem item : items) {
            pane.getChildren().add(createItemCard(item));
        }
    }

    private VBox createItemCard(ClothingItem item) {
        ImageView imageView = new ImageView(loadCached(item.getThumbnailPath(), 100, 130).orElse(null));
        imageView.setFitWidth(92);
        imageView.setFitHeight(122);
        imageView.setPreserveRatio(true);
        StackPane imageHolder = new StackPane();
        imageHolder.setPrefSize(100, 130);
        imageHolder.setBackground(new Background(new BackgroundFill(Color.web("#27243a"), new CornerRadii(12), Insets.EMPTY)));
        imageHolder.setBorder(new Border(new BorderStroke(Color.web("#3a3550"), BorderStrokeStyle.SOLID, new CornerRadii(12), new BorderWidths(1))));
        if (imageView.getImage() != null) {
            imageHolder.getChildren().add(imageView);
        } else {
            Rectangle placeholder = new Rectangle(88, 118, Color.web("#1e1c2b"));
            placeholder.setArcWidth(12);
            placeholder.setArcHeight(12);
            Label label = new Label("PNG");
            label.setTextFill(Color.web("#a89ec5"));
            imageHolder.getChildren().addAll(placeholder, label);
        }

        Label name = new Label(item.getName());
        name.setWrapText(true);
        name.setMaxWidth(110);
        name.setTextFill(Color.web("#f0eeff"));
        Label meta = new Label(item.getColors().isEmpty() ? item.getCategory().getDisplayName() : String.join(", ", item.getColors()));
        meta.setWrapText(true);
        meta.setTextFill(Color.web("#a89ec5"));
        meta.setStyle("-fx-font-size: 11px;");

        VBox card = new VBox(8, imageHolder, name, meta);
        // Show a warning badge if this item's canvas doesn't match the base
        if (baseRawW > 0) {
            loadCached(item.getImagePath(), 0, PREVIEW_HEIGHT).ifPresent(img -> {
                if (Math.abs(img.getWidth() / img.getHeight() - baseRawW / baseRawH) > 0.01) {
                    Label warn = new Label("⚠ re-export");
                    warn.setStyle("-fx-font-size:10px; -fx-text-fill:#ffaa44;");
                    card.getChildren().add(warn);
                }
            });
        }
        card.setPadding(new Insets(10));
        card.setPrefWidth(126);
        applySelectionStyle(card, item);
        card.setOnMouseClicked(event -> {
            closetService.toggleSelection(item);
            refreshPreview();
            refreshAllItemPanes();
        });
        return card;
    }

    // All Procreate PNGs share the same canvas. We decode at this height to keep memory low.
    private static final double PREVIEW_HEIGHT = 800;

    private void refreshPreview() {
        previewPane.getChildren().clear();
        Optional<Image> baseOpt = loadCached(closetService.getClosetData().getBaseImagePath(), 0, PREVIEW_HEIGHT);
        if (baseOpt.isEmpty()) {
            addEmptyPreviewState();
        } else {
            Image baseImage = baseOpt.get();
            // Calculate display size once from the base PNG and reuse for every layer.
            // This guarantees all layers sit at exactly the same absolute coordinates.
            if (baseLayerDisplayW == 0) {
                baseRawW = baseImage.getWidth();
                baseRawH = baseImage.getHeight();
                baseLayerDisplayH = Math.min(PREVIEW_HEIGHT, baseRawH);
                baseLayerDisplayW = baseLayerDisplayH * (baseRawW / baseRawH);
            }
            previewPane.getChildren().add(makeLayerView(baseImage));
            for (ClothingItem item : closetService.getSelectedItems()) {
                loadCached(item.getImagePath(), 0, PREVIEW_HEIGHT).ifPresent(img -> {
                    // Skip layers whose raw canvas doesn't match the base to avoid distortion.
                    // User needs to re-export these from Procreate at the correct canvas size.
                    if (baseRawW > 0 && (Math.abs(img.getWidth() / img.getHeight() - baseRawW / baseRawH) > 0.01)) {
                        return; // wrong canvas – silently skip, card still shows ⚠ badge
                    }
                    previewPane.getChildren().add(makeLayerView(img));
                });
            }
        }

        List<ClothingItem> selectedItems = closetService.getSelectedItems();

        String summary = selectedItems.isEmpty()
                ? "No clothing selected yet. Add your base PNG to src/main/resources/closet-data/base/base.png and drop clothing layers into the wardrobe folders."
                : selectedItems.stream()
                .collect(Collectors.groupingBy(item -> item.getCategory().getDisplayName(), Collectors.mapping(ClothingItem::getName, Collectors.joining(", "))))
                .entrySet()
                .stream()
                .sorted(Comparator.comparing(entry -> entry.getKey()))
                .map(entry -> entry.getKey() + ": " + entry.getValue())
                .collect(Collectors.joining(" • "));
        selectionSummary.setText(summary);
    }

    /** Creates an ImageView that fills the exact same pixel region as every other layer. */
    private ImageView makeLayerView(Image image) {
        ImageView iv = new ImageView(image);
        // Bind to the pane's live size as the maximum bounding box.
        // preserveRatio=true prevents elongation when the pane's aspect ratio
        // differs from the PNG's (1640x2360). Since all correctly-sized layers share
        // the same aspect ratio, JavaFX scales them identically and StackPane centers
        // them at exactly the same position — giving pixel-perfect alignment.
        iv.fitWidthProperty().bind(previewPane.widthProperty());
        iv.fitHeightProperty().bind(previewPane.heightProperty());
        iv.setPreserveRatio(true);
        return iv;
    }

    private void addEmptyPreviewState() {
        VBox emptyState = new VBox(8);
        emptyState.setAlignment(Pos.CENTER);
        Label title = new Label("Add your base model PNG");
        title.setFont(Font.font("System", FontWeight.BOLD, 18));
        title.setTextFill(Color.web("#f0eeff"));
        Label details = new Label("Expected path: src/main/resources/closet-data/base/base.png");
        details.setTextFill(Color.web("#a89ec5"));
        emptyState.getChildren().addAll(title, details);
        previewPane.getChildren().add(emptyState);
    }

    private void refreshSavedOutfits() {
        outfitListView.setItems(FXCollections.observableArrayList(closetService.getSavedOutfits()));
    }

    private void refreshAllItemPanes() {
        for (CategoryType categoryType : CategoryType.values()) {
            FlowPane pane = categoryPanes.get(categoryType);
            if (pane != null) {
                populateItemsPane(pane, closetService.getItemsForCategory(categoryType));
            }
        }
    }

    private void saveCurrentOutfit() {
        if (closetService.getSelectedItems().isEmpty()) {
            showInfo("Nothing selected", "Select at least one item before saving an outfit.");
            return;
        }
        String outfitName = Optional.ofNullable(outfitNameField.getText()).map(String::trim).filter(text -> !text.isBlank()).orElse("Untitled Outfit");
        closetService.saveCurrentOutfit(outfitName, favoriteCheckBox.isSelected());
        outfitNameField.clear();
        favoriteCheckBox.setSelected(false);
        refreshSavedOutfits();
        showInfo("Outfit saved", "Saved \"" + outfitName + "\" to your outfit list.");
    }

    private Optional<Image> loadCached(String resourcePath, double w, double h) {
        if (resourcePath == null || !ResourceUtils.resourceExists(resourcePath)) {
            return Optional.empty();
        }
        String key = resourcePath + "@" + (int) w + "x" + (int) h;
        Image cached = imageCache.get(key);
        if (cached != null) {
            return Optional.of(cached);
        }
        try (InputStream is = ResourceUtils.openResource(resourcePath)) {
            Image image = (w > 0 || h > 0) ? new Image(is, w, h, true, true) : new Image(is);
            imageCache.put(key, image);
            return Optional.of(image);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    private void applySelectionStyle(VBox card, ClothingItem item) {
        boolean selected = closetService.isSelected(item);
        card.setBackground(new Background(new BackgroundFill(selected ? Color.web("#2d2550") : Color.web("#27243a"), new CornerRadii(14), Insets.EMPTY)));
        card.setBorder(new Border(new BorderStroke(selected ? Color.web("#9d80ff") : Color.web("#3a3550"), BorderStrokeStyle.SOLID, new CornerRadii(14), new BorderWidths(selected ? 2 : 1))));
    }

    private Label sectionTitle(String value) {
        Label label = new Label(value);
        label.setFont(Font.font("System", FontWeight.BOLD, 18));
        label.setTextFill(Color.web("#f0eeff"));
        return label;
    }

    private Background cardBackground() {
        return new Background(new BackgroundFill(Color.web("#1e1c2b"), new CornerRadii(20), Insets.EMPTY));
    }

    private Border cardBorder() {
        return new Border(new BorderStroke(Color.web("#3a3550"), BorderStrokeStyle.SOLID, new CornerRadii(20), new BorderWidths(1.2)));
    }

    private void showInfo(String header, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Virtual Closet");
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
