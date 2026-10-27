import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.io.IOException;

public class code {
    public static void main(String[] args) {
        try {
            List<String> lines = Files.readAllLines(Paths.get("data.csv"));

            for (String line : lines) {
                System.out.println(line);
            }
        } catch (IOException e) {
            System.out.println("Error reading the file: " + e.getMessage());
        }
    }
}