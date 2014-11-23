package subclasses;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.image.ImageObserver;

import GameLib.GameManager;
import GameLib.RectCheck;

public class Enemy {

	Image pic;//그림
	Rectangle rect;//충돌체크 대상이 되는 사각형 (총알이나 플레이어)
	public int x, y;//적을 표시해 줄 좌표
	public int hp;//내구
	public int speed;//아래로 내려오는 속도
	
	public Enemy(Image pic, Rectangle rect, int x, int y, int hp, int speed){
		
		this.pic = pic;
		this.rect = rect;
		this.x = x;
		this.y = y;
		this.hp = hp;
		this.speed = speed;

		System.out.println("new y = " + y);
	}
	
	public void draw(GameManager manager, Graphics gContext, ImageObserver _ob){

		gContext.drawImage(pic, x, y, _ob);
	}
	
	public void move(){
		
		y+=speed;
		
	}
	
	public boolean process(int myX, int myY, Rectangle myRect){

		if(myRect==null) return false;
		
		Rectangle _rect1 = new Rectangle(x+rect.x, y+rect.y, rect.width, rect.height);
		Rectangle _rect2 = new Rectangle(myX+myRect.x, myY+myRect.y, myRect.width, myRect.height);
	
		if(RectCheck.check(_rect1, _rect2))
			return true;
		
		return false;
	}
}
