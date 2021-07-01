package org.vivecraft.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.resources.language.I18n;
import org.vivecraft.gui.framework.TwoHandedScreen;
import org.vivecraft.provider.MCVR;
import org.vivecraft.provider.openvr_jna.VRInputAction;

public class GuiRadial extends TwoHandedScreen
{
    private boolean isShift = false;
    String[] arr;

    public void init()
    {
        this.arr = this.minecraft.vrSettings.vrRadialItems;
        String[] astring = this.minecraft.vrSettings.vrRadialItemsAlt;
        this.clearWidgets();
        int i = 8;
        int j = 120;
        int k = 360 / i;
        int l = 48;
        int i1 = this.width / 2;
        int j1 = this.height / 2;

        if (this.isShift)
        {
            this.arr = astring;
        }

        for (int k1 = 0; k1 < i; ++k1)
        {
            KeyMapping keymapping = null;

            for (KeyMapping keymapping1 : this.minecraft.options.keyMappings)
            {
                if (keymapping1.getName().equalsIgnoreCase(this.arr[k1]))
                {
                    keymapping = keymapping1;
                }
            }

            String s = "?";

            if (keymapping != null)
            {
                s = I18n.m_118938_(keymapping.getName());
            }

            int i2 = Math.max(j, this.font.width(s));
            int j2 = 0;
            int k2 = 0;

            if (k1 == 0)
            {
                j2 = 0;
                k2 = -l;
            }
            else if (k1 == 1)
            {
                j2 = i2 / 2 + 8;
                k2 = -l / 2;
            }
            else if (k1 == 2)
            {
                j2 = i2 / 2 + 32;
                k2 = 0;
            }
            else if (k1 == 3)
            {
                j2 = i2 / 2 + 8;
                k2 = l / 2;
            }
            else if (k1 == 4)
            {
                j2 = 0;
                k2 = l;
            }
            else if (k1 == 5)
            {
                j2 = -i2 / 2 - 8;
                k2 = l / 2;
            }
            else if (k1 == 6)
            {
                j2 = -i2 / 2 - 32;
                k2 = 0;
            }
            else if (k1 == 7)
            {
                j2 = -i2 / 2 - 8;
                k2 = -l / 2;
            }

            int l1 = k1;

            if (s != "?")
            {
                this.addRenderableWidget(new Button(i1 + j2 - i2 / 2, j1 + k2 - 10, i2, 20, s, (p) ->
                {
                    if (l1 < 200)
                    {
                        VRInputAction vrinputaction = MCVR.get().getInputAction(this.arr[l1]);

                        if (vrinputaction != null)
                        {
                            vrinputaction.pressBinding();
                            vrinputaction.unpressBinding(2);
                        }
                    }
                    else if (l1 == 201)
                    {
                        this.setShift(!this.isShift);
                    }
                }));
            }
        }
    }

    public void setShift(boolean shift)
    {
        if (shift != this.isShift)
        {
            this.isShift = shift;
            this.init();
        }
    }

    public void render(PoseStack p_96562_, int p_96563_, int p_96564_, float p_96565_)
    {
        this.renderBackground(p_96562_);
        super.render(p_96562_, 0, 0, p_96565_);
    }
}
