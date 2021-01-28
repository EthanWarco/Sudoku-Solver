package main;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.JTextField;
import javax.swing.plaf.FontUIResource;

@SuppressWarnings("serial")
public class Cell extends JTextField implements KeyListener {
	
	public int value;
	public final TextBlock block;
	public final int row;
	public final int column;
	public Set<Integer> possibleValues = new HashSet<Integer>();
	public Set<Cell> triples = new HashSet<Cell>();
	private final Map<Integer, Cell> rowPairs = new HashMap<Integer, Cell>();
	private final Map<Integer, Cell> columnPairs = new HashMap<Integer, Cell>();
	private final Font font = new Font(FontUIResource.DIALOG, Font.PLAIN, 75);
	public final Color[] colors = {Color.GREEN, Color.RED, Color.CYAN, Color.YELLOW, Color.PINK, Color.BLUE, Color.MAGENTA, Color.ORANGE, Color.LIGHT_GRAY};
	
	
	public Cell(int row, int column, TextBlock block, int number) {
		this.block = block;
		this.value = number;
		this.row = row;
		this.column = column;
		for(int i = 1; i <= 9; i++) {
			possibleValues.add(i);
		}
		
		setFont(font);
		setVisible(true);
		addKeyListener(this);
		setBorder(BorderFactory.createMatteBorder(1, 1, 0, 0, Color.BLACK));
		Main.grid[column][row] = this;
	}
	
	public void changeValue(int value) {
		if(value != -1) {
			possibleValues = new HashSet<Integer>();
			possibleValues.add(value);
		} else {
			possibleValues = new HashSet<Integer>();
			for(int i = 1; i <= 9; i++) {
				possibleValues.add(i);
			}
		}
		this.value = value;
	}
	
	
	public boolean checkSet(Set<Cell> cells, boolean isColumn, boolean isRow, boolean isBlock) {
		Map<Integer, Set<Cell>> matches = new HashMap<Integer, Set<Cell>>();
		
		Set<Cell> nakedSubsets = new HashSet<Cell>();
		
		boolean triple = possibleValues.size() == 2;
		
		for(Cell other : cells) {
			if(other.value != -1) {
				continue;
			}
			
			/*
			 * Looks for a naked group, where there are multiple instances of the exact same sets of possible values in a block, column, or row.
			 * The else if below is trying to find a naked triple. The way it confirms naked triples is whenever a cell has a possible value in
			 * common with this cell, it adds it to a hashmap that includes the cell and the set of possible values that must belong to the triple
			 * if there is one. Then, during every loop, it checks if the next cell's possible value set is equal to any of the possible value sets
			 * in the hash map. If it is, then it is confirmed that the group is a naked triple.
			 */
			if(Main.containsOnly(possibleValues, other.possibleValues) && other != this) {
				nakedSubsets.add(other);
			} else if(triple && Main.containsAny(possibleValues, other.possibleValues) && other.possibleValues.size() == 2 && other != this) {
				Set<Integer> tripleValues = getTripleValues(other);
				triples.add(other);
				for(Cell cell : triples) {
					if(cell.possibleValues.equals(tripleValues)) {
						if(cells.contains(cell)) {
							setTriples(cells, other, cell);
						} else {
							setXYWing(other, cell);
						}
					}
				}
			}
			
			//Documents how many times a possible value is contained in each cell per set. If only once, then that possible value can be confirmed to be the value.
			for(int value : other.possibleValues) {
				if(possibleValues.contains(value)) {
					if(matches.containsKey(value)) {
						matches.get(value).add(other);
					} else {
						Set<Cell> set = new HashSet<Cell>();
						set.add(other);
						matches.put(value, set);
					}
				}
			}
		}
		
		/*
		 * Checks if it can confirm the cell through the matches map from earlier
		 */
		for(int key : matches.keySet()) {
			Set<Cell> set = matches.get(key);
			if(set.size() == 1) {
				return Main.setCellValue(this, key);
			} else if((set.size() == 2 || set.size() == 3) && isBlock) {
				if(set.stream().allMatch((c) -> c.column == column)) {
					for(int i = 0; i < 9; i++) {
						Cell removal = Main.grid[column][i];
						if(!set.contains(removal)) {
							removal.removeFromPossibleValues(key);
						}
					}
				} else if(set.stream().allMatch((c) -> c.row == row)) {
					for(int i = 0; i < 9; i++) {
						Cell removal = Main.grid[i][row];
						if(!set.contains(removal)) {
							removal.removeFromPossibleValues(key);
						}
					}
				}
			}
			if(set.size() == 2) {
				xWing(isRow, isColumn, set, key);
			}
		}
		
		//Responds in the case of a naked group
		if(nakedSubsets.size()+1 == possibleValues.size()) {
			for(Cell other : cells) {
				if(!nakedSubsets.contains(other) && other != this) {
					other.removeAllFromPossibleValues(possibleValues);
				}
			}
		}
		
		return false;
	}
	
	
	
	private void setTriples(Set<Cell> cells, Cell triple1, Cell triple2) {
		Set<Integer> values = new HashSet<Integer>(possibleValues);
		values.addAll(triple1.possibleValues);
		for(Cell cell : cells) {
			if(triple1 != cell && triple2 != cell && this != cell) {
				cell.removeAllFromPossibleValues(values);
			}
		}
	}
	
	private Set<Integer> getTripleValues(Cell cell) {
		Set<Integer> values = new HashSet<Integer>(possibleValues);
		values.addAll(cell.possibleValues);
		int removal = -1;
		for(int num : values) {
			if(cell.possibleValues.contains(num) && possibleValues.contains(num)) {
				removal = num;
				break;
			}
		}
		values.remove(removal);
		return values;
	}
	
	
	
	
	public void removeAllFromPossibleValues(Set<Integer> nums) {
		for(int num : nums) {
			removeFromPossibleValues(num);
		}
	}
	
	public void removeFromPossibleValues(int num) {
		possibleValues.remove(num);
		if(rowPairs.containsKey(num)) {
			rowPairs.get(num).rowPairs.remove(num);
			rowPairs.remove(num);
		}
		if(columnPairs.containsKey(num)) {
			columnPairs.get(num).columnPairs.remove(num);
			columnPairs.remove(num);
		}
	}
	
	/*
	 * Here's how I implemented the X-Wing technique.
	 * Whenever I find a candidate that only occurs twice in a row or column, I add that number to a hashmap in each of the pair's class.
	 * The hashmap contains the partner cell that also contains the candidate. Then, I check if there is another pair within the row or column 
	 * that also contains that candidate in its row or column, and check if they have the same column / row. I facilitate these pairs within the
	 * removeFromPossibleValues method.
	 */
	private void xWing(boolean isRow, boolean isColumn, Set<Cell> set, int num) {
		Cell[] arr = new Cell[2];
		set.toArray(arr);
		if(isRow) {
			int rowPair = -1;
			for(int i = 0; i < 9; i++) {
				Cell cell = Main.grid[arr[0].column][i];
				if(i != row && cell.rowPairs.containsKey(num) && cell.rowPairs.get(num) == Main.grid[arr[1].column][i]) {
					rowPair = i;
				}
			}
			if(rowPair != -1) {
				for(int i = 0; i < 9; i++) {
					if(i != rowPair && i != row) {
						Main.grid[arr[0].column][i].removeFromPossibleValues(num);
						Main.grid[arr[1].column][i].removeFromPossibleValues(num);
					}
				}
			}
			
			arr[0].rowPairs.put(num, arr[1]);
			arr[1].rowPairs.put(num, arr[0]);
		} else if(isColumn) {
			int columnPair = -1;
			for(int i = 0; i < 9; i++) {
				Cell cell = Main.grid[i][arr[0].row];
				if(i != column && cell.columnPairs.containsKey(num) && cell.columnPairs.get(num) == Main.grid[i][arr[1].row]) {
					columnPair = i;
					break;
				}
			}
			if(columnPair != -1) {
				for(int i = 0; i < 9; i++) {
					if(i != columnPair && i != column) {
						Main.grid[i][arr[0].row].removeFromPossibleValues(num);
						Main.grid[i][arr[1].row].removeFromPossibleValues(num);
					}
				}
			}
			arr[0].columnPairs.put(num, arr[1]);
			arr[1].columnPairs.put(num, arr[0]);
		}
	}
	
	private void setXYWing(Cell wing, Cell partner) {
		int common = -1;
		for(int num : wing.possibleValues) {
			if(partner.possibleValues.contains(num)) {
				common = num;
				break;
			}
		}
		/*
		 * remove the common candidate from each cell from either block that is intersected by the partner cell. also remove candidate from endpoints of the cell and partner
		 */
		for(Cell cell : wing.block.cells) {
			if(cell.value != -1 && (cell.row == partner.row || cell.column == partner.column)) {
				cell.removeFromPossibleValues(common);
			}
		}
		for(Cell cell : partner.block.cells) {
			if(cell.value != -1 && (cell.row == wing.row || cell.column == wing.column)) {
				cell.removeFromPossibleValues(common);
			}
		}
		Main.grid[wing.column][partner.row].removeFromPossibleValues(common);
		Main.grid[partner.column][wing.row].removeFromPossibleValues(common);
	}
	
	
	
	
	@Override
	public void keyPressed(KeyEvent e) {
		if(e.getKeyCode() == KeyEvent.VK_UP) {
			Main.moveFocus('N');
		} else if(e.getKeyCode() == KeyEvent.VK_DOWN) {
			Main.moveFocus('S');
		} else if(e.getKeyCode() == KeyEvent.VK_RIGHT) {
			Main.moveFocus('E');
		} else if(e.getKeyCode() == KeyEvent.VK_LEFT) {
			Main.moveFocus('W');
		}
	}
	
	@Override
	public void keyTyped(KeyEvent e) {
		if(e.getKeyChar() >= '1' && e.getKeyChar() <= '9') {
			changeValue(Character.getNumericValue(e.getKeyChar()));
			setText("");
			e.setKeyChar(e.getKeyChar());
		} else if(e.getKeyChar() != KeyEvent.VK_BACK_SPACE) {
			if(value != -1) {
				setText("" + value + "");
			} else {
				setText("");
			}
			e.setKeyChar(KeyEvent.CHAR_UNDEFINED);
		} else if(e.getKeyChar() == KeyEvent.VK_BACK_SPACE) {
			changeValue(-1);
		}
	}
	
	@Override
	public void keyReleased(KeyEvent e) {
		
	}
	
}
