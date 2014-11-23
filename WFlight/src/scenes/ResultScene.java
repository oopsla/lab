package scenes;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.KeyEvent;

import GameLib.GameCanvas;
import GameLib.GameManager;

public class ResultScene extends GameCanvas{

	int cnt;
	boolean keyLock;
	int totalScore;
	int viewScore[];
	boolean isNewRecord;
	
	public ResultScene(GameManager manager){
		
		super(manager);

		manager.nowCanvas = (GameCanvas)this;
	}

	@Override
	public void dblpaint(Graphics gContext) {
		// TODO Auto-generated method stub
		
		gContext.drawImage(base, 0, 0, this);
		
		//획득 점수 보여주기
		if(cnt<20)
			manager.drawnum(gContext, numpic, 328, 257, viewScore[0]*(cnt/2), 8, manager.ANC_RIGHT);
		else if(cnt>=20)
			manager.drawnum(gContext, numpic, 328, 257, manager._getScore, 8, manager.ANC_RIGHT);

		//달성 거리 보여주기
		if(20<=cnt && cnt<40)
			manager.drawnum(gContext, numpic, 328, 332, viewScore[1]*((cnt-20)/2), 6, manager.ANC_RIGHT);
		else if(cnt>=40)
			manager.drawnum(gContext, numpic, 328, 332, manager._getRange, 6, manager.ANC_RIGHT);

		//총점수 보여주기
		if(40<=cnt && cnt<60)
			manager.drawnum(gContext, numpic, 328, 434, viewScore[3]*((cnt-40)/2), 6, manager.ANC_RIGHT);
		else if(cnt>=60)
			manager.drawnum(gContext, numpic, 328, 434, totalScore, 8, manager.ANC_RIGHT);

		//획득 골드 보여주기
		if(60<=cnt && cnt<80)
			manager.drawnum(gContext, numpic, 328, 581, viewScore[2]*((cnt-60)/2), 6, manager.ANC_RIGHT);
		else if(cnt>=80)
			manager.drawnum(gContext, numpic, 328, 581, manager._getGold, 6, manager.ANC_RIGHT);
		
		if(isNewRecord && cnt>140)
			gContext.drawImage(stamp, 37, 376, this);
	}

	@Override
	public void update() {
		// TODO Auto-generated method stub
		cnt++;

		if((isNewRecord&&cnt==150) || (!isNewRecord&&cnt==120) )
			keyLock = false;
	}

	@Override
	public void Destroy() {
		// TODO Auto-generated method stub
		super.Destroy();
		manager.remove(this);//GameManager의 firstScene에서 이 씬(클래스)을 add했으므로, remove하여 제거한다.
		releaseImage();
	}

	Image base;
	Image stamp;
	Image numpic;
	@Override
	public void initImage() {
		// TODO Auto-generated method stub
		base = manager.makeImage("rsc/result/result_base.png");
		stamp = manager.makeImage("rsc/result/stamp.png");
		numpic = manager.makeImage("rsc/numpic.png");
	}

	@Override
	public void releaseImage() {
		// TODO Auto-generated method stub
		base = null;
		stamp = null;
		numpic = null;
	}

	
	@Override
	public void SceneStart() {
		// TODO Auto-generated method stub
		cnt = 0;
		keyLock = true;
		isNewRecord = false;
		totalScore = manager._getScore + manager._getRange;
		
		//숫자를 증가시켜서 보여주기 위한 값
		viewScore = new int[4];
		viewScore[0] = manager._getScore/10;
		viewScore[1] = manager._getRange/10;
		viewScore[2] = manager._getGold/10;
		viewScore[3] = totalScore/10;

		manager.gold += manager._getGold;
		if(manager.highscore < totalScore){
			isNewRecord = true;
			manager.highscore = totalScore;
		}
		
		manager.SaveGame("wf.dat");
		
		super.SceneStart();
	}

	@Override
	public void keyReleased(KeyEvent e) {
		// TODO Auto-generated method stub
		
		if(keyLock) return;

		Destroy();
		manager.sceneChange((GameCanvas)new TitleScene(manager));
	}
	
}
