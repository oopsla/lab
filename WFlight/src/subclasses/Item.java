package subclasses;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.image.ImageObserver;

import GameLib.GameManager;
import GameLib.RectCheck;

public class Item {
	
	static final public int NOPROCESS = -1;
	static final public int MOVEOUT = -2;
	static final public int TAKED = 1;

	Image pic;//그림
	Rectangle rect;//충돌체크 대상이 되는 사각형 (플레이어와)
	public int x, y;//표시 좌표
	public int price;//취득 시 얻게 되는 값
	int vx, vy;//좌표 변환값
	int vspeed;//하방가속도
	int cnt;//자체 카운터
	GameManager manager;
	
	public Item(GameManager manager, Image pic, int x, int y){
		
		this.manager = manager;
		this.pic = pic;
		this.x = x;
		this.y = y;
		
		rect = new Rectangle(0,0, 72,72);
		price = 10;
		
		//생성되는 가로 위치에 따라서 횡이동값을 결정한다
		if(x<manager.SCREEN_WIDTH*30/100)
			vx = manager.RAND(3, 5);
		else if(x<manager.SCREEN_WIDTH/2)
			vx = manager.RAND(1, 3);
		else if(x>manager.SCREEN_WIDTH*70/100)
			vx = manager.RAND(-3, -5);
		else //if(x>manager.SCREEN_WIDTH/2)
			vx = manager.RAND(-1, -3);
		
		
		//생성되는 세로 위치에 따라서 수직이동값을 결정한다
		if(y<manager.SCREEN_WIDTH*30/100)
			vy = -5;
		else if(y<manager.SCREEN_WIDTH/2)
			vy = -7;
		else //if(y>manager.SCREEN_WIDTH/2)
			vy = -9;
		
		vspeed = 0;
	}
	public void draw(Graphics gContext, ImageObserver _ob){
		
		manager.drawImageRect(gContext, pic, x, y, (cnt%35)/5*72, 0, 72, 72, manager.ANC_LEFT);
	}
	public int process(int myX, int myY, Rectangle myRect){
		int ret = NOPROCESS;
		
		cnt++;
		
		x+=vx;
		y+=vy;
		
		if(cnt%30==0){
			if(vx<-1)
				vx++;
			else if(vx>1)
				vx--;
		}
		
		if(cnt%5==0){
			vy+=vspeed;
			vspeed++;
		}
		
		if(vy>15)
			vy = 15;//이 이상은 빠르게 떨어지지 않는다.
		
		if(x<-50 || x>manager.SCREEN_WIDTH+50 ||
				y>manager.SCREEN_HEIGHT+100)
			ret = MOVEOUT;//화면 밖으로 사라지면 소실처리 할 수 있도록
		
		if(myRect==null) return NOPROCESS;
		
		Rectangle _rect1 = new Rectangle(x+rect.x, y+rect.y, rect.width, rect.height);
		Rectangle _rect2 = new Rectangle(myX+myRect.x, myY+myRect.y, myRect.width, myRect.height);
	
		if(RectCheck.check(_rect1, _rect2))
			return TAKED;
		
		return ret;
	}
}
