package com.builtbroken.sentryaa.content.fof;

import com.builtbroken.jlib.helpers.MathHelper;
import com.builtbroken.jlib.lang.EnglishLetters;
import com.builtbroken.mc.api.entity.IFoF;
import com.builtbroken.mc.api.tile.IFoFProvider;
import com.builtbroken.mc.api.tile.access.IGuiTile;
import com.builtbroken.mc.api.tile.access.IRotation;
import com.builtbroken.mc.codegen.annotations.MultiBlockWrapped;
import com.builtbroken.mc.codegen.annotations.TileWrapped;
import com.builtbroken.mc.core.network.packet.PacketType;
import com.builtbroken.mc.framework.access.*;
import com.builtbroken.mc.prefab.gui.ContainerDummy;
import com.builtbroken.mc.prefab.inventory.ExternalInventory;
import com.builtbroken.mc.prefab.tile.logic.TileMachineNode;
import com.builtbroken.sentryaa.SentryAA;
import com.builtbroken.sentryaa.content.fof.gui.GuiFoF;
import com.builtbroken.sentryaa.content.fof.gui.GuiSettings;
import cpw.mods.fml.common.network.ByteBufUtils;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.StringUtils;
import net.minecraftforge.common.util.ForgeDirection;

import java.util.ArrayList;
import java.util.List;

/**
 * Friend or foe controller, used to sync FoF tags between launchers, AMS, and other tiles.
 *
 * @see <a href="https://github.com/BuiltBrokenModding/VoltzEngine/blob/development/license.md">License</a> for what you can and can't do with the code.
 * Created by Dark(DarkGuardsman, Robert) on 3/9/2016.
 */
@TileWrapped(className = "TileWrappedFoF")
@MultiBlockWrapped()
public class TileFoF extends TileMachineNode implements IGuiTile, IFoFProvider, IProfileContainer, IRotation
{
    /** Main ID used for FoF system */
    protected String userFoFID;
    /** Archive of past FoF ids that should still be considered active but will not be applied to new objects. */
    protected List<String> archivedFoFIDs = new ArrayList();

    /** Current access profile used for user permissions */
    private AccessProfile profile;
    /** Global profile ID used to load access profile */
    private String globalProfileID;

    /** Client var */
    public boolean hasProfile = false;

    private ForgeDirection rotationCache;

    public TileFoF()
    {
        super("tile.fof.station", SentryAA.DOMAIN);
    }

    @Override
    protected IInventory createInventory()
    {
        return new ExternalInventory(this, 0);
    }


    @Override
    public void firstTick()
    {
        super.firstTick();
        if (isServer())
        {
            if (userFoFID == null || userFoFID.isEmpty())
            {
                userFoFID = getRandomString();
            }
        }
    }

    /**
     * Generates a random string containing numbers and letters between a length of 10 - 30
     * 1,264,020,397,516,800 to 2,730,903,391,116,338,302,840,472,139,202,560,000,000 possible permutations
     * using this method. Not including the number of permutation if capital letters are considered.
     *
     * @return new String
     */
    protected String getRandomString()
    {
        String string = "";
        //Generate random default string
        int[] l = MathHelper.generateRandomIntArray(world().rand, EnglishLetters.values().length + 9, 10 + world().rand.nextInt(20));
        for (int i : l)
        {
            if (i < 10)
            {
                string += i;
            }
            else if (world().rand.nextBoolean())
            {
                string += EnglishLetters.values()[i - 10].name();
            }
            else
            {
                string += EnglishLetters.values()[i - 10].name().toLowerCase();
            }
        }
        return string;
    }

    @Override
    public boolean read(ByteBuf buf, int id, EntityPlayer player, PacketType type)
    {
        if (!super.read(buf, id, player, type))
        {
            if (isServer())
            {
                //Set FoF ID, Main Gui
                if (id == 2)
                {
                    if (hasNode(player, Permissions.machineConfigure.toString()))
                    {
                        String change = ByteBufUtils.readUTF8String(buf);
                        if (buf.readBoolean() && !archivedFoFIDs.contains(userFoFID))
                        {
                            archivedFoFIDs.add(userFoFID);
                        }
                        this.userFoFID = change;
                        sendPacketToGuiUsers(getHost().getPacketForData(1, "confirm"));
                    }
                    else
                    {
                        sendPacketToGuiUsers(getHost().getPacketForData(1, "missing.perm"));
                    }
                    return true;
                }
                //Enable permission system, GuiSettings
                else if (id == 3)
                {
                    if (hasNode(player, Permissions.machineConfigure.toString()))
                    {
                        if (buf.readBoolean())
                        {
                            initProfile();
                            getAccessProfile().getOwnerGroup().addMember(new AccessUser(player));
                        }
                        else
                        {
                            profile = null;
                            globalProfileID = null;
                            sendDescPacket();
                        }
                        sendPacketToGuiUsers(getHost().getPacketForData(1, "[1]confirm"));
                    }
                    else
                    {
                        sendPacketToGuiUsers(getHost().getPacketForData(1, "[2]missing.perm"));
                    }
                }
                //Clear FoF id archive, GuiSettings
                else if (id == 4)
                {
                    if (hasNode(player, Permissions.machineConfigure.toString()))
                    {
                        int s = archivedFoFIDs.size();
                        archivedFoFIDs.clear();
                        sendPacketToGuiUsers(getHost().getPacketForData(1, "[2]removed.ids{" + s + "}"));
                    }
                    else
                    {
                        sendPacketToGuiUsers(getHost().getPacketForData(1, "[2]missing.perm"));
                    }
                    return true;
                }
            }
            else
            {
                GuiScreen screen = Minecraft.getMinecraft().currentScreen;
                if (screen instanceof GuiFoF)
                {
                    GuiFoF gui = (GuiFoF) screen;
                    if (id == 1)
                    {
                        gui.message = ByteBufUtils.readUTF8String(buf);
                        if (gui.message == null)
                        {
                            gui.message = "";
                        }
                        return true;
                    }
                    else if (id == 3)
                    {
                        gui.updateFoFIDField(ByteBufUtils.readUTF8String(buf));
                        return true;
                    }
                }
                else if (screen instanceof GuiSettings)
                {
                    GuiSettings gui = (GuiSettings) screen;
                    if (id == 1)
                    {
                        String message = ByteBufUtils.readUTF8String(buf);
                        String s = message.contains("[") ? message.substring(message.indexOf("["), message.indexOf("]") + 1) : null;
                        int pos = 0;
                        String renderString = message;
                        if (s != null)
                        {
                            renderString = renderString.replace(s, "");
                            s = s.substring(1, 2);
                            try
                            {
                                pos = Integer.parseInt(s);
                            }
                            catch (NumberFormatException e)
                            {

                            }
                        }
                        gui.setMessage(renderString);
                        gui.pos = pos;
                        if (pos == 1 && message.contains("confirm"))
                        {
                            gui.initGui();
                        }
                        return true;
                    }
                }
            }
            return false;
        }
        return true;
    }

    @Override
    public void writeDescPacket(ByteBuf buf)
    {
        super.writeDescPacket(buf);
        buf.writeBoolean(profile != null);
    }

    @Override
    public void readDescPacket(ByteBuf buf)
    {
        super.readDescPacket(buf);
        this.hasProfile = buf.readBoolean();
    }

    protected void sendFoFIDChange(String change, boolean archive)
    {
        sendPacket(getHost().getPacketForData(2, change != null ? change : "", archive));
    }

    protected void sendEnablePermissions(boolean b)
    {
        sendPacket(getHost().getPacketForData(this, 3, b));
    }


    public void doUpdateGuiUsers()
    {
        if (userFoFID != null)
        {
            sendPacketToGuiUsers(getHost().getPacketForData(3, userFoFID));
        }
    }

    protected void initProfile()
    {
        if (profile == null)
        {
            if (!StringUtils.isNullOrEmpty(globalProfileID))
            {
                profile = GlobalAccessSystem.getProfile(globalProfileID);
            }
            else
            {
                profile = new AccessProfile().generateNew("Default", this);
                if (this.username != null)
                {
                    profile.getOwnerGroup().addMember(new AccessUser(getOwnerName(), getOwnerID()));
                }
            }
            sendDescPacket();
        }
    }

    @Override
    public String getProvidedFoFTag()
    {
        return userFoFID;
    }

    @Override
    public boolean isFriendly(Entity entity)
    {
        if (entity instanceof IFoF)
        {
            if (((IFoF) entity).getFoFTag() != null)
            {
                if (((IFoF) entity).getFoFTag().equals(getProvidedFoFTag()))
                {
                    return true;
                }
                return archivedFoFIDs.contains(((IFoF) entity).getFoFTag());
            }
        }
        return false;
    }

    @Override
    public void load(NBTTagCompound nbt)
    {
        super.load(nbt);
        if (nbt.hasKey("fofID"))
        {
            userFoFID = nbt.getString("fofID");
        }
        if (nbt.hasKey("fofArchive"))
        {
            archivedFoFIDs.clear();
            NBTTagCompound tag = nbt.getCompoundTag("fofArchive");
            int size = tag.getInteger("size");
            for (int i = 0; i < size; i++)
            {
                archivedFoFIDs.add(tag.getString("" + i));
            }

        }
        if (nbt.hasKey("globalAccessID"))
        {
            globalProfileID = nbt.getString("globalAccessID");
        }
        else if (nbt.hasKey("localProfile"))
        {
            profile = new AccessProfile(nbt.getCompoundTag("localProfile"));
        }
    }

    @Override
    public NBTTagCompound save(NBTTagCompound nbt)
    {
        super.save(nbt);
        if (!StringUtils.isNullOrEmpty(userFoFID))
        {
            nbt.setString("fofID", userFoFID);
        }
        if (!archivedFoFIDs.isEmpty())
        {
            NBTTagCompound tag = new NBTTagCompound();
            tag.setInteger("size", archivedFoFIDs.size());
            for (int i = 0; i < archivedFoFIDs.size(); i++)
            {
                tag.setString("" + i, archivedFoFIDs.get(i));
            }
            nbt.setTag("fofArchive", tag);
        }
        if (StringUtils.isNullOrEmpty(globalProfileID))
        {
            if (profile != null)
            {
                nbt.setTag("localProfile", profile.save(new NBTTagCompound()));
            }
        }
        else
        {
            nbt.setString("globalAccessID", globalProfileID);
        }
        return nbt;
    }

    @Override
    public Object getServerGuiElement(int ID, EntityPlayer player)
    {
        return new ContainerDummy(player, this);
    }

    @Override
    public Object getClientGuiElement(int ID, EntityPlayer player)
    {
        return new GuiFoF(this, player);
    }

    @Override
    public AccessProfile getAccessProfile()
    {
        return profile;
    }

    @Override
    public void setAccessProfile(AccessProfile profile)
    {
        if (this.profile != null)
        {
            this.profile.removeContainer(this);
        }

        this.profile = profile;
        if (profile != null)
        {
            profile.addContainer(this);
        }
    }

    @Override
    public boolean canAccess(String username)
    {
        return getAccessProfile() == null || getAccessProfile().getUserAccess(username).hasNode(Permissions.machineOpen.toString());
    }

    @Override
    public boolean hasNode(EntityPlayer player, String node)
    {
        return getAccessProfile() == null || getAccessProfile().hasNode(player, node);
    }

    @Override
    public boolean hasNode(String username, String node)
    {
        return getAccessProfile() == null || getAccessProfile().hasNode(username, node);
    }

    @Override
    public void onProfileChange()
    {
        //TODO kick users out of GUI if they do not have access anymore
    }


    public void onPostInit()
    {
        //GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(ICBM.blockFoFStation), "RCR", "PRP", 'C', InventoryUtility.getBlock("icbm:silocontroller"), 'R', UniversalRecipe.CIRCUIT_T2.get(), 'P', UniversalRecipe.PRIMARY_PLATE.get()));
    }

    @Override
    public ForgeDirection getDirection()
    {
        if (rotationCache == null)
        {
            rotationCache = ForgeDirection.getOrientation(getHost().getHostMeta()).getOpposite();
        }
        return rotationCache;
    }
}
