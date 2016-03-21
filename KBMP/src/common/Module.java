package common;

/**
 * @description This class contains all the information about a module
 * @author Ruofan
 *
 */
public class Module {
	private String code;
	private String name;
	// other fields
	
	public Module(String code,String name) {
		this.code = code;
		this.name = name;
	}
	
	public String getCode() {
		return code;
	}
	
	public String getName() {
		return name;
	}
}
