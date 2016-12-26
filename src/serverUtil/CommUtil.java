package serverUtil;

import java.util.UUID;

public class CommUtil {
	
	public static String getUUID() {
		UUID uuid = UUID.randomUUID();
		return uuid.toString().replace("-", "");
	}

}
