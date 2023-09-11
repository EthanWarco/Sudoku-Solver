package main;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;
import javax.swing.plaf.FontUIResource;

public class Main implements ActionListener {
	
	private static JFrame frame;
	private static JPanel sudokuPanel;
	private static JPanel options;
	private static JButton solve;
	private static JButton quit;
	private static JButton clear;
	private static JLabel solvable;
	private static final int width = Toolkit.getDefaultToolkit().getScreenSize().width;
	private static final int height = Toolkit.getDefaultToolkit().getScreenSize().height;
	private static final Font font = new Font(FontUIResource.DIALOG, Font.BOLD, 20);
	private static final Color color = new Color(240, 240, 240);
	
	public static final Cell[][] grid = new Cell[9][9];
	public static final TextBlock[][] textBlocks = new TextBlock[3][3];
	
	public Main() {
		
		
		solve = new JButton("Solve");
		solve.setPreferredSize(new Dimension(width/4 - 100, height/22));
		solve.setFont(font);
		solve.addActionListener(this);
		
		quit = new JButton("Quit");
		quit.setPreferredSize(new Dimension(width/4 - 100, height/22));
		quit.setFont(font);
		quit.addActionListener(this);
		
		clear = new JButton("Clear");
		clear.setPreferredSize(new Dimension(width/4 - 100, height/22));
		clear.setFont(font);
		clear.addActionListener(this);
		
		solvable = new JLabel("");
		solvable.setPreferredSize(new Dimension(width/4 - 100, height/30));
		solvable.setFont(font);
		solvable.setHorizontalAlignment(JLabel.CENTER);
		
		
		
		options = new JPanel();
		options.setBorder(new TitledBorder(BorderFactory.createLineBorder(new Color(186, 198, 205), 2), "Sudoku Solver", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, font));
		options.setBounds(10, 5, width/4 - 20, height - 15);
		options.setBackground(color);
		options.setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridwidth = 3;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		
		gbc.gridy = 0;
		options.add(quit, gbc);
		
		gbc.gridy = 1;
		JLabel blank = new JLabel("");
		blank.setPreferredSize(new Dimension(width/4 - 100, height/110));
		options.add(blank, gbc);
		
		gbc.gridy = 2;
		options.add(clear, gbc);
		
		gbc.gridy = 3;
		blank = new JLabel("");
		blank.setPreferredSize(new Dimension(width/4 - 100, height - height/4));
		options.add(blank, gbc);
		
		gbc.gridy = 4;
		options.add(solvable, gbc);
		
		gbc.gridy = 5;
		options.add(solve, gbc);
		
		
		
		
		sudokuPanel = new JPanel();
		sudokuPanel.setBounds(width/4, 0, 3*width/4, height);
		sudokuPanel.setBackground(Color.WHITE);
		sudokuPanel.setLayout(new GridBagLayout());
		
		JPanel sudoku = new JPanel();
		sudoku.setPreferredSize(new Dimension(height - height/8, height - height/8));
		sudoku.setLayout(new GridLayout(3, 3, 2, 2));
		sudoku.setBackground(Color.BLACK);
		sudoku.setBorder(BorderFactory.createMatteBorder(2, 2, 2, 2, Color.BLACK));
		
		for(int i = 0; i < textBlocks.length; i++) {
			for(int j = 0; j < textBlocks[i].length; j++) {
				textBlocks[i][j] = new TextBlock(i, j);
				sudoku.add(textBlocks[i][j]);
			}
		}
		sudokuPanel.add(sudoku);
		
		
		
		
		frame = new JFrame("Sudoku Solver");
		frame.setFocusable(true);
		frame.setUndecorated(true);
		frame.setVisible(true);
		frame.setSize(new Dimension(width, height));
		frame.setResizable(false);
		frame.setPreferredSize(Toolkit.getDefaultToolkit().getScreenSize());
		frame.getContentPane().setBackground(color);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		frame.setLayout(null);
		frame.getContentPane().add(options);
		frame.getContentPane().add(sudokuPanel);
		frame.pack();
	}
	
	public static void main(String[] args) {
		new Main();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if(e.getSource() == solve) {
			solve();
		} else if(e.getSource() == quit) {
			frame.dispose();
		} else if(e.getSource() == clear) {
			for(int i = 0; i < grid.length; i++) {
				for(int j = 0; j < grid[i].length; j++) {
					grid[i][j].changeValue(-1);
					grid[i][j].setText("");
				}
			}
		}
	}
	
	
	public static void solve() {
		boolean active = false;
		
		Set<Cell> openCells = new HashSet<Cell>();
		for(int i = 0; i < grid.length; i++) {
			for(int j = 0; j < grid[i].length; j++) {
				Cell cell = grid[i][j];
				if(cell.value == -1) {
					openCells.add(cell);
				} else {
					cell.block.removeNumFromAll(cell, cell.value);
					removeNumFromRow(cell.value, cell.row);
					removeNumFromColumn(cell.value, cell.column);
				}
			}
		}
		

		do {
			
			active = false;
			Set<Cell> removeQueue = new HashSet<Cell>();
			
			
			for(Cell cell : openCells) {
				if(cell.possibleValues.size() == 1) {
					ArrayList<Integer> value = new ArrayList<Integer>(cell.possibleValues);
					active = setCellValue(cell, value.get(0));
					removeQueue.add(cell);
				} else {
					if(confirm(cell)) {
						active = true;
						removeQueue.add(cell);
					}
				}
				
			}
			openCells.removeAll(removeQueue);
			
			
			
		} while(active);
		
		
		if(openCells.isEmpty()) {
			solvable.setText("Solved!");
		} else {
			solvable.setText("Not Solvable!");
			for(Cell cell : openCells) {
				cell.setFont(new Font(font.getName(), font.getStyle(), 13));
				Object[] possibleValues = cell.possibleValues.toArray();
				cell.setText(Arrays.toString(possibleValues));
			}
		}
		
	}
	
	
	
	/*
	 * Helper Functions
	 */
	
	public static boolean confirm(Cell cell) {
		Set<Cell> row = new HashSet<Cell>();
		Set<Cell> column = new HashSet<Cell>();
		for(int i = 0; i < 9; i++) {
			row.add(Main.grid[i][cell.row]);
			column.add(Main.grid[cell.column][i]);
		}
		
		if(!cell.checkSet(row, false, true, false)) {
			if(!cell.checkSet(column, true, false, false)) {
				if(!cell.checkSet(cell.block.cells, false, false, true)) {
					cell.triples = new HashSet<Cell>();
					return false;
				}
			}
		}

		cell.triples = new HashSet<Cell>();
		return true;
	}
	
	public static boolean setCellValue(Cell cell, int value) {
		cell.changeValue(value);
		removeNumFromRow(cell.value, cell.row);
		removeNumFromColumn(cell.value, cell.column);
		cell.block.removeNumFromAll(cell, cell.value);
		cell.setForeground(Color.BLUE);
		cell.setText("" + cell.value + "");
		return true;
	}
	
	public static void moveFocus(char dir) {
		if(frame.getFocusOwner() instanceof Cell) {
			Cell cell = (Cell) frame.getFocusOwner();
			switch(dir) {
				case 'N':
					if(cell.row > 0) {
						grid[cell.column][cell.row - 1].requestFocus();
					}
					break;
				case 'S':
					if(cell.row < 8) {
						grid[cell.column][cell.row + 1].requestFocus();
					}
					break;
				case 'W':
					if(cell.column > 0) {
						grid[cell.column - 1][cell.row].requestFocus();
					}
					break;
				case 'E':
					if(cell.column < 8) {
						grid[cell.column + 1][cell.row].requestFocus();
					}
					break;
			}
		}
	}
	
	
	public static void removeNumFromRow(int num, int row) {
		for(int i = 0; i < 9; i++) {
			if(grid[i][row].possibleValues.contains(num)) {
				grid[i][row].removeFromPossibleValues(num);
			}
		}
	}
	
	public static void removeNumFromColumn(int num, int column) {
		for(int i = 0; i < 9; i++) {
			if(grid[column][i].possibleValues.contains(num)) {
				grid[column][i].removeFromPossibleValues(num);
			}
		}
	}
	
	public static boolean containsAny(Set<?> parentSet, Set<?> contained) {
		for(Object o : contained) {
			if(parentSet.contains(o)) {
				return true;
			}
		}
		return false;
	}
	
	public static boolean containsOnly(Set<?> parentSet, Set<?> contained) {
		for(Object o : contained) {
			if(!parentSet.contains(o)) {
				return false;
			}
		}
		return true;
	}
}
