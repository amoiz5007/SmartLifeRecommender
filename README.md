### Smart Life Recommender FX

A sophisticated JavaFX desktop application that provides intelligent recommendations for movies, books, games, anime, and online courses. Built with a modern Netflix-inspired UI design featuring smooth animations, responsive layouts, and an intuitive user experience.

## 🌟 Features

### 🎬 Multi-Category Recommendations

- **Movies**: Action, Comedy, Sci-Fi, Romance
- **Books**: Mystery, Fantasy, Thriller, Non-Fiction
- **Games**: Adventure, Racing, Shooter, Strategy
- **Anime**: Shonen, Seinen, Romance, Slice of Life
- **Courses**: Programming, Design, Business, Language


### 🎨 Modern UI/UX

- **Netflix-Style Design**: Dark theme with red accents
- **Responsive Layout**: Adapts to different window sizes
- **Smooth Animations**: Hover effects, transitions, and loading animations
- **Interactive Elements**: Clickable cards with external link integration
- **Video Intro**: Engaging startup experience


### 🚀 Advanced Features

- **Collapsible Sidebar**: Clean navigation with animated menu
- **Team Page**: Meet the development team with profile links
- **External Integration**: Direct links to IMDb, Goodreads, Steam, etc.
- **Image Support**: Local image loading with fallback placeholders
- **Loading States**: Professional loading animations


## 📋 Prerequisites

- **Java 11 or higher**
- **JavaFX SDK** (if not included with your JDK)
- **IDE**: IntelliJ IDEA, Eclipse, or VS Code with Java extensions


## 🛠️ Installation & Setup

### 1. Clone the Repository

```shellscript
git clone https://github.com/yourusername/smart-life-recommender-fx.git
cd smart-life-recommender-fx
```

### 2. Project Structure

```plaintext
smart-life-recommender-fx/
├── src/
│   └── SmartLifeRecommenderFX.java
├── images/
│   ├── movies/
│   ├── books/
│   ├── games/
│   ├── anime/
│   ├── courses/
│   └── team/
├── introofapp.mp4
├── logo.png
└── README.md
```

### 3. Add Required Assets

- Place your intro video as `introofapp.mp4` in the root directory
- Add your logo as `logo.png` in the root directory
- Organize recommendation images in respective category folders under `images/`


### 4. JavaFX Setup

If JavaFX is not included with your JDK:

**Download JavaFX SDK:**

- Visit [OpenJFX](https://openjfx.io/)
- Download JavaFX SDK for your platform


**Configure IDE:**

- Add JavaFX libraries to your project classpath
- Set VM options: `--module-path /path/to/javafx/lib --add-modules javafx.controls,javafx.fxml,javafx.media`


## 🚀 Running the Application

### Command Line

```shellscript
# Compile
javac --module-path /path/to/javafx/lib --add-modules javafx.controls,javafx.media SmartLifeRecommenderFX.java

# Run
java --module-path /path/to/javafx/lib --add-modules javafx.controls,javafx.media SmartLifeRecommenderFX
```

### IDE

1. Open the project in your IDE
2. Configure JavaFX module path and VM options
3. Run `SmartLifeRecommenderFX.java`


## 🎮 Usage

### Navigation

1. **Startup**: Watch the intro video (click to skip)
2. **Home Page**: Select from 5 main categories
3. **Category Page**: Choose specific genres
4. **Recommendations**: Browse curated content with direct links
5. **Team Page**: Meet the development team


### Interactions

- **Click recommendations** to visit external sites (IMDb, Goodreads, etc.)
- **Use sidebar** for quick navigation between categories
- **Hover effects** provide visual feedback
- **Responsive design** adapts to window resizing


## 🏗️ Architecture

### Core Components

- **Main Application**: `SmartLifeRecommenderFX.java`
- **Data Structure**: HashMap-based recommendation storage
- **UI Components**: Custom VBox cards with gradient backgrounds
- **Navigation**: Sidebar with animated transitions
- **Media Support**: Video intro and image loading


### Design Patterns

- **MVC Architecture**: Separation of data, UI, and logic
- **Responsive Design**: Dynamic sizing based on window dimensions
- **Event-Driven**: JavaFX event handling for user interactions


## 👥 Team

- **Abdul Moiz** (BSE-242) - Team Lead
- **Shahzaib Khan** (BSE-308) - Team Member
- **Ammar Jaffri** (BSE-248) - Team Member


## 🛠️ Technical Details

### Technologies Used

- **JavaFX**: UI framework and multimedia support
- **Java 11+**: Core programming language
- **CSS Styling**: Custom styling for enhanced UI
- **Desktop Integration**: External URL opening


### Key Features Implementation

- **Responsive Layout**: Binding properties for dynamic sizing
- **Animation System**: JavaFX transitions and effects
- **Image Management**: Local file loading with error handling
- **External Links**: Desktop API integration


## 📝 Customization

### Adding New Recommendations

1. Update the `seedData()` method
2. Add images to appropriate category folders
3. Include external URLs for each recommendation


### Modifying UI Theme

- Adjust color constants at the top of the class
- Modify gradient definitions in UI creation methods
- Update CSS styling in `getCustomCSS()` method


### Adding New Categories

1. Update `seedGenres()` method
2. Add data in `seedData()` method
3. Update navigation menu items
4. Add appropriate emoji mappings


## 🐛 Troubleshooting

### Common Issues

- **JavaFX not found**: Ensure JavaFX is properly configured
- **Images not loading**: Check file paths and image directory structure
- **Video not playing**: Verify `introofapp.mp4` exists and is supported format
- **External links not opening**: Ensure Desktop API is supported on your system


### Performance Tips

- Use appropriate image sizes to reduce memory usage
- Consider lazy loading for large image collections
- Optimize video file size for faster startup


## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request


## 📞 Support

For support and questions:

- Create an issue on GitHub
- Contact the development team through their LinkedIn profiles


---

**© 2025 Abdul Moiz and Team - All rights reserved.**
