package de.keksuccino.konkrete.gui.content;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import de.keksuccino.konkrete.input.MouseInput;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

public class HorizontalSwitcher extends DrawableHelper {
	
	private int width;
	private AdvancedImageButton prev;
	private AdvancedImageButton next;
	private int selected = 0;
	private List<String> values = new ArrayList<String>();
	private Color valuecolor = Color.WHITE;
	private Color valuebackcolor = Color.LIGHT_GRAY;
	
	public HorizontalSwitcher(int displayWidth, boolean ignoreBlockedInput, String... values) {
		this.prev = new AdvancedImageButton(0, 0, 20, 20, new Identifier("keksuccino", "arrow_left.png"), true, (press) -> {
			int i = this.selected - 1;
			if (i >= 0) {
				this.selected = i;
			}
		});
		this.prev.ignoreBlockedInput = ignoreBlockedInput;
		
		this.next = new AdvancedImageButton(0, 0, 20, 20, new Identifier("keksuccino", "arrow_right.png"), true, (press) -> {
			int i = this.selected + 1;
			if (i <= this.values.size()-1) {
				this.selected = i;
			}
		});
		this.next.ignoreBlockedInput = ignoreBlockedInput;
		
		if (values != null) {
			this.values.addAll(Arrays.asList(values));
		}
		
		this.width = displayWidth;
	}
	
	@SuppressWarnings("resource")
	public void render(MatrixStack matrix, int x, int y) {
		int mouseX = MouseInput.getMouseX();
		int mouseY = MouseInput.getMouseY();
		float partial = MinecraftClient.getInstance().getTickDelta();
		String sel = "-------";
		if (!this.values.isEmpty()) {
			sel = this.values.get(this.selected);
		}
		
		this.prev.setX(x);
		this.prev.setY(y);
		this.prev.render(matrix, mouseX, mouseY, partial);
		
		//Value background
		fill(matrix, x + 25, y, x + 25 + this.width, y + 20, this.valuebackcolor.getRGB());
		
		//Selected value
		drawCenteredText(matrix, MinecraftClient.getInstance().textRenderer, sel, x + 25 + (this.width/2), y + 5, this.valuecolor.getRGB());
		
		this.next.setX(x + 25 + this.width + 5);;
		this.next.setY(y);
		this.next.render(matrix, mouseX, mouseY, partial);
	}
	
	public void addValue(String value) {
		if (!this.values.contains(value)) {
			this.values.add(value);
			this.selected = 0;
		}
	}
	
	public void removeValue(String value) {
		if (this.values.contains(value)) {
			this.values.remove(value);
			this.selected = 0;
		}
	}
	
	public int getTotalWidth() {
		return this.width + 50;
	}
	
	public int getHeight() {
		return 20;
	}
	
	public void setButtonColor(Color idle, Color hovered, Color idleBorder, Color hoveredBorder, int borderWidth) {
		this.next.setBackgroundColor(idle, hovered, idleBorder, hoveredBorder, borderWidth);
		this.prev.setBackgroundColor(idle, hovered, idleBorder, hoveredBorder, borderWidth);
	}
	
	public void setValueColor(Color color) {
		this.valuecolor = color;
	}
	
	public void setValueBackgroundColor(Color color) {
		this.valuebackcolor = color;
	}
	
	public String getSelectedValue() {
		if (this.values.isEmpty()) {
			return null;
		}
		return this.values.get(this.selected);
	}
	
	public void setSelectedValue(String value) {
		if (this.values.contains(value)) {
			int i = 0;
			for (String s : this.values) {
				if (s.equals(value)) {
					this.selected = i;
					return;
				}
				i++;
			}
		}
	}

}
