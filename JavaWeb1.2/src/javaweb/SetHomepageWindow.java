package javaweb;

import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class SetHomepageWindow {
    
    private final Stage getHomepageURL;
    private final TextField newHomepage;
    private String newHomepageText;
    
    SetHomepageWindow() {
        /////////////
        //BUILD GUI//
        /////////////
        
        getHomepageURL = new Stage();
        getHomepageURL.setTitle("Set Homepage");
        getHomepageURL.setWidth(400);
        getHomepageURL.setResizable(false);
        
        Label newHomepageLabel = new Label("Enter new homepage URL:");
        newHomepageLabel.setStyle("-fx-padding: 3 0 -5 0;");
        
        newHomepage = new TextField();
        newHomepage.setStyle("-fx-background-radius: 0;");
        
        Button set = new Button("Set");
        set.setStyle("-fx-background-radius: 0;");
        Button cancel = new Button("Cancel");
        cancel.setStyle("-fx-background-radius: 0;");
        HBox buttonContainer = new HBox(5, set, cancel);
        buttonContainer.setAlignment(Pos.CENTER_RIGHT);
        
        VBox masterHomepageContainer = new VBox(10, newHomepageLabel, newHomepage, buttonContainer);
        masterHomepageContainer.setStyle("-fx-padding: 0 5 -3 5;");
        getHomepageURL.setScene(new Scene(masterHomepageContainer));
        
        ////////////////////////
        //ADDS FUNCTION TO GUI//
        ////////////////////////
        
        set.setOnAction(e -> {
            newHomepageText = newHomepage.getText();
            getHomepageURL.close();
        });
        
        cancel.setOnAction(e -> {
            getHomepageURL.close();
        });
    }
    
    public String getNewHomepageText() {
        return newHomepageText;
    }
    
    public void showHomepageWindow() {
        getHomepageURL.show();
    }
    
}
