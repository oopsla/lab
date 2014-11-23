package scenes;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.util.Vector;

import subclasses.Bullet;
import subclasses.Effect;
import subclasses.Enemy;
import subclasses.Item;
import GameLib.GameCanvas;
import GameLib.GameManager;

public class GameScene extends GameCanvas {

	public final static int UP_PRESSED = 0x001;
	public final static int DOWN_PRESSED = 0x002;
	public final static int LEFT_PRESSED = 0x004;
	public final static int RIGHT_PRESSED = 0x008;
	public final static int FIRE_PRESSED = 0x010;

	int myX, myY;// 플레이어 캐릭터 위치
	int cnt;

	int bg1Y, bg2Y;// 배경화면 위치

	int myFrame;// 플레이어 캐릭터 애니메이션 프레임
	int myStatus;// 플레이어 상태

	int _speed;// 배경 스크롤 속도
	int _score;// 이번 게임의 득점
	int _gold;// 이번 게임의 획득 골드
	int _range;// 이번 게임의 비행 거리
	int _level;// 내부적으로 계산되는 게임 난이도

	int regen;// 적이 리젠되는 횟수 계산

	int keybuff;
	int keyTime;// 키가 눌리거나 떼었을 때 얼마나 시간이 지났는가 카운팅한다

	int playerWidth;// 플레이어 캐릭터 그림 1프레임의 가로 폭. 연산으로도 구할 수 있지만 자주 사용되기 때문에 따로
					// 저장해둔다

	boolean isPause;

	Vector bullets;// 총알 관리. 총알의 갯수를 예상할 수 없기 때문에 가변적으로 관리한다.
	Vector enemies;// 적 캐릭터 관리.
	Vector effects;// 이펙트 관리
	Vector items;// 아이템 관리

	public GameScene(GameManager manager) {

		super(manager); //상위 클래스 

		manager.nowCanvas = (GameCanvas) this;
	}

	@Override
	public void dblpaint(Graphics gContext) {
		// TODO Auto-generated method stub

		// 배경을 그리고
		gContext.drawImage(bg1, 0, bg1Y, this);
		gContext.drawImage(bg2, 0, bg2Y, this);

		// 적 캐릭터를 그리고
		drawEnemy(gContext);

		// 플레이어를 그리고
		manager.drawImageRect(gContext, player, myX, myY, myFrame * 80, 0, 80,
				120, manager.ANC_LEFT);

		// 플레이어가 발사한 총알을 그리고
		drawBullet(gContext);

		// 아이템을 그리고
		drawItem(gContext);

		// 폭파 이펙트를 그리며
		drawEffect(gContext);

		// 마지막으로 UI를 그린다
		gContext.drawImage(ui1, 6, 14, this);
		gContext.drawImage(ui2, 6, 741, this);

		manager.drawnum(gContext, numpic, 197, 39, _score, 8, manager.ANC_RIGHT);
		manager.drawnum(gContext, numpic, 464, 39, _range / 10, 6,
				manager.ANC_RIGHT);
		manager.drawnum(gContext, numpic, 147, 766, _gold, 6, manager.ANC_RIGHT);
	}

	@Override
	public void update() {
		// TODO Auto-generated method stub
		if (isPause)
			return;
		cnt++;
		keyTime++;

		if (myStatus == 0)
			_range += (_speed / 2);

		bg1Y += _speed;
		bg2Y += _speed;

		if (bg1Y >= manager.SCREEN_HEIGHT)
			bg1Y = -manager.SCREEN_HEIGHT + bg1Y % manager.SCREEN_HEIGHT;// 화면을
																			// 벗어난
																			// 배경그림
																			// 1의
																			// 위치를
																			// 되돌린다
		if (bg2Y >= manager.SCREEN_HEIGHT)
			bg2Y = -manager.SCREEN_HEIGHT + bg2Y % manager.SCREEN_HEIGHT;// 화면을
																			// 벗어난
																			// 배경그림
																			// 2의
																			// 위치를
																			// 되돌린다

		processEnemy();
		processBullet();
		processItem();
		processEffect();

		keyProcerss();
		myProcess();

	}

	@Override
	public void Destroy() {
		// TODO Auto-generated method stub
		super.Destroy();
		manager.remove(this);// GameManager의 firstScene에서 이 씬(클래스)을 add했으므로,
								// remove하여 제거한다.
		releaseImage();
	}

	Image bg1, bg2;// 게임 배경. 무한스크롤을 위해 2개 사용한다
	Image player;// 리네짱
	Image neuroi[];// 적 캐릭터
	Image effect;// 폭발 이펙트용
	Image effect2;// 발사 이펙트용
	Image bullet;// 총알
	Image coin;// 아이템
	Image ui1;// 상단 UI
	Image ui2;// 하단 UI
	Image numpic;// 그림 숫자

	//이미지를 가져옴
	@Override
	public void initImage() {
		// TODO Auto-generated method stub

		neuroi = new Image[6];// 배열 형식의 Image 객체는 반드시 initImage 안에서 초기화한다.
								// (선언부에서 하면 안된다)

		bg1 = manager.makeImage("rsc/game/ground.png");
		bg2 = manager.makeImage("rsc/game/ground.png");// 게임 배경. 무한스크롤을 위해 2개
														// 사용한다
		player = manager.makeImage("rsc/game/lyne.png");// 리네짱
		for (int i = 0; i < 6; i++)
			neuroi[i] = manager.makeImage("rsc/game/neuroi_0" + (i + 1)
					+ ".png");// 적 캐릭터
		effect = manager.makeImage("rsc/game/effect_boom.png");// 폭발 이펙트용
		effect2 = manager.makeImage("rsc/game/effect_fire.png");// 발사 이펙트용
		bullet = manager.makeImage("rsc/game/mybullet01.png");// 총알
		coin = manager.makeImage("rsc/game/coin.png");// 아이템
		numpic = manager.makeImage("rsc/numpic.png");// 그림숫자
		ui1 = manager.makeImage("rsc/game/gameui_01.png");// 상단UI
		ui2 = manager.makeImage("rsc/game/gameui_02.png");// 하단UI
	}

	@Override
	public void releaseImage() {
		// TODO Auto-generated method stub
		bg1 = null;
		bg2 = null;// 게임 배경. 무한스크롤을 위해 2개 사용한다
		player = null;// 리네짱
		for (int i = 0; i < 6; i++)
			neuroi[i] = null;// 적 캐릭터
		effect = null;// 폭발 이펙트용
		effect2 = null;// 발사 이펙트용
		bullet = null;// 총알
		coin = null;// 아이템
		numpic = null;// 그림 숫자
		ui1 = null;// 상단UI
		ui2 = null;// 하단UI
	}

	@Override
	public void SceneStart() {
		// TODO Auto-generated method stub
		// 별도의 씬 초기화를 위해 SceneStart를 오버라이드하고, 마지막에 super를 호출한다
		cnt = 0;

		myStatus = 0;
		playerWidth = player.getWidth(this) / 5;
		myX = (manager.SCREEN_WIDTH - playerWidth) / 2;// 화면 중앙
		myY = 550;// 고정
		myFrame = 2;// 중앙

		// 배경용 좌표 (계산하기 편하게)
		bg1Y = 0;
		bg2Y = -800;// 배경화면 위치

		// 게임 관련 정보 초기화
		_speed = 4;// 배경 스크롤 속도
		_score = 0;// 이번 게임의 득점
		_gold = 0;// 이번 게임의 획득 골드
		_range = 0;// 이번 게임의 비행 거리
		_level = 1;// 내부적으로 계산되는 게임 난이도

		regen = 0;

		keybuff = 0;

		bullets = new Vector();// 총알 관리. 총알의 갯수를 예상할 수 없기 때문에 가변적으로 관리한다.
		enemies = new Vector();// 적 캐릭터 관리.
		effects = new Vector();// 이펙트 관리
		items = new Vector();// 아이템 관리

		bullets.clear();
		enemies.clear();
		effects.clear();
		items.clear();

		isPause = false;

		super.SceneStart();
	}

	@Override
	public void keyPressed(KeyEvent e) {
		// TODO Auto-generated method stub

		keyTime = 0;

		switch (e.getKeyCode()) {
		case KeyEvent.VK_LEFT:
			keybuff |= LEFT_PRESSED;// 멀티키의 누르기 처리
			break;
		case KeyEvent.VK_RIGHT:
			keybuff |= RIGHT_PRESSED;
			break;
		default:
			break;
		}
	}

	@Override
	public void keyReleased(KeyEvent e) {
		// TODO Auto-generated method stub

		keyTime = 0;

		switch (e.getKeyCode()) {
		case KeyEvent.VK_LEFT:
			keybuff &= ~LEFT_PRESSED;// 멀티키의 떼기 처리
			break;
		case KeyEvent.VK_RIGHT:
			keybuff &= ~RIGHT_PRESSED;
			break;
		case KeyEvent.VK_1:
			isPause = !isPause;
			break;
		case KeyEvent.VK_2:
			System.out.println("now speed = " + _speed);
			for (int i = 0; i < enemies.size(); i++)
				System.out.println("new enemy y = "
						+ ((Enemy) enemies.elementAt(i)).y);
			break;
		case KeyEvent.VK_3:
			if (isPause) {

				for (int i = 0; i < enemies.size(); i++) {
					Enemy _buff = (Enemy) enemies.get(i);
					System.out.println(String.format(
							"Enemy : %d, y=%d, speed=%d", i, _buff.y,
							_buff.speed));
				}
			}
			break;

		}
	}

	// 여기서부터 오리지널 함수

	void drawEnemy(Graphics gContext) {

		Enemy _buff;
		for (int i = 0; i < enemies.size(); i++) {
			_buff = (Enemy) enemies.elementAt(i);
			_buff.draw(manager, gContext, this);
		}
	}

	void drawBullet(Graphics gContext) {
		Bullet _buff;
		for (int i = 0; i < bullets.size(); i++) {
			_buff = (Bullet) bullets.elementAt(i);
			_buff.draw(gContext, this);
		}

	}

	void drawItem(Graphics gContext) {

		Item _buff;
		for (int i = 0; i < items.size(); i++) {
			_buff = (Item) items.elementAt(i);
			_buff.draw(gContext, this);
		}
	}

	void drawEffect(Graphics gContext) {

		Effect _buff;
		for (int i = 0; i < effects.size(); i++) {
			_buff = (Effect) effects.elementAt(i);
			_buff.draw(manager, gContext, this);
		}

	}

	void processEnemy() {

		// 네우로이를 생성합니다. 한꺼번에 일렬로 5대를 생성합니다.

		if (cnt > 100
				&& (cnt % 90 == 0 || (manager.RAND(0, 10) == 5 && cnt % 45 == 0))) {
			// 기본적으로는 일정 시간마다 생성하지만, 게임에 변화를 주기 위해 10% 확률로 그 반 간격으로도 생성합니다.

			int localLevel = (regen % 20) / 5;// 한 단계 높은 레벨의 네우로이가 생성되는 갯수 (0~4를
												// 반복)

			// 현재 레벨보다 한 단계 높은 네우로이를 섞어서 낸다
			int neuroiLevel[] = { 0, 0, 0, 0, 0 };
			int setCnt = 0;
			while (setCnt < localLevel) {
				int idx = manager.RAND(0, 4);
				if (neuroiLevel[idx] == 0) {
					neuroiLevel[idx] = 1;
					setCnt++;
				}
			}

			for (int i = 0; i < 5; i++) {

				Enemy _enemy;
				if (_level >= 5) {
					// 레벨이 5 이상이라면 모두 파란 네우로이
					_enemy = new Enemy(neuroi[5], new Rectangle(33, 6, 76, 81),
							i * 96 - 23, -80, 10 * _level, 6 + _speed);
					enemies.add(_enemy);
				} else {
					// 레벨이 5 미만이라면 localLevel값에 따라 현재 레벨과 상위레벨 네우로이를 섞어 낸다
					// 레벨이 1~3 사이라면 가끔 2레벨 위의 네우로이를 섞는다
					if (1 <= _level && _level <= 3 && neuroiLevel[i] == 1
							&& manager.RAND(1, 20) == 10)
						_enemy = new Enemy(neuroi[_level + 2], new Rectangle(
								33, 6, 76, 81), i * 96 - 23, -80,
								10 * (_level + 2), 6 + _speed);
					else
						_enemy = new Enemy(neuroi[_level + neuroiLevel[i]],
								new Rectangle(33, 6, 76, 81), i * 96 - 23, -80,
								10 * (_level + neuroiLevel[i]), 6 + _speed);
					enemies.add(_enemy);
				}
			}
			regen++;

			if (regen % 20 == 0)
				levelup();

			return;
		}

		Enemy _buff;
		for (int i = 0; i < enemies.size(); i++) {
			_buff = (Enemy) enemies.elementAt(i);
			_buff.move();
			boolean ret = _buff
					.process(myX, myY, new Rectangle(12, 20, 55, 50));
			if (ret && myStatus == 0) {// (12,20,55,50)은 플레이어의 충돌 판정 영역
				// 플레이어와의 충돌
				myStatus = 1;
				cnt = 0;
				Effect _effect[] = new Effect[4];
				for (int j = 0; j < 4; j++)
					_effect[j] = new Effect(effect, myX + playerWidth / 2
							+ manager.RAND(-15, 15), myY
							+ manager.RAND(-10, 10), manager.RAND(3, 7), 4);
				_effect[0].setMove(-8, -8);
				_effect[1].setMove(-8, 8);
				_effect[2].setMove(8, -8);
				_effect[3].setMove(8, 8);
				for (int j = 0; j < 4; j++)
					effects.add(_effect[j]);
			} else if (_buff.y >= manager.SCREEN_HEIGHT + 60) {
				enemies.remove(_buff);// 화면 밖으로 나감
				i--;
			}
		}

	}

	void processBullet() {
		Bullet _buff;
		for (int i = 0; i < bullets.size(); i++) {
			_buff = (Bullet) bullets.elementAt(i);
			int idx = _buff.process(enemies);

			switch (idx) {
			case Bullet.NO_PROCESS:// 아무런 변화도 없다
				break;
			case Bullet.MOVEOUT:// 화면 밖으로 사라졌다
				bullets.remove(_buff);
				i--;
				break;
			default:// 어떤 적 캐릭터에게 명중하였다

				Enemy _temp = (Enemy) enemies.elementAt(idx);

				int eHp = _temp.hp;

				_temp.hp -= _buff.pow;

				_buff.pow -= eHp;

				int _x = _temp.x;
				int _y = _temp.y;

				if (_buff.pow <= 0) {
					bullets.remove(_buff);
					i--;
				}

				if (_temp.hp <= 0) {
					// 적의 HP가 바닥나 파괴 처리
					enemies.remove(_temp);

					for (int j = 0; j < 3; j++) {
						Effect _effect = new Effect(effect, _x + 71
								+ manager.RAND(-15, 15), _y + 48
								+ manager.RAND(-10, 10), manager.RAND(3, 7), 4);
						effects.add(_effect);
					}

					// 점수 가산
					_score += (50 + _level * 10);

					// 아이템 드롭
					Item _item;
					_item = new Item(manager, coin, _x + 71, _y + 48);
					items.add(_item);
				} else {
					Effect _effect = new Effect(effect2, _x + 71
							+ manager.RAND(-15, 15), _y + 48
							+ manager.RAND(-10, 10), 3, 2);
					effects.add(_effect);
				}
				break;
			}
		}
	}

	void processItem() {
		Item _buff;
		for (int i = 0; i < items.size(); i++) {
			_buff = (Item) items.elementAt(i);
			int idx = _buff.process(myX, myY, new Rectangle(12, 20, 55, 50));
			switch (idx) {
			case Item.TAKED:// 점수 득점 처리
				_gold += _buff.price;
			case Item.MOVEOUT:// 득점 또는 화면 밖으로 나갔으므로 공통적으로 소멸 처리
				items.remove(_buff);
				break;
			}
		}
	}

	void processEffect() {
		Effect _buff;
		for (int i = 0; i < effects.size(); i++) {
			_buff = (Effect) effects.elementAt(i);
			_buff.process();
		}

		// 삭제는 별도 루프에서 한 번 더 돌린다
		for (int i = 0; i < effects.size(); i++) {
			_buff = (Effect) effects.elementAt(i);
			if (_buff.GET_DELETE()) {
				effects.remove(_buff);
				i--;
			}
		}
	}

	void keyProcerss() {
		if (myStatus == 1)
			return;
		switch (keybuff) {
		case LEFT_PRESSED:
			if (myX > -20)
				myX -= 10;

			if (keyTime > 1 && keyTime % 7 == 0 && myFrame > 0)
				myFrame--;// 캐릭터의 왼쪽 기울어짐을 묘사한다

			break;
		case RIGHT_PRESSED:
			if (myX < manager.SCREEN_WIDTH + 20 - playerWidth)
				myX += 10;

			if (keyTime > 1 && keyTime % 7 == 0 && myFrame < 4)
				myFrame++;// 캐릭터의 오른쪽 기울어짐을 묘사한다

			break;
		}
	}

	void myProcess() {

		if (myStatus == 0) {
			if (keybuff == 0 && keyTime > 1 && keyTime % 7 == 0) {
				if (myFrame < 2)
					myFrame++;
				else if (myFrame > 2)
					myFrame--;
				// 키에서 손을 놓았으면 캐릭터를 다시 중립 상태로 되돌린다.
			}

			// 총알 발사 (자동)
			if (cnt % 7 == 0) {

				int _x = myX + playerWidth / 2 - 12;
				int _y = myY - 17;

				// 싱글샷
				Bullet _bullet = new Bullet(bullet,
						new Rectangle(6, 1, 12, 33), _x, _y, 5);
				bullets.add(_bullet);

				// 트윈샷
				// Bullet _twinbullet[] = new Bullet[2];
				// _twinbullet[0] = new Bullet(bullet, new Rectangle(6,1,12,33),
				// _x-12, _y, 1);
				// _twinbullet[1] = new Bullet(bullet, new Rectangle(6,1,12,33),
				// _x+12, _y, 1);
				// bullets.add(_twinbullet[0]);
				// bullets.add(_twinbullet[1]);
			}
		} else {
			if (myY < manager.SCREEN_HEIGHT + 100) {
				if (cnt % 3 == 0) {
					Effect _effect = new Effect(effect, myX + playerWidth / 2
							+ manager.RAND(-15, 15), myY
							+ manager.RAND(-10, 10), manager.RAND(3, 7), 4);
					effects.add(_effect);
				}
				myY += 8;
			}

			if (cnt >= 150)
				gameover();
		}
	}

	void levelup() {

		if (_level < 5)
			_level++;// 레벨은 5까지

		if (_speed < 20)
			_speed += 2;
	}

	void gameover() {
		manager._getScore = _score;
		manager._getRange = _range / 10;
		manager._getGold = _gold;
		Destroy();
		manager.sceneChange((GameCanvas) new ResultScene(manager));

	}
}
