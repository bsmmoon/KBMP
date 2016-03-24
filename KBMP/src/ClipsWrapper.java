import net.sf.clipsrules.jni.*;

public class ClipsWrapper {
	private boolean initialised = false;
	private Environment clips;
	
	public void run(String command) {
		System.out.println(clips.eval(command));
	}
		
	public void getStatus() {
		
	}
	
	public void reset(String condition) {
		init();
		clips.loadFromString(condition);
		clips.run();
		clips.eval("(reset)");
	}
	
	public void init() {
		if (!initialised) clips = new Environment();
		else clips.reset();
	}
}
