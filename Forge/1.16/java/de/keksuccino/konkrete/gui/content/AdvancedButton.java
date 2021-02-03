package de.keksuccino.konkrete.gui.content;

import java.awt.Color;

import javax.annotation.Nullable;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;

import de.keksuccino.konkrete.input.MouseInput;
import de.keksuccino.konkrete.resources.ExternalTextureResourceLocation;
import de.keksuccino.konkrete.sound.SoundHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.StringTextComponent;

public class AdvancedButton extends Button {

	private boolean handleClick = false;
	private static boolean leftDown = false;
	private boolean leftDownNotHovered = false;
	public boolean ignoreBlockedInput = false;
	private boolean useable = true;
	
	private Color idleColor;
	private Color hoveredColor;
	private Color idleBorderColor;
	private Color hoveredBorderColor;
	private int borderWidth = 2;
	private ResourceLocation backgroundHover;
	private ResourceLocation backgroundNormal;
	String clicksound = null;
	//TODO übernehmen
	String[] description = null;

	private IPressable press;
	
	public AdvancedButton(int x, int y, int widthIn, int heightIn, String buttonText, IPressable onPress) {
		super(x, y, widthIn, heightIn, new StringTextComponent(buttonText), onPress);
		this.press = onPress;
	}
	
	public AdvancedButton(int x, int y, int widthIn, int heightIn, String buttonText, boolean handleClick, IPressable onPress) {
		super(x, y, widthIn, heightIn, new StringTextComponent(buttonText), onPress);
		this.handleClick = handleClick;
		this.press = onPress;
	}

	@Override
	public void onPress() {
		this.press.onPress(this);
	}
	
	//renderButton
	@Override
	public void renderButton(MatrixStack matrix, int mouseX, int mouseY, float partialTicks) {
		if (this.visible) {
			Minecraft mc = Minecraft.getInstance();
			FontRenderer font = mc.fontRenderer;
			
			this.isHovered = mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width && mouseY < this.y + this.height;
			
			RenderSystem.enableBlend();
			if (this.hasColorBackground()) {
				Color border;
				if (!isHovered) {
					fill(matrix, this.x, this.y, this.x + this.width, this.y + this.height, this.idleColor.getRGB());
					border = this.idleBorderColor;
				} else {
					if (this.active) {
						fill(matrix, this.x, this.y, this.x + this.width, this.y + this.height, this.hoveredColor.getRGB());
						border = this.hoveredBorderColor;
					} else {
						fill(matrix, this.x, this.y, this.x + this.width, this.y + this.height, this.idleColor.getRGB());
						border = this.idleBorderColor;
					}
				}
				if (this.hasBorder()) {
					//top
					fill(matrix, this.x, this.y, this.x + this.width, this.y + this.borderWidth, border.getRGB());
					//bottom
					fill(matrix, this.x, this.y + this.height - this.borderWidth, this.x + this.width, this.y + this.height, border.getRGB());
					//left
					fill(matrix, this.x, this.y + this.borderWidth, this.x + this.borderWidth, this.y + this.height - this.borderWidth, border.getRGB());
					//right
					fill(matrix, this.x + this.width - this.borderWidth, this.y + this.borderWidth, this.x + this.width, this.y + this.height - this.borderWidth, border.getRGB());
				}
			} else if (this.hasCustomTextureBackground()) {
				if (this.isHovered()) {
					if (this.active) {
						mc.textureManager.bindTexture(backgroundHover);
					} else {
						mc.textureManager.bindTexture(backgroundNormal);
					}
				} else {
					mc.textureManager.bindTexture(backgroundNormal);
				}
				RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
				blit(matrix, this.x, this.y, 0.0F, 0.0F, this.width, this.height, this.width, this.height);
			} else {
				mc.getTextureManager().bindTexture(WIDGETS_LOCATION);
				RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
				int i = this.getYImage(this.isHovered());
				RenderSystem.defaultBlendFunc();
				RenderSystem.enableDepthTest();
				this.blit(matrix, this.x, this.y, 0, 46 + i * 20, this.width / 2, this.height);
				this.blit(matrix, this.x + this.width / 2, this.y, 200 - this.width / 2, 46 + i * 20, this.width / 2, this.height);
				RenderSystem.disableDepthTest();
			}

			this.renderBg(matrix, mc, mouseX, mouseY);

			AbstractGui.drawCenteredString(matrix, font, new StringTextComponent(getMessageString()), this.x + this.width / 2, this.y + (this.height - 8) / 2, getFGColor());
			
			//TODO übernehmen
			if (this.isHovered()) {
				AdvancedButtonHandler.setActiveDescriptionButton(this);
			}
		}

		if (!this.isHovered() && MouseInput.isLeftMouseDown()) {
			this.leftDownNotHovered = true;
		}
		if (!MouseInput.isLeftMouseDown()) {
			this.leftDownNotHovered = false;
		}
		
		if (this.handleClick && this.useable) {
			if (this.isHovered() && MouseInput.isLeftMouseDown() && !leftDown && !leftDownNotHovered && !this.isInputBlocked() && this.active && this.visible) {
				this.onClick(mouseX, mouseY);
				if (this.clicksound == null) {
					this.playDownSound(Minecraft.getInstance().getSoundHandler());
				} else {
					SoundHandler.resetSound(this.clicksound);
					SoundHandler.playSound(this.clicksound);
				}
				leftDown = true;
			}
			if (!MouseInput.isLeftMouseDown()) {
				leftDown = false;
			}
		}
	}

	private boolean isInputBlocked() {
		if (this.ignoreBlockedInput) {
			return false;
		}
		return MouseInput.isVanillaInputBlocked();
	}
	
	public void setBackgroundColor(@Nullable Color idle, @Nullable Color hovered, @Nullable Color idleBorder, @Nullable Color hoveredBorder, int borderWidth) {
		this.idleColor = idle;
		this.hoveredColor = hovered;
		this.hoveredBorderColor = hoveredBorder;
		this.idleBorderColor = idleBorder;
		
		if (borderWidth >= 0) {
			this.borderWidth = borderWidth;
		} else {
			borderWidth = 0;
		}
	}
	
	public void setBackgroundTexture(ResourceLocation normal, ResourceLocation hovered) {
		this.backgroundNormal = normal;
		this.backgroundHover = hovered;
	}
	
	public void setBackgroundTexture(ExternalTextureResourceLocation normal, ExternalTextureResourceLocation hovered) {
		if (!normal.isReady()) {
			normal.loadTexture();
		}
		if (!hovered.isReady()) {
			hovered.loadTexture();
		}
		this.backgroundHover = hovered.getResourceLocation();
		this.backgroundNormal = normal.getResourceLocation();
	}
	
	public boolean hasBorder() {
		return (this.hasColorBackground() && (this.idleBorderColor != null) && (this.hoveredBorderColor != null));
	}
	
	public boolean hasColorBackground() {
		return ((this.idleColor != null) && (this.hoveredColor != null));
	}
	
	public boolean hasCustomTextureBackground() {
		return ((this.backgroundHover != null) && (this.backgroundNormal != null));
	}

	@Override
	public boolean mouseClicked(double p_mouseClicked_1_, double p_mouseClicked_3_, int p_mouseClicked_5_) {
		if (!this.handleClick) {
			if (this.useable) {
				if (this.active && this.visible) {
			         if (this.isValidClickButton(p_mouseClicked_5_)) {
			            boolean flag = this.clicked(p_mouseClicked_1_, p_mouseClicked_3_);
			            if (flag) {
			               if (this.clicksound == null) {
			            	   this.playDownSound(Minecraft.getInstance().getSoundHandler());
			               } else {
			            	   SoundHandler.resetSound(this.clicksound);
			            	   SoundHandler.playSound(this.clicksound);
			               }
			               this.onClick(p_mouseClicked_1_, p_mouseClicked_3_);
			               return true;
			            }
			         }

			         return false;
			      } else {
			         return false;
			      }
			}
		}
		return false;
	}
	
	//keyPressed
	@Override
	public boolean keyPressed(int p_keyPressed_1_, int p_keyPressed_2_, int p_keyPressed_3_) {
		if (this.handleClick) {
			return false;
		}
		return super.keyPressed(p_keyPressed_1_, p_keyPressed_2_, p_keyPressed_3_);
	}
	
	public void setUseable(boolean b) {
		this.useable = b;
	}
	
	public boolean isUseable() {
		return this.useable;
	}
	
	public void setHandleClick(boolean b) {
		this.handleClick = b;
	}
	
	public String getMessageString() {
		return this.getMessage().getString();
	}
	
	public void setMessage(String msg) {
		this.setMessage(new StringTextComponent(msg));
	}
	
	public int getWidth() {
		return this.width;
	}
	
	public void setWidth(int width) {
		this.width = width;
	}
	
	public int getX() {
		return this.x;
	}
	
	public void setX(int x) {
		this.x = x;
	}
	
	public int getY() {
		return this.y;
	}
	
	public void setY(int y) {
		this.y = y;
	}
	
	public void setHovered(boolean b) {
		this.isHovered = b;
	}

	public void setPressAction(IPressable press) {
		this.press = press;
	}

	public void setClickSound(@Nullable String key) {
		this.clicksound = key;
	}
	
	//TODO übernehmen
	public void setDescription(String... desc) {
		this.description = desc;
	}
	
	//TODO übernehmen
	public String[] getDescription() {
		return this.description;
	}
	
	public static boolean isAnyButtonLeftClicked() {
		return leftDown;
	}

}