package subclasses;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.ImageObserver;

import GameLib.GameManager;

public class Effect {

	Image pic;//그림
	int x, y;//총알을 표시해 줄 좌표
	int vx, vy;//이동해야 한다면 이동방향값
	int step;//다음 프레임으로 넘어가기 위해 필요한 클럭
	int nowstep;
	int frame;//현재 보여주는 프레임
	int totalframe;//총 프레임 수
	boolean isDelete;

	public Effect(Image pic, int x, int y, int step, int totalframe){

		this.pic = pic;
		this.x = x;
		this.y = y;
		this.step = step;
		this.totalframe = totalframe;
		
		nowstep = 0;
		frame = 0;
		
		vx = 0;
		vy = 0;
	}
	
	public void draw(GameManager manager, Graphics gContext, ImageObserver _ob){
		
		manager.drawImageRect(gContext, pic, x, y, (pic.getWidth(_ob)/totalframe)*frame, 0, pic.getWidth(_ob)/totalframe, pic.getHeight(_ob), manager.ANC_CENTER);
	}
	
	public void setMove(int vx, int vy){
		
		this.vx = vx;
		this.vy = vy;
	}

	public boolean process(){

		x+=vx;
		y+=vy;
		
		nowstep++;
		if(nowstep==step){
			frame++;
			nowstep = 0;
			if(frame==totalframe)
				return false;
		}
		return true;
	}
	
	public boolean GET_DELETE(){
		
		return isDelete;
	}
}
