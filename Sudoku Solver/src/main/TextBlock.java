package main;

import java.awt.Color;
import java.awt.GridLayout;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JComponent;


@SuppressWarnings("serial")
public class TextBlock extends JComponent {
	
	public final int row;
	public final int column;
	public final Set<Cell> cells = new HashSet<Cell>();
	
	public TextBlock(int row, int column) {
		this.row = row;
		this.column = column;
		
		setOpaque(true);
		setDoubleBuffered(false);
		setLayout(new GridLayout(3, 3));
		setBackground(Color.BLACK);
		for(int i = 0; i < 3; i++) {
			for(int j = 0; j < 3; j++) {
				Cell cell = new Cell(row*3 + i, column*3 + j, this, -1);
				cells.add(cell);
				add(cell);
			}
		}
		
		
		
	}
	
	public void removeNumFromAll(Cell exception, int num) {
		for(Cell cell : cells) {
			if(cell != exception) {
				cell.removeFromPossibleValues(num);
			}
		}
	}
	
}
