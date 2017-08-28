package com.builtbroken.sentryaa.content.remap;

import com.builtbroken.armory.Armory;
import com.builtbroken.armory.content.sentry.tile.TileSentry;
import com.builtbroken.armory.data.ArmoryDataHandler;
import com.builtbroken.armory.data.sentry.SentryData;
import com.builtbroken.mc.codegen.annotations.TileWrapped;
import com.builtbroken.mc.framework.logic.TileNode;
import com.builtbroken.sentryaa.SentryAA;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;

/**
 * @see <a href="https://github.com/BuiltBrokenModding/VoltzEngine/blob/development/license.md">License</a> for what you can and can't do with the code.
 * Created by Dark(DarkGuardsman, Robert) on 4/23/2017.
 */
@TileWrapped(className = "TileWrapperAmsRemap")
public class NodeAmsRemap extends TileNode
{
    public static SentryData ams;

    public NodeAmsRemap()
    {
        super("tile.remap.ams", SentryAA.DOMAIN);
        if (ams == null)
        {
            ams = (SentryData) ArmoryDataHandler.INSTANCE.get("sentry").get("sentryaa:ams");
        }
    }

    @Override
    public void firstTick()
    {
        super.firstTick();
        if (ams != null)
        {
            //TODO remap
            world().setTile(Armory.SENTRY_BLOCK_REG, xi(), yi(), zi());
            TileEntity tile = world().unwrap().getTileEntity(xi(), yi(), zi());
            if (tile instanceof TileSentry)
            {
                ((TileSentry) tile).setSentryStack(new ItemStack(Armory.blockSentry, 1, ams.meta));
            }
        }
    }

    @Override
    public void load(NBTTagCompound nbt)
    {
        //TODO load ammo
        //TODO load fof station link
    }

    @Override
    public NBTTagCompound save(NBTTagCompound nbt)
    {
        return nbt;
    }
}
