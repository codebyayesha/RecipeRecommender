import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import org.json.JSONArray;
import org.json.JSONObject;

public class RecipeRecommender extends JFrame {

    private static final String API_KEY = "YOUR_API_KEY"; // Replace with your API key

    private JTextField usernameField, searchField;
    private JPasswordField passwordField;
    private JButton loginButton, signUpButton, searchButton, viewFavoritesButton;
    private String currentUser;
    private JEditorPane resultArea;

    private static final String USERS_FILE = "users.txt";
    private static final String FAVORITES_FILE = "favorites.txt";

    public RecipeRecommender() {
        // Main Frame setup
        setTitle("Recipe Recommender - Login");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        setLocationRelativeTo(null);

        // Login Panel
        JPanel loginPanel = new JPanel(new GridBagLayout());
        loginPanel.setBackground(new Color(240, 248, 255));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Title
        JLabel titleLabel = new JLabel("Recipe Recommender", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(new Color(0, 102, 204));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        loginPanel.add(titleLabel, gbc);

        // Username field
        gbc.gridwidth = 1;
        gbc.gridy = 1;
        gbc.gridx = 0;
        loginPanel.add(new JLabel("Username:"), gbc);

        gbc.gridx = 1;
        usernameField = new JTextField(15);
        loginPanel.add(usernameField, gbc);

        // Password field
        gbc.gridy = 2;
        gbc.gridx = 0;
        loginPanel.add(new JLabel("Password:"), gbc);

        gbc.gridx = 1;
        passwordField = new JPasswordField(15);
        loginPanel.add(passwordField, gbc);

        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout());
        loginButton = new JButton("Login");
        signUpButton = new JButton("Sign Up");
        buttonPanel.add(loginButton);
        buttonPanel.add(signUpButton);

        gbc.gridy = 3;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        loginPanel.add(buttonPanel, gbc);

        add(loginPanel);

        // Button Actions
        loginButton.addActionListener(e -> login());
        signUpButton.addActionListener(e -> signUp());

        setVisible(true);
    }

    private void login() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword()).trim();

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Username and password cannot be empty.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(USERS_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] userDetails = line.split(":");
                if (userDetails[0].equals(username) && userDetails[1].equals(password)) {
                    currentUser = username; // Set current user
                    JOptionPane.showMessageDialog(this, "Login successful!", "Success", JOptionPane.INFORMATION_MESSAGE);
                    this.dispose(); // Close login window
                    openFeatureWindow(); // Open the feature window
                    return;
                }
            }
            JOptionPane.showMessageDialog(this, "Invalid username or password.", "Error", JOptionPane.ERROR_MESSAGE);
        } catch (IOException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error accessing user data.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void signUp() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword()).trim();

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Username and password cannot be empty.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(USERS_FILE, true))) {
            writer.write(username + ":" + password);
            writer.newLine();
            JOptionPane.showMessageDialog(this, "Sign-up successful! Please log in.", "Success", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error creating account.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void openFeatureWindow() {
        JFrame featureFrame = new JFrame("Recipe Recommender - Features");
        featureFrame.setSize(600, 600);
        featureFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        featureFrame.setLocationRelativeTo(null);

        JPanel featurePanel = new JPanel(new BorderLayout());

        // Top panel with search functionality
        JPanel searchPanel = new JPanel(new FlowLayout());
        JLabel searchLabel = new JLabel("Ingredients:");
        searchField = new JTextField(20);
        searchButton = new JButton("Search Recipes");
        viewFavoritesButton = new JButton("View Favorites");
        searchPanel.add(searchLabel);
        searchPanel.add(searchField);
        searchPanel.add(searchButton);
        searchPanel.add(viewFavoritesButton);

        featurePanel.add(searchPanel, BorderLayout.NORTH);

        // Text area for displaying results
        resultArea = new JEditorPane();
        resultArea.setContentType("text/html");
        resultArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(resultArea);
        featurePanel.add(scrollPane, BorderLayout.CENTER);

        featureFrame.add(featurePanel);
        featureFrame.setVisible(true);

        // Button actions for the feature window
        searchButton.addActionListener(e -> {
            if (currentUser == null) {
                resultArea.setText("Please log in to search for recipes.");
                return;
            }

            String query = searchField.getText().trim();
            if (!query.isEmpty()) {
                try {
                    String response = searchRecipes(query);
                    displayRecipes(response);
                } catch (Exception ex) {
                    ex.printStackTrace();
                    resultArea.setText("Error fetching recipes. Please try again.");
                }
            } else {
                resultArea.setText("Please enter ingredients to search.");
            }
        });
        viewFavoritesButton.addActionListener(e -> viewFavorites());
    }

    private String searchRecipes(String query) throws Exception {
        String urlString = "https://api.spoonacular.com/recipes/complexSearch?includeIngredients=" + query.replace(" ", "%20") + "&apiKey=" + API_KEY;
        URL url = new URL(urlString);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");

        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String inputLine;
        StringBuilder content = new StringBuilder();
        while ((inputLine = in.readLine()) != null) {
            content.append(inputLine);
        }
        in.close();
        connection.disconnect();

        return content.toString();
    }

    private void viewFavorites() {
        if (currentUser == null) {
            resultArea.setText("Please log in to view favorites.");
            return;
        }
        StringBuilder favorites = new StringBuilder();
        boolean userFavoritesFound = false;

        try (BufferedReader reader = new BufferedReader(new FileReader(FAVORITES_FILE))) {
            String line;
            boolean isCurrentUser = false;

            while ((line = reader.readLine()) != null) {
                // Check for a section header for the current user
                if (line.startsWith(currentUser + ":")) {
                    isCurrentUser = true;
                    userFavoritesFound = true;
                    favorites.append("<html><b>Favorites for ").append(currentUser).append(":</b><br><br>");
                    continue;
                }
                // Exit the user's section when encountering an empty line
                if (isCurrentUser && line.trim().isEmpty()) {
                    isCurrentUser = false;
                }
                // Add recipes to the favorites list
                if (isCurrentUser) {
                    favorites.append(line).append("<br>");
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
            resultArea.setText("Error reading favorites. Please try again.");
            return;
        }

        if (userFavoritesFound) {
            resultArea.setText(favorites.toString() + "</html>");
        } else {
            resultArea.setText("No favorites found for user: " + currentUser);
        }
    }

    private JSONObject fetchRecipeDetails(int id) throws Exception {
        String urlString = "https://api.spoonacular.com/recipes/" + id + "/information?apiKey=" + API_KEY;
        URL url = new URL(urlString);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");

        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String inputLine;
        StringBuilder content = new StringBuilder();
        while ((inputLine = in.readLine()) != null) {
            content.append(inputLine);
        }
        in.close();
        connection.disconnect();

        return new JSONObject(content.toString());
    }
    private void saveToFavorites(String recipeDetails) {
        if (currentUser == null) {
            resultArea.setText("Please log in to save favorites.");
            return;
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FAVORITES_FILE, true))) {
            // Add the username as a header if not already present
            writer.write(currentUser + ":\n");

            // Remove any unnecessary blank lines from the recipeDetails
            String[] lines = recipeDetails.split("\n");
            for (String line : lines) {
                if (!line.trim().isEmpty()) { // Skip blank lines
                    writer.write(line.trim());
                    writer.write("\n");
                }
            }
            resultArea.setText("Recipe saved to favorites!");
        } catch (IOException ex) {
            ex.printStackTrace();
            resultArea.setText("Error saving recipe. Please try again.");
        }
    }

    private void displayRecipes(String jsonResponse) {
        try {
            // Clear the existing content in the result area
            resultArea.setText("");

            JSONObject jsonObject = new JSONObject(jsonResponse);
            JSONArray recipes = jsonObject.getJSONArray("results");

            if (recipes.length() == 0) {
                resultArea.setText("No recipes found for the given ingredients.");
                return;
            }

            // Create a panel to hold all the recipes
            JPanel recipePanel = new JPanel();
            recipePanel.setLayout(new BoxLayout(recipePanel, BoxLayout.Y_AXIS));

            for (int i = 0; i < recipes.length(); i++) {
                JSONObject recipe = recipes.getJSONObject(i);
                String title = recipe.getString("title");
                int id = recipe.getInt("id");

                // Fetch recipe details
                JSONObject recipeDetails = fetchRecipeDetails(id);
                String ingredients = getIngredientsList(recipeDetails).trim();
                String instructions = getInstructions(recipeDetails).trim();

                // Create a panel for this recipe
                JPanel singleRecipePanel = new JPanel();
                singleRecipePanel.setLayout(new BoxLayout(singleRecipePanel, BoxLayout.Y_AXIS));
                singleRecipePanel.setBorder(BorderFactory.createTitledBorder(title));

                // Add recipe details as labels
                singleRecipePanel.add(new JLabel("<html><b>Ingredients:</b><br>" + ingredients.replace("\n", "<br>") + "</html>"));
                singleRecipePanel.add(new JLabel("<html><b>Instructions:</b><br>" + instructions.replace("\n", "<br>") + "</html>"));

                // Add "Save to Favorites" button
                JButton saveButton = new JButton("Save to Favorites");
                saveButton.addActionListener(e -> {
                    // Create the full recipe details to save
                    String recipeDetailsToSave = "Recipe: " + title + "\n\n"
                            + "Ingredients:\n" + ingredients + "\n"
                            + "Instructions:\n" + instructions + "\n";

                    saveToFavorites(recipeDetailsToSave);
                });
                singleRecipePanel.add(saveButton);

                // Add this recipe panel to the main panel
                recipePanel.add(singleRecipePanel);
            }

            // Add the recipe panel to a scrollable area
            JScrollPane scrollPane = new JScrollPane(recipePanel);
            scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

            // Replace the content of the result area with the scrollable panel
            resultArea.setText(""); // Clear any existing text
            resultArea.setLayout(new BorderLayout());
            resultArea.add(scrollPane, BorderLayout.CENTER);

            // Refresh the UI
            resultArea.revalidate();
            resultArea.repaint();
        } catch (Exception e) {
            e.printStackTrace();
            resultArea.setText("Error displaying recipes.");
        }
    }

    private String getIngredientsList(JSONObject recipeDetails) {
        StringBuilder ingredientsList = new StringBuilder();
        JSONArray ingredients = recipeDetails.getJSONArray("extendedIngredients");
        for (int i = 0; i < ingredients.length(); i++) {
            ingredientsList.append(ingredients.getJSONObject(i).getString("original")).append("\n");
        }
        return ingredientsList.toString();
    }

    private String getInstructions(JSONObject recipeDetails) {
        return recipeDetails.getString("instructions");
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(RecipeRecommender::new);
    }
}
