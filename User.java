package seProject;

//Abstract User class for generalization
abstract class User {
 private String username;
 private String password;
 private String id; // School-issued ID

 public User(String username, String password, String id) {
     this.username = username;
     this.password = password;
     this.id = id;
 }

 // Method for login (Functional Req 1, Non-Functional Req 1)
 public boolean login(String enteredUsername, String enteredPassword) {
     return this.username.equals(enteredUsername) && this.password.equals(enteredPassword);
 }

 public String getUsername() {
     return username;
 }

 public String getId() {
     return id;
 }

 // Abstract method for role-specific actions
 public abstract void performRoleActions();
}