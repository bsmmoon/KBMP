import java.nio.file.Files;
import java.nio.file.Paths;

import net.sf.clipsrules.jni.*;

public class ClipsWrapper {
	private boolean initialised = false;
	private Environment clips;
	
	private String CONDITION_DIR;
	
	public void run(String command) {
		System.out.println(clips.eval(command));
	}
		
	public void getStatus() {
		
	}
	
	public void reset() {
		init();
		
		String condition = "";
		try {
			condition = new String(Files.readAllBytes(Paths.get(CONDITION_DIR)));
			clips.loadFromString(condition);
			clips.run();
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
		
		clips.eval("(reset)");
	}
	
	public void init() {
		if (!initialised) clips = new Environment();
		else clips.reset();
	}
}
