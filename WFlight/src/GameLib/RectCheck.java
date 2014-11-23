package GameLib;

import java.awt.Rectangle;

public class RectCheck {

	static public boolean check(Rectangle rect1, Rectangle rect2){
		
		boolean ret = false;

		if(rect1==null)
			return false;
		
		if(rect2==null)
			return false;

		if(
		rect1.x < (rect2.x+rect2.width) &&
		rect2.x < (rect1.x+rect1.width) &&
		rect1.y < (rect2.y+rect2.height) &&
		rect2.y < (rect1.y+rect1.height)
				)
			ret = true;
		
		return ret;
	}
}
