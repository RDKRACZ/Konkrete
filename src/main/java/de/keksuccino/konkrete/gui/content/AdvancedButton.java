package de.keksuccino.konkrete.gui.content;

import java.awt.Color;

import org.jetbrains.annotations.Nullable;

import com.mojang.blaze3d.systems.RenderSystem;

import de.keksuccino.konkrete.input.MouseInput;
import de.keksuccino.konkrete.rendering.RenderUtils;
import de.keksuccino.konkrete.resources.ExternalTextureResourceLocation;
import de.keksuccino.konkrete.sound.SoundHandler;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

public class AdvancedButton extends ButtonWidget {

	private boolean handleClick = false;
	private static boolean leftDown = false;
	private boolean leftDownThis = false;
	private boolean leftDownNotHovered = false;
	public boolean ignoreBlockedInput = false;
	public boolean ignoreLeftMouseDownClickBlock = false;
	public boolean enableRightclick = false;
	public float labelScale = 1.0F;
	private boolean useable = true;
	private boolean labelShadow = true;
	
	private Color idleColor;
	private Color hoveredColor;
	private Color idleBorderColor;
	private Color hoveredBorderColor;
	private float borderWidth = 2.0F;
	private Identifier backgroundHover;
	private Identifier backgroundNormal;
	String clicksound = null;
	String[] description = null;

	private PressAction press;
	
	public AdvancedButton(int x, int y, int widthIn, int heightIn, String buttonText, PressAction onPress) {
		super(x, y, widthIn, heightIn, new LiteralText(buttonText), onPress);
		this.press = onPress;
	}
	
	public AdvancedButton(int x, int y, int widthIn, int heightIn, String buttonText, boolean handleClick, PressAction onPress) {
		super(x, y, widthIn, heightIn, new LiteralText(buttonText), onPress);
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
			MinecraftClient mc = MinecraftClient.getInstance();
			
			this.hovered = mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width && mouseY < this.y + this.height;
			
			RenderSystem.enableBlend();
			if (this.hasColorBackground()) {
				Color border;
				if (!hovered) {
					fill(matrix, this.x, this.y, this.x + this.width, this.y + this.height, this.idleColor.getRGB() | MathHelper.ceil(this.alpha * 255.0F) << 24);
					border = this.idleBorderColor;
				} else {
					if (this.active) {
						fill(matrix, this.x, this.y, this.x + this.width, this.y + this.height, this.hoveredColor.getRGB() | MathHelper.ceil(this.alpha * 255.0F) << 24);
						border = this.hoveredBorderColor;
					} else {
						fill(matrix, this.x, this.y, this.x + this.width, this.y + this.height, this.idleColor.getRGB() | MathHelper.ceil(this.alpha * 255.0F) << 24);
						border = this.idleBorderColor;
					}
				}
				if (this.hasBorder()) {
					//top
					RenderUtils.fill(matrix, this.x, this.y, this.x + this.width, this.y + this.borderWidth, border.getRGB(), this.alpha);
					//bottom
					RenderUtils.fill(matrix, this.x, this.y + this.height - this.borderWidth, this.x + this.width, this.y + this.height, border.getRGB(), this.alpha);
					//left
					RenderUtils.fill(matrix, this.x, this.y + this.borderWidth, this.x + this.borderWidth, this.y + this.height - this.borderWidth, border.getRGB(), this.alpha);
					//right
					RenderUtils.fill(matrix, this.x + this.width - this.borderWidth, this.y + this.borderWidth, this.x + this.width, this.y + this.height - this.borderWidth, border.getRGB(), this.alpha);
				}
			} else if (this.hasCustomTextureBackground()) {
				//TODO neu in 1.17
				Identifier r = backgroundNormal;
				if (this.isHovered() && this.active) {
					r = backgroundHover;
				}
				RenderUtils.bindTexture(r);
				RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, this.alpha);
				drawTexture(matrix, this.x, this.y, 0.0F, 0.0F, this.width, this.height, this.width, this.height);
				//-------------------
			} else {
				//TODO neu in 1.17
				RenderUtils.bindTexture(WIDGETS_TEXTURE);
				RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, this.alpha);
				int i = this.getYImage(this.isHovered());
				RenderSystem.defaultBlendFunc();
				RenderSystem.enableDepthTest();
				this.drawTexture(matrix, this.x, this.y, 0, 46 + i * 20, this.width / 2, this.height);
				this.drawTexture(matrix, this.x + this.width / 2, this.y, 200 - this.width / 2, 46 + i * 20, this.width / 2, this.height);
				RenderSystem.disableDepthTest();
			}

			this.renderBackground(matrix, mc, mouseX, mouseY);

			this.renderLabel(matrix);

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
			if (this.isHovered() && (MouseInput.isLeftMouseDown() || (this.enableRightclick && MouseInput.isRightMouseDown())) && (!leftDown || this.ignoreLeftMouseDownClickBlock) && !leftDownNotHovered && !this.isInputBlocked() && this.active && this.visible) {
				if (!this.leftDownThis) {
					this.onClick(mouseX, mouseY);
					if (this.clicksound == null) {
						this.playDownSound(MinecraftClient.getInstance().getSoundManager());
					} else {
						SoundHandler.resetSound(this.clicksound);
						SoundHandler.playSound(this.clicksound);
					}
					leftDown = true;
					this.leftDownThis = true;
				}
			}
			if (!MouseInput.isLeftMouseDown() && !(MouseInput.isRightMouseDown() && this.enableRightclick)) {
				leftDown = false;
				this.leftDownThis = false;
			}
		}
		
	}
	
	protected void renderLabel(MatrixStack matrix) {
		@SuppressWarnings("resource")
		TextRenderer font = MinecraftClient.getInstance().textRenderer;
		int stringWidth = font.getWidth(getMessageString());
		int stringHeight = 8;
		int pX = (int) (((this.x + (this.width / 2)) - ((stringWidth * this.labelScale) / 2)) / this.labelScale);
		int pY = (int) (((this.y + (this.height / 2)) - ((stringHeight * this.labelScale) / 2)) / this.labelScale);
		
		matrix.push();
		matrix.scale(this.labelScale, this.labelScale, this.labelScale);

		if (this.labelShadow) {
			font.drawWithShadow(matrix, getMessageString(), pX, pY, getFGColor() | MathHelper.ceil(this.alpha * 255.0F) << 24);
		} else {
			font.draw(matrix, getMessageString(), pX, pY, getFGColor() | MathHelper.ceil(this.alpha * 255.0F) << 24);
		}
		
		matrix.pop();
	}
	
	private boolean isInputBlocked() {
		if (this.ignoreBlockedInput) {
			return false;
		}
		return MouseInput.isVanillaInputBlocked();
	}
	
	public void setBackgroundColor(@Nullable Color idle, @Nullable Color hovered, @Nullable Color idleBorder, @Nullable Color hoveredBorder, float borderWidth) {
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
	
	public void setBackgroundColor(@Nullable Color idle, @Nullable Color hovered, @Nullable Color idleBorder, @Nullable Color hoveredBorder, int borderWidth) {
		this.setBackgroundColor(idle, hovered, idleBorder, hoveredBorder, (float) borderWidth);
	}
	
	public void setBackgroundTexture(Identifier normal, Identifier hovered) {
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
			            	   this.playDownSound(MinecraftClient.getInstance().getSoundManager());
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
		this.setMessage(new LiteralText(msg));
	}
	
	public int getWidth() {
		return this.width;
	}
	
	public void setWidth(int width) {
		this.width = width;
	}
	
	public void setHeight(int height) {
		this.height = height;
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
		this.hovered = b;
	}

	public void setPressAction(PressAction press) {
		this.press = press;
	}

	public void setClickSound(@Nullable String key) {
		this.clicksound = key;
	}

	public void setDescription(String... desc) {
		this.description = desc;
	}

	public String[] getDescription() {
		return this.description;
	}
	
	public int getFGColor() {
		return this.active ? 16777215 : 10526880;
	}
	
	public void setLabelShadow(boolean shadow) {
		this.labelShadow = shadow;
	}
	
	public static boolean isAnyButtonLeftClicked() {
		return leftDown;
	}

}
