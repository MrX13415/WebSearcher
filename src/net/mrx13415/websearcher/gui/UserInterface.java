package net.mrx13415.websearcher.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.UIManager;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import net.mrx13415.websearcher.Start;

public class UserInterface extends JFrame{

	/**
	 * 
	 */
	private static final long serialVersionUID = -6958172070310438694L;
	
	private JLabel percentage;
	private JLabel time;
	private JLabel state;
	private JTextField currentIP;
	private JTextField endIP;
	private JTextField port;
	private JTextField timeout;
	private JTextField threadCount;
	private JProgressBar threadState;
	private JProgressBar bar;
	private JButton btnOK;
	private JButton btnCancel;
	private JTextPane resultBox;
	private JScrollPane resultScrollPane;
	
	public UserInterface(ActionListener acl) {
		
		try {
			System.out.print("Load Look&Feel ...\t\t\t");
			
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			
			System.out.println("OK");
		} catch (Exception e) {
			System.out.println("ERROR");
		}
		
		
		state = new JLabel(" ");
		
		percentage = new JLabel(" ");
		
		time = new JLabel(" ");

		currentIP = new JTextField();
		currentIP.setPreferredSize(new Dimension(250, 20));
		
		endIP = new JTextField();
		endIP.setPreferredSize(new Dimension(250, 20));
		
		port = new JTextField("80");
		port.setPreferredSize(new Dimension(150, 20));
		
		timeout = new JTextField("3000");
		timeout.setPreferredSize(new Dimension(30, 20));
		
		threadCount = new JTextField("1");
		threadCount.setPreferredSize(new Dimension(50, 20));
		
		threadState = new JProgressBar();
		
		bar = new JProgressBar();
		
		btnOK = new JButton("Start");
		btnOK.addActionListener(acl);
		btnOK.setPreferredSize(new Dimension(80, btnOK.getPreferredSize().height));
		
		btnCancel = new JButton("Cancel");
		btnCancel.addActionListener(acl);
		btnCancel.setEnabled(false);
		btnCancel.setPreferredSize(new Dimension(80, btnCancel.getPreferredSize().height));
		
		JPanel cnt3Panel = new JPanel(new GridLayout(2, 1, 10, 10));
		cnt3Panel.add(new JLabel("Timeout (ms): "));
		cnt3Panel.add(timeout);
		
		JPanel cnt1Panel = new JPanel(new GridLayout(2, 1, 10, 10));
		cnt1Panel.add(new JLabel("Start Address (IP-Address):"));
		cnt1Panel.add(currentIP);
		
		JPanel cnt7Panel = new JPanel(new GridLayout(2, 1, 10, 10));
		cnt7Panel.add(new JLabel("End Address (IP-Address):"));
		cnt7Panel.add(endIP);
		
		JPanel cnt5Panel = new JPanel(new GridLayout(2, 1, 10, 10));
		cnt5Panel.add(new JLabel("Port(s):"));
		cnt5Panel.add(port);
		
		JPanel cnt2Panel = new JPanel(new GridLayout(2, 1, 10, 10));
		cnt2Panel.add(new JLabel("State: "));
		cnt2Panel.add(state);
		
		JPanel cnt4Panel = new JPanel(new BorderLayout(10, 10));
		cnt4Panel.add(cnt3Panel, BorderLayout.WEST);
		cnt4Panel.add(cnt1Panel, BorderLayout.CENTER);
		cnt4Panel.add(cnt7Panel, BorderLayout.EAST);
		
		JPanel cnt6Panel = new JPanel(new BorderLayout(10, 10));
		cnt6Panel.add(cnt5Panel, BorderLayout.WEST);
		cnt6Panel.add(cnt2Panel, BorderLayout.CENTER);
		
		JPanel cntPanel = new JPanel(new BorderLayout(10, 10));
		cntPanel.add(cnt4Panel, BorderLayout.WEST);
		cntPanel.add(cnt6Panel);
		
		JPanel percPanel = new JPanel(new BorderLayout(10, 10));
		percPanel.add(percentage, BorderLayout.WEST);
		percPanel.add(time, BorderLayout.EAST);
		
		JPanel thr1Panel = new JPanel(new BorderLayout(10, 10));
		thr1Panel.add(new JLabel("Thread count: "), BorderLayout.WEST);
		thr1Panel.add(threadCount, BorderLayout.CENTER);
		
		JPanel thr3Panel = new JPanel(new BorderLayout());
		thr3Panel.add(threadState, BorderLayout.CENTER);
		thr3Panel.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));
		
		JPanel thr2Panel = new JPanel(new BorderLayout(10, 10));
		thr2Panel.add(thr1Panel, BorderLayout.WEST);
		thr2Panel.add(thr3Panel, BorderLayout.CENTER);
		
		JPanel btnPanel = new JPanel(new GridLayout(1, 2, 5, 5));
		btnPanel.add(btnOK);
		btnPanel.add(btnCancel);
		
		JPanel southPanel = new JPanel(new BorderLayout(10, 10));
		southPanel.add(percPanel, BorderLayout.NORTH);
		southPanel.add(thr2Panel, BorderLayout.CENTER);
		southPanel.add(btnPanel, BorderLayout.EAST);
		
		JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
		mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		mainPanel.add(cntPanel, BorderLayout.NORTH);
		mainPanel.add(bar, BorderLayout.CENTER);
		mainPanel.add(southPanel, BorderLayout.SOUTH);
		
		resultBox = new JTextPane();
		resultBox.setEditable(false);
		resultBox.setFont(new Font(Font.MONOSPACED, resultBox.getFont().getStyle(), 11));
		
		JPanel rbp = new JPanel(new BorderLayout());
		rbp.add(resultBox);
		
		resultScrollPane = new JScrollPane(rbp, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		resultScrollPane.setPreferredSize(new Dimension(resultScrollPane.getPreferredSize().width,
				resultScrollPane.getPreferredSize().height * 10));
		
		JPanel rPanel = new JPanel(new BorderLayout(10, 10));
		rPanel.add(new JSeparator(), BorderLayout.NORTH);
		rPanel.add(resultScrollPane);
		rPanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
		
		this.getContentPane().add(mainPanel, BorderLayout.NORTH);
		this.getContentPane().add(rPanel, BorderLayout.CENTER);
		
		this.setTitle(Start.ApplicationVersionString);
		this.setDefaultCloseOperation(EXIT_ON_CLOSE);
		this.pack();
		
		this.setPreferredSize(new Dimension(1000, this.getPreferredSize().height));
		this.setLocationRelativeTo(null);
		this.pack();
		
		this.setVisible(true);
	}
	
	public JLabel getTime() {
		return time;
	}

	public JLabel getPercentage() {
		return percentage;
	}

	public JLabel getStateLabel() {
		return state;
	}

	public JTextField getCurrentIP() {
		return currentIP;
	}

	public JTextField getEndIP() {
		return endIP;
	}

	public JTextField getPort() {
		return port;
	}

	public JTextField getTimeout() {
		return timeout;
	}

	public JProgressBar getBar() {
		return bar;
	}

	public JButton getBtnOK() {
		return btnOK;
	}

	public JButton getBtnCancel() {
		return btnCancel;
	}

	public JProgressBar getThreadState() {
		return threadState;
	}

	public JTextField getThreadCount() {
		return threadCount;
	}

	public JTextPane getResultBox() {
		return resultBox;
	}
	
	public void appendResultLine(String text, Color color){
		Style style = resultBox.addStyle(color.toString(), null);
        StyleConstants.setForeground(style, color);
        StyledDocument doc = resultBox.getStyledDocument();
        
		try {
			text += "\n";
			resultBox.getStyledDocument().insertString(doc.getLength(), text, style);
			resultBox.setCaretPosition(doc.getLength());
		} catch (BadLocationException e) {}
	}
}
