import java.nio.file.Files;
import java.nio.file.Paths;

public class Storage {
	private String CONDITION_DIR = "";
	
	public String readCondition() {
		String condition = "";
		try {
			condition = new String(Files.readAllBytes(Paths.get(CONDITION_DIR)));
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
		return condition;
	}
	
	public String readModules() {
		return "";
	}
}
