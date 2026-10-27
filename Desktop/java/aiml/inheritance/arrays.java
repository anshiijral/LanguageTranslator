public class arrays{
           public static void main(String[] args){
              int[]marks={80,70,90};
              int total=0;
              for(int i=0;i<3;i++){
                total+=marks[i];
              }
              double avg=total/3.0;
              System.out.println("total : "+total);
           }
}