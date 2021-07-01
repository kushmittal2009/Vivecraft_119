package org.vivecraft.gameplay.trackers;

import java.nio.ByteBuffer;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.protocol.game.ServerboundCustomPayloadPacket;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.phys.Vec3;
import org.vivecraft.api.NetworkHelper;
import org.vivecraft.api.VRData;
import org.vivecraft.gameplay.VRPlayer;
import org.vivecraft.utils.math.Vector3;

public class BowTracker extends Tracker
{
    private double lastcontrollersDist;
    private double lastcontrollersDot;
    private double controllersDist;
    private double controllersDot;
    private double currentDraw;
    private double lastDraw;
    public boolean isDrawing;
    private boolean pressed;
    private boolean lastpressed;
    private boolean canDraw;
    private boolean lastcanDraw;
    public long startDrawTime;
    private final double notchDotThreshold = 20.0D;
    private double maxDraw;
    private long maxDrawMillis = 1100L;
    private Vec3 aim;
    float tsNotch = 0.0F;
    int hapcounter = 0;
    int lasthapStep = 0;

    public BowTracker(Minecraft mc)
    {
        super(mc);
    }

    public Vec3 getAimVector()
    {
        return this.aim;
    }

    public float getDrawPercent()
    {
        return (float)(this.currentDraw / this.maxDraw);
    }

    public boolean isNotched()
    {
        return this.canDraw || this.isDrawing;
    }

    public static boolean isBow(ItemStack itemStack)
    {
        if (itemStack == ItemStack.EMPTY)
        {
            return false;
        }
        else if (Minecraft.getInstance().vrSettings.bowMode == 0)
        {
            return false;
        }
        else if (Minecraft.getInstance().vrSettings.bowMode == 1)
        {
            return itemStack.getItem() == Items.BOW;
        }
        else
        {
            return itemStack.getItem().getUseAnimation(itemStack) == UseAnim.BOW;
        }
    }

    public static boolean isHoldingBow(LivingEntity e, InteractionHand hand)
    {
        return Minecraft.getInstance().vrSettings.seated ? false : isBow(e.getItemInHand(hand));
    }

    public static boolean isHoldingBowEither(LivingEntity e)
    {
        return isHoldingBow(e, InteractionHand.MAIN_HAND) || isHoldingBow(e, InteractionHand.OFF_HAND);
    }

    public boolean isActive(LocalPlayer p)
    {
        if (p == null)
        {
            return false;
        }
        else if (this.mc.gameMode == null)
        {
            return false;
        }
        else if (!p.isAlive())
        {
            return false;
        }
        else if (p.isSleeping())
        {
            return false;
        }
        else
        {
            return isHoldingBow(p, InteractionHand.MAIN_HAND) || isHoldingBow(p, InteractionHand.OFF_HAND);
        }
    }

    public boolean isCharged()
    {
        return Util.getMillis() - this.startDrawTime >= this.maxDrawMillis;
    }

    public void reset(LocalPlayer player)
    {
        this.isDrawing = false;
    }

    public Tracker.EntryPoint getEntryPoint()
    {
        return Tracker.EntryPoint.SPECIAL_ITEMS;
    }

    public void doProcess(LocalPlayer player)
    {
        VRData vrdata = this.mc.vrPlayer.vrdata_world_render;

        if (vrdata == null)
        {
            vrdata = this.mc.vrPlayer.vrdata_world_pre;
        }

        VRPlayer vrplayer = this.mc.vrPlayer;

        if (this.mc.vrSettings.seated)
        {
            this.aim = vrdata.getController(0).getCustomVector(new Vec3(0.0D, 0.0D, 1.0D));
        }
        else
        {
            this.lastcontrollersDist = this.controllersDist;
            this.lastcontrollersDot = this.controllersDot;
            this.lastpressed = this.pressed;
            this.lastDraw = this.currentDraw;
            this.lastcanDraw = this.canDraw;
            this.maxDraw = (double)this.mc.player.getBbHeight() * 0.22D;
            Vec3 vec3 = vrdata.getController(0).getPosition();
            Vec3 vec31 = vrdata.getController(1).getPosition();
            this.controllersDist = vec31.distanceTo(vec3);
            Vec3 vec32 = new Vec3(0.0D, 1.0D, 0.0D);
            Vec3 vec33 = vrdata.getHand(1).getCustomVector(vec32).scale(this.maxDraw * 0.5D).add(vec31);
            double d0 = vec3.distanceTo(vec33);
            this.aim = vec3.subtract(vec31).normalize();
            Vec3 vec34 = vrdata.getController(0).getCustomVector(new Vec3(0.0D, 0.0D, -1.0D));
            Vector3 vector3 = new Vector3((float)vec34.x, (float)vec34.y, (float)vec34.z);
            Vec3 vec35 = vrdata.getHand(1).getCustomVector(new Vec3(0.0D, -1.0D, 0.0D));
            Vector3 vector31 = new Vector3((float)vec35.x, (float)vec35.y, (float)vec35.z);
            this.controllersDot = (180D / Math.PI) * Math.acos((double)vector31.dot(vector3));
            this.pressed = this.mc.options.keyAttack.isDown();
            float f = (float)(0.15D * (double)vrdata.worldScale);
            boolean flag = isHoldingBow(player, InteractionHand.MAIN_HAND);
            InteractionHand interactionhand = flag ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
            ItemStack itemstack = ItemStack.EMPTY;
            ItemStack itemstack1 = ItemStack.EMPTY;

            if (flag)
            {
                itemstack1 = player.getMainHandItem();
                itemstack = player.getProjectile(itemstack1);
            }
            else
            {
                if (player.getMainHandItem().is(ItemTags.ARROWS))
                {
                    itemstack = player.getMainHandItem();
                }

                itemstack1 = player.getOffhandItem();
            }

            int i = itemstack1.getUseDuration();
            int j = itemstack1.getUseDuration() - 15;
            int k = 0;

            if (itemstack != ItemStack.EMPTY && d0 <= (double)f && this.controllersDot <= 20.0D)
            {
                if (!this.canDraw)
                {
                    this.startDrawTime = Util.getMillis();
                }

                this.canDraw = true;
                this.tsNotch = (float)Util.getMillis();

                if (!this.isDrawing)
                {
                    player.setItemInUseClient(itemstack1, interactionhand);
                    player.setItemInUseCountClient(i);
                    //Minecraft.getInstance().physicalGuiManager.preClickAction();
                }
            }
            else if ((float)Util.getMillis() - this.tsNotch > 500.0F)
            {
                this.canDraw = false;
                player.setItemInUseClient(ItemStack.EMPTY, interactionhand);
            }

            if (!this.isDrawing && this.canDraw && this.pressed && !this.lastpressed)
            {
                this.isDrawing = true;
                //Minecraft.getInstance().physicalGuiManager.preClickAction();
                this.mc.gameMode.useItem(player, player.level, interactionhand);
            }

            if (this.isDrawing && !this.pressed && this.lastpressed && (double)this.getDrawPercent() > 0.0D)
            {
                this.mc.vr.triggerHapticPulse(0, 500);
                this.mc.vr.triggerHapticPulse(1, 3000);
                ServerboundCustomPayloadPacket serverboundcustompayloadpacket = NetworkHelper.getVivecraftClientPacket(NetworkHelper.PacketDiscriminators.DRAW, ByteBuffer.allocate(4).putFloat(this.getDrawPercent()).array());
                Minecraft.getInstance().getConnection().send(serverboundcustompayloadpacket);
                this.mc.gameMode.releaseUsingItem(player);
                serverboundcustompayloadpacket = NetworkHelper.getVivecraftClientPacket(NetworkHelper.PacketDiscriminators.DRAW, ByteBuffer.allocate(4).putFloat(0.0F).array());
                Minecraft.getInstance().getConnection().send(serverboundcustompayloadpacket);
                this.isDrawing = false;
            }

            if (!this.pressed)
            {
                this.isDrawing = false;
            }

            if (!this.isDrawing && this.canDraw && !this.lastcanDraw)
            {
                this.mc.vr.triggerHapticPulse(1, 800);
                this.mc.vr.triggerHapticPulse(0, 800);
            }

            if (this.isDrawing)
            {
                this.currentDraw = this.controllersDist - (double)f;

                if (this.currentDraw > this.maxDraw)
                {
                    this.currentDraw = this.maxDraw;
                }

                int j1 = 0;

                if (this.getDrawPercent() > 0.0F)
                {
                    j1 = (int)(this.getDrawPercent() * 500.0F) + 700;
                }

                int l = (int)((float)itemstack1.getUseDuration() - this.getDrawPercent() * (float)this.maxDrawMillis);
                player.setItemInUseClient(itemstack1, interactionhand);
                double d1 = (double)this.getDrawPercent();

                if (d1 >= 1.0D)
                {
                    player.setItemInUseCountClient(k);
                }
                else if (d1 > 0.4D)
                {
                    player.setItemInUseCountClient(j);
                }
                else
                {
                    player.setItemInUseCountClient(i);
                }

                int i1 = (int)(d1 * 4.0D * 4.0D * 3.0D);

                if (i1 % 2 == 0 && this.lasthapStep != i1)
                {
                    this.mc.vr.triggerHapticPulse(0, j1);

                    if (d1 == 1.0D)
                    {
                        this.mc.vr.triggerHapticPulse(1, j1);
                    }
                }

                if (this.isCharged() && this.hapcounter % 4 == 0)
                {
                    this.mc.vr.triggerHapticPulse(1, 200);
                }

                this.lasthapStep = i1;
                ++this.hapcounter;
            }
            else
            {
                this.hapcounter = 0;
                this.lasthapStep = 0;
            }
        }
    }
}
