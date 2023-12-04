// By 106403052 資管二B 黃品毅

import java.awt.AWTException;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.Timer;

public class MazeFrame extends JFrame {
		
	private static Scanner read ;
	
	private GridLayout gridLayout ;
	
	private JPanel mazePane ;
	private JPanel hpPanel ;
	private JLabel hpValue ;
	private JProgressBar hp ;
	private JLabel[] cubes = new JLabel[100] ; 
	private int[][] nodes = new int[10][10] ; // 0為道路，1為牆壁，2為出口
	private JDialog countdown ;
	
	private ImageIcon brickwall = new ImageIcon( "brickwall.png" ) ;
	private ImageIcon diamond = new ImageIcon( "diamond.png" ) ;
	private ImageIcon heart = new ImageIcon( "heart.png" ) ;
	
	private int choose ;
	private Boolean win = false ;
	
	private Robot mouseLocker ;
	Dimension screenSize ;
	Timer lock ;
	Random random = new Random() ;
	
	ScheduledExecutorService threads ;
	ScheduledExecutorService hpChange ;
	
	public MazeFrame() {
		
		gridLayout = new GridLayout( 10, 10 ) ;
		mazePane = new JPanel() ;
		mazePane.setLayout( gridLayout ) ;
		mazePane.setEnabled( false ) ;
		hpPanel = new JPanel() ;
		hpPanel.setLayout( new BorderLayout() ) ;
		hp = new JProgressBar( 0, 100 ) ;
		hp.setValue( 100 ) ;
		hp.setForeground(Color.RED) ;
		JLabel hpLabel = new JLabel("HP") ;
		hpValue = new JLabel( String.valueOf( hp.getValue() ) ) ;
		hpLabel.setFont( new Font( "Serif", Font.BOLD, 20 ) ) ;
		hpValue.setFont( hpLabel.getFont() ) ;
		hpPanel.add( hpLabel, BorderLayout.WEST ) ;
		hpPanel.add( hpValue, BorderLayout.EAST ) ;
		hpPanel.add( hp, BorderLayout.SOUTH ) ;
		
		this.setLayout( new BorderLayout() ) ;
		add( hpPanel, BorderLayout.NORTH ) ;
		add( mazePane, BorderLayout.CENTER ) ;
		
		countdown = new JDialog( this ) ;
		countdown.setSize( 300, 150 ) ;
		countdown.setUndecorated( true ) ;  // 非不透明要設為Undecorated
		countdown.setBackground( new Color(0.0f, 0.0f, 0.0f, 0.6f) ) ; // 半透明(R,G,B,Alpha)
		
		this.setEnabled( false ) ;
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE) ;
		this.setSize(900,900) ;
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize() ; 
		this.setLocation( (int)( screenSize.getWidth()-this.getWidth() )/2, (int)( screenSize.getHeight()-this.getHeight() )/2 ) ;
		this.setResizable( false ) ;
		this.setVisible(true) ;
		
		try {
			mouseLocker = new Robot() ;
		} catch (AWTException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		countdown.setLocationRelativeTo( this ) ;
		
		putInEmptyCubes() ;
		resizeIcon( brickwall ) ;
		resizeIcon( diamond ) ;
		resizeIcon( heart ) ;
		
		readMap() ;
		
		choose = JOptionPane.showOptionDialog(this, "歡迎來到「寶石迷宮」！", "Diamond Maze"
				, JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null, new String[]{"開始遊戲","離開"}, null ) ;
		
		while( choose == 0 ) {
			
			drawMap() ;
			chooseHeart() ;
			mouseFixed() ;
			countdown() ;
			mouseLock() ;
			play() ;
			
		}
		
		System.exit(0) ;
		
	}

	public void putInEmptyCubes() {
		
		Arrays.stream( cubes )
			.map( cube -> cube = new JLabel() )
			.forEach( cube -> this.mazePane.add( cube ) ) ;
		
	}
	public void resizeIcon( ImageIcon icon ) {
		
		Image intermedium = icon.getImage().getScaledInstance( this.mazePane.getWidth() / gridLayout.getColumns()
				, this.mazePane.getHeight() / gridLayout.getRows(), Image.SCALE_SMOOTH ) ;
		icon.setImage( intermedium ) ;
				
	}

	public void countdown() {
		
		countdown.setVisible( true ) ;
		countdown.setName("waiting");
		
		Graphics graphics = countdown.getGraphics() ;
		graphics.setFont( new Font( "Dialog", Font.BOLD, 45 ) ) ;
		graphics.setColor( new Color( 1.0f, 1.0f, 1.0f, 1.0f ) ) ;
		
		final Timer timer = new Timer(1000, new ActionListener() {
			
			int count = 3 ;
			
			@Override
			public void actionPerformed(ActionEvent event) {
				if( countdown.getName().equals( "Start !" ) ) {
					( (Timer)event.getSource() ).stop() ; 
				}
				
				if( count > 0 ) {
					countdown.setName( String.valueOf( count ) ) ;
					count-- ;
				}
				else {
					if( !countdown.getName().equals( "Start !" ) ) {
						countdown.setName("Start !") ;
					}
				}
			}
			
	    });
	    timer.start();
	    
	    while( timer.isRunning() ) {
	    	if( countdown.getName().equals( "waiting" ) ) {
	    	}
	    	else {
	    		if( !countdown.getName().equals( "Start !" ) ) {
		    		graphics.drawString( countdown.getName(), 
			    			countdown.getWidth()/2 - 12, countdown.getHeight()/2 + 20 ) ;
		    	}
		    	else {
		    		graphics.drawString( countdown.getName(), 
			    			countdown.getWidth()/2 - 65, countdown.getHeight()/2 + 20 ) ;
		    	}
	    	}
	    	countdown.repaint() ;
	    }
	    this.setEnabled( true ) ;
	    countdown.setVisible( false ) ;
	}
	
	public void readMap() {
		
		try {
			read = new Scanner( Paths.get( "map.txt" ) );
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		for( int i=0 ; i<10 ; i++ ) {
			Iterator<Integer> line = Arrays.stream( read.nextLine().split("\t") )
					.mapToInt( value -> Integer.parseInt( value ) )
					.iterator() ;
			for( int j=0 ; j<10 ; j++ ) {
				nodes[i][j] = line.next() ;
			}
		}
		
	}
	
	public void drawMap() {
		
		Iterator<Component> labels = Arrays.stream( this.mazePane.getComponents() ).iterator() ;
		
		Arrays.stream( nodes ).forEach( line -> {
			
			Arrays.stream( line ).forEach( node -> {
				
				if( node == 0 ) {
					setRoad( (JLabel) labels.next() ) ;
				}
				else if( node == 1 ) {
					setBrickwall( (JLabel) labels.next() ) ;
				}
				else {
					setDiamond( (JLabel) labels.next() ) ;
				}
				
			} ) ;
			
		} ) ;
		
	}
	public void chooseHeart() {
		threads = Executors.newScheduledThreadPool(8) ;
		
		Arrays.stream( this.mazePane.getComponents() ).forEach( cube -> {
			if( random.nextInt(7) == 0 ) {
				threads.scheduleWithFixedDelay( () -> {
					cubeSwitch( (JLabel)cube ) ;
				}, 1, random.nextInt(3000)+1, TimeUnit.MILLISECONDS) ;
			}
		}) ;
	}
	public void cubeSwitch(JLabel cube) {
		if( cube.getName().equals("brickwall") ) {
			( (JLabel)cube ).removeMouseListener( ( (JLabel)cube ).getMouseListeners()[0] ) ;
			setHeart( (JLabel)cube ) ;
			try {
				Thread.sleep( ( random.nextInt(3000)+1 ) ) ;
			} catch (InterruptedException e) {
				// TODO Auto-generted catch block
			}
			if( cube.getName().equals("heart") ) {
				( (JLabel)cube ).removeMouseListener( ( (JLabel)cube ).getMouseListeners()[0] ) ;
				setBrickwall( (JLabel)cube ) ;
			}
		}
	}
	
	public void setHeart(JLabel cube) {
		cube.setIcon(heart) ;
		cube.setName("heart") ;
		cube.addMouseListener( new HeartHDL() ) ;
	}
	public void setRoad(JLabel cube) {
		cube.setName("road") ;
		cube.setBackground(Color.white) ;	
		cube.addMouseListener( new RoadHDL() ) ;
	}
	public void setBrickwall(JLabel cube) {
		cube.setIcon(brickwall) ;
		cube.setName("brickwall") ;
		cube.addMouseListener( new BrickwallHDL() ) ;
	}
	public void setDiamond(JLabel cube) {
		cube.setIcon(diamond) ;
		cube.setName("diamond") ;
		cube.addMouseListener( new DiamondHDL() ) ;
	}
	public void enterRoad() {
		hpChange = Executors.newScheduledThreadPool(1) ;
		hpChange.scheduleWithFixedDelay( () -> { 
			hp.setValue( hp.getValue() - 2 ) ; 
		} , 0, 1, TimeUnit.SECONDS) ;
	}
	public void enterWall() {
		hpChange = Executors.newScheduledThreadPool(1) ;
		hpChange.scheduleWithFixedDelay( () -> { 
			hp.setValue( hp.getValue() - 20 ) ; 
		} , 0, 1, TimeUnit.SECONDS) ;
	}
	private class HeartHDL extends MouseAdapter {
		@Override
		public void mouseEntered(MouseEvent e) {
			hp.setValue( hp.getValue() + 10 ) ;
			( (JLabel)e.getSource() ).setIcon(null) ;
			( (JLabel)e.getSource() ).removeMouseListener( ( (JLabel)e.getSource() ).getMouseListeners()[0] ) ;
			setRoad( (JLabel)e.getSource() ) ;
			hpChange = Executors.newScheduledThreadPool(1) ;
			hpChange.scheduleWithFixedDelay( () -> { 
				hp.setValue( hp.getValue() - 2 ) ; 
			} , 1, 1, TimeUnit.SECONDS) ;
		}	
	}
	private class RoadHDL extends MouseAdapter {
		@Override
		public void mouseEntered(MouseEvent e) {
			enterRoad() ;
		}
		public void mouseExited(MouseEvent e) {
			if( !hpChange.isShutdown() )
			hpChange.shutdown(); 
		}
	}
	private class BrickwallHDL extends MouseAdapter {
		@Override
		public void mouseEntered(MouseEvent e) {
			enterWall() ;
		}
		public void mouseExited(MouseEvent e) {
			if( !hpChange.isShutdown() )
			hpChange.shutdownNow() ;
		}
	}
	private class DiamondHDL extends MouseAdapter {
		@Override
		public void mouseEntered(MouseEvent e) {
			win = true ;
		}
	}
	
	public void removeMap() {
		
		for( Component label : this.mazePane.getComponents() ) {
			for( MouseListener listener : label.getMouseListeners() ) {
				label.removeMouseListener( listener ) ;
			}
		}
		
	}
	
	public void play() {
		
		while( !win ) {
			
			if( hp.getValue() <= 0 ) {
				break ;
			}
			hpValue.setText( String.valueOf( hp.getValue() ) ) ;
			this.repaint() ;
		}
		
		this.setEnabled( false ) ;
		hpValue.setText( String.valueOf( hp.getValue() ) ) ;
		
		if( win ) {
			choose = JOptionPane.showOptionDialog(this, "恭喜你贏了！要再來一局嗎？", "Diamond Maze"
					, JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null, new String[]{"再來一局","離開"}, null ) ;
		}
		else {
			choose = JOptionPane.showOptionDialog(this, "You Lose. 要再來一局嗎？", "Diamond Maze"
					, JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null, new String[]{"再來一局","離開"}, null ) ;
			hpChange.shutdown() ;
		}
		
		if( choose == 0 ) {
			reset() ;
		}
		else {
			threads.shutdownNow() ;
		}
	}
	public void reset() {
		threads.shutdownNow() ;
		win = false ;
		hp.setValue( 100 ) ;
		hpValue.setText( String.valueOf( hp.getValue() ) ) ;
		removeMap() ;
		lock.stop() ;
	}
	
	public void mouseFixed() {
		ActionListener mouseFixed = new ActionListener() {
			Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize() ; 
			
			@Override
			public void actionPerformed(ActionEvent event) {
				
				if( countdown.isVisible() ){
					mouseLocker.mouseMove( (int)Math.round( ( screenSize.getWidth()+mazePane.getWidth() )/2 - mazePane.getWidth()*0.05  )
							, (int)Math.round( ( screenSize.getHeight()+mazePane.getHeight() )/2 - mazePane.getHeight()*0.05 + hpPanel.getHeight() ) ) ;
				}
				else {
					( (Timer)event.getSource() ).stop() ; 
				}
				
			}
			
	    } ;
	    
	    Timer fixed = new Timer( 1, mouseFixed ) ;
	    fixed.start() ;
	}
	public void mouseLock() {
		
		ActionListener mouseHDL = new ActionListener() {
			Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize() ; 
			private Point oldPoint = null ;
			private Point point = null ;
			
			@Override
			public void actionPerformed(ActionEvent event) {
				
				oldPoint = point ;
				point = MouseInfo.getPointerInfo().getLocation() ;
				
				if( mazePane.isVisible() ) {
					if( oldPoint == null ) {
						if( point.getX() < ( screenSize.getWidth()-mazePane.getWidth() )/2 
								|| point.getY() < ( screenSize.getHeight()-mazePane.getHeight() )/2 + hpPanel.getHeight()
								|| point.getX() > ( screenSize.getWidth()+mazePane.getWidth() )/2 
								|| point.getY() > ( screenSize.getHeight()+mazePane.getHeight() )/2  ) {
							mouseLocker.mouseMove( (int)Math.round( ( screenSize.getWidth()+mazePane.getWidth() )/2 - mazePane.getWidth()*0.05 ) 
									, (int)Math.round( ( screenSize.getHeight()+mazePane.getHeight() )/2 - mazePane.getHeight()*0.05 + hpPanel.getHeight() ) ) ;
							point.setLocation( ( screenSize.getWidth()+mazePane.getWidth() )/2 - mazePane.getWidth()*0.05 
									, ( screenSize.getHeight()+mazePane.getHeight() )/2 - mazePane.getHeight()*0.05 + hpPanel.getHeight() ) ;
						}
						else {
						}
					}
					else {
						if( point.getX() < ( screenSize.getWidth()-mazePane.getWidth() )/2 
								|| point.getY() < ( screenSize.getHeight()-mazePane.getHeight() )/2 + hpPanel.getHeight()
								|| point.getX() > ( screenSize.getWidth()+mazePane.getWidth() )/2 
								|| point.getY() > ( screenSize.getHeight()+mazePane.getHeight() )/2  ) {
							mouseLocker.mouseMove( (int)Math.round( oldPoint.getX() ) 
									, (int)Math.round( oldPoint.getY() ) ) ;
							point.setLocation( oldPoint.getX() , oldPoint.getY() ) ;
						}
						else {
						}
					}
				}
				else {
					( (Timer)event.getSource() ).stop() ; 
				}
				
			}
			
	    } ;
		
		lock = new Timer( 1, mouseHDL ) ; 
		lock.start();
		
	}
	
}
