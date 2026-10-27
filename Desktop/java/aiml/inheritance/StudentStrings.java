import java.util.Scanner;
public class StudentStrings{
    public static void main(String[] args){
    Scanner sc=new Scanner(System.in);
    System.out.print("Enter the student name:");
    String name=sc.nextLine();
    name=name.trim();
    if(name.equalsIgnoreCase("rahul")){
        System.out.println("Name matched");
    }else{
        System.out.println("Name not matched");
    }
    sc.close();
}
}