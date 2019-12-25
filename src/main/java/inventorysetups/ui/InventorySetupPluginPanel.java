/*
 * Copyright (c) 2019, Dillon <https://github.com/dillydill123>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package inventorysetups.ui;

import net.runelite.api.InventoryID;
import net.runelite.api.ItemContainer;
import net.runelite.api.ItemID;
import net.runelite.client.game.ItemManager;
import inventorysetups.InventorySetup;
import inventorysetups.InventorySetupItem;
import inventorysetups.InventorySetupPlugin;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.PluginPanel;
import net.runelite.client.ui.components.PluginErrorPanel;
import net.runelite.client.util.AsyncBufferedImage;
import net.runelite.client.util.ImageUtil;
import net.runelite.client.util.SwingUtil;

import javax.imageio.ImageIO;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import static jogamp.common.os.elf.SectionArmAttributes.Tag.File;

public class InventorySetupPluginPanel extends PluginPanel
{

	private static ImageIcon ADD_ICON;
	private static ImageIcon ADD_HOVER_ICON;
	private static ImageIcon BACK_ICON;
	private static ImageIcon BACK_HOVER_ICON;

	private final JPanel noSetupsPanel;
	private final JPanel invEqPanel;
	private final JPanel overviewPanel;
	private final JScrollPane contentWrapperPane;

	private final JLabel addMarker;
	private final JLabel backMarker;

	private final InventorySetupContainerPanel invPanel;
	private final InventorySetupContainerPanel eqpPanel;

	private InventorySetup currentSelectedSetup;

	private final InventorySetupPlugin plugin;

	static
	{
		final BufferedImage addIcon = ImageUtil.getResourceStreamFromClass(InventorySetupPlugin.class, "/add_icon.png");
		ADD_ICON = new ImageIcon(addIcon);
		ADD_HOVER_ICON = new ImageIcon(ImageUtil.alphaOffset(addIcon, 0.53f));

		final BufferedImage backIcon = ImageUtil.getResourceStreamFromClass(InventorySetupPlugin.class, "/back_arrow_icon.png");
		BACK_ICON = new ImageIcon(ImageUtil.flipImage(backIcon, true, false));
		BACK_HOVER_ICON = new ImageIcon(ImageUtil.alphaOffset(ImageUtil.flipImage(backIcon, true, false), 0.53f));

	}

	public InventorySetupPluginPanel(final InventorySetupPlugin plugin, final ItemManager itemManager)
	{
		super(false);
		this.currentSelectedSetup = null;
		this.plugin = plugin;
		this.invPanel = new InventorySetupInventoryPanel(itemManager, plugin);
		this.eqpPanel = new InventorySetupEquipmentPanel(itemManager, plugin);
		this.noSetupsPanel = new JPanel();
		this.invEqPanel = new JPanel();
		this.overviewPanel = new JPanel();


		AsyncBufferedImage rod = itemManager.getImage(ItemID.RING_OF_DUELING8);
		rod.onLoaded(new Runnable() {
			@Override
			public void run() {
				File outputFile = new File("C:/Users/Dillon/Desktop/test.png");
				try {
					BufferedImage blackAndWhite = new BufferedImage(rod.getWidth(), rod.getHeight(), BufferedImage.TYPE_BYTE_BINARY);
					Graphics2D graphics = blackAndWhite.createGraphics();
					graphics.drawImage(rod, 0, 0, null);
					ImageIO.write(blackAndWhite, "png", outputFile);
				}
				catch(IOException e) {

				}
			}
		});

		// setup the title
		final JLabel title = new JLabel();
		title.setText("Inventory Setups");
		title.setForeground(Color.WHITE);

		// setup the add marker (+ sign in the top right)
		addMarker = new JLabel(ADD_ICON);
		addMarker.setToolTipText("Add a new inventory setup");
		addMarker.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mouseClicked(MouseEvent e)
			{
				plugin.addInventorySetup();
			}

			@Override
			public void mouseEntered(MouseEvent e)
			{
				addMarker.setIcon(ADD_HOVER_ICON);
			}

			@Override
			public void mouseExited(MouseEvent e)
			{
				addMarker.setIcon(ADD_ICON);
			}
		});

		// back to overview marker TODO: should be same height as add icon
		backMarker = new JLabel(BACK_ICON);
		backMarker.setToolTipText("Return to setups");
		backMarker.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mouseClicked(MouseEvent e)
			{
				noSetupsPanel.setVisible(false);
				invEqPanel.setVisible(false);
				backMarker.setVisible(false);
				overviewPanel.setVisible(true);
				addMarker.setVisible(true);

			}

			@Override
			public void mouseEntered(MouseEvent e)
			{
				backMarker.setIcon(BACK_HOVER_ICON);
			}

			@Override
			public void mouseExited(MouseEvent e)
			{
				backMarker.setIcon(BACK_ICON);
			}
		});

		// the panel on the top right that holds the add and delete buttons
		final JPanel markersPanel = new JPanel();
		markersPanel.setLayout(new FlowLayout());
		markersPanel.add(addMarker);
		markersPanel.add(backMarker);
		backMarker.setVisible(false);
		addMarker.setVisible(true);

		// the top panel that has the title and the buttons
		final JPanel titleAndMarkersPanel = new JPanel();
		titleAndMarkersPanel.setLayout(new BorderLayout());
		titleAndMarkersPanel.add(title, BorderLayout.WEST);
		titleAndMarkersPanel.add(markersPanel, BorderLayout.EAST);

		// the panel that stays at the top and doesn't scroll
		// contains the title and buttons
		final JPanel northAnchoredPanel = new JPanel();
		northAnchoredPanel.setLayout(new BoxLayout(northAnchoredPanel, BoxLayout.Y_AXIS));
		northAnchoredPanel.setBorder(new EmptyBorder(0, 0, 10, 0));
		northAnchoredPanel.add(titleAndMarkersPanel);
		northAnchoredPanel.add(Box.createRigidArea(new Dimension(0, 10)));

		// the panel that holds the inventory and equipment panels
		final BoxLayout invEqLayout = new BoxLayout(invEqPanel, BoxLayout.Y_AXIS);
		invEqPanel.setLayout(invEqLayout);
		invEqPanel.add(invPanel);
		invEqPanel.add(Box.createRigidArea(new Dimension(0, 10)));
		invEqPanel.add(eqpPanel);

		// setup the error panel. It's wrapped around a normal panel
		// so it doesn't stretch to fill the parent panel
		final PluginErrorPanel errorPanel = new PluginErrorPanel();
		errorPanel.setContent("Inventory Setups", "Create an inventory setup.");
		noSetupsPanel.add(errorPanel);

		// the panel that holds the inventory panels, error panel, and the overview panel
		final JPanel contentPanel = new JPanel();
		final BoxLayout contentLayout = new BoxLayout(contentPanel, BoxLayout.Y_AXIS);
		contentPanel.setLayout(contentLayout);
		contentPanel.add(invEqPanel);
		contentPanel.add(noSetupsPanel);
		contentPanel.add(overviewPanel);

		// wrapper for the main content panel to keep it from stretching
		final JPanel contentWrapper = new JPanel(new BorderLayout());
		contentWrapper.add(Box.createGlue(), BorderLayout.CENTER);
		contentWrapper.add(contentPanel, BorderLayout.NORTH);
		this.contentWrapperPane = new JScrollPane(contentWrapper);
		this.contentWrapperPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

		setLayout(new BorderLayout());
		setBorder(new EmptyBorder(10, 10, 10, 10));
		add(northAnchoredPanel, BorderLayout.NORTH);
		add(this.contentWrapperPane, BorderLayout.CENTER);

	}

	public void init()
	{
		overviewPanel.setLayout(new GridBagLayout());
		overviewPanel.setBackground(ColorScheme.DARK_GRAY_COLOR);

		GridBagConstraints constraints = new GridBagConstraints();
		constraints.fill = GridBagConstraints.HORIZONTAL;
		constraints.weightx = 1;
		constraints.gridx = 0;
		constraints.gridy = 0;

		for (final InventorySetup setup : plugin.getInventorySetups())
		{
			InventorySetupPanel newPanel = new InventorySetupPanel(plugin, this, setup);
			overviewPanel.add(newPanel, constraints);
			constraints.gridy++;

			overviewPanel.add(Box.createRigidArea(new Dimension(0, 10)), constraints);
			constraints.gridy++;
		}

		invEqPanel.setVisible(false);

		noSetupsPanel.setVisible(plugin.getInventorySetups().isEmpty());
		overviewPanel.setVisible(!plugin.getInventorySetups().isEmpty());

	}

	public void rebuild()
	{
		overviewPanel.removeAll();
		init();
		revalidate();
		repaint();
	}

	public void setCurrentInventorySetup(final InventorySetup inventorySetup)
	{
		currentSelectedSetup = inventorySetup;
		invPanel.setSlots(inventorySetup);
		eqpPanel.setSlots(inventorySetup);

		if (currentSelectedSetup.isHighlightDifference())
		{
			final ArrayList<InventorySetupItem> normInv = plugin.getNormalizedContainer(InventoryID.INVENTORY);
			final ArrayList<InventorySetupItem> normEqp = plugin.getNormalizedContainer(InventoryID.EQUIPMENT);

			invPanel.highlightDifferences(normInv, inventorySetup);
			eqpPanel.highlightDifferences(normEqp, inventorySetup);
		}
		else
		{
			invPanel.resetSlotColors();
			eqpPanel.resetSlotColors();
		}

		addMarker.setVisible(false);
		backMarker.setVisible(true);

		invEqPanel.setVisible(true);
		noSetupsPanel.setVisible(false);
		overviewPanel.setVisible(false);

		// reset scrollbar back to top
		this.contentWrapperPane.getVerticalScrollBar().setValue(0);

		validate();
		repaint();

	}

	public void highlightDifferences(final InventoryID type)
	{
		if (!invEqPanel.isVisible() || !currentSelectedSetup.isHighlightDifference())
		{
			return;
		}

		final ArrayList<InventorySetupItem> container = plugin.getNormalizedContainer(type);
		switch (type)
		{
			case INVENTORY:
				invPanel.highlightDifferences(container, currentSelectedSetup);
				break;

			case EQUIPMENT:
				eqpPanel.highlightDifferences(container, currentSelectedSetup);
				break;
		}
	}

}