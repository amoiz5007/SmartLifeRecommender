import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.paint.CycleMethod;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.Glow;
import javafx.scene.effect.InnerShadow;
import javafx.stage.Stage;
import javafx.stage.Modality;
import javafx.stage.StageStyle;
import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;
import javafx.animation.Timeline;
import javafx.animation.KeyFrame;
import javafx.util.Duration;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.scene.shape.Rectangle;
import javafx.scene.web.WebView;
import javafx.scene.web.WebEngine;
import javafx.beans.binding.Bindings;

import java.awt.Desktop;
import java.io.File;
import java.net.URI;
import java.util.*;

public class SmartLifeRecommenderFX extends Application {
    
    // Color scheme - Netflix style (Red and Dark)
    private static final Color TEXT_COLOR = Color.rgb(255, 255, 255);
    private static final Color RED_ACCENT = Color.rgb(229, 9, 20);
    private static final Color RED_LIGHT = Color.rgb(255, 100, 100);
    private static final Color SIDEBAR_BG = Color.rgb(20, 20, 30);
    private static final Color SIDEBAR_HOVER = Color.rgb(229, 9, 20, 0.8);
    
    // Data structure
    private Map<String, Map<String, List<Recommendation>>> data = new HashMap<>();
    private Map<String, Set<String>> genres = new HashMap<>();
    
    // UI Components
    private BorderPane root;
    private VBox contentArea;
    private VBox sidebar;
    private ImageView logoView;
    private Button toggleButton;
    private String currentView = "Home";
    private String currentCategory = "";
    private String currentGenre = "";
    private Stage primaryStage;
    private boolean sidebarVisible = false;
    
    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        
        // Initialize data
        seedGenres();
        seedData();
        
        // Show video intro first
        showVideoIntro();
    }
    
    private void showVideoIntro() {
        try {
            // Create intro scene
            StackPane introPane = new StackPane();
            introPane.setStyle("-fx-background-color: black;");
            
            // Load and play video
            File videoFile = new File("introofapp.mp4");
            if (videoFile.exists()) {
                Media media = new Media(videoFile.toURI().toString());
                MediaPlayer mediaPlayer = new MediaPlayer(media);
                MediaView mediaView = new MediaView(mediaPlayer);
                
                // Set video size to be responsive
                mediaView.fitWidthProperty().bind(primaryStage.widthProperty());
                mediaView.fitHeightProperty().bind(primaryStage.heightProperty());
                mediaView.setPreserveRatio(true);
                
                introPane.getChildren().add(mediaView);
                
                Scene introScene = new Scene(introPane, 1400, 900);
                primaryStage.setTitle("Smart Life Recommender");
                primaryStage.setScene(introScene);
                primaryStage.show();
                
                // Play video
                mediaPlayer.play();
                
                // When video ends, show main app
                mediaPlayer.setOnEndOfMedia(() -> {
                    mediaPlayer.dispose();
                    Platform.runLater(this::initializeMainApp);
                });
                
                // Skip video on click
                introPane.setOnMouseClicked(e -> {
                    mediaPlayer.stop();
                    mediaPlayer.dispose();
                    initializeMainApp();
                });
                
            } else {
                // If video not found, go directly to main app
                System.out.println("Video file not found: introofapp.mp4");
                initializeMainApp();
            }
        } catch (Exception e) {
            System.out.println("Error loading video: " + e.getMessage());
            initializeMainApp();
        }
    }
    
    private void initializeMainApp() {
        // Create main layout with gradient background
        root = new BorderPane();
        
        // Create gradient background
        LinearGradient gradient = new LinearGradient(
            0, 0, 1, 1, true, CycleMethod.NO_CYCLE,
            new Stop(0, Color.rgb(15, 15, 25)),
            new Stop(0.5, Color.rgb(25, 25, 35)),
            new Stop(1, Color.rgb(35, 35, 45))
        );
        
        BackgroundFill backgroundFill = new BackgroundFill(gradient, null, null);
        Background background = new Background(backgroundFill);
        root.setBackground(background);
        
        // Create toggle button for sidebar
        toggleButton = createToggleButton();
        root.setTop(toggleButton);
        
        // Create content area WITHOUT ScrollPane - everything fits in window
        contentArea = new VBox();
        contentArea.setAlignment(Pos.TOP_CENTER);
        
        // Bind content area to available space (window height minus toggle button)
        contentArea.prefHeightProperty().bind(primaryStage.heightProperty().subtract(60)); // Subtract toggle button height
        contentArea.prefWidthProperty().bind(primaryStage.widthProperty());
        
        // Responsive padding that scales with window size
        contentArea.paddingProperty().bind(Bindings.createObjectBinding(() -> {
            double width = primaryStage.getWidth();
            double height = primaryStage.getHeight();
            double padding = Math.max(10, Math.min(30, width * 0.015));
            return new Insets(padding);
        }, primaryStage.widthProperty(), primaryStage.heightProperty()));
        
        // Responsive spacing that scales with available height
        contentArea.spacingProperty().bind(Bindings.createDoubleBinding(() -> {
            double height = contentArea.getPrefHeight();
            return Math.max(10, height * 0.02);
        }, contentArea.prefHeightProperty()));
        
        root.setCenter(contentArea);
        
        // Load initial content directly (NO LOADING SCREEN)
        loadHomeContentDirect();
        
        // Create scene with enhanced styling
        Scene scene = new Scene(root, 1400, 900);
        scene.getStylesheets().add("data:text/css," + getCustomCSS());
        
        // Make window resizable and responsive
        primaryStage.setMinWidth(1000);
        primaryStage.setMinHeight(700);
        primaryStage.setTitle("Smart Life Recommender");
        primaryStage.setScene(scene);
        primaryStage.show();
    }
    
    private Button createToggleButton() {
        Button toggle = new Button("☰");
        toggle.setPrefSize(60, 50);
        toggle.setMaxSize(60, 50);
        toggle.setMinSize(60, 50);
        toggle.setCursor(Cursor.HAND);
        
        // Enhanced styling for toggle button
        LinearGradient toggleGradient = new LinearGradient(
            0, 0, 1, 0, true, CycleMethod.NO_CYCLE,
            new Stop(0, Color.rgb(229, 9, 20, 0.9)),
            new Stop(1, Color.rgb(180, 0, 0, 0.9))
        );
        BackgroundFill toggleFill = new BackgroundFill(toggleGradient, new CornerRadii(0, 0, 15, 0, false), null);
        toggle.setBackground(new Background(toggleFill));
        
        toggle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 20));
        toggle.setTextFill(Color.WHITE);
        
        // Add shadow effect
        DropShadow toggleShadow = new DropShadow();
        toggleShadow.setColor(Color.rgb(0, 0, 0, 0.5));
        toggleShadow.setRadius(10);
        toggleShadow.setOffsetY(3);
        toggle.setEffect(toggleShadow);
        
        // Hover effects
        toggle.setOnMouseEntered(e -> {
            LinearGradient hoverGradient = new LinearGradient(
                0, 0, 1, 0, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.rgb(255, 30, 30, 1.0)),
                new Stop(1, Color.rgb(200, 20, 20, 1.0))
            );
            BackgroundFill hoverFill = new BackgroundFill(hoverGradient, new CornerRadii(0, 0, 15, 0, false), null);
            toggle.setBackground(new Background(hoverFill));
            
            Glow hoverGlow = new Glow();
            hoverGlow.setLevel(0.5);
            toggle.setEffect(hoverGlow);
        });
        
        toggle.setOnMouseExited(e -> {
            toggle.setBackground(new Background(toggleFill));
            toggle.setEffect(toggleShadow);
        });
        
        toggle.setOnAction(e -> toggleSidebar());
        return toggle;
    }
    
    private void toggleSidebar() {
        if (sidebarVisible) {
            // Hide sidebar
            root.setLeft(null);
            sidebarVisible = false;
            
            // Expand content area
            contentArea.prefWidthProperty().bind(primaryStage.widthProperty());
        } else {
            // Show sidebar
            if (sidebar == null) {
                sidebar = createSidebar();
            }
            root.setLeft(sidebar);
            sidebarVisible = true;
            
            // Shrink content area to accommodate sidebar
            contentArea.prefWidthProperty().bind(primaryStage.widthProperty().subtract(250));
        }
    }
    
    private VBox createSidebar() {
        VBox sidebarContainer = new VBox();
        sidebarContainer.setPrefWidth(250);
        sidebarContainer.setMaxWidth(250);
        sidebarContainer.setMinWidth(250);
        sidebarContainer.setAlignment(Pos.TOP_CENTER);
        sidebarContainer.setSpacing(0);
        
        // Enhanced gradient background for sidebar
        LinearGradient sidebarGradient = new LinearGradient(
            0, 0, 1, 1, true, CycleMethod.NO_CYCLE,
            new Stop(0, Color.rgb(20, 20, 30)),
            new Stop(0.5, Color.rgb(25, 25, 35)),
            new Stop(1, Color.rgb(30, 30, 40))
        );
        BackgroundFill sidebarFill = new BackgroundFill(sidebarGradient, null, null);
        sidebarContainer.setBackground(new Background(sidebarFill));
        
        // Add drop shadow to sidebar
        DropShadow sidebarShadow = new DropShadow();
        sidebarShadow.setColor(Color.rgb(0, 0, 0, 0.5));
        sidebarShadow.setRadius(15);
        sidebarShadow.setOffsetX(5);
        sidebarContainer.setEffect(sidebarShadow);
        
        // Create sidebar header with logo
        VBox sidebarHeader = createSidebarHeader();
        sidebarContainer.getChildren().add(sidebarHeader);
        
        // Create navigation menu
        VBox navigationMenu = createNavigationMenu();
        sidebarContainer.getChildren().add(navigationMenu);
        
        // Add spacer to push footer to bottom
        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);
        sidebarContainer.getChildren().add(spacer);
        
        // Create sidebar footer
        VBox sidebarFooter = createSidebarFooter();
        sidebarContainer.getChildren().add(sidebarFooter);
        
        return sidebarContainer;
    }
    
    private VBox createSidebarHeader() {
        VBox header = new VBox();
        header.setAlignment(Pos.CENTER);
        header.setPadding(new Insets(20, 15, 20, 15));
        header.setSpacing(10);
        
        // Enhanced header background
        LinearGradient headerGradient = new LinearGradient(
            0, 0, 1, 0, true, CycleMethod.NO_CYCLE,
            new Stop(0, Color.rgb(229, 9, 20, 0.2)),
            new Stop(0.5, Color.rgb(180, 0, 0, 0.1)),
            new Stop(1, Color.rgb(229, 9, 20, 0.2))
        );
        BackgroundFill headerFill = new BackgroundFill(headerGradient, new CornerRadii(15), null);
        header.setBackground(new Background(headerFill));
        
        // Load and add logo
        try {
            File logoFile = new File("logo.png");
            if (logoFile.exists()) {
                Image logoImage = new Image(logoFile.toURI().toString());
                logoView = new ImageView(logoImage);
                logoView.setFitWidth(80);
                logoView.setFitHeight(80);
                logoView.setPreserveRatio(true);
                logoView.setSmooth(true);
                
                // Add glow effect to logo
                Glow logoGlow = new Glow();
                logoGlow.setLevel(0.4);
                logoView.setEffect(logoGlow);
                
                header.getChildren().add(logoView);
            }
        } catch (Exception e) {
            System.out.println("Error loading logo: " + e.getMessage());
        }
        
        // App title
        Label titleLabel = new Label("Smart Life");
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        titleLabel.setTextFill(TEXT_COLOR);
        
        Label subtitleLabel = new Label("Recommender");
        subtitleLabel.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 14));
        subtitleLabel.setTextFill(RED_LIGHT);
        
        header.getChildren().addAll(titleLabel, subtitleLabel);
        
        return header;
    }
    
    private VBox createNavigationMenu() {
        VBox menu = new VBox();
        menu.setAlignment(Pos.TOP_CENTER);
        menu.setPadding(new Insets(10, 0, 10, 0));
        menu.setSpacing(5);
        
        // Navigation items with icons and labels
        String[][] navItems = {
            {"🏠", "Home"},
            {"🎬", "Movies"},
            {"📚", "Books"},
            {"🌸", "Anime"},
            {"🎓", "Courses"},
            {"🎮", "Games"},
            {"👥", "Our Team"}
        };
        
        for (String[] item : navItems) {
            Button navButton = createSidebarButton(item[0], item[1]);
            menu.getChildren().add(navButton);
        }
        
        return menu;
    }
    
    private Button createSidebarButton(String icon, String text) {
    Button button = new Button();
    button.setPrefWidth(220);
    button.setPrefHeight(50);
    button.setMaxWidth(220);
    button.setAlignment(Pos.CENTER_LEFT);
    button.setCursor(Cursor.HAND);
    
    // Create button content with icon and text
    HBox buttonContent = new HBox();
    buttonContent.setAlignment(Pos.CENTER_LEFT);
    buttonContent.setSpacing(15);
    buttonContent.setPadding(new Insets(0, 0, 0, 20));
    
    // Icon label - WHITE COLOR
    Label iconLabel = new Label(icon);
    iconLabel.setFont(Font.font(20));
    iconLabel.setTextFill(Color.WHITE);
    
    // Text label - WHITE COLOR  
    Label textLabel = new Label(text);
    textLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
    textLabel.setTextFill(Color.WHITE);
    
    buttonContent.getChildren().addAll(iconLabel, textLabel);
    button.setGraphic(buttonContent);
    
    // PURE BLACK BACKGROUND
    BackgroundFill buttonFill = new BackgroundFill(Color.BLACK, new CornerRadii(10), null);
    button.setBackground(new Background(buttonFill));
    
    // SUBTLE BORDER
    button.setStyle(
        "-fx-border-color: rgba(80, 80, 80, 0.5); " +
        "-fx-border-width: 1; " +
        "-fx-border-radius: 10; " +
        "-fx-background-radius: 10;"
    );
    
    // Hover effects (same as above)
    button.setOnMouseEntered(e -> {
        LinearGradient hoverGradient = new LinearGradient(
            0, 0, 1, 0, true, CycleMethod.NO_CYCLE,
            new Stop(0, SIDEBAR_HOVER),
            new Stop(1, Color.rgb(180, 0, 0, 0.8))
        );
        BackgroundFill hoverFill = new BackgroundFill(hoverGradient, new CornerRadii(10), null);
        button.setBackground(new Background(hoverFill));
        
        Glow hoverGlow = new Glow();
        hoverGlow.setLevel(0.5);
        button.setEffect(hoverGlow);
        
        TranslateTransition slideIn = new TranslateTransition(Duration.millis(200), button);
        slideIn.setToX(10);
        slideIn.play();
        
        ScaleTransition scaleUp = new ScaleTransition(Duration.millis(200), button);
        scaleUp.setToX(1.02);
        scaleUp.setToY(1.02);
        scaleUp.play();
    });
    
    button.setOnMouseExited(e -> {
        button.setBackground(new Background(buttonFill));
        button.setEffect(null);
        
        TranslateTransition slideOut = new TranslateTransition(Duration.millis(200), button);
        slideOut.setToX(0);
        slideOut.play();
        
        ScaleTransition scaleDown = new ScaleTransition(Duration.millis(200), button);
        scaleDown.setToX(1.0);
        scaleDown.setToY(1.0);
        scaleDown.play();
    });
    
    // Click handlers (same as above)
    button.setOnAction(e -> {
        ScaleTransition clickScale = new ScaleTransition(Duration.millis(100), button);
        clickScale.setToX(0.95);
        clickScale.setToY(0.95);
        clickScale.setOnFinished(event -> {
            ScaleTransition scaleBack = new ScaleTransition(Duration.millis(100), button);
            scaleBack.setToX(1.0);
            scaleBack.setToY(1.0);
            scaleBack.play();
        });
        clickScale.play();
        
        handleNavigation(text);
        toggleSidebar();
    });
    
    return button;
}
    
    private void handleNavigation(String destination) {
        switch (destination) {
            case "Home":
                loadHomeContentDirect();
                break;
            case "Movies":
                currentCategory = "Movies";
                loadCategoryGenres("Movies");
                break;
            case "Books":
                currentCategory = "Books";
                loadCategoryGenres("Books");
                break;
            case "Anime":
                currentCategory = "Anime";
                loadCategoryGenres("Anime");
                break;
            case "Courses":
                currentCategory = "Courses";
                loadCategoryGenres("Courses");
                break;
            case "Games":
                currentCategory = "Games";
                loadCategoryGenres("Games");
                break;
            case "Our Team":
                loadOurTeamPage();
                break;
        }
    }
    
    private VBox createSidebarFooter() {
        VBox footer = new VBox();
        footer.setAlignment(Pos.CENTER);
        footer.setPadding(new Insets(15));
        footer.setSpacing(8);
        
        // Enhanced footer background
        LinearGradient footerGradient = new LinearGradient(
            0, 0, 1, 0, true, CycleMethod.NO_CYCLE,
            new Stop(0, Color.rgb(229, 9, 20, 0.1)),
            new Stop(1, Color.rgb(180, 0, 0, 0.1))
        );
        BackgroundFill footerFill = new BackgroundFill(footerGradient, new CornerRadii(15), null);
        footer.setBackground(new Background(footerFill));
        
        // Version info
        Label versionLabel = new Label("v1.0.0");
        versionLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        versionLabel.setTextFill(RED_LIGHT);
        
        // Copyright
        Label copyrightLabel = new Label("© 2025 Abdul Moiz and Team - All rights reserved.");
        copyrightLabel.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 10));
        copyrightLabel.setTextFill(Color.rgb(150, 150, 150));
        
        footer.getChildren().addAll(versionLabel, copyrightLabel);
        
        return footer;
    }

    private void loadOurTeamPage() {
        contentArea.getChildren().clear();
        currentView = "Team";
        
        // Calculate available height for content
        double availableHeight = contentArea.getPrefHeight();
        
        // Enhanced header - takes 20% of available height
        VBox headerSection = createTeamHeader(availableHeight * 0.2);
        contentArea.getChildren().add(headerSection);
        
        // Enhanced team section - takes 80% of available height
        VBox teamSection = createTeamSection(availableHeight * 0.8);
        contentArea.getChildren().add(teamSection);
    }
    
    private VBox createTeamHeader(double maxHeight) {
        VBox header = new VBox();
        header.setMaxHeight(maxHeight);
        header.setPrefHeight(maxHeight);
        header.setAlignment(Pos.CENTER);
        
        // Responsive spacing and padding based on allocated height
        double padding = Math.max(10, maxHeight * 0.1);
        double spacing = Math.max(8, maxHeight * 0.15);
        
        header.setPadding(new Insets(padding));
        header.setSpacing(spacing);
        
        // Add gradient background to header
        LinearGradient headerGradient = new LinearGradient(
            0, 0, 1, 1, true, CycleMethod.NO_CYCLE,
            new Stop(0, Color.rgb(229, 9, 20, 0.1)),
            new Stop(0.5, Color.rgb(180, 0, 0, 0.05)),
            new Stop(1, Color.rgb(229, 9, 20, 0.1))
        );
        BackgroundFill headerFill = new BackgroundFill(headerGradient, new CornerRadii(25), null);
        header.setBackground(new Background(headerFill));
        
        // Add glow effect
        DropShadow headerShadow = new DropShadow();
        headerShadow.setColor(Color.rgb(229, 9, 20, 0.3));
        headerShadow.setRadius(30);
        headerShadow.setSpread(0.2);
        header.setEffect(headerShadow);
        
        // Enhanced title with responsive font
        Label titleLabel = new Label("👥 Our Amazing Team");
        titleLabel.fontProperty().bind(Bindings.createObjectBinding(() -> {
            double fontSize = Math.max(20, Math.min(maxHeight * 0.3, 40));
            return Font.font("Segoe UI", FontWeight.BOLD, fontSize);
        }, primaryStage.widthProperty()));
        titleLabel.setTextFill(TEXT_COLOR);
        
        // Add glow effect to title
        Glow titleGlow = new Glow();
        titleGlow.setLevel(0.4);
        titleLabel.setEffect(titleGlow);
        
        // Enhanced subtitle
        Label subtitleLabel = new Label("Meet the brilliant minds behind Smart Life Recommender");
        subtitleLabel.fontProperty().bind(Bindings.createObjectBinding(() -> {
            double fontSize = Math.max(12, Math.min(maxHeight * 0.15, 18));
            return Font.font("Segoe UI", FontWeight.NORMAL, fontSize);
        }, primaryStage.widthProperty()));
        subtitleLabel.setTextFill(Color.rgb(200, 200, 200));
        subtitleLabel.setAlignment(Pos.CENTER);
        
        header.getChildren().addAll(titleLabel, subtitleLabel);
        return header;
    }
    
    private VBox createTeamSection(double maxHeight) {
        VBox section = new VBox();
        section.setAlignment(Pos.CENTER);
        section.setMaxHeight(maxHeight);
        section.setPrefHeight(maxHeight);
        
        // Responsive spacing
        double spacing = Math.max(15, maxHeight * 0.05);
        section.setSpacing(spacing);
        
        // Enhanced team grid
        GridPane teamGrid = new GridPane();
        teamGrid.setAlignment(Pos.CENTER);
        
        // Team member data - Updated with URLs (LinkedIn, GitHub, Portfolio, etc.)
        String[][] teamMembers = {
            {"Abdul Moiz", "BSE-242", "images/team/abdul_moiz.jpg", "Team Lead", "https://www.linkedin.com/in/abdul-moiz-55a722194/"},
            {"Shahzaib Khan", "BSE-308", "images/team/shahzaib.jpg", "Team Member", "https://www.linkedin.com/in/muhammad-shahzaib-khan-401459241/"},
            {"Ammar Jaffri", "BSE-248", "images/team/ammar.jpg", "Team Member", "https://www.linkedin.com/in/ammar-jaffri-655b22348/"}
        };

        // Calculate smaller card dimensions to match recommendation cards
        double cardHeight = Math.max(160, maxHeight * 0.5);
        double cardWidth = Math.max(140, primaryStage.getWidth() * 0.15);

        // Responsive gaps
        double gap = Math.max(20, primaryStage.getWidth() * 0.03);
        teamGrid.setHgap(gap);
        teamGrid.setVgap(gap);

        // Display all 3 members in one row
        for (int i = 0; i < teamMembers.length; i++) {
            VBox memberCard = createTeamMemberCard(
                teamMembers[i][0], // name
                teamMembers[i][1], // roll number
                teamMembers[i][2], // image path
                teamMembers[i][3], // role
                teamMembers[i][4], // URL - NEW PARAMETER
                cardWidth, 
                cardHeight
            );
            teamGrid.add(memberCard, i, 0);
        }
        
        section.getChildren().add(teamGrid);
        return section;
    }
    
    private VBox createTeamMemberCard(String name, String rollNumber, String imagePath, String role, String profileUrl, double cardWidth, double cardHeight) {
        VBox card = new VBox();
        card.setAlignment(Pos.CENTER);
        card.setPrefSize(cardWidth, cardHeight);
        card.setMaxSize(cardWidth, cardHeight);
        card.setMinSize(cardWidth, cardHeight);
        card.setCursor(Cursor.HAND); // Make it clear the card is clickable
        
        // Responsive spacing and padding
        double padding = Math.max(15, cardHeight * 0.08);
        double spacing = Math.max(10, cardHeight * 0.05);
        
        card.setPadding(new Insets(padding));
        card.setSpacing(spacing);
        
        // Enhanced gradient background with red theme
        LinearGradient cardGradient = new LinearGradient(
            0, 0, 1, 1, true, CycleMethod.NO_CYCLE,
            new Stop(0, Color.rgb(45, 45, 55)),
            new Stop(0.5, Color.rgb(35, 35, 45)),
            new Stop(1, Color.rgb(25, 25, 35))
        );
        BackgroundFill cardFill = new BackgroundFill(cardGradient, new CornerRadii(20), null);
        card.setBackground(new Background(cardFill));
        
        // Enhanced shadow
        DropShadow cardShadow = new DropShadow();
        cardShadow.setColor(Color.rgb(0, 0, 0, 0.5));
        cardShadow.setRadius(20);
        cardShadow.setOffsetY(10);
        card.setEffect(cardShadow);
        
        // Member image
        ImageView memberImage = new ImageView();
        double imageSize = Math.min(cardWidth * 0.6, cardHeight * 0.4);
        memberImage.setFitWidth(imageSize);
        memberImage.setFitHeight(imageSize);
        memberImage.setPreserveRatio(true);
        memberImage.setSmooth(true);
        
        // Load member image or use placeholder
        Image image = loadLocalImage(imagePath, imageSize, imageSize);
        if (image != null) {
            memberImage.setImage(image);
        } else {
            // Create placeholder with initials
            Label placeholder = new Label(getInitials(name));
            placeholder.setPrefSize(imageSize, imageSize);
            placeholder.setMaxSize(imageSize, imageSize);
            placeholder.setMinSize(imageSize, imageSize);
            placeholder.setAlignment(Pos.CENTER);
            placeholder.setFont(Font.font("Segoe UI", FontWeight.BOLD, imageSize * 0.3));
            placeholder.setTextFill(TEXT_COLOR);
            placeholder.setStyle("-fx-background-color: rgb(229, 9, 20); -fx-background-radius: " + (imageSize/2) + ";");
            card.getChildren().add(placeholder);
        }
        
        // Add rounded corners to image
        if (image != null) {
            Rectangle clip = new Rectangle(imageSize, imageSize);
            clip.setArcWidth(imageSize);
            clip.setArcHeight(imageSize);
            memberImage.setClip(clip);
            card.getChildren().add(memberImage);
        }
        
        // Member name
        Label nameLabel = new Label(name);
        double nameSize = Math.max(14, cardHeight * 0.08);
        nameLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, nameSize));
        nameLabel.setTextFill(TEXT_COLOR);
        nameLabel.setAlignment(Pos.CENTER);
        
        // Roll number
        Label rollLabel = new Label("Roll: " + rollNumber);
        double rollSize = Math.max(8, cardHeight * 0.06);
        rollLabel.setFont(Font.font("Segoe UI", FontWeight.NORMAL, rollSize));
        rollLabel.setTextFill(RED_LIGHT);
        rollLabel.setAlignment(Pos.CENTER);
        
        // Role
        Label roleLabel = new Label(role);
        double roleSize = Math.max(10, cardHeight * 0.05);
        roleLabel.setFont(Font.font("Segoe UI", FontWeight.NORMAL, roleSize));
        roleLabel.setTextFill(Color.rgb(190, 190, 190));
        roleLabel.setAlignment(Pos.CENTER);
        
        // NEW: Click hint label
        Label clickHintLabel = new Label("🔗 Click to visit profile");
        double hintSize = Math.max(8, cardHeight * 0.04);
        clickHintLabel.setFont(Font.font("Segoe UI", FontWeight.NORMAL, hintSize));
        clickHintLabel.setTextFill(Color.rgb(150, 150, 150));
        clickHintLabel.setAlignment(Pos.CENTER);
        
        card.getChildren().addAll(nameLabel, rollLabel, roleLabel, clickHintLabel);
        
        // Enhanced hover effects
        card.setOnMouseEntered(e -> {
            LinearGradient hoverGradient = new LinearGradient(
                0, 0, 1, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.rgb(229, 9, 20, 0.3)),
                new Stop(0.5, Color.rgb(255, 100, 100, 0.2)),
                new Stop(1, Color.rgb(229, 9, 20, 0.3))
            );
            BackgroundFill hoverFill = new BackgroundFill(hoverGradient, new CornerRadii(20), null);
            card.setBackground(new Background(hoverFill));
            
            ScaleTransition scaleUp = new ScaleTransition(Duration.millis(200), card);
            scaleUp.setToX(1.05);
            scaleUp.setToY(1.05);
            scaleUp.play();
            
            Glow hoverGlow = new Glow();
            hoverGlow.setLevel(0.4);
            card.setEffect(hoverGlow);
            
            // Change hint text color on hover
            clickHintLabel.setTextFill(RED_LIGHT);
        });
        
        card.setOnMouseExited(e -> {
            card.setBackground(new Background(cardFill));
            
            ScaleTransition scaleDown = new ScaleTransition(Duration.millis(200), card);
            scaleDown.setToX(1.0);
            scaleDown.setToY(1.0);
            scaleDown.play();
            
            card.setEffect(cardShadow);
            
            // Reset hint text color
            clickHintLabel.setTextFill(Color.rgb(150, 150, 150));
        });
        
        // NEW: Click handler to open profile URL
        card.setOnMouseClicked(e -> {
            if (profileUrl != null && !profileUrl.isEmpty()) {
                // Add click animation
                ScaleTransition clickScale = new ScaleTransition(Duration.millis(100), card);
                clickScale.setToX(0.95);
                clickScale.setToY(0.95);
                clickScale.setOnFinished(event -> {
                    ScaleTransition scaleBack = new ScaleTransition(Duration.millis(100), card);
                    scaleBack.setToX(1.0);
                    scaleBack.setToY(1.0);
                    scaleBack.play();
                });
                clickScale.play();
                
                // Open the URL
                openURL(profileUrl);
            } else {
                System.out.println("No profile URL available for " + name);
            }
        });
        
        return card;
    }
    
    private String getInitials(String name) {
        String[] parts = name.split(" ");
        StringBuilder initials = new StringBuilder();
        for (String part : parts) {
            if (!part.isEmpty()) {
                initials.append(part.charAt(0));
            }
        }
        return initials.toString().toUpperCase();
    }
    
    private String getCustomCSS() {
        return
            ".button {\n" +
            "    -fx-font-family: 'Segoe UI';\n" +
            "    -fx-font-weight: bold;\n" +
            "    -fx-cursor: hand;\n" +
            "}\n" +
            "\n" +
            ".button:hover {\n" +
            "    -fx-scale-x: 1.05;\n" +
            "    -fx-scale-y: 1.05;\n" +
            "}\n" +
            "\n" +
            ".label {\n" +
            "    -fx-font-family: 'Segoe UI';\n" +
            "}\n";
    }
    
    private void seedGenres() {
        // Movies genres
        Set<String> movieGenres = new LinkedHashSet<>();
        movieGenres.add("Action");
        movieGenres.add("Comedy");
        movieGenres.add("Sci-Fi");
        movieGenres.add("Romance");
        genres.put("Movies", movieGenres);
        
        // Books genres
        Set<String> bookGenres = new LinkedHashSet<>();
        bookGenres.add("Mystery");
        bookGenres.add("Fantasy");
        bookGenres.add("Thriller");
        bookGenres.add("Non-Fiction");
        genres.put("Books", bookGenres);
        
        // Games genres
        Set<String> gameGenres = new LinkedHashSet<>();
        gameGenres.add("Adventure");
        gameGenres.add("Racing");
        gameGenres.add("Shooter");
        gameGenres.add("Strategy");
        genres.put("Games", gameGenres);
        
        // Anime genres
        Set<String> animeGenres = new LinkedHashSet<>();
        animeGenres.add("Shonen");
        animeGenres.add("Seinen");
        animeGenres.add("Romance");
        animeGenres.add("Slice of Life");
        genres.put("Anime", animeGenres);
        
        // Courses genres
        Set<String> courseGenres = new LinkedHashSet<>();
        courseGenres.add("Programming");
        courseGenres.add("Design");
        courseGenres.add("Business");
        courseGenres.add("Language");
        genres.put("Courses", courseGenres);
    }
    
    private void seedData() {
        // MOVIES
        Map<String, List<Recommendation>> movieData = new HashMap<>();

        // Action Movies - WITH TRAILERS
        List<Recommendation> action = new ArrayList<>();
        action.add(new Recommendation("John Wick", "images/movies/action/john_wick.jpg", "https://www.imdb.com/title/tt2911666/", "https://www.youtube.com/watch?v=C0BMx-qxsP4"));
        action.add(new Recommendation("The Legend of Maula Jatt", "images/movies/action/maula_jatt.jpg", "https://www.imdb.com/title/tt4139928/", "https://www.youtube.com/watch?v=pEWqOAcYgpQ"));
        action.add(new Recommendation("The Fall Guy", "images/movies/action/fall_guy.jpg", "https://www.imdb.com/title/tt1684562/", "https://www.youtube.com/watch?v=j7jPnwVGdZ8"));
        action.add(new Recommendation("Mission: Impossible", "images/movies/action/mission_impossible.jpg", "https://www.imdb.com/title/tt9603212/", "https://www.youtube.com/watch?v=avz06PDqDbM"));
        action.add(new Recommendation("Extraction II", "images/movies/action/extraction_2.jpg", "https://www.imdb.com/title/tt12263384/", "https://www.youtube.com/watch?v=Y274jZs5s7s"));
        action.add(new Recommendation("John Wick: Chapter 4", "images/movies/action/john_wick_4.jpg", "https://www.imdb.com/title/tt10366206/", "https://www.youtube.com/watch?v=qEVUtrk8_B4"));
        action.add(new Recommendation("Godzilla Vs Kong", "images/movies/action/godzilla_vs_kong.jpg", "https://www.imdb.com/title/tt5034838/", "https://www.youtube.com/watch?v=odM92ap8_c0"));
        action.add(new Recommendation("Fast X", "images/movies/action/fast_x.jpg", "https://www.imdb.com/title/tt5433140/", "https://www.youtube.com/watch?v=32RAq6JzY-w"));
        movieData.put("Action", action);

        // Comedy Movies - WITH TRAILERS
        List<Recommendation> comedy = new ArrayList<>();
        comedy.add(new Recommendation("Housefull 5", "images/movies/comedy/housefull_5.jpg", "https://www.imdb.com/title/tt9104736/", "https://www.youtube.com/watch?v=example"));
        comedy.add(new Recommendation("Jumanji", "images/movies/comedy/jumanji.jpg", "https://www.imdb.com/title/tt2283362/", "https://www.youtube.com/watch?v=2QKg5SZ_35I"));
        comedy.add(new Recommendation("The Hangover", "images/movies/comedy/the_hangover.jpg", "https://www.imdb.com/title/tt1119646/", "https://www.youtube.com/watch?v=tcdUhdOlz9M"));
        comedy.add(new Recommendation("3 Idiots", "images/movies/comedy/3_idiots.jpg", "https://www.imdb.com/title/tt1187043/", "https://www.youtube.com/watch?v=K0eDlFX9GMc"));
        comedy.add(new Recommendation("Deadpool", "images/movies/comedy/deadpool.jpg", "https://www.imdb.com/title/tt1431045/", "https://www.youtube.com/watch?v=9vN6DHB6bJc"));
        comedy.add(new Recommendation("crew", "images/movies/comedy/crew.jpg", "https://www.imdb.com/title/tt21383812/", "https://www.youtube.com/watch?v=example"));
        comedy.add(new Recommendation("Na Baligh Afraad", "images/movies/comedy/Na Baligh Afraad.jpg", "https://www.imdb.com/title/tt32552818/", "https://www.youtube.com/watch?v=example"));
        comedy.add(new Recommendation("Ant-Man", "images/movies/comedy/ant_man.jpg", "https://www.imdb.com/title/tt0478970/", "https://www.youtube.com/watch?v=pWdKf3MneyI"));
        movieData.put("Comedy", comedy);

        // Sci-Fi Movies - WITH TRAILERS
        List<Recommendation> scifi = new ArrayList<>();
        scifi.add(new Recommendation("Interstellar", "images/movies/scifi/interstellar.jpg", "https://www.imdb.com/title/tt0816692/", "https://www.youtube.com/watch?v=zSWdZVtXT7E"));
        scifi.add(new Recommendation("Inception", "images/movies/scifi/inception.jpg", "https://www.imdb.com/title/tt1375666/", "https://www.youtube.com/watch?v=YoHD9XEInc0"));
        scifi.add(new Recommendation("Blade Runner 2049", "images/movies/scifi/blade_runner_2049.jpg", "https://www.imdb.com/title/tt1856101/", "https://www.youtube.com/watch?v=gCcx85zbxz4"));
        scifi.add(new Recommendation("The Matrix", "images/movies/scifi/matrix.jpg", "https://www.imdb.com/title/tt0133093/", "https://www.youtube.com/watch?v=vKQi3bBA1y8"));
        scifi.add(new Recommendation("Dune", "images/movies/scifi/dune.jpg", "https://www.imdb.com/title/tt1160419/", "https://www.youtube.com/watch?v=n9xhJrPXop4"));
        scifi.add(new Recommendation("Arrival", "images/movies/scifi/arrival.jpg", "https://www.imdb.com/title/tt2543164/", "https://www.youtube.com/watch?v=tFMo3UJ4B4g"));
        scifi.add(new Recommendation("Ex Machina", "images/movies/scifi/ex_machina.jpg", "https://www.imdb.com/title/tt0470752/", "https://www.youtube.com/watch?v=EoQuVnKhxaM"));
        scifi.add(new Recommendation("Gravity", "images/movies/scifi/gravity.jpg", "https://www.imdb.com/title/tt1454468/", "https://www.youtube.com/watch?v=OiTiKOy59o4"));
        movieData.put("Sci-Fi", scifi);     

        // Romance Movies - WITH TRAILERS
        List<Recommendation> romance = new ArrayList<>();
        romance.add(new Recommendation("The Notebook", "images/movies/romance/notebook.jpg", "https://www.imdb.com/title/tt0332280/", "https://www.youtube.com/watch?v=FC6biTjEyZw"));
        romance.add(new Recommendation("La La Land", "images/movies/romance/la_la_land.jpg", "https://www.imdb.com/title/tt3783958/", "https://www.youtube.com/watch?v=0pdqf4P9MB8"));
        romance.add(new Recommendation("Titanic", "images/movies/romance/titanic.jpg", "https://www.imdb.com/title/tt0120338/", "https://www.youtube.com/watch?v=2e-eXJ6HgkQ"));
        romance.add(new Recommendation("The Fault in Our Stars", "images/movies/romance/fault_in_stars.jpg", "https://www.imdb.com/title/tt2582846/", "https://www.youtube.com/watch?v=9ItBvH5J6ss"));
        romance.add(new Recommendation("Me Before You", "images/movies/romance/me_before_you.jpg", "https://www.imdb.com/title/tt2674426/", "https://www.youtube.com/watch?v=Eh993__rOxA"));
        romance.add(new Recommendation("A Star is Born", "images/movies/romance/star_is_born.jpg", "https://www.imdb.com/title/tt1517451/", "https://www.youtube.com/watch?v=nSbzyEJ8X9E"));
        romance.add(new Recommendation("Pride and Prejudice", "images/movies/romance/pride_prejudice.jpg", "https://www.imdb.com/title/tt0414387/", "https://www.youtube.com/watch?v=1dYv5u6v55Y"));
        romance.add(new Recommendation("Casablanca", "images/movies/romance/casablanca.jpg", "https://www.imdb.com/title/tt0120338/", "https://www.youtube.com/watch?v=BkL9l7qovsE"));
        movieData.put("Romance", romance);

        data.put("Movies", movieData);

        // BOOKS
        Map<String, List<Recommendation>> bookData = new HashMap<>();

        // Mystery Books
        List<Recommendation> mystery = new ArrayList<>();
        mystery.add(new Recommendation("Gone Girl", "images/books/mystery/gone_girl.jpg", "https://www.goodreads.com/book/show/19288043-gone-girl"));
        mystery.add(new Recommendation("The Girl with the Dragon Tattoo", "images/books/mystery/dragon_tattoo.jpg", "https://www.goodreads.com/book/show/2429135.The_Girl_with_the_Dragon_Tattoo"));
        mystery.add(new Recommendation("The Silent Patient", "images/books/mystery/silent_patient.jpg", "https://www.goodreads.com/book/show/40097951-the-silent-patient"));
        mystery.add(new Recommendation("Big Little Lies", "images/books/mystery/big_little_lies.jpg", "https://www.goodreads.com/book/show/19486412-big-little-lies"));
        mystery.add(new Recommendation("The Thursday Murder Club", "images/books/mystery/thursday_murder_club.jpg", "https://www.goodreads.com/book/show/46000520-the-thursday-murder-club"));
        mystery.add(new Recommendation("In the Woods", "images/books/mystery/in_the_woods.jpg", "https://www.goodreads.com/book/show/2459785.In_the_Woods"));
        mystery.add(new Recommendation("The Cuckoo's Calling", "images/books/mystery/cuckoos_calling.jpg", "https://www.goodreads.com/book/show/16160797-the-cuckoo-s-calling"));
        mystery.add(new Recommendation("And Then There Were None", "images/books/mystery/and_then_there_were_none.jpg", "https://www.goodreads.com/book/show/16299.And_Then_There_Were_None"));
        bookData.put("Mystery", mystery);

        // Fantasy Books
        List<Recommendation> fantasy = new ArrayList<>();
        fantasy.add(new Recommendation("Harry Potter", "images/books/fantasy/harry_potter.jpg", "https://www.goodreads.com/book/show/3.Harry_Potter_and_the_Sorcerer_s_Stone"));
        fantasy.add(new Recommendation("The Hobbit", "images/books/fantasy/hobbit.jpg", "https://www.goodreads.com/book/show/5907.The_Hobbit"));
        fantasy.add(new Recommendation("Game of Thrones", "images/books/fantasy/game_of_thrones.jpg", "https://www.goodreads.com/book/show/13496.A_Game_of_Thrones"));
        fantasy.add(new Recommendation("The Name of the Wind", "images/books/fantasy/name_of_wind.jpg", "https://www.goodreads.com/book/show/186074.The_Name_of_the_Wind"));
        fantasy.add(new Recommendation("The Way of Kings", "images/books/fantasy/way_of_kings.jpg", "https://www.goodreads.com/book/show/7235533-the-way-of-kings"));
        fantasy.add(new Recommendation("The Fellowship of the Ring", "images/books/fantasy/fellowship_ring.jpg", "https://www.goodreads.com/book/show/34.The_Fellowship_of_the_Ring"));
        fantasy.add(new Recommendation("Mistborn", "images/books/fantasy/mistborn.jpg", "https://www.goodreads.com/book/show/68428.The_Final_Empire"));
        fantasy.add(new Recommendation("The Chronicles of Narnia", "images/books/fantasy/chronicles_narnia.jpg", "https://www.goodreads.com/book/show/100915.The_Lion_the_Witch_and_the_Wardrobe"));
        bookData.put("Fantasy", fantasy);

        // Thriller Books
        List<Recommendation> thriller = new ArrayList<>();
        thriller.add(new Recommendation("The Girl on the Train", "images/books/thriller/girl_on_train.jpg", "https://www.goodreads.com/book/show/22557272-the-girl-on-the-train"));
        thriller.add(new Recommendation("The Da Vinci Code", "images/books/thriller/da_vinci_code.jpg", "https://www.goodreads.com/book/show/968.The_Da_Vinci_Code"));
        thriller.add(new Recommendation("The Silence of the Lambs", "images/books/thriller/silence_lambs.jpg", "https://www.goodreads.com/book/show/23807.The_Silence_of_the_Lambs"));
        thriller.add(new Recommendation("Shutter Island", "images/books/thriller/shutter_island.jpg", "https://www.goodreads.com/book/show/21686.Shutter_Island"));
        thriller.add(new Recommendation("The Talented Mr. Ripley", "images/books/thriller/talented_mr_ripley.jpg", "https://www.goodreads.com/book/show/243395.The_Talented_Mr_Ripley"));
        thriller.add(new Recommendation("Rebecca", "images/books/thriller/rebecca.jpg", "https://www.goodreads.com/book/show/17899948-rebecca"));
        thriller.add(new Recommendation("The Firm", "images/books/thriller/firm.jpg", "https://www.goodreads.com/book/show/5358.The_Firm"));
        thriller.add(new Recommendation("Psycho", "images/books/thriller/psycho.jpg", "https://www.goodreads.com/book/show/6931.Psycho"));
        bookData.put("Thriller", thriller);

        // Non-Fiction Books
        List<Recommendation> nonfiction = new ArrayList<>();
        nonfiction.add(new Recommendation("Thinking, Fast and Slow", "images/books/nonfiction/thinking_fast_slow.jpg", "https://www.goodreads.com/book/show/11468377-thinking-fast-and-slow"));
        nonfiction.add(new Recommendation("Atomic Habits", "images/books/nonfiction/atomic_habits.jpg", "https://www.goodreads.com/book/show/40121378-atomic-habits"));
        nonfiction.add(new Recommendation("Sapiens", "images/books/nonfiction/sapiens.jpg", "https://www.goodreads.com/book/show/23692271-sapiens"));
        nonfiction.add(new Recommendation("Educated", "images/books/nonfiction/educated.jpg", "https://www.goodreads.com/book/show/35133922-educated"));
        nonfiction.add(new Recommendation("The 7 Habits of Highly Effective People", "images/books/nonfiction/7_habits.jpg", "https://www.goodreads.com/book/show/36072.The_7_Habits_of_Highly_Effective_People"));
        nonfiction.add(new Recommendation("Becoming", "images/books/nonfiction/becoming.jpg", "https://www.goodreads.com/book/show/38746485-becoming"));
        nonfiction.add(new Recommendation("The Power of Now", "images/books/nonfiction/power_of_now.jpg", "https://www.goodreads.com/book/show/6708.The_Power_of_Now"));
        nonfiction.add(new Recommendation("Man's Search for Meaning", "images/books/nonfiction/mans_search_meaning.jpg", "https://www.goodreads.com/book/show/4069.Man_s_Search_for_Meaning"));
        bookData.put("Non-Fiction", nonfiction);

        data.put("Books", bookData);

        // GAMES
        Map<String, List<Recommendation>> gameData = new HashMap<>();

        // Adventure Games
        List<Recommendation> adventure = new ArrayList<>();
        adventure.add(new Recommendation("The Legend of Zelda: Breath of the Wild", "images/games/adventure/zelda_botw.jpg", "https://www.nintendo.com/us/store/products/the-legend-of-zelda-breath-of-the-wild-switch/"));
        adventure.add(new Recommendation("Uncharted 4", "images/games/adventure/uncharted_4.jpg", "https://www.playstation.com/en-us/games/uncharted-4-a-thiefs-end/"));
        adventure.add(new Recommendation("Red Dead Redemption 2", "images/games/adventure/red_dead_2.jpg", "https://www.rockstargames.com/reddeadredemption2/"));
        adventure.add(new Recommendation("The Witcher 3", "images/games/adventure/witcher_3.jpg", "https://www.thewitcher.com/us/en/witcher3"));
        adventure.add(new Recommendation("Assassin's Creed Valhalla", "images/games/adventure/ac_valhalla.jpg", "https://www.ubisoft.com/en-us/game/assassins-creed/valhalla"));
        adventure.add(new Recommendation("God of War", "images/games/adventure/god_of_war.jpg", "https://www.playstation.com/en-us/games/god-of-war/"));
        adventure.add(new Recommendation("Horizon Zero Dawn", "images/games/adventure/horizon_zero_dawn.jpg", "https://www.playstation.com/en-us/games/horizon-zero-dawn/"));
        adventure.add(new Recommendation("Spider-Man", "images/games/adventure/spider_man.jpg", "https://www.playstation.com/en-us/games/marvels-spider-man/"));
        gameData.put("Adventure", adventure);

        // Racing Games
        List<Recommendation> racing = new ArrayList<>();
        racing.add(new Recommendation("Forza Horizon 5", "images/games/racing/forza_horizon_5.jpg", "https://forza.net/horizon"));
        racing.add(new Recommendation("Need for Speed Heat", "images/games/racing/nfs_heat.jpg", "https://www.ea.com/games/need-for-speed/need-for-speed-heat"));
        racing.add(new Recommendation("Gran Turismo 7", "images/games/racing/gran_turismo_7.jpg", "https://www.playstation.com/en-us/games/gran-turismo-7/"));
        racing.add(new Recommendation("F1 23", "images/games/racing/f1_23.jpg", "https://www.ea.com/games/f1/f1-23"));
        racing.add(new Recommendation("Dirt Rally 2.0", "images/games/racing/dirt_rally_2.jpg", "https://www.codemasters.com/game/dirt-rally-2-0/"));
        racing.add(new Recommendation("The Crew 2", "images/games/racing/crew_2.jpg", "https://www.ubisoft.com/en-us/game/the-crew/the-crew-2"));
        racing.add(new Recommendation("Burnout Paradise", "images/games/racing/burnout_paradise.jpg", "https://www.ea.com/games/burnout/burnout-paradise-remastered"));
        racing.add(new Recommendation("Mario Kart 8 Deluxe", "images/games/racing/mario_kart_8.jpg", "https://www.nintendo.com/us/store/products/mario-kart-8-deluxe-switch/"));
        gameData.put("Racing", racing);

        // Shooter Games
        List<Recommendation> shooter = new ArrayList<>();
        shooter.add(new Recommendation("Call of Duty: Modern Warfare II", "images/games/shooter/cod_mw2.jpg", "https://www.callofduty.com/modernwarfare2"));
        shooter.add(new Recommendation("Apex Legends", "images/games/shooter/apex_legends.jpg", "https://www.ea.com/games/apex-legends"));
        shooter.add(new Recommendation("Valorant", "images/games/shooter/valorant.jpg", "https://playvalorant.com/"));
        shooter.add(new Recommendation("Overwatch 2", "images/games/shooter/overwatch_2.jpg", "https://overwatch.blizzard.com/"));
        shooter.add(new Recommendation("Counter-Strike 2", "images/games/shooter/counter_strike_2.jpg", "https://www.counter-strike.net/"));
        shooter.add(new Recommendation("Battlefield 2042", "images/games/shooter/battlefield_2042.jpg", "https://www.ea.com/games/battlefield/battlefield-2042"));
        shooter.add(new Recommendation("Doom Eternal", "images/games/shooter/doom_eternal.jpg", "https://bethesda.net/en/game/doom"));
        shooter.add(new Recommendation("Halo Infinite", "images/games/shooter/halo_infinite.jpg", "https://www.halowaypoint.com/halo-infinite"));
        gameData.put("Shooter", shooter);

        // Strategy Games
        List<Recommendation> strategy = new ArrayList<>();
        strategy.add(new Recommendation("Age of Empires IV", "images/games/strategy/age_of_empires_4.jpg", "https://www.ageofempires.com/games/age-of-empires-iv/"));
        strategy.add(new Recommendation("Starcraft II", "images/games/strategy/starcraft_2.jpg", "https://starcraft2.com/"));
        strategy.add(new Recommendation("Civilization VI", "images/games/strategy/civilization_6.jpg", "https://civilization.com/"));
        strategy.add(new Recommendation("Total War: Warhammer III", "images/games/strategy/total_war_warhammer_3.jpg", "https://www.totalwar.com/games/warhammer-iii/"));
        strategy.add(new Recommendation("Command & Conquer", "images/games/strategy/command_conquer.jpg", "https://www.ea.com/games/command-and-conquer"));
        strategy.add(new Recommendation("XCOM 2", "images/games/strategy/xcom_2.jpg", "https://xcom.com/"));
        strategy.add(new Recommendation("Europa Universalis IV", "images/games/strategy/europa_universalis_4.jpg", "https://www.paradoxinteractive.com/games/europa-universalis-iv"));
        strategy.add(new Recommendation("Chess.com", "images/games/strategy/chess.png", "https://www.chess.com/"));
        gameData.put("Strategy", strategy);

        data.put("Games", gameData);

        // ANIME - WITH TRAILERS
        Map<String, List<Recommendation>> animeData = new HashMap<>();

        // Shonen Anime - WITH TRAILERS
        List<Recommendation> shonen = new ArrayList<>();
        shonen.add(new Recommendation("Attack on Titan", "images/anime/shonen/aot.jpg", "https://myanimelist.net/anime/16498/Shingeki_no_Kyojin", "https://www.youtube.com/watch?v=LHtdKWJdif4"));
        shonen.add(new Recommendation("Demon Slayer", "images/anime/shonen/demon_slayer.jpg", "https://myanimelist.net/anime/38000/Kimetsu_no_Yaiba", "https://www.youtube.com/watch?v=VQGCKyvzIM4"));
        shonen.add(new Recommendation("My Hero Academia", "images/anime/shonen/mha.jpg", "https://myanimelist.net/anime/31964/Boku_no_Hero_Academia", "https://www.youtube.com/watch?v=D5fYOnwYkj4"));
        shonen.add(new Recommendation("Naruto", "images/anime/shonen/naruto.jpg", "https://myanimelist.net/anime/20/Naruto", "https://www.youtube.com/watch?v=1dy2zPPrKD0"));
        shonen.add(new Recommendation("One Piece", "images/anime/shonen/one_piece.jpg", "https://www.justwatch.com/us/tv-show/one-piece", "https://www.youtube.com/watch?v=MCb13lbVGE0"));
        shonen.add(new Recommendation("Dragon Ball Z", "images/anime/shonen/dbz.jpg", "https://myanimelist.net/anime/813/Dragon_Ball_Z", "https://www.youtube.com/watch?v=HKMNKS-9ugY"));
        shonen.add(new Recommendation("Bleach", "images/anime/shonen/bleach.jpg", "https://myanimelist.net/anime/269/Bleach", "https://www.youtube.com/watch?v=bReQJNrBbSI"));
        shonen.add(new Recommendation("Jujutsu Kaisen", "images/anime/shonen/jujutsu_kaisen.jpg", "https://myanimelist.net/anime/40748/Jujutsu_Kaisen", "https://www.youtube.com/watch?v=pkKu9hLT-t8"));
        animeData.put("Shonen", shonen);

        // Seinen Anime - WITH TRAILERS
        List<Recommendation> seinen = new ArrayList<>();
        seinen.add(new Recommendation("Death Note", "images/anime/seinen/death_note.jpg", "https://myanimelist.net/anime/1535/Death_Note", "https://www.youtube.com/watch?v=NlJZ-YgAt-c"));
        seinen.add(new Recommendation("Monster", "images/anime/seinen/monster.jpg", "https://myanimelist.net/anime/19/Monster", "https://www.youtube.com/watch?v=9aS-EgdAq6U"));
        seinen.add(new Recommendation("Psycho-Pass", "images/anime/seinen/psysho_pass.jpg", "https://myanimelist.net/anime/13601/Psycho-Pass", "https://www.youtube.com/watch?v=kwmXh1IzV3g"));
        seinen.add(new Recommendation("Tokyo Ghoul", "images/anime/seinen/tokyo_ghoul.jpg", "https://myanimelist.net/anime/22319/Tokyo_Ghoul", "https://www.youtube.com/watch?v=vGuQeQsoRgU"));
        seinen.add(new Recommendation("Berserk", "images/anime/seinen/berserk.jpg", "https://myanimelist.net/anime/33/Berserk", "https://www.youtube.com/watch?v=p286WtHoK2g"));
        seinen.add(new Recommendation("Cowboy Bebop", "images/anime/seinen/cowboy_bepop.jpg", "https://myanimelist.net/anime/1/Cowboy_Bebop", "https://www.youtube.com/watch?v=qig4KOK2R2g"));
        seinen.add(new Recommendation("Ghost in the Shell", "images/anime/seinen/ghost_in_shell.jpg", "https://myanimelist.net/anime/43/Ghost_in_the_Shell__Stand_Alone_Complex", "https://www.youtube.com/watch?v=YQiDVL9eARw"));
        seinen.add(new Recommendation("Parasyte", "images/anime/seinen/parasyte.jpg", "https://myanimelist.net/anime/22535/Kiseijuu__Sei_no_Kakuritsu", "https://www.youtube.com/watch?v=Rm8UjBAS3cs"));
        animeData.put("Seinen", seinen);

        // Romance Anime - WITH TRAILERS
        List<Recommendation> animeRomance = new ArrayList<>();
        animeRomance.add(new Recommendation("Your Name", "images/anime/romance/your_name.jpg", "https://myanimelist.net/anime/32281/Kimi_no_Na_wa", "https://www.youtube.com/watch?v=xU47nhruN-Q"));
        animeRomance.add(new Recommendation("A Silent Voice", "images/anime/romance/silent_voice.jpg", "https://myanimelist.net/anime/28851/Koe_no_Katachi", "https://www.youtube.com/watch?v=nfK6UgLra7g"));
        animeRomance.add(new Recommendation("Weathering with You", "images/anime/romance/weathering_with_you.jpg", "https://myanimelist.net/anime/38826/Tenki_no_Ko", "https://www.youtube.com/watch?v=Q6iK6DjV_f8"));
        animeRomance.add(new Recommendation("Your Lie in April", "images/anime/romance/your_lie_april.jpg", "https://myanimelist.net/anime/23273/Shigatsu_wa_Kimi_no_Uso", "https://www.youtube.com/watch?v=9sTQ0QdkN3Q"));
        animeRomance.add(new Recommendation("Toradora!", "images/anime/romance/toradora.jpg", "https://myanimelist.net/anime/4224/Toradora", "https://www.youtube.com/watch?v=ya94Zd6XOks"));
        animeRomance.add(new Recommendation("Clannad", "images/anime/romance/clannad.jpg", "https://myanimelist.net/anime/2167/Clannad", "https://www.youtube.com/watch?v=P-zhIImKP5k"));
        animeRomance.add(new Recommendation("Kaguya-sama: Love is War", "images/anime/romance/kaguya_sama.jpg", "https://myanimelist.net/anime/37999/Kaguya-sama_wa_Kokurasetai__Tensai-tachi_no_Renai_Zunousen", "https://www.youtube.com/watch?v=lTlzDfhPtFA"));
        animeRomance.add(new Recommendation("Horimiya", "images/anime/romance/horimiya.jpg", "https://myanimelist.net/anime/42897/Horimiya", "https://www.youtube.com/watch?v=_YsAJlYEbDc"));
        animeData.put("Romance", animeRomance);

        // Slice of Life Anime - WITH TRAILERS
        List<Recommendation> sliceOfLife = new ArrayList<>();
        sliceOfLife.add(new Recommendation("Spirited Away", "images/anime/sliceoflife/spirited_away.jpg", "https://myanimelist.net/anime/199/Sen_to_Chihiro_no_Kamikakushi", "https://www.youtube.com/watch?v=ByXuk9QqQkk"));
        sliceOfLife.add(new Recommendation("My Neighbor Totoro", "images/anime/sliceoflife/my_neighbor_totoro.jpg", "https://myanimelist.net/anime/523/Tonari_no_Totoro", "https://www.youtube.com/watch?v=92a7Hj0ijLs"));
        sliceOfLife.add(new Recommendation("Violet Evergarden", "images/anime/sliceoflife/violet_evergarden.jpg", "https://myanimelist.net/anime/33352/Violet_Evergarden", "https://www.youtube.com/watch?v=0CJeDetA45Q"));
        sliceOfLife.add(new Recommendation("K-On!", "images/anime/sliceoflife/k_on.jpg", "https://myanimelist.net/anime/5680/K-On", "https://www.youtube.com/watch?v=gnd1wSIyN-g"));
        sliceOfLife.add(new Recommendation("Barakamon", "images/anime/sliceoflife/barakamon.jpg", "https://myanimelist.net/anime/22789/Barakamon", "https://www.youtube.com/watch?v=qvWBaIhyQuU"));
        sliceOfLife.add(new Recommendation("March Comes in Like a Lion", "images/anime/sliceoflife/march_comes_lion.jpg", "https://myanimelist.net/anime/31646/3-gatsu_no_Lion", "https://www.youtube.com/watch?v=cKWqPXkLgzY"));
        sliceOfLife.add(new Recommendation("Hyouka", "images/anime/sliceoflife/hyouka.jpg", "https://myanimelist.net/anime/12189/Hyouka", "https://www.youtube.com/watch?v=Npqtet5wlDs"));
        sliceOfLife.add(new Recommendation("Classroom of the Elite", "images/anime/sliceoflife/cote.jpg", "https://www.imdb.com/title/tt7263328/", "https://www.youtube.com/watch?v=xKbKz3hXGSs"));
        animeData.put("Slice of Life", sliceOfLife);

        data.put("Anime", animeData);

        // COURSES
        Map<String, List<Recommendation>> courseData = new HashMap<>();

        // Programming Courses
        List<Recommendation> programming = new ArrayList<>();
        programming.add(new Recommendation("Complete Java Bootcamp", "images/courses/programming/java.jpg", "https://www.udemy.com/course/java-the-complete-java-developer-course/"));
        programming.add(new Recommendation("Python for Everybody", "images/courses/programming/python.png", "https://www.coursera.org/specializations/python"));
        programming.add(new Recommendation("Web Development Bootcamp", "images/courses/programming/web_dev_bootcamp.jpg", "https://www.udemy.com/course/the-web-developer-bootcamp/"));
        programming.add(new Recommendation("React - The Complete Guide", "images/courses/programming/react_complete.jpeg", "https://www.udemy.com/course/react-the-complete-guide-incl-redux/"));
        programming.add(new Recommendation("JavaScript Algorithms", "images/courses/programming/js_algorithms.jpg", "https://www.udemy.com/course/js-algorithms-and-data-structures-masterclass/"));
        programming.add(new Recommendation("Node.js Developer Course", "images/courses/programming/nodejs_course.jpg", "https://www.udemy.com/course/the-complete-nodejs-developer-course-2/"));
        programming.add(new Recommendation("Machine Learning A-Z", "images/courses/programming/machine_learning.jpg", "https://www.udemy.com/course/machinelearning/"));
        programming.add(new Recommendation("iOS App Development", "images/courses/programming/ios_development.jpg", "https://www.udemy.com/course/ios-13-app-development-bootcamp/"));
        courseData.put("Programming", programming);

        // Design Courses
        List<Recommendation> design = new ArrayList<>();
        design.add(new Recommendation("UI/UX Design Masterclass", "images/courses/design/ui_ux_masterclass.jpeg", "https://www.udemy.com/course/ui-ux-web-design-using-adobe-xd/"));
        design.add(new Recommendation("Adobe Photoshop CC", "images/courses/design/photoshop_cc.jpeg", "https://www.udemy.com/course/adobe-photoshop-cc-essentials-training-course/"));
        design.add(new Recommendation("Figma UI UX Design", "images/courses/design/figma.jpeg", "https://www.udemy.com/course/figma-ux-ui-design-user-experience-tutorial-course/"));
        design.add(new Recommendation("Graphic Design Masterclass", "images/courses/design/graphic_design.jpg", "https://www.udemy.com/course/graphic-design-masterclass-everything-you-need-to-know/"));
        design.add(new Recommendation("Adobe Illustrator CC", "images/courses/design/illustrator_cc.jpeg", "https://www.udemy.com/course/illustrator-cc-masterclass/"));
        design.add(new Recommendation("Logo Design Masterclass", "images/courses/design/logo_design.jpg", "https://www.udemy.com/course/logo-design-masterclass/"));
        design.add(new Recommendation("After Effects CC", "images/courses/design/after_effects.jpg", "https://www.udemy.com/course/after-effects-cc-2018-complete-course/"));
        design.add(new Recommendation("Blender 3D Complete", "images/courses/design/blender_3d.jpg", "https://www.udemy.com/course/blender-3d-from-zero-to-hero/"));
        courseData.put("Design", design);

        // Business Courses
        List<Recommendation> business = new ArrayList<>();
        business.add(new Recommendation("Digital Marketing Course", "images/courses/business/digital_marketing.jpg", "https://www.udemy.com/course/the-complete-digital-marketing-course/"));
        business.add(new Recommendation("Project Management", "images/courses/business/project_management.jpg", "https://www.udemy.com/course/the-project-management-course-beginner-to-project-manager/"));
        business.add(new Recommendation("Financial Analysis", "images/courses/business/financial_analysis.jpg", "https://www.udemy.com/course/the-complete-financial-analyst-course/"));
        business.add(new Recommendation("Excel Masterclass", "images/courses/business/excel.jpeg", "https://www.udemy.com/course/microsoft-excel-2013-from-beginner-to-advanced-and-beyond/"));
        business.add(new Recommendation("Social Media Marketing", "images/courses/business/socialmedia.jpeg", "https://www.udemy.com/course/become-a-kick-ass-social-media-manager/"));
        business.add(new Recommendation("Entrepreneurship", "images/courses/business/enter.png", "https://www.udemy.com/course/the-lean-startup/"));
        business.add(new Recommendation("Sales Training", "images/courses/business/sales.png", "https://www.udemy.com/course/sales-training-practical-sales-techniques/"));
        business.add(new Recommendation("Leadership Skills", "images/courses/business/leadership.jpg", "https://www.udemy.com/course/practical-leadership/"));
        courseData.put("Business", business);

        // Language Courses
        List<Recommendation> language = new ArrayList<>();
        language.add(new Recommendation("Complete Spanish Course", "images/courses/language/spanish_complete.jpg", "https://www.udemy.com/course/complete-spanish-course-learn-spanish-language/"));
        language.add(new Recommendation("English Grammar Masterclass", "images/courses/language/english_grammar.jpg", "https://www.udemy.com/course/english-grammar-course-for-esl-students/"));
        language.add(new Recommendation("French for Beginners", "images/courses/language/french_beginners.jpg", "https://www.udemy.com/course/french-for-beginners-course/"));
        language.add(new Recommendation("German Complete Course", "images/courses/language/german_complete.jpg", "https://www.udemy.com/course/complete-german-course-learn-german-for-beginners/"));
        language.add(new Recommendation("Japanese for Everyone", "images/courses/language/japanese_everyone.jpg", "https://www.udemy.com/course/japanese-for-everyone-course/"));
        language.add(new Recommendation("Mandarin Chinese", "images/courses/language/mandarin_chinese.jpg", "https://www.udemy.com/course/complete-mandarin-chinese-course/"));
        language.add(new Recommendation("Italian Masterclass", "images/courses/language/italian_masterclass.jpg", "https://www.udemy.com/course/complete-italian-course-learn-italian-for-beginners/"));
        language.add(new Recommendation("Arabic for Beginners", "images/courses/language/arabic_beginners.jpg", "https://www.udemy.com/course/arabic-for-beginners-course/"));
        courseData.put("Language", language);

        data.put("Courses", courseData);
    }
    
    // HOME PAGE - Category Selection (NO LOADING SCREEN, NO SCROLL)
    private void loadHomeContentDirect() {
        contentArea.getChildren().clear();
        currentView = "Home";
        
        // Calculate available height for content
        double availableHeight = contentArea.getPrefHeight();
        
        // Enhanced hero section - takes 35% of available height
        VBox heroSection = createFixedHeroSection(availableHeight * 0.35);
        contentArea.getChildren().add(heroSection);
        
        // Enhanced category selection - takes 55% of available height
        VBox categorySection = createFixedCategorySection(availableHeight * 0.55);
        contentArea.getChildren().add(categorySection);
        
        // Decorative section - takes remaining 10%
        VBox decorativeSection = createFixedDecorativeSection(availableHeight * 0.1);
        contentArea.getChildren().add(decorativeSection);
    }
    

    private VBox createFixedHeroSection(double maxHeight) {
        VBox heroSection = new VBox();
        heroSection.setAlignment(Pos.CENTER);
        heroSection.setMaxHeight(maxHeight);
        heroSection.setPrefHeight(maxHeight);

        // Responsive padding and spacing based on allocated height
        double padding = Math.max(10, maxHeight * 0.1);
        double spacing = Math.max(8, maxHeight * 0.08);

        heroSection.setPadding(new Insets(padding));
        heroSection.setSpacing(spacing);

        // Add gradient background to hero
        LinearGradient heroGradient = new LinearGradient(
            0, 0, 1, 1, true, CycleMethod.NO_CYCLE,
            new Stop(0, Color.rgb(229, 9, 20, 0.1)),
            new Stop(0.5, Color.rgb(180, 0, 0, 0.05)),
            new Stop(1, Color.rgb(229, 9, 20, 0.1))
        );
        BackgroundFill heroFill = new BackgroundFill(heroGradient, new CornerRadii(25), null);
        heroSection.setBackground(new Background(heroFill));

        // Add glow effect
        DropShadow heroShadow = new DropShadow();
        heroShadow.setColor(Color.rgb(229, 9, 20, 0.3));
        heroShadow.setRadius(30);
        heroShadow.setSpread(0.2);
        heroSection.setEffect(heroShadow);

        // Enhanced title with responsive font size based on available height
        Label titleLabel = new Label("Smart Life Recommender");
        titleLabel.fontProperty().bind(Bindings.createObjectBinding(() -> {
            double fontSize = Math.max(18, Math.min(maxHeight * 0.12, 36));
            return Font.font("Segoe UI", FontWeight.BOLD, fontSize);
        }, primaryStage.widthProperty()));
        titleLabel.setTextFill(TEXT_COLOR);

        // Add glow effect to title
        Glow titleGlow = new Glow();
        titleGlow.setLevel(0.4);
        titleLabel.setEffect(titleGlow);

        // Enhanced subtitle
        Label subtitleLabel = new Label("Discover your next favorite movie, book, game, anime, or course");
        subtitleLabel.fontProperty().bind(Bindings.createObjectBinding(() -> {
            double fontSize = Math.max(9, Math.min(maxHeight * 0.06, 14));
            return Font.font("Segoe UI", FontWeight.NORMAL, fontSize);
        }, primaryStage.widthProperty()));
        subtitleLabel.setTextFill(Color.rgb(200, 200, 200));
        subtitleLabel.setWrapText(true);
        subtitleLabel.setAlignment(Pos.CENTER);

        // Enhanced description with red theme
        Label descLabel = new Label("🔥 Powered by intelligent recommendations 🔥");
        descLabel.fontProperty().bind(Bindings.createObjectBinding(() -> {
            double fontSize = Math.max(7, Math.min(maxHeight * 0.05, 12));
            return Font.font("Segoe UI", FontWeight.NORMAL, fontSize);
        }, primaryStage.widthProperty()));
        descLabel.setTextFill(RED_LIGHT);

        // Add pulsing animation to description
        FadeTransition pulse = new FadeTransition(Duration.seconds(2), descLabel);
        pulse.setFromValue(0.7);
        pulse.setToValue(1.0);
        pulse.setCycleCount(FadeTransition.INDEFINITE);
        pulse.setAutoReverse(true);
        pulse.play();

        heroSection.getChildren().addAll(titleLabel, subtitleLabel, descLabel);
        return heroSection;
    }

    private VBox createFixedCategorySection(double maxHeight) {
        VBox section = new VBox();
        section.setAlignment(Pos.CENTER);
        section.setMaxHeight(maxHeight);
        section.setPrefHeight(maxHeight);

        // Responsive spacing based on allocated height
        double spacing = Math.max(10, maxHeight * 0.08);
        section.setSpacing(spacing);

        // Enhanced section title
        Label sectionTitle = new Label("🎯 Choose Your Adventure");
        sectionTitle.fontProperty().bind(Bindings.createObjectBinding(() -> {
            double fontSize = Math.max(14, Math.min(maxHeight * 0.06, 22));
            return Font.font("Segoe UI", FontWeight.BOLD, fontSize);
        }, primaryStage.widthProperty()));
        sectionTitle.setTextFill(TEXT_COLOR);

        // Add glow effect
        Glow titleGlow = new Glow();
        titleGlow.setLevel(0.3);
        sectionTitle.setEffect(titleGlow);

        // Enhanced category grid with BIGGER CARDS
        GridPane categoryGrid = new GridPane();
        categoryGrid.setAlignment(Pos.CENTER);

        // Calculate available space for grid - MAKE CARDS BIGGER
        double gridHeight = maxHeight - spacing - 40; // Subtract title and spacing
        double cardHeight = Math.max(100, gridHeight * 0.4); // Increased from 0.25 to 0.4
        double cardWidth = Math.max(160, primaryStage.getWidth() * 0.16); // Increased from 0.12 to 0.16

        // Responsive gaps
        double gap = Math.max(10, primaryStage.getWidth() * 0.015);
        categoryGrid.setHgap(gap);
        categoryGrid.setVgap(gap);

        String[] categories = {"Movies", "Books", "Games", "Anime", "Courses"};
        String[] descriptions = {
            "🎬 Action, Comedy, Sci-Fi, Romance",
            "📚 Mystery, Fantasy, Thriller, Non-Fiction",
            "🎮 Adventure, Racing, Shooter, Strategy",
            "🌸 Shonen, Seinen, Romance, Slice of Life",
            "🎓 Programming, Design, Business, Language"
        };
        String[] emojis = {"🎬", "📚", "🎮", "🌸", "🎓"};

        for (int i = 0; i < categories.length; i++) {
            VBox categoryCard = createFixedCategoryCard(categories[i], descriptions[i], emojis[i], cardWidth, cardHeight);
            int col = i % 3;
            int row = i / 3;
            categoryGrid.add(categoryCard, col, row);
        }

        section.getChildren().addAll(sectionTitle, categoryGrid);
        return section;
    }

    private VBox createFixedCategoryCard(String category, String description, String emoji, double cardWidth, double cardHeight) {
        VBox card = new VBox();
        card.setAlignment(Pos.CENTER);
        card.setCursor(Cursor.HAND);
        card.setPrefSize(cardWidth, cardHeight);
        card.setMaxSize(cardWidth, cardHeight);
        card.setMinSize(cardWidth, cardHeight);

        // Responsive spacing and padding based on card size
        double padding = Math.max(8, cardHeight * 0.1);
        double spacing = Math.max(5, cardHeight * 0.08);

        card.setPadding(new Insets(padding));
        card.setSpacing(spacing);

        // Enhanced gradient background with red theme
        LinearGradient cardGradient = new LinearGradient(
            0, 0, 1, 1, true, CycleMethod.NO_CYCLE,
            new Stop(0, Color.rgb(45, 45, 55)),
            new Stop(0.5, Color.rgb(35, 35, 45)),
            new Stop(1, Color.rgb(25, 25, 35))
        );
        BackgroundFill cardFill = new BackgroundFill(cardGradient, new CornerRadii(20), null);
        card.setBackground(new Background(cardFill));

        // Enhanced shadow
        DropShadow cardShadow = new DropShadow();
        cardShadow.setColor(Color.rgb(0, 0, 0, 0.4));
        cardShadow.setRadius(15);
        cardShadow.setOffsetY(8);
        card.setEffect(cardShadow);

        // Responsive emoji size - BIGGER
        Label emojiLabel = new Label(emoji);
        double emojiSize = Math.max(24, cardHeight * 0.25); // Increased from 0.2 to 0.25
        emojiLabel.setFont(Font.font(emojiSize));

        // Enhanced title with responsive font - BIGGER
        Label titleLabel = new Label(category);
        double titleSize = Math.max(14, cardHeight * 0.15); // Increased from 0.12 to 0.15
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, titleSize));
        titleLabel.setTextFill(TEXT_COLOR);

        // Enhanced description with responsive font - BIGGER
        Label descLabel = new Label(description);
        double descSize = Math.max(9, cardHeight * 0.08); // Increased from 0.06 to 0.08
        descLabel.setFont(Font.font("Segoe UI", FontWeight.NORMAL, descSize));
        descLabel.setTextFill(Color.rgb(190, 190, 190));
        descLabel.setWrapText(true);
        descLabel.setAlignment(Pos.CENTER);
        descLabel.setMaxWidth(cardWidth - padding * 2);

        card.getChildren().addAll(emojiLabel, titleLabel, descLabel);

        // Enhanced hover effects with red theme
        card.setOnMouseEntered(e -> {
            LinearGradient hoverGradient = new LinearGradient(
                0, 0, 1, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.rgb(229, 9, 20, 0.8)),
                new Stop(0.5, Color.rgb(255, 100, 100, 0.6)),
                new Stop(1, Color.rgb(229, 9, 20, 0.8))
            );
            BackgroundFill hoverFill = new BackgroundFill(hoverGradient, new CornerRadii(20), null);
            card.setBackground(new Background(hoverFill));

            // Scale animation
            ScaleTransition scaleUp = new ScaleTransition(Duration.millis(200), card);
            scaleUp.setToX(1.05);
            scaleUp.setToY(1.05);
            scaleUp.play();

            // Enhanced glow
            Glow hoverGlow = new Glow();
            hoverGlow.setLevel(0.6);
            card.setEffect(hoverGlow);
        });

        card.setOnMouseExited(e -> {
            card.setBackground(new Background(cardFill));

            // Scale back
            ScaleTransition scaleDown = new ScaleTransition(Duration.millis(200), card);
            scaleDown.setToX(1.0);
            scaleDown.setToY(1.0);
            scaleDown.play();

            card.setEffect(cardShadow);
        });

        // Click handler
        card.setOnMouseClicked(e -> {
            currentCategory = category;
            loadCategoryGenres(category);
        });

        return card;
    }

    private VBox createFixedDecorativeSection(double maxHeight) {
        VBox section = new VBox();
        section.setAlignment(Pos.CENTER);
        section.setMaxHeight(maxHeight);
        section.setPrefHeight(maxHeight);

        // Responsive spacing based on allocated height
        double spacing = Math.max(5, maxHeight * 0.2);
        section.setSpacing(spacing);

        // Decorative separator with responsive width
        Rectangle separator = new Rectangle();
        separator.widthProperty().bind(Bindings.createDoubleBinding(() -> {
            double width = primaryStage.getWidth();
            return Math.max(200, Math.min(400, width * 0.3));
        }, primaryStage.widthProperty()));
        separator.setHeight(Math.max(2, maxHeight * 0.15));

        LinearGradient sepGradient = new LinearGradient(
            0, 0, 1, 0, true, CycleMethod.NO_CYCLE,
            new Stop(0, Color.TRANSPARENT),
            new Stop(0.5, RED_ACCENT),
            new Stop(1, Color.TRANSPARENT)
        );
        separator.setFill(sepGradient);

        // Stats or info with responsive font
        Label statsLabel = new Label("🔥 Over 160+ Curated Recommendations 🔥");
        double fontSize = Math.max(8, maxHeight * 0.3);
        statsLabel.setFont(Font.font("Segoe UI", FontWeight.NORMAL, fontSize));
        statsLabel.setTextFill(Color.rgb(180, 180, 180));

        section.getChildren().addAll(separator, statsLabel);
        return section;
    }

    // CATEGORY PAGE -  Genre Selection (NO SCROLL)
    private void loadCategoryGenres(String category) {
        showFixedLoadingAnimation();

        Task<Void> loadingTask = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                Thread.sleep(1000);
                return null;
            }
        };

        loadingTask.setOnSucceeded(e -> {
            Platform.runLater(() -> {
                contentArea.getChildren().clear();
                currentView = "Category";

                // Calculate available height for content
                double availableHeight = contentArea.getPrefHeight();

                // Enhanced header - takes 25% of available height
                VBox headerSection = createFixedCategoryHeader(category, availableHeight * 0.25);
                contentArea.getChildren().add(headerSection);

                // Enhanced genre selection - takes 75% of available height
                VBox genreSection = createFixedGenreSection(category, availableHeight * 0.75);
                contentArea.getChildren().add(genreSection);
            });
        });

        new Thread(loadingTask).start();
    }

    private VBox createFixedCategoryHeader(String category, double maxHeight) {
        VBox header = new VBox();
        header.setMaxHeight(maxHeight);
        header.setPrefHeight(maxHeight);
        header.setAlignment(Pos.TOP_LEFT);

        // Responsive spacing and padding based on allocated height
        double padding = Math.max(10, maxHeight * 0.1);
        double spacing = Math.max(8, maxHeight * 0.15);

        header.setPadding(new Insets(padding, 0, padding, 0));
        header.setSpacing(spacing);

        // Enhanced title with emoji and responsive font
        String emoji = getEmojiForCategory(category);
        Label titleLabel = new Label(emoji + " " + category + " Genres");
        double titleSize = Math.max(16, maxHeight * 0.28);
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, titleSize));
        titleLabel.setTextFill(TEXT_COLOR);

        // Add glow effect
        Glow titleGlow = new Glow();
        titleGlow.setLevel(0.4);
        titleLabel.setEffect(titleGlow);

        // Enhanced subtitle with responsive font
        Label subtitleLabel = new Label("Choose a genre to explore amazing recommendations");
        double subtitleSize = Math.max(9, maxHeight * 0.12);
        subtitleLabel.setFont(Font.font("Segoe UI", FontWeight.NORMAL, subtitleSize));
        subtitleLabel.setTextFill(Color.rgb(190, 190, 190));

        header.getChildren().addAll(titleLabel, subtitleLabel);
        return header;
    }

    private String getEmojiForCategory(String category) {
        String emoji;
        switch (category) {
            case "Movies":
                emoji = "🎬";
                break;
            case "Books":
                emoji = "📚";
                break;
            case "Games":
                emoji = "🎮";
                break;
            case "Anime":
                emoji = "🌸";
                break;
            case "Courses":
                emoji = "🎓";
                break;
            default:
                emoji = "⭐";
                break;
        }
        return emoji;
    }

    private VBox createFixedGenreSection(String category, double maxHeight) {
        VBox section = new VBox();
        section.setAlignment(Pos.CENTER);
        section.setMaxHeight(maxHeight);
        section.setPrefHeight(maxHeight);

        // Responsive spacing
        double spacing = Math.max(15, maxHeight * 0.08);
        section.setSpacing(spacing);

        // Enhanced genre grid with fixed sizing
        GridPane genreGrid = new GridPane();
        genreGrid.setAlignment(Pos.CENTER);

        // Calculate card dimensions based on available space
        double cardHeight = Math.max(60, maxHeight * 0.25);
        double cardWidth = Math.max(140, primaryStage.getWidth() * 0.15);

        // Responsive gaps
        double gap = Math.max(15, primaryStage.getWidth() * 0.02);
        genreGrid.setHgap(gap);
        genreGrid.setVgap(gap);

        Set<String> categoryGenres = genres.get(category);
        if (categoryGenres != null) {
            int index = 0;
            for (String genre : categoryGenres) {
                VBox genreCard = createFixedGenreCard(category, genre, cardWidth, cardHeight);
                int col = index % 2;
                int row = index / 2;
                genreGrid.add(genreCard, col, row);
                index++;
            }
        }

        section.getChildren().add(genreGrid);
        return section;
    }

    private VBox createFixedGenreCard(String category, String genre, double cardWidth, double cardHeight) {
        VBox card = new VBox();
        card.setAlignment(Pos.CENTER);
        card.setCursor(Cursor.HAND);
        card.setPrefSize(cardWidth, cardHeight);
        card.setMaxSize(cardWidth, cardHeight);
        card.setMinSize(cardWidth, cardHeight);

        // Responsive spacing and padding based on card size
        double padding = Math.max(10, cardHeight * 0.15);
        double spacing = Math.max(8, cardHeight * 0.1);

        card.setPadding(new Insets(padding));
        card.setSpacing(spacing);

        // Enhanced gradient background with red theme
        LinearGradient cardGradient = new LinearGradient(
            0, 0, 1, 1, true, CycleMethod.NO_CYCLE,
            new Stop(0, Color.rgb(50, 50, 60)),
            new Stop(0.5, Color.rgb(40, 40, 50)),
            new Stop(1, Color.rgb(30, 30, 40))
        );
        BackgroundFill cardFill = new BackgroundFill(cardGradient, new CornerRadii(20), null);
        card.setBackground(new Background(cardFill));

        // Enhanced shadow
        DropShadow cardShadow = new DropShadow();
        cardShadow.setColor(Color.rgb(0, 0, 0, 0.5));
        cardShadow.setRadius(12);
        cardShadow.setOffsetY(6);
        card.setEffect(cardShadow);

        // Enhanced genre label with responsive font
        Label genreLabel = new Label(genre);
        double genreSize = Math.max(12, cardHeight * 0.2);
        genreLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, genreSize));
        genreLabel.setTextFill(TEXT_COLOR);

        // Get count and create enhanced count label
        Map<String, List<Recommendation>> categoryData = data.get(category);
        int count = 0;
        if (categoryData != null && categoryData.containsKey(genre)) {
            count = categoryData.get(genre).size();
        }

        Label countLabel = new Label("🔥 " + count + " recommendations");
        double countSize = Math.max(8, cardHeight * 0.1);
        countLabel.setFont(Font.font("Segoe UI", FontWeight.NORMAL, countSize));
        countLabel.setTextFill(RED_LIGHT);

        card.getChildren().addAll(genreLabel, countLabel);

        // Enhanced hover effects with red theme
        card.setOnMouseEntered(e -> {
            LinearGradient hoverGradient = new LinearGradient(
                0, 0, 1, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.rgb(229, 9, 20, 0.7)),
                new Stop(0.5, Color.rgb(255, 100, 100, 0.5)),
                new Stop(1, Color.rgb(229, 9, 20, 0.7))
            );
            BackgroundFill hoverFill = new BackgroundFill(hoverGradient, new CornerRadii(20), null);
            card.setBackground(new Background(hoverFill));

            // Scale animation
            ScaleTransition scaleUp = new ScaleTransition(Duration.millis(200), card);
            scaleUp.setToX(1.08);
            scaleUp.setToY(1.08);
            scaleUp.play();

            // Enhanced glow
            Glow hoverGlow = new Glow();
            hoverGlow.setLevel(0.5);
            card.setEffect(hoverGlow);
        });

        card.setOnMouseExited(e -> {
            card.setBackground(new Background(cardFill));

            ScaleTransition scaleDown = new ScaleTransition(Duration.millis(200), card);
            scaleDown.setToX(1.0);
            scaleDown.setToY(1.0);
            scaleDown.play();

            card.setEffect(cardShadow);
        });

        // Click handler
        card.setOnMouseClicked(e -> {
            currentGenre = genre;
            loadGenreRecommendations(category, genre);
        });

        return card;
    }

    // GENRE PAGE - Recommendations (NO SCROLL)
    private void loadGenreRecommendations(String category, String genre) {
        showFixedLoadingAnimation();

        Task<Void> loadingTask = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                Thread.sleep(1000);
                return null;
            }
        };

        loadingTask.setOnSucceeded(e -> {
            Platform.runLater(() -> {
                contentArea.getChildren().clear();
                currentView = "Genre";

                // Calculate available height for content
                double availableHeight = contentArea.getPrefHeight();

                // Enhanced header - takes 15% of available height (reduced from 20%)
                VBox headerSection = createFixedGenreHeader(category, genre, availableHeight * 0.15);
                contentArea.getChildren().add(headerSection);

                // Enhanced recommendations - takes 85% of available height (increased from 80%)
                VBox recommendationsSection = createFixedRecommendationsSection(category, genre, availableHeight * 0.85);
                contentArea.getChildren().add(recommendationsSection);
            });
        });

        new Thread(loadingTask).start();
    }

    private VBox createFixedGenreHeader(String category, String genre, double maxHeight) {
        VBox header = new VBox();
        header.setMaxHeight(maxHeight);
        header.setPrefHeight(maxHeight);
        header.setAlignment(Pos.TOP_LEFT);

        // Responsive spacing and padding based on allocated height
        double padding = Math.max(8, maxHeight * 0.1);
        double spacing = Math.max(6, maxHeight * 0.15);

        header.setPadding(new Insets(padding, 0, padding, 0));
        header.setSpacing(spacing);

        // Enhanced title with responsive font
        String emoji = getEmojiForCategory(category);
        Label titleLabel = new Label(emoji + " " + genre + " " + category);
        double titleSize = Math.max(14, maxHeight * 0.28);
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, titleSize));
        titleLabel.setTextFill(TEXT_COLOR);

        // Add glow effect
        Glow titleGlow = new Glow();
        titleGlow.setLevel(0.4);
        titleLabel.setEffect(titleGlow);

        // Enhanced subtitle with responsive font
        Label subtitleLabel = new Label("🎯 Click on any recommendation to explore more");
        double subtitleSize = Math.max(7, maxHeight * 0.1);
        subtitleLabel.setFont(Font.font("Segoe UI", FontWeight.NORMAL, subtitleSize));
        subtitleLabel.setTextFill(Color.rgb(190, 190, 190));

        header.getChildren().addAll(titleLabel, subtitleLabel);
        return header;
    }

    private VBox createFixedRecommendationsSection(String category, String genre, double maxHeight) {
        VBox section = new VBox();
        section.setAlignment(Pos.CENTER);
        section.setMaxHeight(maxHeight);
        section.setPrefHeight(maxHeight);

        // Responsive spacing
        double spacing = Math.max(10, maxHeight * 0.03);
        section.setSpacing(spacing);

        // Enhanced recommendations grid with OPTIMAL CARD SIZING
        GridPane recommendationsGrid = new GridPane();
        recommendationsGrid.setAlignment(Pos.CENTER);

        // Calculate how many recommendations we have
        Map<String, List<Recommendation>> categoryData = data.get(category);
        int totalRecommendations = 0;
        if (categoryData != null && categoryData.containsKey(genre)) {
            totalRecommendations = categoryData.get(genre).size();
        }

        // OPTIMAL LAYOUT: 4 columns, 2 rows for 8 recommendations
        int columns = 4;
        int rows = 2; // Fixed to 2 rows for better card size
        
        // Calculate available space for cards
        double availableWidth = primaryStage.getWidth() - (sidebarVisible ? 310 : 60); // Account for sidebar and padding
        double availableHeight = maxHeight - spacing * 3; // Account for section spacing
        
        // Calculate BIGGER card dimensions that still fit on screen
        double cardWidth = Math.max(200, (availableWidth - (columns - 1) * 20) / columns); // Increased minimum
        
        // Calculate card height for 2 rows with good spacing
        double totalVerticalGap = (rows - 1) * 20; // Gap between rows
        double cardHeight = Math.max(240, (availableHeight - totalVerticalGap) / rows); // Increased minimum
        
        // Ensure cards don't get too big on large screens
        cardWidth = Math.min(cardWidth, 280);
        cardHeight = Math.min(cardHeight, 320);

        // Set responsive gaps
        double gap = 20; // Fixed gap for consistent spacing
        recommendationsGrid.setHgap(gap);
        recommendationsGrid.setVgap(gap);

        // Add cards to grid
        if (categoryData != null && categoryData.containsKey(genre)) {
            List<Recommendation> recommendations = categoryData.get(genre);

            for (int i = 0; i < recommendations.size(); i++) {
                VBox card = createFixedRecommendationCard(recommendations.get(i), cardWidth, cardHeight);
                int col = i % columns;
                int row = i / columns;
                recommendationsGrid.add(card, col, row);
            }
        }

        section.getChildren().add(recommendationsGrid);
        return section;
    }

    // ENHANCED RECOMMENDATION CARD WITH POPUP FUNCTIONALITY
    private VBox createFixedRecommendationCard(Recommendation rec, double cardWidth, double cardHeight) {
        VBox card = new VBox();
        card.setAlignment(Pos.TOP_CENTER);
        card.setCursor(Cursor.HAND);
        card.setPrefSize(cardWidth, cardHeight);
        card.setMaxSize(cardWidth, cardHeight);
        card.setMinSize(cardWidth, cardHeight);

        // Enhanced gradient background with red theme
        LinearGradient cardGradient = new LinearGradient(
            0, 0, 1, 1, true, CycleMethod.NO_CYCLE,
            new Stop(0, Color.rgb(40, 40, 50)),
            new Stop(0.5, Color.rgb(30, 30, 40)),
            new Stop(1, Color.rgb(20, 20, 30))
        );
        BackgroundFill cardFill = new BackgroundFill(cardGradient, new CornerRadii(15), null);
        card.setBackground(new Background(cardFill));

        // Enhanced shadow
        DropShadow cardShadow = new DropShadow();
        cardShadow.setColor(Color.rgb(0, 0, 0, 0.6));
        cardShadow.setRadius(15);
        cardShadow.setOffsetY(8);
        card.setEffect(cardShadow);

        // BIGGER image sizing for better visibility
        ImageView imageView = new ImageView();
        double imageHeight = cardHeight * 0.72; // Increased image portion

        imageView.setFitWidth(cardWidth - 12); // Proper padding from card edges
        imageView.setFitHeight(imageHeight);
        imageView.setPreserveRatio(true);
        imageView.setSmooth(true);

        Image image = loadLocalImage(rec.imagePath, cardWidth - 12, imageHeight);

        if (image != null) {
            imageView.setImage(image);

            // Add rounded corners to image
            Rectangle clip = new Rectangle(cardWidth - 12, imageHeight);
            clip.setArcWidth(15);
            clip.setArcHeight(15);
            imageView.setClip(clip);

            // Center the image in the card
            VBox imageContainer = new VBox();
            imageContainer.setAlignment(Pos.CENTER);
            imageContainer.setPadding(new Insets(6));
            imageContainer.getChildren().add(imageView);
            card.getChildren().add(imageContainer);
        } else {
            // Enhanced fallback with bigger sizing
            Label errorLabel = new Label("🖼️\nImage not available");
            errorLabel.setTextFill(Color.rgb(150, 150, 150));
            errorLabel.setPrefSize(cardWidth - 12, imageHeight);
            errorLabel.setMaxSize(cardWidth - 12, imageHeight);
            errorLabel.setMinSize(cardWidth - 12, imageHeight);
            errorLabel.setAlignment(Pos.CENTER);
            double errorFontSize = Math.max(14, cardHeight * 0.06); // Bigger error font
            errorLabel.setFont(Font.font("Segoe UI", FontWeight.NORMAL, errorFontSize));
            errorLabel.setStyle("-fx-background-color: rgb(50, 50, 60); -fx-border-color: rgb(80, 80, 90); -fx-border-width: 1; -fx-border-radius: 10; -fx-background-radius: 10;");
            
            VBox errorContainer = new VBox();
            errorContainer.setAlignment(Pos.CENTER);
            errorContainer.setPadding(new Insets(6));
            errorContainer.getChildren().add(errorLabel);
            card.getChildren().add(errorContainer);
        }

        // BIGGER text content with proper proportions
        VBox textPanel = new VBox();
        textPanel.setAlignment(Pos.CENTER);
        double textHeight = cardHeight * 0.28; // Proper text space
        textPanel.setPrefHeight(textHeight);
        textPanel.setMaxHeight(textHeight);
        textPanel.setMinSize(cardWidth, textHeight);

        double textPadding = Math.max(10, cardWidth * 0.05); // Good padding
        double textSpacing = Math.max(5, textHeight * 0.12); // Good spacing
        textPanel.setPadding(new Insets(textPadding));
        textPanel.setSpacing(textSpacing);

        // BIGGER and MORE VISIBLE title
        Label titleLabel = new Label(rec.title);
        double titleSize = Math.max(14, cardHeight * 0.06); // Much bigger font
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, titleSize));
        titleLabel.setTextFill(TEXT_COLOR);
        titleLabel.setWrapText(true);
        titleLabel.setMaxWidth(cardWidth - textPadding * 2);
        titleLabel.setMaxHeight(textHeight * 0.65); // Adequate space for title
        titleLabel.setAlignment(Pos.CENTER);

        // BIGGER hint label
        Label hintLabel = new Label("🔥 Click to explore →");
        double hintSize = Math.max(11, cardHeight * 0.04); // Bigger hint font
        hintLabel.setFont(Font.font("Segoe UI", FontWeight.NORMAL, hintSize));
        hintLabel.setTextFill(Color.rgb(180, 180, 180));
        hintLabel.setAlignment(Pos.CENTER);

        textPanel.getChildren().addAll(titleLabel, hintLabel);
        card.getChildren().add(textPanel);

        // Enhanced hover effects with red theme
        card.setOnMouseEntered(e -> {
            LinearGradient hoverGradient = new LinearGradient(
                0, 0, 1, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.rgb(229, 9, 20, 0.8)),
                new Stop(0.5, Color.rgb(180, 0, 0, 0.6)),
                new Stop(1, Color.rgb(229, 9, 20, 0.8))
            );
            BackgroundFill hoverFill = new BackgroundFill(hoverGradient, new CornerRadii(15), null);
            card.setBackground(new Background(hoverFill));
            
            // Scale animation
            ScaleTransition scaleUp = new ScaleTransition(Duration.millis(200), card);
            scaleUp.setToX(1.04); // Good hover scale
            scaleUp.setToY(1.04);
            scaleUp.play();
            
            // Enhanced glow
            Glow hoverGlow = new Glow();
            hoverGlow.setLevel(0.6);
            card.setEffect(hoverGlow);
            
            hintLabel.setTextFill(RED_LIGHT);
        });
        
        card.setOnMouseExited(e -> {
            card.setBackground(new Background(cardFill));
            
            ScaleTransition scaleDown = new ScaleTransition(Duration.millis(200), card);
            scaleDown.setToX(1.0);
            scaleDown.setToY(1.0);
            scaleDown.play();
            
            card.setEffect(cardShadow);
            hintLabel.setTextFill(Color.rgb(180, 180, 180));
        });

        // ENHANCED CLICK HANDLER WITH POPUP
        card.setOnMouseClicked(e -> {
            // Add click animation
            ScaleTransition clickScale = new ScaleTransition(Duration.millis(100), card);
            clickScale.setToX(0.95);
            clickScale.setToY(0.95);
            clickScale.setOnFinished(event -> {
                ScaleTransition scaleBack = new ScaleTransition(Duration.millis(100), card);
                scaleBack.setToX(1.0);
                scaleBack.setToY(1.0);
                scaleBack.play();
            });
            clickScale.play();
            
            // Show enhanced popup dialog
            showRecommendationDialog(rec);
        });

        return card;
    }

    // NEW: Enhanced dialog method
    private void showRecommendationDialog(Recommendation rec) {
        // Create custom dialog
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.initOwner(primaryStage);
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initStyle(StageStyle.DECORATED);
        dialog.setTitle("🎬 " + rec.title);
        dialog.setHeaderText("Choose an action:");
        
        // Create custom buttons
        ButtonType goToLinkButton = new ButtonType("🌐 Go to Link", ButtonBar.ButtonData.OK_DONE);
        ButtonType watchTrailerButton = new ButtonType("🎥 Watch Trailer", ButtonBar.ButtonData.APPLY);
        ButtonType cancelButton = new ButtonType("❌ Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        
        // Add buttons based on availability
        if (rec.trailerUrl != null && !rec.trailerUrl.isEmpty()) {
            dialog.getDialogPane().getButtonTypes().addAll(goToLinkButton, watchTrailerButton, cancelButton);
        } else {
            dialog.getDialogPane().getButtonTypes().addAll(goToLinkButton, cancelButton);
        }
        
        // Style the dialog
        dialog.getDialogPane().setStyle(
            "-fx-background-color: rgb(25, 25, 35); " +
            "-fx-text-fill: white; " +
            "-fx-font-family: 'Segoe UI'; " +
            "-fx-font-size: 14px;"
        );
        
        // Create content
        VBox content = new VBox();
        content.setSpacing(15);
        content.setPadding(new Insets(20));
        content.setAlignment(Pos.CENTER);
        
        // Add recommendation image if available
        ImageView dialogImage = new ImageView();
        Image image = loadLocalImage(rec.imagePath, 200, 150);
        if (image != null) {
            dialogImage.setImage(image);
            dialogImage.setFitWidth(200);
            dialogImage.setFitHeight(150);
            dialogImage.setPreserveRatio(true);
            dialogImage.setSmooth(true);
            
            // Add rounded corners
            Rectangle clip = new Rectangle(200, 150);
            clip.setArcWidth(15);
            clip.setArcHeight(15);
            dialogImage.setClip(clip);
            
            content.getChildren().add(dialogImage);
        }
        
        // Add title and description
        Label titleLabel = new Label(rec.title);
        titleLabel.setStyle("-fx-text-fill: white; -fx-font-size: 18px; -fx-font-weight: bold;");
        titleLabel.setWrapText(true);
        titleLabel.setMaxWidth(300);
        titleLabel.setAlignment(Pos.CENTER);
        
        Label descLabel = new Label("🔥 Choose your action below:");
        descLabel.setStyle("-fx-text-fill: rgb(200, 200, 200); -fx-font-size: 12px;");
        descLabel.setAlignment(Pos.CENTER);
        
        content.getChildren().addAll(titleLabel, descLabel);
        dialog.getDialogPane().setContent(content);
        
        // Handle button clicks
        Optional<ButtonType> result = dialog.showAndWait();
        
        if (result.isPresent()) {
            if (result.get() == goToLinkButton) {
                // Go to external link
                if (rec.redirectUrl != null && !rec.redirectUrl.isEmpty()) {
                    openURL(rec.redirectUrl);
                } else {
                    showAlert("No Link Available", "Sorry, no external link is available for this recommendation.");
                }
            } else if (result.get() == watchTrailerButton) {
                // Watch trailer in WebView
                if (rec.trailerUrl != null && !rec.trailerUrl.isEmpty()) {
                    openTrailerWindow(rec);
                } else {
                    showAlert("No Trailer Available", "Sorry, no trailer is available for this recommendation.");
                }
            }
            // Cancel button does nothing (dialog closes automatically)
        }
    }

    // NEW: Method to open trailer in WebView window
    // ENHANCED: Method with JavaScript control for better video stopping
private void openTrailerWindow(Recommendation rec) {
    // Create new stage for trailer
    Stage trailerStage = new Stage();
    trailerStage.initOwner(primaryStage);
    trailerStage.initModality(Modality.NONE);
    trailerStage.setTitle("🎥 Trailer: " + rec.title);
    
    // Create WebView
    WebView webView = new WebView();
    WebEngine webEngine = webView.getEngine();
    
    // Enable JavaScript (required for YouTube)
    webEngine.setJavaScriptEnabled(true);
    
    // Set WebView size
    webView.setPrefSize(800, 600);
    webView.setMinSize(640, 480);
    
    // Create layout
    VBox trailerLayout = new VBox();
    trailerLayout.setAlignment(Pos.CENTER);
    trailerLayout.setStyle("-fx-background-color: black;");
    
    // Add header with title and close button
    HBox header = new HBox();
    header.setAlignment(Pos.CENTER_LEFT);
    header.setPadding(new Insets(10));
    header.setSpacing(10);
    header.setStyle("-fx-background-color: rgb(25, 25, 35);");
    
    Label headerTitle = new Label("🎥 " + rec.title + " - Trailer");
    headerTitle.setStyle("-fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold;");
    
    Button closeButton = new Button("❌ Close");
    closeButton.setStyle(
        "-fx-background-color: rgb(229, 9, 20); " +
        "-fx-text-fill: white; " +
        "-fx-font-weight: bold; " +
        "-fx-background-radius: 5;"
    );
    
    // Method to stop video playback
    Runnable stopVideo = () -> {
        try {
            // Try to pause the video using JavaScript
            webEngine.executeScript(
                "var videos = document.getElementsByTagName('video');" +
                "for(var i = 0; i < videos.length; i++) {" +
                "    videos[i].pause();" +
                "    videos[i].currentTime = 0;" +
                "}" +
                "var iframes = document.getElementsByTagName('iframe');" +
                "for(var i = 0; i < iframes.length; i++) {" +
                "    iframes[i].src = 'about:blank';" +
                "}"
            );
        } catch (Exception e) {
            System.out.println("Could not execute JavaScript to stop video: " + e.getMessage());
        }
        
        // Load blank page as fallback
        webEngine.load("about:blank");
    };
    
    // Close button action
    closeButton.setOnAction(e -> {
        stopVideo.run();
        trailerStage.close();
    });
    
    Region spacer = new Region();
    HBox.setHgrow(spacer, Priority.ALWAYS);
    
    header.getChildren().addAll(headerTitle, spacer, closeButton);
    
    // Add components to layout
    trailerLayout.getChildren().addAll(header, webView);
    VBox.setVgrow(webView, Priority.ALWAYS);
    
    // Create scene
    Scene trailerScene = new Scene(trailerLayout, 800, 650);
    trailerStage.setScene(trailerScene);
    
    // Window close event handlers
    trailerStage.setOnCloseRequest(e -> {
        stopVideo.run();
    });
    
    trailerStage.setOnHiding(e -> {
        stopVideo.run();
    });
    
    // Load the trailer URL with enhanced HTML wrapper
    String embedUrl = rec.getEmbedUrl();
    if (embedUrl != null) {
        // Create HTML wrapper with better video control
        String htmlContent = 
            "<!DOCTYPE html>" +
            "<html>" +
            "<head>" +
            "    <style>" +
            "        body { margin: 0; padding: 0; background: black; }" +
            "        iframe { width: 100%; height: 100vh; border: none; }" +
            "    </style>" +
            "</head>" +
            "<body>" +
            "    <iframe src='" + embedUrl + "' " +
            "            allow='accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture' " +
            "            allowfullscreen>" +
            "    </iframe>" +
            "</body>" +
            "</html>";
        
        webEngine.loadContent(htmlContent);
    } else {
        // Fallback: show error message
        webEngine.loadContent(
            "<html><body style='background-color: #1a1a2e; color: white; font-family: Arial; text-align: center; padding: 50px;'>" +
            "<h2>🚫 Trailer Not Available</h2>" +
            "<p>Sorry, the trailer for <strong>" + rec.title + "</strong> could not be loaded.</p>" +
            "<p>Please try the external link instead.</p>" +
            "</body></html>"
        );
    }
    
    // Handle WebView errors
    webEngine.setOnError(event -> {
        System.out.println("WebView error: " + event.getMessage());
        webEngine.loadContent(
            "<html><body style='background-color: #1a1a2e; color: white; font-family: Arial; text-align: center; padding: 50px;'>" +
            "<h2>🚫 Error Loading Trailer</h2>" +
            "<p>There was an error loading the trailer for <strong>" + rec.title + "</strong>.</p>" +
            "<p>Please check your internet connection and try again.</p>" +
            "</body></html>"
        );
    });
    
    // Show the trailer window
    trailerStage.show();
    
    // Center the trailer window
    trailerStage.setX(primaryStage.getX() + (primaryStage.getWidth() - trailerStage.getWidth()) / 2);
    trailerStage.setY(primaryStage.getY() + (primaryStage.getHeight() - trailerStage.getHeight()) / 2);
}

    // NEW: Helper method to show alerts
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.initOwner(primaryStage);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        
        // Style the alert
        alert.getDialogPane().setStyle(
            "-fx-background-color: rgb(25, 25, 35); " +
            "-fx-text-fill: white; " +
            "-fx-font-family: 'Segoe UI';"
        );
        
        alert.showAndWait();
    }
    
    private void showFixedLoadingAnimation() {
        contentArea.getChildren().clear();
        
        VBox loadingContainer = new VBox();
        loadingContainer.setAlignment(Pos.CENTER);
        loadingContainer.setSpacing(20);
        loadingContainer.prefHeightProperty().bind(contentArea.prefHeightProperty());
        loadingContainer.prefWidthProperty().bind(contentArea.prefWidthProperty());
        
        // Enhanced loading spinner with red theme
        Label spinnerLabel = new Label("🔄");
        spinnerLabel.setFont(Font.font(60));
        spinnerLabel.setTextFill(RED_ACCENT);
        
        // Rotation animation
        Timeline rotationAnimation = new Timeline(
            new KeyFrame(Duration.ZERO, e -> spinnerLabel.setRotate(0)),
            new KeyFrame(Duration.seconds(1), e -> spinnerLabel.setRotate(360))
        );
        rotationAnimation.setCycleCount(Timeline.INDEFINITE);
        rotationAnimation.play();
        
        // Enhanced loading text
        Label loadingLabel = new Label("Loading amazing recommendations...");
        loadingLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        loadingLabel.setTextFill(TEXT_COLOR);
        
        // Pulsing animation for text
        FadeTransition pulse = new FadeTransition(Duration.seconds(1), loadingLabel);
        pulse.setFromValue(0.5);
        pulse.setToValue(1.0);
        pulse.setCycleCount(FadeTransition.INDEFINITE);
        pulse.setAutoReverse(true);
        pulse.play();
        
        loadingContainer.getChildren().addAll(spinnerLabel, loadingLabel);
        contentArea.getChildren().add(loadingContainer);
    }
    
    private Image loadLocalImage(String imagePath, double width, double height) {
        try {
            File imageFile = new File(imagePath);
            if (imageFile.exists()) {
                return new Image(imageFile.toURI().toString(), width, height, true, true);
            } else {
                // Try with different extensions
                String[] extensions = {".jpg", ".jpeg", ".png", ".gif", ".bmp"};
                String basePath = imagePath.substring(0, imagePath.lastIndexOf('.'));
                
                for (String ext : extensions) {
                    File altFile = new File(basePath + ext);
                    if (altFile.exists()) {
                        return new Image(altFile.toURI().toString(), width, height, true, true);
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Error loading image: " + imagePath + " - " + e.getMessage());
        }
        return null;
    }
    
    private void openURL(String url) {
        try {
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().browse(new URI(url));
            } else {
                System.out.println("Desktop not supported. URL: " + url);
            }
        } catch (Exception e) {
            System.out.println("Error opening URL: " + e.getMessage());
        }
    }
    
    // Enhanced Recommendation class with trailer support
    public static class Recommendation {
        public String title;
        public String imagePath;
        public String redirectUrl;
        public String trailerUrl; // NEW: YouTube trailer URL
        
        public Recommendation(String title, String imagePath, String redirectUrl) {
            this.title = title;
            this.imagePath = imagePath;
            this.redirectUrl = redirectUrl;
            this.trailerUrl = null; // Default no trailer
        }
        
        // NEW: Constructor with trailer support
        public Recommendation(String title, String imagePath, String redirectUrl, String trailerUrl) {
            this.title = title;
            this.imagePath = imagePath;
            this.redirectUrl = redirectUrl;
            this.trailerUrl = trailerUrl;
        }
        
        // Helper method to get YouTube embed URL
        public String getEmbedUrl() {
            if (trailerUrl != null && trailerUrl.contains("youtube.com/watch?v=")) {
                String videoId = trailerUrl.split("v=")[1];
                if (videoId.contains("&")) {
                    videoId = videoId.split("&")[0];
                }
                return "https://www.youtube.com/embed/" + videoId + "?autoplay=1";
            } else if (trailerUrl != null && trailerUrl.contains("youtu.be/")) {
                String videoId = trailerUrl.split("youtu.be/")[1];
                if (videoId.contains("?")) {
                    videoId = videoId.split("\\?")[0];
                }
                return "https://www.youtube.com/embed/" + videoId + "?autoplay=1";
            }
            return trailerUrl; // Return as-is if not YouTube or already embed format
        }
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}