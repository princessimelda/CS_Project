package clientside;

import static javafx.application.Application.launch;
import javafx.application.Application;
import javafx.stage.Stage;

public class Main extends Application {
    private Stage primaryStage; 
    
    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;  
        
        UserInterface userInterface = new UserInterface(primaryStage, this::showLoginPage);
        primaryStage.setScene(userInterface.getScene());
        primaryStage.setTitle("Signup Page");
        primaryStage.show();
        primaryStage.setMaximized(true);
    }

    private void showLoginPage() {
        UserInterface userInterface = new UserInterface(primaryStage, this::showLoginPage);
        primaryStage.setScene(userInterface.getScene());
        primaryStage.setTitle("Login Page");
        primaryStage.setMaximized(true);
    }

    public static void main(String[] args) {
        launch(args);
    }
}































/*package clientside;

import static javafx.application.Application.launch;
import javafx.application.Application;
import javafx.stage.Stage;

public class Main extends Application {
    @Override
    public void start(Stage primaryStage) {
       
        UserInterface signupPage = new UserInterface(primaryStage, this::showLoginPage);
   
        primaryStage.setScene(signupPage.getScene());
        primaryStage.setTitle("Signup Page");
        primaryStage.show();
    
    }
    
    private void showLoginPage() {
        
        UserInterface loginPage = new UserInterface(primaryStage, this::showLoginPage);
        primaryStage.setScene(loginPage.getScene());
        primaryStage.setTitle("Login Page");

    }

    public static void main(String[] args) {
        launch(args);
    }
}
*/