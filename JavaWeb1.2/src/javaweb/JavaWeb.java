/*
Goals for this version:
*Move About to its own screen DONE
*Allow user to change homepage
-->Preserve this to next run
*/
package javaweb;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayDeque;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Application;
import javafx.concurrent.Worker.State;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;

public class JavaWeb extends Application {
    
    boolean backClicked, enteredBackLoop, forwardClicked, newLoad;
    ArrayDeque<String> webHistory = new ArrayDeque();
    ArrayDeque<String> forwardHistory = new ArrayDeque();
    String homepage, locationBuffer;

    public static void main(String[] args) {
        launch();
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        //Gets user preferences for this run
        
        getUserSetHomepage();
        
        ///////////////////////////////////////
        //THIS SECTION BUILDS THE BROWSER GUI//
        ///////////////////////////////////////
        
        //Constructs the back/forward block in the window
        Button back = new Button("\u23F4");
        back.setStyle("-fx-ellipsis-string: \"Stop\";"
                    + "-fx-background-radius: 0;");
        Button forward = new Button("\u23F5");
        forward.setStyle("-fx-ellipsis-string: \"Stop\";"
                       + "-fx-background-radius: 0;");
        HBox backForward = new HBox(-1, back, forward);
        
        //Constructs the stop/refresh block in the window
        Button stop = new Button("Stop");
        stop.setStyle("-fx-ellipsis-string: \"Stop\";"
                    + "-fx-background-radius: 0;");
        Button refresh = new Button("Refresh");
        refresh.setStyle("-fx-ellipsis-string: \"Refresh\";"
                       + "-fx-background-radius: 0;");
        HBox pageControls = new HBox(-1, stop, refresh);
        
        //Constructs the address bar and go button in the window
        TextField urlBar = new TextField();
        urlBar.setStyle("-fx-background-radius: 0;");
        Button go = new Button("Go");
        go.setStyle("-fx-ellipsis-string: \"Go\";"
                  + "-fx-background-radius: 0;");
        
        //Constructs the options menu
        MenuItem setHomepage = new MenuItem("Set homepage...");
        MenuItem aboutJavaWeb = new MenuItem("About JavaWeb v1.2");
        Menu optionsMenu = new Menu("\u23EC");
        optionsMenu.show();
        optionsMenu.getItems().addAll(setHomepage, aboutJavaWeb);
        MenuBar menus = new MenuBar();
        menus.setStyle("-fx-padding: -1 0 -1 0;"
                     + "-fx-border-style: solid;"
                     + "-fx-border-color: DARKGRAY;");
        menus.getMenus().addAll(optionsMenu);
        
        //Puts all browser controls in one main container
        HBox navContainer = new HBox(5, backForward, pageControls, urlBar, go, menus);
        navContainer.setStyle("-fx-padding: 3 5 3 5;");
        
        //Constructs a WebEngine (with listeners) and WebView
        WebView wv = new WebView();
        WebEngine we = wv.getEngine();
        we.load(homepage);
        primaryStage.setTitle("JavaWeb");
        addUpdateListeners(we, urlBar, primaryStage);
        
        //Sets scene and shows stage
        VBox mainContainer = new VBox(navContainer, wv);
        Scene scene = new Scene(mainContainer);
        urlBar.prefWidthProperty().bind(scene.widthProperty());
        wv.prefWidthProperty().bind(scene.widthProperty());
        wv.prefHeightProperty().bind(scene.heightProperty());
        primaryStage.setWidth(500);
        primaryStage.setHeight(500);
        primaryStage.setScene(scene);
        primaryStage.show();
        
        /////////////////////////////////////////
        //THIS SECTION ADDS FUNCTION TO THE GUI//
        /////////////////////////////////////////
        back.setOnAction(e -> {
            if (!webHistory.isEmpty()) {
                enteredBackLoop = true;
                backClicked = true;
                forwardHistory.push(locationBuffer);
                we.load(webHistory.pop());
            }
        });
        
        forward.setOnAction(e -> {
            if (!forwardHistory.isEmpty()) {
                forwardClicked = true;
                we.load(forwardHistory.pop());
            }
        });
        
        stop.setOnAction(e -> {
            we.getLoadWorker().cancel();
            primaryStage.setTitle(we.getTitle() + " - JavaWeb");
            urlBar.setText(we.getLocation());
        });
        
        refresh.setOnAction(e -> {
            we.reload();
        });
        
        urlBar.setOnKeyPressed(keyPressed -> {
            if (keyPressed.getCode() == KeyCode.ENTER)
                loadPageFromURLBar(urlBar, we);
        });
        
        go.setOnAction(e -> {
            loadPageFromURLBar(urlBar, we);
        });
        
        setHomepage.setOnAction(e -> {
            SetHomepageWindow shw = createHomepageWindow();
            String newHomepage = getNewHomepage(shw);
            setHomepage(newHomepage);
        });
        
        aboutJavaWeb.setOnAction(e -> {
            showAboutScreen();
        });
        
        scene.setOnKeyPressed(keyPressed -> {
            if (keyPressed.getCode() == KeyCode.F5) we.reload();
            if (keyPressed.getCode() == KeyCode.ESCAPE){
                we.getLoadWorker().cancel();
                primaryStage.setTitle(we.getTitle() + " - JavaWeb");
                urlBar.setText(we.getLocation());
            }
        });
        
        primaryStage.setOnCloseRequest(e -> {
            exportHomepage();
        });
    }
    
    /**
     * Adds listeners to the WebEngine to update browser window load percentages
     * and current URL.
     * @param we The WebEngine instance of the current browser instance.
     * @param urlBar The Address Bar of the current browser instance.
     * @param primaryStage The Stage comprising the current browser instance.
     */
    private void addUpdateListeners(WebEngine we, TextField urlBar, Stage primaryStage) {
        //Monitors the current state (loading, running, succeeded, etc.) of the WebEngine
        we.getLoadWorker().stateProperty().addListener((obsState, oldState, newState) -> {
            //If the WebEngine anything but State SUCCEEDED, displays loading 
            //percentages in the Stage title and pushes the most recently
            //visited site to the webHistory stack
            if (newState != State.SUCCEEDED)
                //Monitors load progress of the WebEngine
                we.getLoadWorker().progressProperty().addListener((obsValue, oldValue, newValue) -> {
                    if (newLoad) {
                        primaryStage.setTitle("LOADING: " +
                            Integer.toString((int) we.getLoadWorker().getWorkDone())
                            + "% - JavaWeb");
                    }
                    
                    if (!locationBuffer.isEmpty() && !backClicked &&
                            !locationBuffer.equals(we.getLocation())) {
                        webHistory.push(locationBuffer);
                        locationBuffer = "";
                    }
                });
            
            //If the WebEngine is State SUCCEEDED, displays the title of the
            //webpage in the Stage title and puts the current URL in a buffer to
            //potentially be added to webHistory stack later
            else {
                primaryStage.setTitle(we.getTitle() + " - JavaWeb");
                urlBar.setText(we.getLocation());             
                locationBuffer = we.getLocation();
                if (enteredBackLoop && !(backClicked || forwardClicked)) {
                    enteredBackLoop = false;
                    forwardHistory.clear();
                }
                backClicked = false;
                newLoad = false;
                forwardClicked = false;
            }
            
            //Resets newLoad flag if current load progress is zero
            //(Uses load progress due to dynamic content on certain sites)
            if (we.getLoadWorker().getProgress() == 0.0) newLoad = true;
        });
    }
    
    /**
     * Loads webpage based on URL or search queries in URL Bar.
     * @param urlBar The Address Bar of the current browser instance.
     * @param we The WebEngine instance of the current browser instance.
     */
    private void loadPageFromURLBar(TextField urlBar, WebEngine we) {
        newLoad = true;
            
        if (!urlBar.getText().startsWith("http://") && !urlBar.getText().startsWith("https://"))
            urlBar.setText("http://" + urlBar.getText());

        if (urlBar.getText().matches("http(s?)://((\\p{Alnum}+\\.)?)\\p{Graph}+\\.\\p{Alnum}+(/?)\\p{Graph}*"))
            we.load(urlBar.getText());
        else {
            String searchTerms = urlBar.getText().substring(7).replaceAll(" ", "%20");
            we.load("https://www.bing.com/search?q=" + searchTerms);
        }
    }
    
    private SetHomepageWindow createHomepageWindow() {
        SetHomepageWindow shw = new SetHomepageWindow();
        shw.showHomepageWindow();
        return shw;
    }
    
    private boolean exportHomepage() {
        try {
            ObjectOutputStream oos = new ObjectOutputStream(
                    new FileOutputStream("homepage.dat"));
            oos.writeObject(homepage);
            oos.close();
            return true;
        } catch (IOException ex) {
            //CREATE ERROR WINDOW, RETURN FALSE
            return false;
        }
    }
    
    private String getNewHomepage(SetHomepageWindow shw) {
        return shw.getNewHomepageText();
    }
    
    private void getUserSetHomepage() {
        try {
            ObjectInputStream ois = new ObjectInputStream(
                new FileInputStream("homepage.dat"));
            System.out.println((String) ois.readObject());
            homepage = (String) ois.readObject();
            ois.close();
        } catch (IOException | ClassNotFoundException exc) {
            homepage = "http://www.bing.com";
        }
    }
    
    private void setHomepage(String homepage) {
        System.out.println(homepage);
        this.homepage = homepage;
    }
    
    private void showAboutScreen() {
        Alert about = new Alert(AlertType.INFORMATION);
        about.setTitle("About JavaWeb");
        about.setHeaderText("JavaWeb v1.2");
        about.setContentText("\u00A92017 Adam Horvath-Smith\n"
                           + "Webpage rendering done using JavaFX WebEngine.\n"
                           + "All rights reserved.");
        about.show();
    }
    
}
