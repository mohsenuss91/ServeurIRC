package com.mroch.view;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.FlowLayout;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.SwingConstants;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Dimension;

public class AddSalonWindow extends JFrame {


	private final JPanel contentPanel = new JPanel();
	private JTextField txtSalonName;
	private JButton AjouterSalonButton;
	private JButton cancelButton;
	
	/**
	 * Launch the application.
	 */
	
	public AddSalonWindow() {
		setTitle("Ajouter Salon");
		setResizable(false);
		setBounds(100, 100, 660, 246);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(null);
		
		JLabel lblSalonName = new JLabel("Nom du salon : ");
		lblSalonName.setBounds(71, 67, 150, 26);
		contentPanel.add(lblSalonName);
		
		txtSalonName = new JTextField();
		txtSalonName.setBounds(242, 64, 368, 32);
		contentPanel.add(txtSalonName);
		txtSalonName.setColumns(10);
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				AjouterSalonButton = new JButton("Ajouter");
				AjouterSalonButton.setActionCommand("OK");
				buttonPane.add(AjouterSalonButton);
				getRootPane().setDefaultButton(AjouterSalonButton);
			}
			{
				cancelButton = new JButton("Cancel");
				cancelButton.setActionCommand("Cancel");
				buttonPane.add(cancelButton);
			}
		}
	}

	/**
	 * R�cuperer le bouton d'ajout de salon
	 * @return JButton
	 */
	public JButton getAjouterSalonButton() {
		return AjouterSalonButton;
	}
	
	/**
	 * R�cuperer le bouton de cancel
	 * @return JButton
	 */
	public JButton getCancelButton() {
		return cancelButton;
	}
	
	/**
	 * R�cup�rer le TextField Nom du salon
	 * @return JTextField
	 */
	public JTextField getTxtSalonName() {
		return txtSalonName;
	}
}
