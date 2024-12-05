package org.example;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Random;

public class App extends JFrame {
    private int rows, cols, mines;
    private JButton[][] buttons;
    private boolean[][] isMine;
    private int[][] neighborCounts;
    private boolean[][] revealed;
    private boolean gameOver = false;
    private boolean firstClick = true;
    private Timer timer;
    private int timeElapsed = 0;
    private JLabel timerLabel;

    public App(int rows, int cols, int mines) {
        this.rows = rows;
        this.cols = cols;
        this.mines = mines;

        setTitle("Minesweeper");
        setSize(600, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Панель для таймера
        JPanel topPanel = new JPanel();
        timerLabel = new JLabel("Время: 0");
        timerLabel.setFont(new Font("Arial", Font.BOLD, 16));
        topPanel.add(timerLabel);
        add(topPanel, BorderLayout.NORTH);

        // Создаем игровое поле
        JPanel gamePanel = new JPanel();
        gamePanel.setLayout(new GridLayout(rows, cols));
        buttons = new JButton[rows][cols];
        isMine = new boolean[rows][cols];
        neighborCounts = new int[rows][cols];
        revealed = new boolean[rows][cols];
        initializeGame(gamePanel);

        add(gamePanel, BorderLayout.CENTER);
        setVisible(true);

        // Таймер
        timer = new Timer(1000, e -> {
            timeElapsed++;
            timerLabel.setText("Время: " + timeElapsed);
        });
    }

    private void initializeGame(JPanel gamePanel) {
        // Инициализация кнопок
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                buttons[i][j] = new JButton();
                buttons[i][j].setFont(new Font("Arial", Font.BOLD, 14));
                buttons[i][j].setFocusPainted(false);

                final int x = i;
                final int y = j;
                buttons[i][j].addMouseListener(new java.awt.event.MouseAdapter() {
                    @Override
                    public void mouseClicked(java.awt.event.MouseEvent e) {
                        if (SwingUtilities.isRightMouseButton(e)) {
                            toggleFlag(x, y);
                        } else if (SwingUtilities.isLeftMouseButton(e)) {
                            handleClick(x, y);
                        }
                    }
                });

                gamePanel.add(buttons[i][j]);
            }
        }
    }

    private void placeMines(int startX, int startY) {
        Random random = new Random();
        int placedMines = 0;

        while (placedMines < mines) {
            int x = random.nextInt(rows);
            int y = random.nextInt(cols);

            if ((x == startX && y == startY) || isMine[x][y]) continue; // Гарантируем, что первая клетка безопасна
            isMine[x][y] = true;
            placedMines++;
        }
    }

    private void calculateNeighbors() {
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (isMine[i][j]) continue;

                int count = 0;
                for (int dx = -1; dx <= 1; dx++) {
                    for (int dy = -1; dy <= 1; dy++) {
                        int nx = i + dx, ny = j + dy;
                        if (nx >= 0 && nx < rows && ny >= 0 && ny < cols && isMine[nx][ny]) {
                            count++;
                        }
                    }
                }
                neighborCounts[i][j] = count;
            }
        }
    }

    private void handleClick(int x, int y) {
        if (gameOver || revealed[x][y]) return;

        if (firstClick) {
            firstClick = false;
            placeMines(x, y);
            calculateNeighbors();
            timer.start();
        }

        if (isMine[x][y]) {
            buttons[x][y].setText("💣");
            buttons[x][y].setBackground(Color.RED);
            endGame(false);
        } else {
            revealCell(x, y);
            if (checkWin()) {
                endGame(true);
            }
        }
    }

    private void revealCell(int x, int y) {
        if (x < 0 || x >= rows || y < 0 || y >= cols || revealed[x][y]) return;

        revealed[x][y] = true;
        buttons[x][y].setEnabled(false);
        if (neighborCounts[x][y] > 0) {
            buttons[x][y].setText(String.valueOf(neighborCounts[x][y]));
        } else {
            for (int dx = -1; dx <= 1; dx++) {
                for (int dy = -1; dy <= 1; dy++) {
                    revealCell(x + dx, y + dy);
                }
            }
        }
    }

    private void toggleFlag(int x, int y) {
        if (!buttons[x][y].isEnabled() || revealed[x][y]) return;

        String currentText = buttons[x][y].getText();
        if ("🚩".equals(currentText)) {
            buttons[x][y].setText("");
        } else {
            buttons[x][y].setText("🚩");
        }
    }

    private boolean checkWin() {
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (!isMine[i][j] && !revealed[i][j]) {
                    return false;
                }
            }
        }
        return true;
    }

    private void endGame(boolean win) {
        gameOver = true;
        timer.stop();
        String message = win ? "Вы победили!" : "Вы проиграли!";
        JOptionPane.showMessageDialog(this, message);

        // Перезапуск игры
        int choice = JOptionPane.showConfirmDialog(this, "Хотите сыграть снова?");
        if (choice == JOptionPane.YES_OPTION) {
            dispose();
            new App(rows, cols, mines);
        } else {
            dispose();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            String[] options = {"Лёгкий", "Средний", "Сложный"};
            int choice = JOptionPane.showOptionDialog(null, "Выберите уровень сложности", "Сапёр",
                    JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);

            if (choice == 0) new App(9, 9, 10);        // Лёгкий
            else if (choice == 1) new App(16, 16, 40); // Средний
            else if (choice == 2) new App(16, 30, 99); // Сложный
        });
    }
}