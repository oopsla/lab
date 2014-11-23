package GameLib;

import java.awt.Canvas;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;


abstract public class GameCanvas extends Canvas implements Runnable{


	public GameManager manager; //개임의 화면이 변경될 때마다 변경된다. 
	
	private boolean roof = true;
	private int fps = 16;// 스레드를 fps*(1/1000)초에 한 번 돌린다. 즉, 16은 약 60프레임/초
	
	Image dblbuff;
	Graphics gContext;
	
	//생성자
	public GameCanvas(GameManager manager){
		
		this.manager = manager;
		
		initImage();//각 씬 단위에서 사용할 이미지 리소스를 확보한다
		SceneStart();
		System.out.println("시작한건가?");
	}
	
	public void SceneStart(){//스레드를 활성화 시킨다
		
		roof = true;
		
		Thread _runner = new Thread(this);
		_runner.start(); //run 실행
	}

	//공용으로 쓰이는 스레드 메인
	@Override
	public void run() {
		// TODO Auto-generated method stub
		while(roof){
			
			this.repaint();
			update();
			
			try{
				Thread.sleep(fps);
			}catch(Exception e){
			}
			
		}		
	}
	
	//화면 갱신
	public void paint(Graphics g){
		if(gContext==null) {
			dblbuff=createImage( manager.SCREEN_WIDTH, manager.SCREEN_HEIGHT);
			gContext=dblbuff.getGraphics();
			return;
		}

		update(g);
	}
	
	public void update(Graphics g){//화면 깜박거림을 줄이기 위해, paint에서 화면을 바로 묘화하지 않고 update 메소드를 호출하게 한다.
		if(gContext==null) return;
		dblpaint(gContext);//오프스크린 버퍼에 그리기
		g.drawImage(dblbuff,0,0,this);//오프스크린 버퍼를 메인화면에 그린다.
	}	

	//상속한 클래스에서 반드시 오버라이딩 할 것 
	abstract public void dblpaint(Graphics gContext);//각 씬 단계에서 오버라이딩하여 사용한다
	abstract public void update();//매 스레드마다 불리는 부분. 이미지 그리기 이외의 처리할 내용을 여기에 담는다

	//반드시 오버라이딩하지 않아도 되지만, 해 주는것이 좋은 함수
	public void Destroy(){
		//씬을 소멸시키려는 시점에서 호출한다
		roof = false;//스레드를 중지한다
	}
	
	//반드시 오버라이딩하지 않아도 되는 함수
	public void initImage(){}//해당 씬에서 사용할 초기 이미지 리소스를 선언한다
	public void releaseImage(){}//사용이 끝난 이미지 리소스를 해제한다
	public void keyTyped(KeyEvent e) {	}
	public void keyPressed(KeyEvent e) {	}
	public void keyReleased(KeyEvent e) {	}
	public void mouseClicked(MouseEvent e) {		}
	public void mousePressed(MouseEvent e) {	}
	public void mouseReleased(MouseEvent e) {	}
	public void mouseEntered(MouseEvent e) {	}
	public void mouseExited(MouseEvent e) {	}
	public void mouseDragged(MouseEvent e) {	}
	public void mouseMoved(MouseEvent e) {		}

}
