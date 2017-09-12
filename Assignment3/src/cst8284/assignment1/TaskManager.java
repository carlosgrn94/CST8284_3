/*
 * TaskManager.java
 * Carlos Guillermo Rivera Negrete, 040877658
 * 17W_CST8284_300: 17W_CST8284_300 Object-Oriented Programming (Java)
 * Assignment 3
 * April 21, 2017
 * David B. Houtman
 */

package cst8284.assignment1;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.Optional;


import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.RotateTransition;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;
import javafx.util.Duration;

/**
 * 
 * TaskManager is a class that extends JavaFX Application.
 * This class displays a List of ToDo objects.
 * It first asks to the user to chose a .todo file and then it
 * loads the file into a friendly user interface.
 * The application lets the user move through each object 
 * with 4 buttons (first, back, next and last), or by selecting the 
 * title shown in the left side of the interface.
 * It also contains a menu which allows the user to 
 * open a new file, save any changes made to the objects, 
 * add and remove new ToDo objects, and exit the application.
 * 
 * 
 * @author Carlos Guillermo Rivera Negrete
 * @see Scene
 * @version 1.0, April 21, 2017
 * @since 1.0
 *
 */

public class TaskManager extends Application{
	
	private static ArrayList<ToDo> toDoArray;
	private static int currentToDoElement;
	private Stage primaryStage;
	private TextField tFieldTask;
	private TextArea tAreaSubject;
	private TextField tFieldDueDate;
	private RadioButton rb1;
	private RadioButton rb2;
	private RadioButton rb3;
	private int priority;
	
	/**
	 * Overrides the start method from Application.
	 * Loads getSplashScene Scene into the primary stage scene and then shows it.
	 */
	@Override
	public void start(Stage primaryStage) throws IOException {
		try{
			primaryStage.setScene(getSplashScene("Click here to open"));
			setStage(primaryStage);
			getStage().setTitle("To Do List Assignment2");
			getStage().setResizable(false);
			getStage().show();
			
			getStage().setOnCloseRequest(e->{
				if(getToDoArray()!=null){
					if(isToDoArrayListDirty()){
						saveFile();
					} 
					Alert alert = alertBox("exit");
					Optional <ButtonType> choice = alert.showAndWait();
					if(choice.get()==ButtonType.CANCEL){
						e.consume();
					}
				} else
					Platform.exit();
			});
			
			
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	/**
	 * Creates an animated scene that when clicked calls the method openFile
	 * 
	 * @param defaultText the text to display in the scene
	 * @return an animated scene
	 */
	public Scene getSplashScene(String defaultText){
		BorderPane pane = new BorderPane();
		Text text = new Text(defaultText);
		pane.setCenter(text);
		text.setStyle("-fx-font: 50px Tahoma; -fx-stroke-width: 1;");
		Scene scene = new Scene(pane, 1200, 600);
		FadeTransition ft = new FadeTransition();
		ft.setDuration(new Duration(1000));
	    ft.setFromValue(1.0);
	    ft.setToValue(0.0);
	    
	    RotateTransition rt = new RotateTransition(Duration.millis(1000), text);
	    rt.setByAngle(180);
	    
	    ParallelTransition pt = new ParallelTransition(text, ft, rt);
	    
		
		scene.setOnMouseClicked((e) -> {
			pt.play();
			Platform.runLater(new Runnable(){
				@Override
				public void run() {
					pt.onFinishedProperty().set(e1->{
					openFile(scene);
				});
				}
			});
			
		});
		
		return scene;
		
	}
	
	/**
	 * Opens the file chosen by the user from the getToDoFile method
	 * 
	 * @param scene the scene where the filechoose will be displayed
	 */
	public void openFile(Scene scene){
		
		Optional<ButtonType> choice = null;
		while(true){
			FileUtils fileU = new FileUtils();
			
			File todoFile = getToDoFile();
			
			
			
			if(FileUtils.fileExists(todoFile) && todoFile.getName().endsWith(".todo")){
				FileUtils.setAbsPath(todoFile);
				setToDoArray(fileU.getToDoArray(FileUtils.getAbsPath()));
				noEmpties();
				setToDoElement(0);
				scene.setOnMouseClicked(null);
				changeScene();
				break;
			} else{
				Alert alert = alertBox("fileNotFound");
				choice = alert.showAndWait();
				if(choice.get()==ButtonType.OK){
					Platform.exit();
					break;
				}
			}
			
			if(getToDoArray() != null){
				break;
			}

		}
	}
	
	/**
	 * Displays a filechooser to the user to select a .todo file
	 * 
	 * @return a .todo file
	 */
	public File getToDoFile(){
		FileChooser fc = new FileChooser();
		//fc.setInitialDirectory(new File("D:\\")); 
		fc.setTitle("Open ToDo File");
		fc.getExtensionFilters().addAll(
			new ExtensionFilter("Quiz Files", "*.todo"),
			new ExtensionFilter("All Files", "*.*")
				);
		File todoFile = fc.showOpenDialog(primaryStage);
		
		return todoFile;
	}
	
	/**
	 * Alerts the user asking if they want to save the file
	 * and depending on the user desicion it saves or not the file.
	 */
	public void saveFile(){
		Alert alert = alertBox("save");
		Optional<ButtonType> choice = alert.showAndWait();
		if(choice.get()==ButtonType.OK){
			saveCenterPaneContents2ToDo();
			cleanArray();
		}
		FileUtils.setToDoArrayListToFile(getToDoArray(), FileUtils.getAbsPath());
	}
	
	/**
	 * Changes the remove property to false from all elements in the ArrayList of ToDos
	 */
	public void cleanArray(){
		for(ToDo todo: getToDoArray()){
			if(todo.isRemoveSet()) {
				todo.setRemove(false);
			}
		}
	}

	/**
	 * Creates four buttons that later are added to a HBox.
	 * It also uses lambda expressions to set an action to each of the buttons.
	 * The buttons when clicked will set the currentToDoElement and change the current scene.
	 * The buttons will be disabled if they can not be used, for example, if you in the first
	 * element of the toDoArray then the first and back buttons will be disabled.
	 * 
	 * @return a HBox with four buttons
	 */
	public HBox getHBoxButtons(){
		
		HBox hpButtons = new HBox();
		Button first = new Button("First");
		Button back = new Button("Back");
		Button next = new Button("Next");
		Button last = new Button("Last");
		hpButtons.getChildren().addAll(first, back, next, last);
		hpButtons.setPadding(new Insets(20, 0, 60, 500));
		hpButtons.setSpacing(10);
		
		//Disable buttons
		if(currentToDoElement == getToDoArray().size()-1){
			last.setDisable(true);
			next.setDisable(true);
		}
		
		if(currentToDoElement == 0){
			first.setDisable(true);
			back.setDisable(true);
		}
		
		first.setOnMouseClicked((e) -> {
			if(isToDoArrayListDirty()){
				saveFile();
				cleanArray();
			}
			setToDoElement(0);
			changeScene();
		});
		
		back.setOnMouseClicked((e) -> {
			if(isToDoArrayListDirty()){
				saveFile();
				cleanArray();
			}
			setToDoElement(--currentToDoElement);
			changeScene();
		});
		
		next.setOnMouseClicked((e) -> {
			if(isToDoArrayListDirty()){
				saveFile();
				cleanArray();
			}
			setToDoElement(++currentToDoElement);
			changeScene();
		});
		
		last.setOnMouseClicked((e) -> {
			if(isToDoArrayListDirty()){
				saveFile();
				cleanArray();
			}
			setToDoElement(getToDoArray().size()-1);
			changeScene();
		});
		
		return hpButtons;
	}
	
	/**
	 * Creates three radio buttons, it then loads this buttons into a ToggleGroup and then into a HBox.
	 * The buttons will be selected depending on the priority property of the ToDo object.
	 * 
	 * @return a HBox with three radio buttons
	 */
	public HBox getHBoxRadioButtons(){
		
		
		ToggleGroup tg = new ToggleGroup();

		setRb1(new RadioButton("1"));
		setRb2(new RadioButton("2"));
		setRb3(new RadioButton("3"));

		if(getToDoArray().get(currentToDoElement).getPriority() == 1){
			getRb1().setSelected(true);
		}
		else if(getToDoArray().get(currentToDoElement).getPriority() == 2){
			getRb2().setSelected(true);
		}
		else if (getToDoArray().get(currentToDoElement).getPriority() == 3){
			getRb3().setSelected(true);
		}
		
		getRb1().setToggleGroup(tg);
		getRb2().setToggleGroup(tg);
		getRb2().setPadding(new Insets(0, 15, 0, 15));
		getRb3().setToggleGroup(tg);
		
		getRb1().setOnMouseClicked(e->{
			System.out.println("RB1");
			getToDoArray().get(currentToDoElement).setRemove(true);
		});
		getRb2().setOnMouseClicked(e->{
			System.out.println("RB2");
			getToDoArray().get(currentToDoElement).setRemove(true);
		});
		getRb3().setOnMouseClicked(e->{
			System.out.println("RB3");
			getToDoArray().get(currentToDoElement).setRemove(true);
		});
		
		HBox hbox = new HBox();
		hbox.getChildren().addAll(getRb1(), getRb2(), getRb3());
		
		return hbox;
	}

	/**
	 * Creates two TextFields, one TextArea and four Labels.
	 * Each TextField and the TextArea hold properties from a ToDo object.
	 * The TextFields, TextArea and Labels are loaded into the GridPane.
	 * 
	 * @param todo ToDo object which will be used to set the TextFields and TextArea of the GridPane
	 * @return a GridPane that holds two TextFields and one TexArea showing properties of a ToDo object, as well as four Labels
	 */
	public GridPane getGridPane(ToDo todo){
		
		
		settFieldTask(new TextField(todo.getTitle()));
		settAreaSubject(new TextArea(todo.getSubject()));
		settFieldDueDate(new TextField(todo.getDueDate().toString().substring(0,10)));
		
		Label lTask = new Label("Task");
		Label lSubject = new Label("Subject");
		Label lDueDate = new Label("Due Date");
		Label lPriority = new Label("Priority");
		
		GridPane gPane = new GridPane();
		gPane.setPadding(new Insets(50, 10, 10, 10));
		gPane.setVgap(10);
		gPane.setHgap(20);
		
		gPane.add(lTask, 0, 0);
		gPane.add(lSubject, 0, 1);
		gPane.add(lDueDate, 0, 2);
		gPane.add(lPriority, 0, 3);
		
		gPane.add(gettFieldTask(), 1, 0);
		gPane.add(gettAreaSubject(), 1, 1);
		gPane.add(gettFieldDueDate(), 1, 2);
		
		gettFieldTask().setOnMouseClicked(e->{
			getToDoArray().get(currentToDoElement).setRemove(true);
		});
		
		gettFieldTask().setOnKeyPressed(e->{
			getToDoArray().get(currentToDoElement).setRemove(true);
		});
		
		gettFieldDueDate().setOnMouseClicked(e->{
			getToDoArray().get(currentToDoElement).setRemove(true);
		});
		
		gettFieldDueDate().setOnKeyPressed(e->{
			getToDoArray().get(currentToDoElement).setRemove(true);
		});
		
		gettAreaSubject().setOnMouseClicked(e->{
			getToDoArray().get(currentToDoElement).setRemove(true);
		});
		
		gettAreaSubject().setOnKeyPressed(e->{
			getToDoArray().get(currentToDoElement).setRemove(true);
		});

		gPane.add(getHBoxRadioButtons(), 1, 3);
		
		
		return gPane;
	}
	
	/**
	 * Creates a Menu and five MenuItems.
	 * Each MenuItem has a behaviour, the first one is to open a file, the second one is to save a file,
	 * the third one adds a new ToDo object, the fourth one removes a ToDo object and the last one exits the program.
	 * Each MenuItem has a setOnAction method.
	 * 
	 * @return a MenuBar with five different MenuItems
	 */
	public MenuBar menuBar(){
		Menu fileMenu = new Menu("File");
		MenuItem open = new MenuItem("Open");
		MenuItem save = new MenuItem("Save");
		MenuItem add = new MenuItem("Add ToDo");
		MenuItem remove = new MenuItem("Remove ToDo");
		MenuItem exit = new MenuItem("Exit");
		
		open.setOnAction(e->{
			if(isToDoArrayListDirty()){
				saveFile();
			}
			openFile(getStage().getScene());
		});
		
		add.setOnAction(e->{
			setToDoElement(currentToDoElement+1);
			ToDo td = new ToDo();
			getToDoArray().add(getCurrentToDoElement(), td);
			changeScene();
		});
		
		save.setOnAction(e->{
			saveFile();
		});
		
		remove.setOnAction(e->{
			
			Alert alert = alertBox("remove");
			Optional<ButtonType> choice = alert.showAndWait();
			if(choice.get()==ButtonType.OK){
				
				if(getToDoArray().size() == 1){
					getToDoArray().remove(currentToDoElement);
					getToDoArray().add(0, new ToDo());
				}else if(currentToDoElement == getToDoArray().size()-1){
					getToDoArray().remove(currentToDoElement);
					do{
						setToDoElement(--currentToDoElement);
					} while(getToDoArray().get(currentToDoElement).isEmptySet());
				}else{
					getToDoArray().remove(currentToDoElement);
				} 
					changeScene();
					getToDoArray().get(currentToDoElement).setRemove(true);
			}
			
		});
		
		exit.setOnAction(e->{
			if(isToDoArrayListDirty()){
				saveFile();
			}
			Alert alert = alertBox("exit");
			Optional<ButtonType> choice = alert.showAndWait();
			if(choice.get()==ButtonType.OK){
				Platform.exit();
			}
		});
		
		MenuBar mBar = new MenuBar();
		mBar.getMenus().addAll(fileMenu);
		fileMenu.getItems().addAll(open, save, add, remove, exit);
		
		return mBar;
	}
	
	/**
	 * Creates an Alert with a different message, using a switch statement, depending on the option chosen.
	 * 
	 * @param option the selected option of the message to be shown
	 * @return an Alert to be displayed on the interface
	 */
	public Alert alertBox(String option){

		Alert alert = new Alert(AlertType.CONFIRMATION);
		
		switch(option){
		case "exit":
			alert.setTitle("Exit");
			alert.setHeaderText("Do you wish exit (OK) or continue (Cancel)?");
			break;
			
		case "remove":
			alert.setTitle("Remove");
			alert.setHeaderText("Are you sure you want to remove? \n (OK) to continue (Cancel) to cancel");
			break;
			
		case "fileNotFound":
			alert.setTitle("No File Name Entered");
			alert.setHeaderText("No file entered. Do you wish exit (OK) or continue (Cancel)?");
			break;
			
		case "save":
			alert.setTitle("Save File");
			alert.setHeaderText("Do you wish to save the file? (OK) or (Cancel)?");
			break;
		}
		
		
		
		return alert;
	}
	
	/**
	 * Removes the empty elements from the toDoArray variable.
	 */
	public void noEmpties(){

		for (Iterator<ToDo> iterator = getToDoArray().iterator(); iterator.hasNext(); ) {
		    ToDo todo = iterator.next();
		    if (todo.isEmptySet())  iterator.remove();
		}
	}
	
	/**
	 * Creates a TableView which is loaded with the title and priority of each element of the ToDo ArrayList,
	 * the TableView has two columns, one for the Title and one for the Priority.
	 * The TableView uses the setOnMouseClicked method to display the selected row.
	 * 
	 * @return a VBox holding a TableView that displays the title and priority of the elements of the ToDo ArrayList
	 */
	public VBox getListOfTitles(){
		VBox box = new VBox();
		
		ObservableList<ToDo> ol = FXCollections.observableArrayList(getToDoArray());
		
		TableView<ToDo> table = new TableView<ToDo>();
		
		TableColumn<ToDo, String> title = new TableColumn<ToDo, String>("Title");
		TableColumn<ToDo, String> priority = new TableColumn<ToDo, String>("Priority");
		
		title.setCellValueFactory(new PropertyValueFactory<>("Title"));
		priority.setCellValueFactory(new PropertyValueFactory<>("Priority"));
		
		table.getColumns().addAll(title, priority);
		table.setItems(ol);
		
		table.setOnMouseClicked(e->{
			if (isToDoArrayListDirty()) {saveFile(); cleanArray();}
			if(table.getSelectionModel().getSelectedIndex() < 0 
					|| table.getSelectionModel().getSelectedIndex() > getToDoArray().size()-1)
				;
			else{
				System.out.println(table.getSelectionModel().getSelectedIndex());
				setToDoElement(table.getSelectionModel().getSelectedIndex());
				changeScene();
			}
		});
		
		box.getChildren().add(table);
		
		return box;
	}
	
	/**
	 * Sorts the elements of the ArrayList depending on the button selected.
	 * 
	 * @return a VBox with six buttons to sort the ArrayList
	 */
	public VBox getSortButtons(){
		VBox box = new VBox();
		
		Button sortTitle = new Button("Sort by Title");
		Button sortSubject = new Button("Sort by Subject");
		Button sortDueDate = new Button("Sort by Due Date");
		Button sortPriority = new Button("Sort by Priority");
		Button sortCompleted = new Button("Sort by Completed");
		Button sortReverse = new Button("Sort by Reverse");
		
		sortTitle.setOnAction(e->{
			if(isToDoArrayListDirty())saveFile();
			Collections.sort(getToDoArray(), Comparator.comparing(ToDo::getTitle));
			setToDoElement(0);
			changeScene();
			});
		
		sortSubject.setOnAction(e->{if(isToDoArrayListDirty())saveFile();
		Collections.sort(getToDoArray(), Comparator.comparing(ToDo::getSubject));
		setToDoElement(0);changeScene();});
		
		sortDueDate.setOnAction(e->{if(isToDoArrayListDirty())saveFile();Collections.sort(getToDoArray(), Comparator.comparing(ToDo::getDueDate));setToDoElement(0);changeScene();});
		
		sortCompleted.setOnAction(e->{if(isToDoArrayListDirty())saveFile();Collections.sort(getToDoArray(), Comparator.comparing(ToDo::isCompleted));setToDoElement(0);changeScene();});
		
		sortCompleted.setOnAction(e->{ if(isToDoArrayListDirty())saveFile();Collections.sort(getToDoArray(), Comparator.comparing(ToDo::isCompleted));setToDoElement(0);changeScene();});
		
		sortReverse.setOnAction(e->{if(isToDoArrayListDirty())saveFile();Collections.reverse(getToDoArray());setToDoElement(0);changeScene();});
		
		box.getChildren().addAll(sortTitle, sortSubject, sortDueDate, sortPriority, sortCompleted, sortReverse);
		
		return box;
	}
	
	/**
	 * Creates a BorderPane that displays in the center a GridPane, in the left and right two VBoxes
	 * and in the buttom a HBox.
	 * 
	 * @param todo ToDo object used to call other methods such as getGridPane
	 * @return a BorderPane holding two VBoxes, one HBox and one GridPane
	 */
	public BorderPane getToDoPane(ToDo todo){
		
		BorderPane bPane = new BorderPane();
		bPane.setCenter(getGridPane(todo));
		bPane.setBottom(getHBoxButtons());
		
		bPane.setLeft(getListOfTitles());
		bPane.setRight(getSortButtons());
		
		bPane.setTop(menuBar());
		
		return bPane;
	}
	
	/**
	 * Changes the Scene accordint to the position of the array.
	 */
	public void changeScene() {
		getStage().setScene(getToDoScene(getToDoArray().get(currentToDoElement)));
    }
	
	/**
	 * Creates a new Scene and chagnes the root to a new Pane.
	 * 
	 * @param todo ToDo Object used to call the getToDoPane method
	 * @return a Scene with a new Pane
	 */
	public Scene getToDoScene(ToDo todo){
		Scene scene = getStage().getScene();
		scene.setRoot(getToDoPane(todo));
		
		return scene;
	}
	
	/**
	 * 
	 * @param tdArray new ArrayList of ToDo Objects
	 */
	public void setToDoArray(ArrayList<ToDo> tdArray){
		toDoArray = tdArray;
	}
	
	/**
	 * 
	 * @return the ArrayList of ToDo elements
	 */
	public static ArrayList<ToDo> getToDoArray(){
		return toDoArray;
	}
	
	/**
	 * 
	 * @return the currentToDoElement value
	 */
	public static int getCurrentToDoElement(){
		return currentToDoElement;
	}
	
	/**
	 * 
	 * @param element new value for currentToDoElement
	 */
	public static void setToDoElement(int element){
		currentToDoElement = element;
	}
	
	/**
	 * 
	 * @param pStage new Stage for primaryStage
	 */
	public void setStage(Stage pStage){
		this.primaryStage = pStage;
	}
	
	/**
	 * 
	 * @return primaryStage Stage
	 */
	public Stage getStage(){
		return this.primaryStage;
	}
	
	/**
	 * Calls the launch method from Application
	 * 
	 * @param args unused
	 * @throws IOException on input error
	 */
	public static void main(String[] args) throws IOException {
		Application.launch(args);
		
	}

	/**
	 * Creates a temporary ToDo object that holds the changes made by the user
	 */
	public void saveCenterPaneContents2ToDo(){

	    DateFormat df = new SimpleDateFormat("E MMM dd"); 
	    Date dueDate = null;
	    try {
	        dueDate = df.parse(gettFieldDueDate().getText());
	    } catch (ParseException e) {
	        e.printStackTrace();
	    }
	    
	    if(getRb1().isSelected())
	    	setPriority(1);
	    else if(getRb2().isSelected())
	    	setPriority(2);
	    else if(getRb3().isSelected())
	    	setPriority(3);
	    
		ToDo tempTodo = new ToDo(
				gettAreaSubject().getText(),
				gettFieldTask().getText(),
				getPriority(),
				dueDate,
				false,
				true,
				false
				);
		
		getToDoArray().set(currentToDoElement, tempTodo);
		
	}
	
	/**
	 * Returns true if a remove property from any element to the ToDo ArrayList is set to true, otherwise returns false
	 * 
	 * @return a variable holding the true of false depending on the ArrayList properties
	 */
	public boolean isToDoArrayListDirty(){
		boolean dirty = false;
		for(ToDo todo: getToDoArray())
			if(todo.isRemoveSet()) {
				dirty = true;
			}
		return dirty;
	}
	
	/**
	 * 
	 * @return a TextField for the title of a ToDo element
	 */
	public TextField gettFieldTask() {
		return tFieldTask;
	}

	/**
	 * 
	 * @param tFieldTask new TextField for tFieldTask
	 */
	public void settFieldTask(TextField tFieldTask) {
		this.tFieldTask = tFieldTask;
	}

	/**
	 * 
	 * @return a TextArea for the subject of a ToDo element
	 */
	public TextArea gettAreaSubject() {
		return tAreaSubject;
	}

	/**
	 * 
	 * @param tAreaSubject new TextArea for tAreaSubject
	 */
	public void settAreaSubject(TextArea tAreaSubject) {
		this.tAreaSubject = tAreaSubject;
	}

	/**
	 * 
	 * @return a TextField for the due date of a ToDo element
	 */
	public TextField gettFieldDueDate() {
		return tFieldDueDate;
	}

	/**
	 * 
	 * @param tFieldDueDate new FieldText for tFieldDueDate
	 */
	public void settFieldDueDate(TextField tFieldDueDate) {
		this.tFieldDueDate = tFieldDueDate;
	}

	/**
	 * 
	 * @return priority value
	 */
	public int getPriority() {
		return this.priority;
	}

	/**
	 * 
	 * @param priority new value for priority
	 */
	public void setPriority(int priority) {
		this.priority = priority;
	}

	/**
	 * 
	 * @return radio button rb1
	 */
	public RadioButton getRb1() {
		return rb1;
	}

	/**
	 * 
	 * @param rb1 new radio button for rb1
	 */
	public void setRb1(RadioButton rb1) {
		this.rb1 = rb1;
	}

	/**
	 * 
	 * @return radio button rb2
	 */
	public RadioButton getRb2() {
		return rb2;
	}

	/**
	 * 
	 * @param rb2 new radio button for rb2
	 */
	public void setRb2(RadioButton rb2) {
		this.rb2 = rb2;
	}

	/**
	 * 
	 * @return radio button rb3
	 */
	public RadioButton getRb3() {
		return rb3;
	}

	/**
	 * 
	 * @param rb3 new radio button for rb3
	 */
	public void setRb3(RadioButton rb3) {
		this.rb3 = rb3;
	}

}
