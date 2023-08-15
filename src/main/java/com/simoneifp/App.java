package com.simoneifp;

//import java.io.BufferedReader;
//import java.io.FileReader;
//import java.io.IOException;

import javafx.application.Application;
import javafx.collections.ObservableList;
import javafx.scene.control.CheckBox;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.stage.Window;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListView;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Button;

import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.io.IOException;
import java.util.ResourceBundle;


// import java.io.IOException;

public class App extends Application
{

    private static boolean is_init = false;
    private static String csvPath = "./src/main/java/com/simoneifp/recs.csv";
    //Array to store the locations of the fxml files
    private String[] fxml_locations= {"/menu.fxml", "/rec archive.fxml", "/rec pop up.fxml", "/missing data.fxml"};

    //Stores the pop-up for creating a new recommendation
    private Stage rec_create_popup;

    private static RecommendationCSVHandler csv;

    //fields that are in the create pop up
    @FXML private TextField rec_title;

    @FXML private TextField rec_title_two;
    @FXML private TextArea rec_content;
    @FXML private ComboBox rec_type;

    //fields that are on the recommendation viewer page
    @FXML private CheckBox archive_view;
    @FXML private TextField searchbar;
    @FXML private ListView<Recommendation> recommendationBox;

    @FXML private Button archive_toggle;

    //runs on start up, loads into the recommendation database
    @Override
    public void start(Stage stage) throws Exception {
        csv = new RecommendationCSVHandler(csvPath);
        csv.load();
        /*
         *Using the line below the method can load in any fxml file that is located in the main/resources folder
         */

        Parent root = FXMLLoader.load(getClass().getResource(fxml_locations[1]));
        Scene scene = new Scene(root);
        stage.setTitle("Recommendation Database");

        stage.setScene(scene);
        stage.show();
        stage.setOnCloseRequest(e -> {
            try {
                close_program();
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        });
    }

    @FXML
    public void initialize() {
        if(!is_init) {
            recommendationBox.getItems().addAll(csv.getRecommendations());
            is_init = true;
        }
    }

    public static void main(String[] args) throws IOException {
        launch(args);
    }

    //Will handle archiving a recommendation
    @FXML
    public void archive_button(){
        Recommendation selected = recommendationBox.getSelectionModel().getSelectedItem();
        if (selected != null) {
            if (selected.isArchived())
                csv.unarchiveRecommendation(selected);
            else
                csv.archiveRecommendation(selected);
            //Refresh listview
            recommendationBox.getItems().remove(selected);
            recommendationBox.getItems().add(selected);
            archive_text_update();
        }
    }

    /**
     * ---the following connect to the main window---
     */

    //will handle deleting an archived recommendation
    @FXML
    public void archive_delete(){
        Recommendation selected = recommendationBox.getSelectionModel().getSelectedItem();
        if (selected != null) {
            csv.deleteRecommendation(recommendationBox.getSelectionModel().getSelectedItem());
            recommendationBox.getItems().remove(selected);
        }
    }

  
    @FXML
    public void archive_copy() {
        Recommendation selected = recommendationBox.getSelectionModel().getSelectedItem();
        if (selected != null) {
            StringSelection body = new StringSelection(selected.getTitle() + "\n" + selected.getBody());
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(body, null);
        }
    }
    
    //toggles between whether archived recommendations are visible
    @FXML
    public void toggle_view(){
        refreshList();
    }

    @FXML
    private void refreshList() {
        if (archive_view.isSelected())
            recommendationBox.getItems().removeIf(r -> r.isArchived());
        else 
            recommendationBox.getItems().addAll(csv.getRecommendations().stream().filter(r -> !recommendationBox.getItems().contains(r)).toList());
    }
    //when enter is pressed in the search bar will search and show relevant recommendations
    @FXML
    public void search_recs(){
        String search = searchbar.getText();
        recommendationBox.getItems().removeAll(recommendationBox.getItems());
        recommendationBox.getItems().addAll(csv.searchRecommendations(search, archive_view.isSelected()));
        if(search.equals("")) {
            refreshList();

        }
    }

    /**
     *---The following functions connect to the recommendation creation pop-up---
     */

    //handles cancelling creating a recommendation closing the pop-up
    @FXML
    public void rec_cancel(){
        clear_extra_windows();
        Stage.getWindows().get(0).requestFocus();
    }

    //handles creating a recommendation and adding it to the database
    @FXML
    public void rec_create() throws Exception {
        String title = rec_title.getText();
        String title2 = rec_title_two.getText();
        String cat = (String) rec_type.getValue();
        String content = rec_content.getText();

        //if data is missing creates a pop-up that tells the user to fill in the rest of the data fields
        if((title.equals("")) || (title2.equals("")) || (cat == null) || (content.equals(""))){
            if(Stage.getWindows().size() < 3) {
                Parent root = new FXMLLoader().load(getClass().getResource(fxml_locations[3]));
                Scene scene = new Scene(root);
                Stage stg = new Stage();
                stg.setScene(scene);
                stg.setTitle("Data Missing");
                stg.show();
            } else{
                Stage.getWindows().get(2).requestFocus();
            }
        } else { //if nothing is missing creates the recommendation
            clear_extra_windows();
            Recommendation rec = new Recommendation();
            rec.setTitle(title);
            rec.setSecondary(title2);
            rec.setArchived(false);
            com.simoneifp.Category type;
            if(cat.equals("Medical")){
                type = com.simoneifp.Category.MEDICAL;
            } else if(cat.equals("Education")){
                type = com.simoneifp.Category.EDUCATION;
            } else{
                type = com.simoneifp.Category.OTHER;
            }
            csv.addRecommendations(rec);
            rec.setCategory(type);
            rec.setBody(content);
            csv.save();

            //System.out.println(_csv);
            //System.out.println(rec);
        }
    }

    //handles creating the pop-up for making a new recommendation
    @FXML
    public void rec_create_popup() throws IOException {
        if(Stage.getWindows().size() == 1) {
            System.out.println("open_rec_create");
            Parent root = FXMLLoader.load(getClass().getResource(fxml_locations[2]));
            Scene scene = new Scene(root);
            Stage stg = new Stage();
            stg.setScene(scene);
            stg.setTitle("Recommendation Creator");
            stg.show();
            stg.setOnCloseRequest(e -> clear_extra_windows());
            rec_create_popup = stg;
        } else {
            rec_create_popup.requestFocus();
        }
    }

    @FXML
    public void close_missing_popup(){
        Stage stg = (Stage) Stage.getWindows().get(2);
        stg.close();
        Stage.getWindows().get(1).requestFocus();
        // Add new recommendation

    }

    @FXML
    public void archive_text_update(){
        Recommendation selected = recommendationBox.getSelectionModel().getSelectedItem();
        if(selected.isArchived()){
            archive_toggle.setText("Unarchive");
        } else{
            archive_toggle.setText("Archive");
        }

    }

    public void clear_extra_windows(){
        while(Stage.getWindows().size() > 1){
            Stage stg = (Stage) Stage.getWindows().get(Stage.getWindows().size()-1);
            stg.close();
        }
    }

    private void close_program() throws Exception{
        csv.save();
        while(Stage.getWindows().size() > 0){
            Stage stg = (Stage) Stage.getWindows().get(Stage.getWindows().size()-1);
            stg.close();
        }
    }

    private void update_search_view(){
        recommendationBox.getItems().removeAll(recommendationBox.getItems());
        recommendationBox.getItems().addAll(csv.searchRecommendations(searchbar.getText(), archive_view.isSelected()));
        refreshList();
    }


}
