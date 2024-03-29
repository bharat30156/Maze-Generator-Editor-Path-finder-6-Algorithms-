package SourcePackage;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DragSource;
import java.awt.dnd.DragSourceAdapter;
import java.awt.dnd.DragSourceDragEvent;
import java.awt.dnd.DragSourceDropEvent;
import java.awt.dnd.DragSourceMotionListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.TransferHandler;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 * 
 * @author Bharat
 *
 */
public class GUI {
    

    private final ImageIcon startIcon = new ImageIcon(Toolkit.getDefaultToolkit().getImage(getClass().getResource(
                "/Icons/start.png")));
    private final ImageIcon goalIcon = new ImageIcon(Toolkit.getDefaultToolkit().getImage(getClass().getResource(
                "/Icons/goal.png")));
    private final ImageIcon legendIcon = new ImageIcon(Toolkit.getDefaultToolkit().getImage(getClass().getResource(
                    "/Icons/legend.png")));
    private final ImageIcon labyIcon = new ImageIcon(Toolkit.getDefaultToolkit().getImage(getClass().getResource(
                "/Icons/laby.png")));
    private JFrame mainFrame;//main window
    private MazePanel mazePanel;//displays maze
    private boolean saved;//maze saved
    private String directory;//save directory
    private Maze maze;//open maze, holds static maze data
    private MazeSearch solver;//solves the open maze
    private Thread runThread;//runs to solve the maze
    private boolean pause;//thread paused
    private MazeGenerator generator;//generates maze
    private boolean helpOpen;//open help window
    private boolean generatorMode;//to check whether to reset on File menu click
    
    /**
     * Constructor, builds GUI
     */
    public GUI(){
        saved = true;
        directory = null;
        solver = null;
        pause = false;
        helpOpen = false;
        generatorMode = false;

        
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            System.out.println("Using Java default look&feel");
        }
        
        mainFrame = new JFrame("My First Project");
        mainFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        mainFrame.setLayout(new BorderLayout(15, 10));
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        
        maze = new Maze(16, 16);
        mazePanel = new MazePanel(maze);
        
        JPanel mainPanel = new JPanel(new GridLayout(0, 1));
        mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        mainPanel.add(mazePanel);
        
        DragSource ds = DragSource.getDefaultDragSource();
            
        DragSourceMotionListener dsml = new DragSourceMotionListener() {

                @Override
                public void dragMouseMoved(DragSourceDragEvent dsde) {
                    DataFlavor flavor = dsde.getDragSourceContext().getTransferable().getTransferDataFlavors()[0];
                    String data = null;
                    try {
                        data = dsde.getDragSourceContext().getTransferable().getTransferData(flavor).toString();
                    } catch (UnsupportedFlavorException ex) {
                        System.out.println("DnD Unsupported Flavor");
                    } catch (IOException ex) {
                        System.out.println("DnD IOException");
                    }
                    mazePanel.setDnDPreview(data);
                    
                }
            };
        ds.addDragSourceMotionListener(dsml);
        ds.addDragSourceListener(new DragSourceAdapter() {
            @Override
            public void dragDropEnd(DragSourceDropEvent dsde) {
                mazePanel.endPreview();
            }
        });

            
        
        
        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("File");
        menuBar.add(fileMenu);
        JMenuItem newMaze = new JMenuItem("New Maze...");
        JMenuItem openMaze = new JMenuItem("Open Maze...");
        JMenuItem saveMazeAs = new JMenuItem("Save As...");
        JMenuItem saveMaze = new JMenuItem("Save");
        saveMazeAs.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                saveAs();
            }
        });
        saveMaze.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                save();
            }
        });
        JMenuItem exit = new JMenuItem("Exit");
        exit.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                savePrompt(mainFrame);
                if (runThread != null){
                        runThread.interrupt();
                }
                mainFrame.dispose();
            }
        });
        
        JMenuItem clear = new JMenuItem("Clear Maze");
        clear.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                mazePanel.getMaze().whiten();
                mazePanel.repaint();
            }
        });
        
        fileMenu.add(newMaze);
        fileMenu.add(openMaze);
        fileMenu.addSeparator();
        fileMenu.add(saveMaze);
        fileMenu.add(saveMazeAs);
        fileMenu.addSeparator();
        fileMenu.add(clear);
        fileMenu.addSeparator();
        fileMenu.add(exit);
        

        
        JMenu helpMenu  = new JMenu("Help");
        JMenuItem legend = new JMenuItem("Legend");
        JMenuItem about = new JMenuItem("About...");
        helpMenu.add(legend);
        helpMenu.addSeparator();
        helpMenu.add(about);
        menuBar.add(helpMenu);
        legend.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                if (!helpOpen){
                    JDialog helpDialog = new JDialog(mainFrame, "Help", false);
                
                    JLabel iconLabel = new JLabel(legendIcon);
                
                    helpDialog.add(iconLabel, BorderLayout.CENTER);
                    helpDialog.pack();
                    helpDialog.setLocationRelativeTo(mainFrame);
                    helpDialog.setResizable(false);
                    helpDialog.setVisible(true);
                    helpOpen = true;
                    helpDialog.addWindowListener(new WindowAdapter() {

                        @Override
                        public void windowClosing(WindowEvent e){
                            helpOpen = false;
                        }
                    });
                }
            }
        });
        
        about.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                JDialog aboutDialog = new JDialog(mainFrame, "About", true);
                
                JPanel aboutPanel = new JPanel();
                aboutPanel.setLayout(new BoxLayout(aboutPanel, BoxLayout.Y_AXIS));
                JLabel iconLabel = new JLabel(labyIcon);
                aboutPanel.add(iconLabel);
                aboutPanel.add(Box.createRigidArea(new Dimension(0, 5)));
                
                JLabel aboutLabel = new JLabel("<html> version 1.2 <br><br>"
                        + "GUI: Chris Darisaplis<br>"
                        + "Algorithms implementation: Chris Samarinas<br><br>"
                        + "Aristotle University of Thessaloniki<br>"
                        + "Faculty of Sciences<br>"
                        + "Department of Informatics</html>");
                aboutLabel.setAlignmentX(JComponent.LEFT_ALIGNMENT);
                aboutPanel.add(aboutLabel);
                
                JEditorPane linkPane= new JEditorPane();
                linkPane.setContentType("text/html");
                linkPane.setText("<p align='center'>Source code on <a href='https://github.com/greekdev/Laby/'>GitHub</a>.</p>");
                linkPane.setEditable(false);
                linkPane.setOpaque(false);
                linkPane.setAlignmentX(JComponent.LEFT_ALIGNMENT);
                linkPane.addHyperlinkListener(new HyperlinkListener() {

                    @Override
                    public void hyperlinkUpdate(HyperlinkEvent hle) {
                        if (HyperlinkEvent.EventType.ACTIVATED.equals(hle.getEventType())) {
                            System.out.println(hle.getURL());
                            Desktop desktop = Desktop.getDesktop();
                            try {
                                desktop.browse(hle.getURL().toURI());
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                        }
                    }
                });
                
                aboutPanel.add(linkPane);
                
                aboutPanel.setBorder(BorderFactory.createEmptyBorder(10,10,10,0));
                aboutDialog.add(aboutPanel);
                
                
                aboutDialog.pack();
                aboutDialog.setLocationRelativeTo(mainFrame);
                aboutDialog.setResizable(false);
                aboutDialog.setVisible(true);
            }
        });
        
        
        JPanel randomPanel = new JPanel(new BorderLayout());
        JCheckBox random = new JCheckBox("Choose cells randomly");
        randomPanel.add(random);
        randomPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 20));
        
        JPanel commandPanel = new JPanel();
        commandPanel.setLayout(new BoxLayout(commandPanel, BoxLayout.Y_AXIS));
        
        JLabel IDLabel = new JLabel("Set depth increment", JLabel.CENTER);
        IDLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        IDLabel.setVisible(false);
        JSpinner IDSpinner = new JSpinner(new SpinnerNumberModel(2, 1, Integer.MAX_VALUE, 1));
        IDSpinner.setAlignmentX(Component.CENTER_ALIGNMENT);
        IDSpinner.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));
        IDSpinner.setVisible(false);
        
        JLabel algoLabel = new JLabel("Algorithm");
        algoLabel.setAlignmentX(JLabel.CENTER_ALIGNMENT);
        String[] algorithms = {"DFS", "BFS", "ID", "Best-First", "Hill Climbing",
            "A*", "Maze Generator (DFS)"};
        JComboBox<String> algoSelection = new JComboBox<>(algorithms);
        algoSelection.setAlignmentX(Component.CENTER_ALIGNMENT);
        algoSelection.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));
        
        JPanel distanceSelectionPanel = new JPanel(new GridLayout(0, 1));
        JLabel distanceLabel = new JLabel("Heuristic Funciton", JLabel.CENTER);
        String[] distanceFunctions = {"Manhattan", "Euclidean"};
        JComboBox<String> distanceSelection = new JComboBox<>(distanceFunctions);
        distanceSelectionPanel.add(distanceLabel);
        distanceSelectionPanel.add(distanceSelection);
        distanceSelectionPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        distanceSelectionPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));
        distanceSelectionPanel.setVisible(false);
        
        JLabel statusLabel = new JLabel("Status: Not Started");
        statusLabel.setAlignmentX(JLabel.CENTER_ALIGNMENT);
        JLabel stepInfoLabel = new JLabel("Steps: 0");
        stepInfoLabel.setAlignmentX(JLabel.CENTER_ALIGNMENT);
        JLabel maxFrontLabel = new JLabel("Max frontier size: 0");
        maxFrontLabel.setAlignmentX(JLabel.CENTER_ALIGNMENT);
        JLabel solutionLabel = new JLabel("Max solution length: 0");
        solutionLabel.setAlignmentX(JLabel.CENTER_ALIGNMENT);
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.add(statusLabel);
        infoPanel.add(stepInfoLabel);
        infoPanel.add(solutionLabel);
        infoPanel.add(maxFrontLabel);
        infoPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        infoPanel.setBorder(BorderFactory.createTitledBorder(infoPanel.getBorder(),
                "Information"));
        infoPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 300));
        
        JLabel stepLabel = new JLabel("Step delay (msec):", JLabel.CENTER);
        stepLabel.setAlignmentX(JLabel.LEFT_ALIGNMENT);
        JSlider speedSlider = new JSlider(SwingConstants.HORIZONTAL, 0, 2000, 1000);
        speedSlider.setMajorTickSpacing(100);
        speedSlider.setPaintTicks(true);
        speedSlider.setPaintLabels(true);
        JSpinner speedSpinner = new JSpinner(new SpinnerNumberModel(1000, 0,
                2000, 1));
        speedSpinner.addChangeListener(new ChangeListener() {

            @Override
            public void stateChanged(ChangeEvent e) {
                speedSlider.setValue((Integer)speedSpinner.getValue());
            }
        });
        
        
        Hashtable labelTable = new Hashtable();
        labelTable.put( new Integer(0), new JLabel("0") );
        labelTable.put( new Integer(1000), new JLabel("1000"));
        labelTable.put( new Integer(2000), new JLabel("2000"));
        speedSlider.setLabelTable( labelTable );
        speedSlider.addChangeListener(new ChangeListener() {

            @Override
            public void stateChanged(ChangeEvent e) {
                speedSpinner.setValue(speedSlider.getValue());
            }
        });
        JPanel speedPanel = new JPanel(new GridLayout(1, 2));
        speedPanel.add(stepLabel);
        speedPanel.add(speedSpinner);
        speedPanel.setMaximumSize(new Dimension(9000, 100));
        
        JCheckBox arrowBox = new JCheckBox("Arrows from predecessors");
        arrowBox.setAlignmentX(JCheckBox.CENTER_ALIGNMENT);
        
        arrowBox.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                mazePanel.setDrawArros(arrowBox.isSelected());
            }
        });
        
        JCheckBox gridBox = new JCheckBox("Grid");
        gridBox.setSelected(true);
        gridBox.setAlignmentX(JCheckBox.CENTER_ALIGNMENT);
        gridBox.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                mazePanel.setDrawGrid(gridBox.isSelected());
            }
        });
        
        
        
        JButton runButton = new JButton("Run");
        JButton stopButton = new JButton("Reset");
        JButton pauseButton = new JButton("Pause");
        JButton nextButton = new JButton("Next Step");
        runButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        pauseButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                statusLabel.setText("Status: Paused");
                pauseButton.setEnabled(false);
                runButton.setEnabled(true);
                stopButton.setEnabled(true);
                nextButton.setEnabled(true);
                pause = true;
            }
        });
        
        stopButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                mazePanel.setEditable(true);
                if (runThread != null){
                    runThread.interrupt();
                }
                random.setEnabled(true);
                algoSelection.setEnabled(true);
                IDSpinner.setEnabled(true);
                runButton.setEnabled(true);
                nextButton.setEnabled(true);
                speedSlider.setEnabled(true);
                speedSpinner.setEnabled(true);
                stopButton.setEnabled(false);
                pause = false;
                clearMaze();
                if (algoSelection.getSelectedItem().equals("Maze Generator (DFS)")){
                    mazePanel.getMaze().whiten();
                }
                statusLabel.setText("Status: Not Started");
                
            }
        });
        runButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                if (algoSelection.getSelectedItem().toString().equals("Maze Generator (DFS)")){
                    statusLabel.setText("Status: Generating...");
                    generatorMode = true;
                    random.setEnabled(false);
                    algoSelection.setEnabled(false);
                    IDSpinner.setEnabled(false);
                    stopButton.setEnabled(true);
                    pauseButton.setEnabled(true);
                    nextButton.setEnabled(false);
                    runButton.setEnabled(false);
                    if (!pause){
                        mazePanel.blacken();
                        generator = new MazeGenerator(mazePanel.getMaze().getColumns(),
                            mazePanel.getMaze().getRows(), random.isSelected(),
                            mazePanel.getMaze());
                            mazePanel.repaint();
                    }
                    pause = false;
                    runThread = new Thread(new Runnable() {

                        @Override
                        public void run() {
                            try {
                                while(generator.nextStep(speedSlider.getValue())){
                                    if (pause){
                                        runThread.interrupt();
                                    }
                                    mazePanel.repaint();
                                }
                            } catch (InterruptedException e) {
                                System.out.println("Interrupted");
                            }
                            pauseButton.setEnabled(false);
                            algoSelection.setEnabled(true);
                            if (!pause){
                                speedSlider.setEnabled(false);
                                speedSpinner.setEnabled(false);
                                statusLabel.setText("Status: Maze generated!");
                            }
                            mazePanel.repaint();
                        }
                    });
                    runThread.start();
                }
                else if (canSolve()){
                    statusLabel.setText("Status: Running...");
                    generatorMode = false;
                    mazePanel.setEditable(false);
                    pause = false;
                    random.setEnabled(false);
                    algoSelection.setEnabled(false);
                    IDSpinner.setEnabled(false);
                    stopButton.setEnabled(true);
                    pauseButton.setEnabled(true);
                    nextButton.setEnabled(false);
                    runButton.setEnabled(false);
                    if (solver == null){
                        solver = setSolver(algoSelection.getSelectedItem().toString(),
                                random.isSelected(), (Integer)IDSpinner.getValue(),
                                distanceSelection);
                    }
                    runThread = new Thread(new Runnable() {

                        @Override
                        public void run() {
                            try {
                                while (!solver.isSolved() && solver.nextStep(speedSlider.
                                    getValue())){
                                    mazePanel.repaint();
                                    if (pause){
                                        runThread.interrupt();
                                    }
                                    mazePanel.getMaze().setSolution(solver.getSolution());
                                    stepInfoLabel.setText("Steps: "+solver.getSteps());
                                    solutionLabel.setText("Solution Length: "+
                                        mazePanel.getMaze().getSolution().size());
                                    maxFrontLabel.setText("Max frontier size: "+
                                        solver.getMaxFront());
                                }
                            } catch (InterruptedException p) {
                                System.out.println("Interrupted!");
                            }
                            mazePanel.getMaze().setSolution(solver.getSolution());
                            mazePanel.repaint();
                            if (!pause){
                                if (mazePanel.getMaze().getSolution() != null && 
                                        mazePanel.getMaze().getSolution().get(mazePanel.getMaze().getSolution().size() - 1)
                                        .y == mazePanel.getMaze().getGoal().x && mazePanel.getMaze().getSolution()
                                        .get(mazePanel.getMaze().getSolution().size() - 1).x == mazePanel.getMaze().getGoal().y){
                                    statusLabel.setText("Status: Solved!");
                                }
                                else{
                                    statusLabel.setText("Status: No solution found");
                                }
                                
                                pauseButton.setEnabled(false);
                                speedSlider.setEnabled(false);
                                speedSpinner.setEnabled(false);
                                stepInfoLabel.setText("Steps: "+solver.getSteps());
                                solutionLabel.setText("Solution Length: "+
                                        mazePanel.getMaze().getSolution().size());
                                maxFrontLabel.setText("Max frontier size: "+
                                        solver.getMaxFront());
                                solver = null;
                                
                            }
                            
                        }
                    });
                    runThread.start();
                    
                }
            }
        });
        runButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));
        pauseButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        pauseButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));
        pauseButton.setEnabled(false);
        stopButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));
        stopButton.setEnabled(false);
        stopButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        nextButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));
        nextButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        nextButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                statusLabel.setText("Status: Paused");
                if (algoSelection.getSelectedItem().toString().equals("Maze Generator (DFS)")){
                    if (!pause){
                        stopButton.setEnabled(true);
                        mazePanel.blacken();
                        generator = new MazeGenerator(mazePanel.getMaze().getColumns(),
                            mazePanel.getMaze().getRows(), random.isSelected(),
                            mazePanel.getMaze());
                            mazePanel.repaint();
                    }
                    pause = true;
                    try {
                        if (!generator.nextStep(0)){
                            runButton.setEnabled(false);
                            pauseButton.setEnabled(false);
                            speedSlider.setEnabled(false);
                            speedSpinner.setEnabled(false);
                            random.setEnabled(false);
                            nextButton.setEnabled(false);
                            stopButton.setEnabled(true);
                        }
                    } catch (InterruptedException n) {
                        System.out.println("Interrupted");
                    }
                    random.setEnabled(false);
                    mazePanel.repaint();
                }
                else if (canSolve()){
                    mazePanel.setEditable(false);
                    random.setEnabled(false);
                    algoSelection.setEnabled(false);
                    IDSpinner.setEnabled(false);
                    stopButton.setEnabled(true);
                    if (solver == null){
                        solver = setSolver(algoSelection.getSelectedItem().toString(),
                                random.isSelected(), (Integer)IDSpinner.getValue(),
                                distanceSelection);
                        try {
                            solver.nextStep(0);
                            mazePanel.getMaze().setSolution(solver.getSolution());
                            
                        } catch (InterruptedException p) {
                            System.out.println("Interrupted!");
                        }
                    }
                    try {
                        if (!solver.isSolved()){
                            solver.nextStep(0);
                            mazePanel.getMaze().setSolution(solver.getSolution());
                            stepInfoLabel.setText("Steps: "+solver.getSteps());
                            solutionLabel.setText("Solution Length: "+
                                        mazePanel.getMaze().getSolution().size());
                            maxFrontLabel.setText("Max frontier size: "+
                                        solver.getMaxFront());
                        }
                         else {
                            if (mazePanel.getMaze().getSolution() != null && 
                                        mazePanel.getMaze().getSolution().get(mazePanel.getMaze().getSolution().size() - 1)
                                        .y == mazePanel.getMaze().getGoal().x && mazePanel.getMaze().getSolution()
                                        .get(mazePanel.getMaze().getSolution().size() - 1).x == mazePanel.getMaze().getGoal().y){
                                    statusLabel.setText("Status: Solved!");
                            }
                            else{
                                statusLabel.setText("Status: No solution found");
                            }
                            nextButton.setEnabled(false);
                            runButton.setEnabled(false);
                            speedSlider.setEnabled(false);
                            speedSpinner.setEnabled(false);
                            stepInfoLabel.setText("Steps: "+solver.getSteps());
                            solutionLabel.setText("Solution Length: "+
                                        mazePanel.getMaze().getSolution().size());
                            maxFrontLabel.setText("Max frontier size: "+
                                        solver.getMaxFront());
                        }
                    mainFrame.repaint();
                    } catch (InterruptedException po){
                        System.out.println("interrupted!");
                    }
                }
            }
                
        });
        
        algoSelection.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                if (algoSelection.getSelectedItem().toString().equals("ID")){
                    IDLabel.setVisible(true);
                    IDSpinner.setVisible(true);
                }
                else{
                    IDLabel.setVisible(false);
                    IDSpinner.setVisible(false);
                }
                if (algoSelection.getSelectedItem().toString().equals("Best-First")
                        ||algoSelection.getSelectedItem().toString().equals("Hill Climbing")
                        ||algoSelection.getSelectedItem().toString().equals("A*")){
                            randomPanel.setVisible(false);
                            distanceSelectionPanel.setVisible(true);
                }
                else{
                    randomPanel.setVisible(true);
                    distanceSelectionPanel.setVisible(false);
                }
                if (algoSelection.getSelectedItem().toString().equals("Maze Generator (DFS)")){
                    random.setText("Classic maze");
                    arrowBox.setVisible(false);
                    random.setSelected(false);
                    
                    mazePanel.repaint();
                }
                else{
                    random.setText("Choose next cells randomly");
                    random.setSelected(false);
                    arrowBox.setVisible(true);
                    mazePanel.repaint();
                }
                pause = false;
                runButton.setEnabled(true);
                IDSpinner.setEnabled(true);
                stopButton.setEnabled(false);
                pauseButton.setEnabled(false);
                nextButton.setEnabled(true);
                random.setEnabled(true);
                speedSlider.setEnabled(true);
                speedSpinner.setEnabled(true);
            }
        });
        
        JPanel editPanel = new JPanel(new GridLayout(1, 2));
        JLabel startLabel = new JLabel(startIcon);
        JLabel goalLabel = new JLabel(goalIcon);
        editPanel.add(startLabel);
        editPanel.add(goalLabel);
        editPanel.setAlignmentX(JComponent.CENTER_ALIGNMENT);
        editPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        editPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(
        Color.BLACK), "Set Start&Goal"));
        
        startLabel.setToolTipText("Start");
        startLabel.setText("S");
        startLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e){
                startLabel.setTransferHandler(new TransferHandler("text"));
                goalLabel.setTransferHandler(new TransferHandler(null));
                JComponent c = (JComponent)e.getSource();
                TransferHandler handler = c.getTransferHandler();
                handler.setDragImage(startIcon.getImage());
                handler.exportAsDrag(c, e, TransferHandler.COPY);
            }
            
            @Override
            public void mouseEntered(MouseEvent e){
                
            }
            
            
        });
           
        goalLabel.setToolTipText("Goal");
        goalLabel.setText("G");
        goalLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e){
                goalLabel.setTransferHandler(new TransferHandler("text"));
                startLabel.setTransferHandler(new TransferHandler(null));
                JComponent c = (JComponent)e.getSource();
                TransferHandler handler = c.getTransferHandler();
                handler.setDragImage(goalIcon.getImage());
                handler.exportAsDrag(c, e, TransferHandler.COPY);
            }
        });
        
        JPanel runPanel = new JPanel();
        runPanel.setLayout(new BoxLayout(runPanel, BoxLayout.Y_AXIS));
        
        runPanel.add(algoLabel);
        runPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        runPanel.add(algoSelection);
        runPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        runPanel.add(IDLabel);
        runPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        runPanel.add(IDSpinner);
        runPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        runPanel.add(randomPanel);
        runPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        runPanel.add(distanceSelectionPanel);
        runPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        runPanel.add(runButton);
        runPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        runPanel.add(pauseButton);
        runPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        runPanel.add(stopButton);
        runPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        runPanel.add(nextButton);
        runPanel.add(Box.createRigidArea(new Dimension(0, 30)));
        runPanel.add(speedPanel);
        runPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        runPanel.add(speedSlider);
        runPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        runPanel.add(gridBox);
        runPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        runPanel.add(arrowBox);
        runPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.BLACK),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        runPanel.setBorder(BorderFactory.createTitledBorder(runPanel.getBorder(),
                "Run Panel"));
        
        
        commandPanel.add(Box.createVerticalGlue());
        commandPanel.add(runPanel);
        commandPanel.add(infoPanel);
        commandPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        commandPanel.add(editPanel);
        commandPanel.add(Box.createVerticalGlue());
        commandPanel.setBorder(new EmptyBorder(5, 15, 5, 15));
       
       fileMenu.addMouseListener(new MouseListener() {

            @Override
            public void mouseClicked(MouseEvent e) {
                if(!generatorMode){
                    stopButton.doClick();
                }
            }

            @Override
            public void mousePressed(MouseEvent e) {
                
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                
            }

            @Override
            public void mouseExited(MouseEvent e) {
                
            }
        });
       
       newMaze.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                stopButton.doClick();
                newMaze();
            }
        });
       
       openMaze.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                stopButton.doClick();
                openMaze();
            }
        });
       

        mazePanel.setBorder(new EmptyBorder(10, 10, 10, 20));
        mainFrame.setJMenuBar(menuBar);
        mainFrame.add(mainPanel, BorderLayout.CENTER);
        mainFrame.add(commandPanel, BorderLayout.EAST);
        
        mainFrame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e){
                savePrompt(mainFrame);
                if (runThread != null){
                    runThread.interrupt();
                }
                mainFrame.dispose();
            }
        });

        
        

        mainFrame.setMinimumSize(new Dimension(800, 650));
        mainFrame.setPreferredSize(new Dimension(800, 650));
        mainFrame.pack();
        mainFrame.setLocation(screenSize.width/2 - (mainFrame.getWidth())/2,
                screenSize.height/2 - (mainFrame.getHeight()/2));
        mainFrame.setVisible(true);
    }
    
    
    /**
     * Saves maze to a specific location
     */
    private void saveAs (){
        JFileChooser chooser =  new JFileChooser();
        chooser.setDialogTitle("Save As");
        chooser.setAcceptAllFileFilterUsed(false);
        chooser.addChoosableFileFilter(new FileNameExtensionFilter("Text files (.txt)", "txt"));
        int selection = chooser.showSaveDialog(mainFrame);
        if (selection == JFileChooser.APPROVE_OPTION){
            String filename = chooser.getSelectedFile().getPath();
            if (chooser.getSelectedFile().exists()){
                String[] options = {"Yes", "No"};
                int n = JOptionPane.showOptionDialog(chooser, "This file already"
                        + "exists. Overwrite?",
                        "Confirm Overwrite", JOptionPane.YES_NO_OPTION,
                        JOptionPane.WARNING_MESSAGE, null, options, options[1]);
                if (n == 0){
                    mazePanel.getMaze().saveMaze(filename);
                    directory = filename;
                    saved = true;
                }
                
            }
            else{
                mazePanel.getMaze().saveMaze(filename + ".txt");
                directory = filename + ".txt";
                saved = true;
            }
        }
        
        
    }
    
 
    private void save (){
        if (directory != null){
            File mazeFile = new File(directory);
            if (mazeFile.exists()){
                mazePanel.getMaze().saveMaze(directory);
                saved = true;
            }
            else{
                saveAs();
            }
            
        }
        else{
            saveAs();
        }
    }
    
    
    private void savePrompt (JFrame parent){
        if (!saved){
            int n = JOptionPane.showConfirmDialog(parent, "This maze has"
                    + " been modified. Save changes?", "Select an option",
            JOptionPane.YES_NO_OPTION);
            if (n == 0){
                save();
            }
        }
        saved = true;
    }
    
    /**
     * Creates GUI to build a new maze
     */
    private void newMaze(){
        if (runThread != null){
            runThread.interrupt();
        }
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int maxSize;
        if (screenSize.height > screenSize.width){
            maxSize = screenSize.height/2;
        }
        else{
            maxSize = screenSize.width/2;
        }
        JDialog newDialog = new JDialog(mainFrame, "Build New Maze", true);
        newDialog.setResizable(false);
        JPanel newPanel = new JPanel();
        newPanel.setMaximumSize(new Dimension(50, 50));
        newPanel.setLayout(new BoxLayout(newPanel, BoxLayout.Y_AXIS));
        newPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        GridLayout gridLayout = new GridLayout(1, 2, 5, 5);
        
        JSpinner rowSpinner = new JSpinner(new SpinnerNumberModel(mazePanel.getMaze().getRows(),
                2, maxSize, 1));
        JSpinner columnSpinner = new JSpinner(new SpinnerNumberModel(mazePanel.getMaze().getColumns(),
                2, maxSize, 1));
        
        JCheckBox keepOld = new JCheckBox("Keep previous maze");
        keepOld.setAlignmentX(Component.CENTER_ALIGNMENT);
       
        JPanel rowPanel= new JPanel(gridLayout);
        rowPanel.add(new JLabel("Rows: "));
        rowPanel.add(rowSpinner);
        
        JPanel columnPanel = new JPanel(gridLayout);
        columnPanel.add(new JLabel("Columns: "));
        columnPanel.add(columnSpinner);
        

        JButton buildButton = new JButton("Build");
        
        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                newDialog.dispose();
            }
        });
        JPanel buttonPanel = new JPanel(gridLayout);
        buttonPanel.add(buildButton);
        buttonPanel.add(cancelButton);
        
        MazePanel newMazePanel = new MazePanel(new Maze(mazePanel.getMaze().getRows(),
                mazePanel.getMaze().getColumns()));
        newMazePanel.setMaximumSize(new Dimension(500, 500));
        newMazePanel.setEditable(false);
        newMazePanel.setMoveable(true);
        newMazePanel.setDrawGrid(mazePanel.getDrawGrid());
        newMazePanel.setOriginalMaze(mazePanel.getMaze());
        newMazePanel.getMaze().copyMazeObstacles(mazePanel.getMaze(), 0, 0);
        JPanel borderPanel = new JPanel(new GridLayout(0, 1));
        borderPanel.setBorder(new EmptyBorder(15, 10, 10, 10));
        borderPanel.add(newMazePanel);
        
        JCheckBox gridBox = new JCheckBox("Draw grid");
        gridBox.setAlignmentX(JComponent.CENTER_ALIGNMENT);
        gridBox.setSelected(mazePanel.getDrawGrid());
        gridBox.setVisible(false);
        
        gridBox.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                if (gridBox.isSelected()){
                    newMazePanel.setDrawGrid(true);
                }
                else{
                    newMazePanel.setDrawGrid(false);
                }
            }
        });
        
        keepOld.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                if (keepOld.isSelected()){
                    borderPanel.setVisible(true);
                    gridBox.setVisible(true);
                    newMazePanel.invalidate();
                    newMazePanel.repaint();
                    newDialog.setSize(new Dimension(500, 400));
                    newDialog.repaint();
                }
                else{
                    borderPanel.setVisible(false);
                    gridBox.setVisible(false);
                    newDialog.pack();
                }
            }
        });
        
        rowSpinner.addChangeListener(new ChangeListener() {

            @Override
            public void stateChanged(ChangeEvent e) {
                int spinnerValue = (Integer)rowSpinner.getValue();
                int currentRows = newMazePanel.getMaze().getRows();
                if (spinnerValue> currentRows){
                    for (int j = currentRows;j< spinnerValue;j++){
                        newMazePanel.getMaze().addRow(newMazePanel.getOriginalMaze());
                    }
                    newMazePanel.invalidate();
                    newMazePanel.repaint();
                } else if (spinnerValue< currentRows){
                    for (int j = spinnerValue;j< currentRows;j++){
                        newMazePanel.getMaze().removeRow();
                    }
                    newMazePanel.invalidate();
                    newMazePanel.repaint();
                }
            }
        });
        
        columnSpinner.addChangeListener(new ChangeListener() {

            @Override
            public void stateChanged(ChangeEvent e) {
                int spinnerValue = (Integer)columnSpinner.getValue();
                int currentColumns = newMazePanel.getMaze().getColumns();
                if (spinnerValue> currentColumns){
                    for (int j = currentColumns;j< spinnerValue;j++){
                        newMazePanel.getMaze().addColumn(newMazePanel.getOriginalMaze());                        
                    }
                    newMazePanel.invalidate();
                    newMazePanel.repaint();
                } else if (spinnerValue< currentColumns){
                    for (int j = spinnerValue;j< currentColumns;j++){
                        newMazePanel.getMaze().removeColumn();
                    }
                    newMazePanel.invalidate();
                    newMazePanel.repaint();
                }
            }
        });
        buildButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                if (keepOld.isSelected()){
                    maze = newMazePanel.getMaze();
                }
                else{
                    maze = new Maze((Integer)rowSpinner.getValue(), (Integer)columnSpinner.getValue());
                }
                mazePanel.setMaze(maze);
                if (maze.getStart() != null && (maze.getStart().x< 0 || maze.getStart().y <0 ||
                        maze.getStart().x>= maze.getRows() || maze.getStart().y>= maze.getColumns())){
                    maze.setStart(null);
                }
                if (maze.getGoal() != null && (maze.getGoal().x<0 || maze.getGoal().y<0 ||
                        maze.getGoal().x>=maze.getRows() || maze.getGoal().y>= maze.getColumns())){
                    maze.setGoal(null);
                }
                solver = null;
                pause = false;
                directory = null;
                saved = true;
                mainFrame.setPreferredSize(mazePanel.getPreferredSize());
                if (!(mainFrame.getExtendedState() == JFrame.MAXIMIZED_BOTH))
                {
                    mainFrame.pack();
                    mainFrame.setLocation(screenSize.width/2 - (mainFrame.getWidth())/2,
                        screenSize.height/2 - (mainFrame.getHeight()/2));
                    if((Integer)rowSpinner.getValue()>64||(Integer)columnSpinner.getValue()>64){
                        mainFrame.setExtendedState(JFrame.MAXIMIZED_BOTH);
                    }
                }
                mainFrame.repaint();
                newDialog.dispose();
            }
        });
        borderPanel.setVisible(false);
        
        
        newPanel.add(Box.createVerticalGlue());
        newPanel.add(rowPanel);
        newPanel.add(columnPanel);
        newPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        newPanel.add(keepOld);
        newPanel.add(gridBox);
        newPanel.add(borderPanel);
        newPanel.add(buttonPanel);
        newPanel.add(Box.createVerticalGlue());
        
        
        newDialog.add(newPanel);
        newDialog.setMaximumSize(new Dimension(500, 500));
        newDialog.setLocationRelativeTo(mainFrame);
        newDialog.pack();
        
        newDialog.setVisible(true);
    }
    
   
    private boolean openMaze(){
        boolean flag = false;
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Open");
        chooser.setAcceptAllFileFilterUsed(false);
        chooser.addChoosableFileFilter(new FileNameExtensionFilter("Text files (.txt)",
                "txt"));
        int selection = chooser.showOpenDialog(mainFrame);
        if (selection == JFileChooser.APPROVE_OPTION){
            String filename = chooser.getSelectedFile().getPath();
            savePrompt(mainFrame);
            maze = new Maze(filename);
            mazePanel.setMaze(maze);
            solver = null;
            mainFrame.setPreferredSize(mazePanel.getPreferredSize());
            if (!(mainFrame.getExtendedState() == JFrame.MAXIMIZED_BOTH))
                {
                    mainFrame.pack();
                    mainFrame.setLocation(screenSize.width/2 - (mainFrame.getWidth())/2,
                        screenSize.height/2 - (mainFrame.getHeight()/2));
                }
            mainFrame.repaint();
            directory = filename;
            saved = true;
            pause = false;
            flag = true;
        }
        return flag;
    }
    
    
    private void updateCurrentSpeedLabel(JLabel speedLabel, int newValue){
        speedLabel.setText("Step delay(msec): "+Integer.toString(newValue));
    }
    
   
    private MazeSearch setSolver (String algorithm, boolean random, int depth,
            JComboBox<String> distanceFunction){
        boolean manhattan;
        if (distanceFunction.getSelectedItem().toString().equals("Euclidean")){
            manhattan = false;
        }
        else{
            manhattan = true;
        }
        if (algorithm.equals("DFS")){
            solver = new MazeSolverDBFS(mazePanel.getMazeData(), random, true,
                        mazePanel.getMaze());
        }
        if (algorithm.equals("BFS")){
            solver = new MazeSolverDBFS(mazePanel.getMazeData(), random, false,
                        mazePanel.getMaze());
        }
        if (algorithm.equals("ID")){
            solver = new MazeSolverID(mazePanel.getMazeData(), random, depth,
                    mazePanel.getMaze());
        }
        if (algorithm.equals("Best-First")){
            solver = new MazeSolverBF(mazePanel.getMazeData(), false,manhattan, mazePanel.getMaze());
        }
        if (algorithm.equals("Hill Climbing")){
            solver = new MazeSolverHC(mazePanel.getMazeData(),manhattan, mazePanel.getMaze());
        }
        if (algorithm.equals("A*")){
            solver = new MazeSolverBF(mazePanel.getMazeData(), true,manhattan, mazePanel.getMaze());
        }
        return solver;
        
    }
    
   
    private boolean canSolve(){
        if (mazePanel.getMaze().getStart() == null){
            JOptionPane.showMessageDialog(mainFrame, "Set up a starting point to"
                    + " solve this maze!");
            return false;
        }
        if (mazePanel.getMaze().getGoal() == null){
            JOptionPane.showMessageDialog(mainFrame, "Set up a goal to"
                    + " solve this maze!");
            return false;
        }
        return true;
    }
    
    /**
     * Clears this maze, 
     */
    private void clearMaze(){
        for (int i = 0;i< mazePanel.getMaze().getRows();i++){
            for (int j = 0;j< mazePanel.getMaze().getColumns();j++){
                mazePanel.getMaze().getMazeLogic()[i][j].setIsFront(false);
                mazePanel.getMaze().getMazeLogic()[i][j].isVisited = false;
                mazePanel.getMaze().setSolution(new ArrayList<>());
                mazePanel.getMaze().setCurrent(null);
                mainFrame.repaint();
                solver = null;
                pause = false;
            }
        }
            
    }
    
    

}
