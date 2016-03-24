/*
 * Workflow:
 * 		1. Receives user command from view
 * 		2. Parse command into CLIPS command (ClipsParser)
 * 		3. Pass the command to the CLIPS (ClipsWrapper)
 * 		4. Fetch status from CLIPS (ClipsWrapper)
 * 		5. Return status to view
 */
public class Logic {
	private Storage storage;
	private ClipsWrapper clips;
	
	private Model model;
	
	public Logic() {
		this.storage = new Storage();
		this.clips = new ClipsWrapper();
		this.model = new Model(this.clips);
		
		String condition = this.storage.readCondition();
		this.clips.reset(condition);
	}
	
	public void execute(String command) {
		this.clips.run(command);
		this.model.update();
		
		return;
	}
}
