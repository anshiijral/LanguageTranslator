package aiml.inheritance;
class Person{
    String name;
    void displayName(){
          System.out.println("Name:"+name);
    }
}
class Student extends Person{
    int rollNumber;
    void displayRollNumber(){
        System.out.println("roll number:"+rollNumber);
    }
    public static void main(String args[]){
      Student s1=new Student();
      s1.name="Rahul";
      s1.rollNumber=101;
      s1.displayName();
      s1.displayRollNumber();
    }
}
   
