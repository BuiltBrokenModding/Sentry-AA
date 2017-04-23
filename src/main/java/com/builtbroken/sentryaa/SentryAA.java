package com.builtbroken.sentryaa;

import com.builtbroken.mc.lib.mod.AbstractMod;
import com.builtbroken.mc.lib.mod.AbstractProxy;
import com.builtbroken.mc.lib.mod.ModCreativeTab;
import com.builtbroken.mc.prefab.inventory.InventoryUtility;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLLoadCompleteEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import net.minecraft.item.ItemStack;

/**
 * @see <a href="https://github.com/BuiltBrokenModding/VoltzEngine/blob/development/license.md">License</a> for what you can and can't do with the code.
 * Created by Dark(DarkGuardsman, Robert) on 4/23/2017.
 */
@Mod(modid = SentryAA.DOMAIN, name = "Sentry AA", version = SentryAA.VERSION, dependencies = SentryAA.DEPENDENCIES)
public class SentryAA extends AbstractMod
{
    /** Name of the channel and mod ID. */
    public static final String DOMAIN = "sentryaa";
    public static final String PREFIX = DOMAIN + ":";

    /** The version of WatchYourStep. */
    public static final String MAJOR_VERSION = "@MAJOR@";
    public static final String MINOR_VERSION = "@MINOR@";
    public static final String REVISION_VERSION = "@REVIS@";
    public static final String BUILD_VERSION = "@BUILD@";
    public static final String VERSION = MAJOR_VERSION + "." + MINOR_VERSION + "." + REVISION_VERSION + "." + BUILD_VERSION;
    public static final String DEPENDENCIES = "required-after:voltzengine;required-after:armory;";

    @Mod.Instance(DOMAIN)
    public static SentryAA INSTANCE;

    @SidedProxy(clientSide = "com.builtbroken.sentryaa.CommonProxy", serverSide = "com.builtbroken.sentryaa.CommonProxy")
    public static CommonProxy proxy;

    public SentryAA()
    {
        super(DOMAIN);
        getManager().setTab(new ModCreativeTab(DOMAIN));
    }

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
        super.preInit(event);
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event)
    {
        super.init(event);
        if (getManager().defaultTab instanceof ModCreativeTab)
        {
            ((ModCreativeTab) getManager().defaultTab).itemStack = new ItemStack(InventoryUtility.getBlock(PREFIX + "fofstation"));
        }
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event)
    {
        super.postInit(event);
    }

    @Mod.EventHandler
    public void loadComplete(FMLLoadCompleteEvent event)
    {
        super.loadComplete(event);
    }

    @Override
    public AbstractProxy getProxy()
    {
        return proxy;
    }
}
