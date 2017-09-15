package gregtech;

import cpw.mods.fml.common.*;
import cpw.mods.fml.common.event.*;
import cpw.mods.fml.common.registry.EntityRegistry;
import forestry.api.recipes.ICentrifugeRecipe;
import forestry.api.recipes.ISqueezerRecipe;
import forestry.api.recipes.RecipeManagers;
import gregtech.api.GregTech_API;
import gregtech.api.enchants.Enchantment_EnderDamage;
import gregtech.api.enchants.Enchantment_Radioactivity;
import gregtech.api.enums.*;
import gregtech.api.interfaces.internal.IGT_Mod;
import gregtech.api.util.*;
import gregtech.common.GT_DummyWorld;
import gregtech.common.GT_Network;
import gregtech.common.GT_Proxy;
import gregtech.common.GT_RecipeAdder;
import gregtech.common.entities.GT_Entity_Arrow;
import gregtech.common.entities.GT_Entity_Arrow_Potion;
import gregtech.common.items.armor.components.LoadArmorComponents;
import gregtech.common.tileentities.machines.basic.GT_MetaTileEntity_Massfabricator;
import gregtech.loaders.load.GT_FuelLoader;
import gregtech.loaders.load.GT_SonictronLoader;
import gregtech.loaders.misc.GT_Achievements;
import gregtech.loaders.misc.GT_Bees;
import gregtech.loaders.misc.GT_CoverLoader;
import gregtech.loaders.postload.*;
import gregtech.loaders.preload.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidStack;
import org.apache.commons.lang3.StringUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.PrintStream;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Mod(modid = "gregtech", name = "GregTech", version = "MC1710", useMetadata = false, dependencies = "after:IC2; after:Forestry; after:PFAAGeologica; after:Thaumcraft; after:Railcraft; after:appliedenergistics2; after:ThermalExpansion; after:TwilightForest; after:harvestcraft; after:magicalcrops; after:BuildCraft|Transport; after:BuildCraft|Silicon; after:BuildCraft|Factory; after:BuildCraft|Energy; after:BuildCraft|Core; after:BuildCraft|Builders; after:GalacticraftCore; after:GalacticraftMars; after:GalacticraftPlanets; after:ThermalExpansion|Transport; after:ThermalExpansion|Energy; after:ThermalExpansion|Factory; after:RedPowerCore; after:RedPowerBase; after:RedPowerMachine; after:RedPowerCompat; after:RedPowerWiring; after:RedPowerLogic; after:RedPowerLighting; after:RedPowerWorld; after:RedPowerControl; after:UndergroundBiomes;")
public class GT_Mod implements IGT_Mod {
    public static final int VERSION = 509, SUBVERSION = 31;
    public static final int TOTAL_VERSION = calculateTotalGTVersion(VERSION, SUBVERSION);
    @Mod.Instance("gregtech")
    public static GT_Mod instance;
    @SidedProxy(modId = "gregtech", clientSide = "gregtech.common.GT_Client", serverSide = "gregtech.common.GT_Server")
    public static GT_Proxy gregtechproxy;
    public static GT_Achievements achievements;

    static {
        if ((509 != GregTech_API.VERSION) || (509 != GT_ModHandler.VERSION) || (509 != GT_OreDictUnificator.VERSION) || (509 != GT_Recipe.VERSION) || (509 != GT_Utility.VERSION) || (509 != GT_RecipeRegistrator.VERSION) || (509 != Element.VERSION) || (509 != Materials.VERSION) || (509 != OrePrefixes.VERSION)) {
            throw new GT_ItsNotMyFaultException("One of your Mods included GregTech-API Files inside it's download, mention this to the Mod Author, who does this bad thing, and tell him/her to use reflection. I have added a Version check, to prevent Authors from breaking my Mod that way.");
        }
    }

    public GT_Mod() {
        GT_Values.GT = this;
        GT_Values.DW = new GT_DummyWorld();
        GT_Values.NW = new GT_Network();
        GT_Values.RA = new GT_RecipeAdder();

        Textures.BlockIcons.VOID.name();
        Textures.ItemIcons.VOID.name();
    }

    @Mod.EventHandler
    public void onPreLoad(FMLPreInitializationEvent aEvent) {
        Locale.setDefault(Locale.ENGLISH);
        if (GregTech_API.sPreloadStarted) {
            return;
        }
        for (Runnable tRunnable : GregTech_API.sBeforeGTPreload) {
            tRunnable.run();
        }
        File tFile = new File(new File(aEvent.getModConfigurationDirectory(), "GregTech"), "GregTech.cfg");
        Configuration tMainConfig = new Configuration(tFile);
        tMainConfig.load();
        tFile = new File(new File(aEvent.getModConfigurationDirectory(), "GregTech"), "IDs.cfg");
        GT_Config.sConfigFileIDs = new Configuration(tFile);
        GT_Config.sConfigFileIDs.load();
        GT_Config.sConfigFileIDs.save();
        GregTech_API.sRecipeFile = new GT_Config(new Configuration(new File(new File(aEvent.getModConfigurationDirectory(), "GregTech"), "Recipes.cfg")));
        GregTech_API.sMachineFile = new GT_Config(new Configuration(new File(new File(aEvent.getModConfigurationDirectory(), "GregTech"), "MachineStats.cfg")));
        GregTech_API.sWorldgenFile = new GT_Config(new Configuration(new File(new File(aEvent.getModConfigurationDirectory(), "GregTech"), "WorldGeneration.cfg")));
        GregTech_API.sMaterialProperties = new GT_Config(new Configuration(new File(new File(aEvent.getModConfigurationDirectory(), "GregTech"), "MaterialProperties.cfg")));
        GregTech_API.sMaterialComponents = new GT_Config(new Configuration(new File(new File(aEvent.getModConfigurationDirectory(), "GregTech"), "MaterialComponents.cfg")));
        GregTech_API.sUnification = new GT_Config(new Configuration(new File(new File(aEvent.getModConfigurationDirectory(), "GregTech"), "Unification.cfg")));
        GregTech_API.sSpecialFile = new GT_Config(new Configuration(new File(new File(aEvent.getModConfigurationDirectory(), "GregTech"), "Other.cfg")));
        GregTech_API.sOPStuff = new GT_Config(new Configuration(new File(new File(aEvent.getModConfigurationDirectory(), "GregTech"), "OverpoweredStuff.cfg")));
        GregTech_API.sModularArmor = new GT_Config(new Configuration(new File(new File(aEvent.getModConfigurationDirectory(), "GregTech"), "ModularArmor.cfg")));

        GregTech_API.sClientDataFile = new GT_Config(new Configuration(new File(aEvent.getModConfigurationDirectory().getParentFile(), "GregTech.cfg")));

        GT_Log.mLogFile = new File(aEvent.getModConfigurationDirectory().getParentFile(), "logs/GregTech.log");
        String aTextGeneral = "general";
        try {
            if (!GT_Log.mLogFile.exists()) {
                GT_Log.mLogFile.createNewFile();
            }
            GT_Log.out = GT_Log.err = new PrintStream(GT_Log.mLogFile);
            GT_Log.mOreDictLogFile = new File(aEvent.getModConfigurationDirectory().getParentFile(), "logs/OreDict.log");
            if (!GT_Log.mOreDictLogFile.exists()) {
                GT_Log.mOreDictLogFile.createNewFile();
            }
        } catch (Throwable e) {}

        gregtechproxy.onPreLoad();

        GT_Log.out.println("GT_Mod: Setting Configs");
        GT_Values.D1 = tMainConfig.get(aTextGeneral, "Debug", false).getBoolean(false);
        GT_Values.D2 = tMainConfig.get(aTextGeneral, "Debug2", false).getBoolean(false);

        GregTech_API.TICKS_FOR_LAG_AVERAGING = tMainConfig.get(aTextGeneral, "TicksForLagAveragingWithScanner", 25).getInt(25);
        GregTech_API.MILLISECOND_THRESHOLD_UNTIL_LAG_WARNING = tMainConfig.get(aTextGeneral, "MillisecondsPassedInGTTileEntityUntilLagWarning", 100).getInt(100);
        if (tMainConfig.get(aTextGeneral, "disable_STDOUT", false).getBoolean(false)) {
            System.out.close();
        }
        if (tMainConfig.get(aTextGeneral, "disable_STDERR", false).getBoolean(false)) {
            System.err.close();
        }
        GregTech_API.sMachineExplosions = tMainConfig.get("machines", "machines_explosion_damage", true).getBoolean(false);
        GregTech_API.sMachineFlammable = tMainConfig.get("machines", "machines_flammable", true).getBoolean(false);
        GregTech_API.sMachineNonWrenchExplosions = tMainConfig.get("machines", "explosions_on_nonwrenching", true).getBoolean(false);
        GregTech_API.sMachineWireFire = tMainConfig.get("machines", "wirefire_on_explosion", true).getBoolean(false);
        GregTech_API.sMachineFireExplosions = tMainConfig.get("machines", "fire_causes_explosions", true).getBoolean(false);
        GregTech_API.sMachineRainExplosions = tMainConfig.get("machines", "rain_causes_explosions", true).getBoolean(false);
        GregTech_API.sMachineThunderExplosions = tMainConfig.get("machines", "lightning_causes_explosions", true).getBoolean(false);
        GregTech_API.sConstantEnergy = tMainConfig.get("machines", "constant_need_of_energy", true).getBoolean(false);
        GregTech_API.sColoredGUI = tMainConfig.get("machines", "colored_guis_when_painted", true).getBoolean(false);

        GregTech_API.sTimber = tMainConfig.get(aTextGeneral, "timber_axe", true).getBoolean(true);
        GregTech_API.sDrinksAlwaysDrinkable = tMainConfig.get(aTextGeneral, "drinks_always_drinkable", false).getBoolean(false);
        GregTech_API.sDoShowAllItemsInCreative = tMainConfig.get(aTextGeneral, "show_all_metaitems_in_creative_and_NEI", false).getBoolean(false);
        GregTech_API.sMultiThreadedSounds = tMainConfig.get(aTextGeneral, "sound_multi_threading", false).getBoolean(false);
        String SBdye0 = "ColorModulation.";
        for (Dyes tDye : Dyes.values()) {
            if ((tDye != Dyes.dyeNULL) && (tDye.mIndex < 0)) {
                String SBdye1 = SBdye0 + tDye;
                tDye.mRGBa[0] = ((short) Math.min(255, Math.max(0, GregTech_API.sClientDataFile.get(SBdye1, "R", tDye.mRGBa[0]))));
                tDye.mRGBa[1] = ((short) Math.min(255, Math.max(0, GregTech_API.sClientDataFile.get(SBdye1, "G", tDye.mRGBa[1]))));
                tDye.mRGBa[2] = ((short) Math.min(255, Math.max(0, GregTech_API.sClientDataFile.get(SBdye1, "B", tDye.mRGBa[2]))));
            }
        }
        gregtechproxy.mMaxEqualEntitiesAtOneSpot = tMainConfig.get(aTextGeneral, "MaxEqualEntitiesAtOneSpot", 3).getInt(3);
        gregtechproxy.mSkeletonsShootGTArrows = tMainConfig.get(aTextGeneral, "SkeletonsShootGTArrows", 16).getInt(16);
        gregtechproxy.mFlintChance = tMainConfig.get(aTextGeneral, "FlintAndSteelChance", 30).getInt(30);
        gregtechproxy.mItemDespawnTime = tMainConfig.get(aTextGeneral, "ItemDespawnTime", 6000).getInt(6000);
        gregtechproxy.mDisableVanillaOres = tMainConfig.get(aTextGeneral, "DisableVanillaOres", true).getBoolean(true);
        gregtechproxy.mNerfDustCrafting = tMainConfig.get(aTextGeneral, "NerfDustCrafting", true).getBoolean(true);
        gregtechproxy.mIncreaseDungeonLoot = tMainConfig.get(aTextGeneral, "IncreaseDungeonLoot", true).getBoolean(true);
        gregtechproxy.mAxeWhenAdventure = tMainConfig.get(aTextGeneral, "AdventureModeStartingAxe", true).getBoolean(true);
        gregtechproxy.mHardcoreCables = tMainConfig.get(aTextGeneral, "HardCoreCableLoss", false).getBoolean(false);
        gregtechproxy.mSurvivalIntoAdventure = tMainConfig.get(aTextGeneral, "forceAdventureMode", false).getBoolean(false);
        gregtechproxy.mHungerEffect = tMainConfig.get(aTextGeneral, "AFK_Hunger", false).getBoolean(false);
        gregtechproxy.mHardRock = tMainConfig.get(aTextGeneral, "harderstone", false).getBoolean(false);
        gregtechproxy.mInventoryUnification = tMainConfig.get(aTextGeneral, "InventoryUnification", true).getBoolean(true);
        gregtechproxy.mGTBees = tMainConfig.get(aTextGeneral, "GTBees", true).getBoolean(true);
        gregtechproxy.mCraftingUnification = tMainConfig.get(aTextGeneral, "CraftingUnification", true).getBoolean(true);
        gregtechproxy.mNerfedWoodPlank = tMainConfig.get(aTextGeneral, "WoodNeedsSawForCrafting", true).getBoolean(true);
        gregtechproxy.mNerfedVanillaTools = tMainConfig.get(aTextGeneral, "smallerVanillaToolDurability", true).getBoolean(true);
        gregtechproxy.mSortToTheEnd = tMainConfig.get(aTextGeneral, "EnsureToBeLoadedLast", true).getBoolean(true);
        gregtechproxy.mDisableIC2Cables = tMainConfig.get(aTextGeneral, "DisableIC2Cables", true).getBoolean(true);
        gregtechproxy.mAchievements = tMainConfig.get(aTextGeneral, "EnableAchievements", true).getBoolean(true);
        gregtechproxy.mAE2Integration = GregTech_API.sSpecialFile.get(ConfigCategories.general, "EnableAE2Integration", Loader.isModLoaded("appliedenergistics2"));
        gregtechproxy.mAE2Tunnel = GregTech_API.sSpecialFile.get(ConfigCategories.general, "EnableAE2Tunnel", false);
        gregtechproxy.mNerfedCombs = tMainConfig.get(aTextGeneral, "NerfCombs", true).getBoolean(true);
        gregtechproxy.mNerfedCrops = tMainConfig.get(aTextGeneral, "NerfCrops", true).getBoolean(true);
        gregtechproxy.mHideUnusedOres = tMainConfig.get(aTextGeneral, "HideUnusedOres", true).getBoolean(true);
        gregtechproxy.mHideRecyclingRecipes = tMainConfig.get(aTextGeneral, "HideRecyclingRecipes", true).getBoolean(true);
        gregtechproxy.mArcSmeltIntoAnnealed = tMainConfig.get(aTextGeneral, "ArcSmeltIntoAnnealedWrought", true).getBoolean(true);
        gregtechproxy.mEnableAllMaterials = tMainConfig.get("general", "EnableAllMaterials", false).getBoolean(false);
        gregtechproxy.mEnableAllComponents = tMainConfig.get("general", "EnableAllComponents", false).getBoolean(false);
        gregtechproxy.mPollution = tMainConfig.get("Pollution", "EnablePollution", true).getBoolean(true);
        gregtechproxy.mPollutionSmogLimit = tMainConfig.get("Pollution", "SmogLimit", 500000).getInt(500000);
        gregtechproxy.mPollutionPoisonLimit = tMainConfig.get("Pollution", "PoisonLimit", 750000).getInt(750000);
        gregtechproxy.mPollutionVegetationLimit = tMainConfig.get("Pollution", "VegetationLimit", 1000000).getInt(1000000);
        gregtechproxy.mPollutionSourRainLimit = tMainConfig.get("Pollution", "SourRainLimit", 2000000).getInt(2000000);
        gregtechproxy.mExplosionItemDrop = tMainConfig.get("general", "ExplosionItemDrops", false).getBoolean(false);
        gregtechproxy.mAddGTRecipesToIC2Machines = tMainConfig.get("general", "AddGTRecipesToIC2Machines", true).getBoolean(true);
        gregtechproxy.mUndergroundOil.getConfig(tMainConfig, "undergroundfluid");
        gregtechproxy.mEnableCleanroom = tMainConfig.get("general", "EnableCleanroom", true).getBoolean(true);
        gregtechproxy.mLowGravProcessing = Loader.isModLoaded(GT_Values.MOD_ID_GC_CORE) && tMainConfig.get("general", "LowGravProcessing", true).getBoolean(true);
        gregtechproxy.mCropNeedBlock = tMainConfig.get("general", "CropNeedBlockBelow", true).getBoolean(true);
        gregtechproxy.mDisableOldChemicalRecipes = tMainConfig.get("general", "DisableOldChemicalRecipes", false).getBoolean(false);
        gregtechproxy.mAMHInteraction = tMainConfig.get("general", "AllowAutoMaintenanceHatchInteraction", false).getBoolean(false);
        GregTech_API.mOutputRF = GregTech_API.sOPStuff.get(ConfigCategories.general, "OutputRF", true);
        GregTech_API.mInputRF = GregTech_API.sOPStuff.get(ConfigCategories.general, "InputRF", false);
        GregTech_API.mEUtoRF = GregTech_API.sOPStuff.get(ConfigCategories.general, "100EUtoRF", 360);
        GregTech_API.mRFtoEU = GregTech_API.sOPStuff.get(ConfigCategories.general, "100RFtoEU", 20);
        GregTech_API.mRFExplosions = GregTech_API.sOPStuff.get(ConfigCategories.general, "RFExplosions", false);
        GregTech_API.meIOLoaded = Loader.isModLoaded("EnderIO");
        gregtechproxy.mForceFreeFace = GregTech_API.sMachineFile.get(ConfigCategories.machineconfig, "forceFreeFace",false);
        gregtechproxy.mEasierIVPlusCables = tMainConfig.get("general", "EasierIVPlusCables", false).getBoolean(false);
        gregtechproxy.mBrickedBlastFurnace = tMainConfig.get("general", "BrickedBlastFurnace", true).getBoolean(true);
        gregtechproxy.mMixedOreOnlyYieldsTwoThirdsOfPureOre = tMainConfig.get("general", "MixedOreOnlyYieldsTwoThirdsOfPureOre", false).getBoolean(false);
        gregtechproxy.enableBlackGraniteOres = GregTech_API.sWorldgenFile.get("general", "enableBlackGraniteOres", gregtechproxy.enableBlackGraniteOres);
        gregtechproxy.enableRedGraniteOres = GregTech_API.sWorldgenFile.get("general", "enableRedGraniteOres", gregtechproxy.enableRedGraniteOres);
        gregtechproxy.enableMarbleOres = GregTech_API.sWorldgenFile.get("general", "enableMarbleOres", gregtechproxy.enableMarbleOres);
        gregtechproxy.enableBasaltOres = GregTech_API.sWorldgenFile.get("general", "enableBasaltOres", gregtechproxy.enableBasaltOres);
        gregtechproxy.enableGCOres = GregTech_API.sWorldgenFile.get("general", "enableGCOres", gregtechproxy.enableGCOres);
        gregtechproxy.enableUBOres = GregTech_API.sWorldgenFile.get("general", "enableUBOres", gregtechproxy.enableUBOres);
        
        GregTech_API.mUseOnlyGoodSolderingMaterials = GregTech_API.sRecipeFile.get(ConfigCategories.Recipes.harderrecipes, "useonlygoodsolderingmaterials", GregTech_API.mUseOnlyGoodSolderingMaterials);

        if (tMainConfig.get("general", "hardermobspawners", true).getBoolean(true)) {
            Blocks.mob_spawner.setHardness(500.0F).setResistance(6000000.0F);
        }
        gregtechproxy.mOnline = tMainConfig.get(aTextGeneral, "online", true).getBoolean(false);

        gregtechproxy.mUpgradeCount = Math.min(64, Math.max(1, tMainConfig.get("features", "UpgradeStacksize", 4).getInt()));
        for (OrePrefixes tPrefix : OrePrefixes.values()) {
            if (tPrefix.mIsUsedForOreProcessing) {
                tPrefix.mDefaultStackSize = ((byte) Math.min(64, Math.max(1, tMainConfig.get("features", "MaxOreStackSize", 64).getInt())));
            } else if (tPrefix == OrePrefixes.plank) {
                tPrefix.mDefaultStackSize = ((byte) Math.min(64, Math.max(16, tMainConfig.get("features", "MaxPlankStackSize", 64).getInt())));
            } else if ((tPrefix == OrePrefixes.wood) || (tPrefix == OrePrefixes.treeLeaves) || (tPrefix == OrePrefixes.treeSapling) || (tPrefix == OrePrefixes.log)) {
                tPrefix.mDefaultStackSize = ((byte) Math.min(64, Math.max(16, tMainConfig.get("features", "MaxLogStackSize", 64).getInt())));
            } else if (tPrefix.mIsUsedForBlocks) {
                tPrefix.mDefaultStackSize = ((byte) Math.min(64, Math.max(16, tMainConfig.get("features", "MaxOtherBlockStackSize", 64).getInt())));
            }
        }
        Materials.init();

        GT_Log.out.println("GT_Mod: Saving Main Config");
        tMainConfig.save();

        GT_Log.out.println("GT_Mod: Generating Lang-File");
        GT_LanguageManager.sEnglishFile = new Configuration(new File(aEvent.getModConfigurationDirectory().getParentFile(), "GregTech.lang"));
        GT_LanguageManager.sEnglishFile.load();

        EntityRegistry.registerModEntity(GT_Entity_Arrow.class, "GT_Entity_Arrow", 1, GT_Values.GT, 160, 1, true);
        EntityRegistry.registerModEntity(GT_Entity_Arrow_Potion.class, "GT_Entity_Arrow_Potion", 2, GT_Values.GT, 160, 1, true);

        List<String> oreTags = new ArrayList<String>();
        if (Loader.isModLoaded("MineTweaker3")) {
            File globalDir = new File("scripts");
            if (globalDir.exists()) {
                List<String> scripts = new ArrayList<String>();
                for (File file : globalDir.listFiles()) {
                    if (file.getName().endsWith(".zs")) {
                        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                            String line;
                            while ((line = br.readLine()) != null) {
                                scripts.add(line);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
                String pattern1 = "<";
                String pattern2 = ">";

                Pattern p = Pattern.compile(Pattern.quote(pattern1) + "(.*?)" + Pattern.quote(pattern2));
                for (String text : scripts) {
                    Matcher m = p.matcher(text);
                    while (m.find()) {
                        String hit = m.group(1);
                        if (hit.startsWith("ore:")) {
                            hit = hit.substring(4);
                            if (!oreTags.contains(hit)) {
                                oreTags.add(hit);
                            }
                        } else if(hit.startsWith("gregtech:gt.metaitem.0")) {
                            hit = hit.substring(22);
                            int mIt = Integer.parseInt(hit.substring(0, 1));
                            if (mIt>0) {
                                int meta = 0;
                                try {
                                    hit = hit.substring(2);
                                    meta = Integer.parseInt(hit);
                                } catch(Exception e){
                                    if (GT_Values.D1) System.out.println("parseError: "+hit);
                                }
                                if (meta>0&&meta<32000) {
                                    int prefix = meta/1000;
                                    int material = meta % 1000;
                                    String tag = "";
                                    String[] tags = new String[]{};
                                    if (mIt==1)tags = new String[]{"dustTiny","dustSmall","dust","dustImpure","dustPure","crushed","crushedPurified","crushedCentrifuged","gem","nugget",null,"ingot","ingotHot",null,null,null,null,"plate",null,null,null,null,"plateDense","stick","lens",null,"bolt","screw","ring","foil","cell","cellPlasma"};
                                    if (mIt==2)tags = new String[]{"toolHeadSword", "toolHeadPickaxe", "toolHeadShovel", "toolHeadAxe", "toolHeadHoe", "toolHeadHammer", "toolHeadFile", "toolHeadSaw", "toolHeadDrill", "toolHeadChainsaw", "toolHeadWrench", "toolHeadUniversalSpade", "toolHeadSense", "toolHeadPlow", "toolHeadArrow", "toolHeadBuzzSaw", "turbineBlade", null, null, "wireFine", "gearGtSmall", "rotor", null, null, "spring", null, null, "gemChipped", "gemFlawed", "gemFlawless", "gemExquisite", "gearGt"};
                                    if (tags.length>prefix) tag = tags[prefix];
                                    if (GregTech_API.sGeneratedMaterials[material]!=null){
                                        tag += GregTech_API.sGeneratedMaterials[material].mName;
                                        if(!oreTags.contains(tag)) oreTags.add(tag);
                                    }else if(material>0) {
                                        if (GT_Values.D1) System.out.println("MaterialDisabled: "+material+" "+m.group(1));
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        String[] preS = new String[]{"dustTiny","dustSmall","dust","dustImpure","dustPure","crushed","crushedPurified","crushedCentrifuged","gem","nugget","ingot","ingotHot","plate","plateDense","stick","lens","bolt","screw","ring","foil","cell","cellPlasma","toolHeadSword", "toolHeadPickaxe", "toolHeadShovel", "toolHeadAxe", "toolHeadHoe", "toolHeadHammer", "toolHeadFile", "toolHeadSaw", "toolHeadDrill", "toolHeadChainsaw", "toolHeadWrench", "toolHeadUniversalSpade", "toolHeadSense", "toolHeadPlow", "toolHeadArrow", "toolHeadBuzzSaw", "turbineBlade", "wireFine", "gearGtSmall", "rotor", "springSmall", "spring", "gemChipped", "gemFlawed", "gemFlawless", "gemExquisite"};

        List<String> mMTTags = new ArrayList<String>();
        for (String test : oreTags){
            if (StringUtils.startsWithAny(test, preS)){
                mMTTags.add(test);
                if (GT_Values.D1) System.out.println("oretag: "+test);
            }
        }

        System.out.println("reenableMetaItems");
        for (String reEnable : mMTTags){ //TODO FIX WITH NEW SYSTEM
            OrePrefixes tPrefix = OrePrefixes.getOrePrefix(reEnable);
            if (tPrefix != null){
                Materials tName = Materials.get(reEnable.replaceFirst(tPrefix.toString(), ""));
                if (tName != null){
                    tPrefix.mDisabledItems.remove(tName);
                    tPrefix.mGeneratedItems.add(tName);
                    if (tPrefix == OrePrefixes.screw){
                        OrePrefixes.bolt.mDisabledItems.remove(tName);
                        OrePrefixes.bolt.mGeneratedItems.add(tName);
                        OrePrefixes.stick.mDisabledItems.remove(tName);
                        OrePrefixes.stick.mGeneratedItems.add(tName);
                    } else if (tPrefix == OrePrefixes.spring){
                        OrePrefixes.stick.mDisabledItems.remove(tName);
                        OrePrefixes.stick.mGeneratedItems.add(tName);
                    } else if (tPrefix == OrePrefixes.rotor){
                        OrePrefixes.ring.mDisabledItems.remove(tName);
                        OrePrefixes.ring.mGeneratedItems.add(tName);
                    }
                } else {
                    System.out.println("noMaterial "+reEnable);
                }
            } else {
                System.out.println("noPrefix "+reEnable);
            }
        }

        new Enchantment_EnderDamage();
        new Enchantment_Radioactivity();

        new GT_Loader_OreProcessing().run();
        new GT_Loader_OreDictionary().run();
        new GT_Loader_ItemData().run();
        new GT_Loader_Item_Block_And_Fluid().run();
        new GT_Loader_MetaTileEntities().run();

        new GT_Loader_CircuitBehaviors().run();
        new GT_SonictronLoader().run();
        new GT_SpawnEventHandler();

        if (GregTech_API.sRecipeFile.get(ConfigCategories.Recipes.gregtechrecipes, "SolarPanel", true)) {
            GT_ModHandler.addCraftingRecipe(ItemList.Cover_SolarPanel.get(1),    GT_ModHandler.RecipeBits.NOT_REMOVABLE | GT_ModHandler.RecipeBits.REVERSIBLE, new Object[]{"SGS", "CPC", 'C', OrePrefixes.circuit.get(Materials.Basic), 'G', new ItemStack(Blocks.glass_pane, 1), 'P', ItemList.Plate_CarbonAlloy.get(1), 'S', ItemList.Circuit_Silicon_Wafer});
            GT_ModHandler.addCraftingRecipe(ItemList.Cover_SolarPanel_8V.get(1), GT_ModHandler.RecipeBits.NOT_REMOVABLE | GT_ModHandler.RecipeBits.REVERSIBLE, new Object[]{"SGS", "CPC","R R", 'C', OrePrefixes.circuit.get(Materials.Advanced), 'G', new ItemStack(Blocks.glass_pane, 1), 'P', OrePrefixes.wireGt04.get(Materials.Graphene), 'S', ItemList.Circuit_Silicon_Wafer2,'R', OrePrefixes.plate.get(Materials.GalliumArsenide)});
            GT_ModHandler.addCraftingRecipe(ItemList.Cover_SolarPanel_LV.get(1), GT_ModHandler.RecipeBits.NOT_REMOVABLE | GT_ModHandler.RecipeBits.REVERSIBLE, new Object[]{"SGS", "CPC","R R", 'C', OrePrefixes.circuit.get(Materials.Master), 'G', new ItemStack(Blocks.glass, 1)/*TODO GT_ModHandler.getIC2Item("reinforcedGlass", 1)*/, 'P', OrePrefixes.wireGt16.get(Materials.Graphene), 'S', ItemList.Circuit_Silicon_Wafer3,'R', OrePrefixes.plate.get(Materials.IndiumGalliumPhosphide)});
        }
        if (GregTech_API.sOPStuff.get(ConfigCategories.Recipes.gregtechrecipes, "SolarPanel8V", false)) {
            GT_ModHandler.addCraftingRecipe(ItemList.Cover_SolarPanel_8V.get(1), GT_ModHandler.RecipeBits.NOT_REMOVABLE | GT_ModHandler.RecipeBits.REVERSIBLE, new Object[]{"SSS", "STS", "SSS", 'S', ItemList.Cover_SolarPanel, 'T', OrePrefixes.circuit.get(Materials.Advanced)});
        }
        if (GregTech_API.sOPStuff.get(ConfigCategories.Recipes.gregtechrecipes, "SolarPanelLV", false)) {
            GT_ModHandler.addCraftingRecipe(ItemList.Cover_SolarPanel_LV.get(1), GT_ModHandler.RecipeBits.NOT_REMOVABLE | GT_ModHandler.RecipeBits.REVERSIBLE, new Object[]{" S ", "STS", " S ", 'S', ItemList.Cover_SolarPanel_8V, 'T', ItemList.Transformer_LV_ULV});
        }
        if (GregTech_API.sOPStuff.get(ConfigCategories.Recipes.gregtechrecipes, "SolarPanelMV", false)) {
            GT_ModHandler.addCraftingRecipe(ItemList.Cover_SolarPanel_MV.get(1), GT_ModHandler.RecipeBits.NOT_REMOVABLE | GT_ModHandler.RecipeBits.REVERSIBLE, new Object[]{" S ", "STS", " S ", 'S', ItemList.Cover_SolarPanel_LV, 'T', ItemList.Transformer_MV_LV});
        }
        if (GregTech_API.sOPStuff.get(ConfigCategories.Recipes.gregtechrecipes, "SolarPanelHV", false)) {
            GT_ModHandler.addCraftingRecipe(ItemList.Cover_SolarPanel_HV.get(1), GT_ModHandler.RecipeBits.NOT_REMOVABLE | GT_ModHandler.RecipeBits.REVERSIBLE, new Object[]{" S ", "STS", " S ", 'S', ItemList.Cover_SolarPanel_MV, 'T', ItemList.Transformer_HV_MV});
        }
        if (GregTech_API.sOPStuff.get(ConfigCategories.Recipes.gregtechrecipes, "SolarPanelEV", false)) {
            GT_ModHandler.addCraftingRecipe(ItemList.Cover_SolarPanel_EV.get(1), GT_ModHandler.RecipeBits.NOT_REMOVABLE | GT_ModHandler.RecipeBits.REVERSIBLE, new Object[]{" S ", "STS", " S ", 'S', ItemList.Cover_SolarPanel_HV, 'T', ItemList.Transformer_EV_HV});
        }
        if (GregTech_API.sOPStuff.get(ConfigCategories.Recipes.gregtechrecipes, "SolarPanelIV", false)) {
            GT_ModHandler.addCraftingRecipe(ItemList.Cover_SolarPanel_IV.get(1), GT_ModHandler.RecipeBits.NOT_REMOVABLE | GT_ModHandler.RecipeBits.REVERSIBLE, new Object[]{" S ", "STS", " S ", 'S', ItemList.Cover_SolarPanel_EV, 'T', ItemList.Transformer_IV_EV});
        }
        if (GregTech_API.sOPStuff.get(ConfigCategories.Recipes.gregtechrecipes, "SolarPanelLuV", false)) {
            GT_ModHandler.addCraftingRecipe(ItemList.Cover_SolarPanel_LuV.get(1), GT_ModHandler.RecipeBits.NOT_REMOVABLE | GT_ModHandler.RecipeBits.REVERSIBLE, new Object[]{" S ", "STS", " S ", 'S', ItemList.Cover_SolarPanel_IV, 'T', ItemList.Transformer_LuV_IV});
        }
        if (GregTech_API.sOPStuff.get(ConfigCategories.Recipes.gregtechrecipes, "SolarPanelZPM", false)) {
            GT_ModHandler.addCraftingRecipe(ItemList.Cover_SolarPanel_ZPM.get(1), GT_ModHandler.RecipeBits.NOT_REMOVABLE | GT_ModHandler.RecipeBits.REVERSIBLE, new Object[]{" S ", "STS", " S ", 'S', ItemList.Cover_SolarPanel_LuV, 'T', ItemList.Transformer_ZPM_LuV});
        }
        if (GregTech_API.sOPStuff.get(ConfigCategories.Recipes.gregtechrecipes, "SolarPanelUV", false)) {
            GT_ModHandler.addCraftingRecipe(ItemList.Cover_SolarPanel_UV.get(1), GT_ModHandler.RecipeBits.NOT_REMOVABLE | GT_ModHandler.RecipeBits.REVERSIBLE, new Object[]{" S ", "STS", " S ", 'S', ItemList.Cover_SolarPanel_ZPM, 'T', ItemList.Transformer_UV_ZPM});
        }
        double outputMultiplier = Math.min(GregTech_API.sOPStuff.get(ConfigCategories.Recipes.gregtechrecipes, "StoneDustOutputMultiplier", 0.6), 1.0);
        if (GregTech_API.sOPStuff.get(ConfigCategories.Recipes.gregtechrecipes, "StoneDustCentrifugation", false)) {
            int fullChance = (int)(10000 * outputMultiplier);
            int[] chances = new int[]{fullChance, fullChance, fullChance, fullChance, fullChance, (int)(5500 * outputMultiplier)};
            GT_Values.RA.addCentrifugeRecipe(Materials.Stone.getDust(1), GT_Values.NI, GT_Values.NF, GT_Values.NF, Materials.Quartzite.getDustSmall(1), Materials.PotassiumFeldspar.getDustSmall(1), Materials.Marble.getDustTiny(2), Materials.Biotite.getDustTiny(1), Materials.MetalMixture.getDustTiny(1), Materials.Sodalite.getDustTiny(1), chances, 500, 72);
            GT_Values.RA.addCentrifugeRecipe(Materials.MetalMixture.getDust(1), GT_Values.NI, GT_Values.NF, GT_Values.NF, Materials.BandedIron.getDustSmall(1), Materials.Bauxite.getDustSmall(1), Materials.Pyrolusite.getDustTiny(2), Materials.Barite.getDustTiny(1), Materials.Chromite.getDustTiny(1), Materials.Ilmenite.getDustTiny(1), new int[]{10000, 10000, 10000, 10000, 10000, 6000}, 500, 120);
        }
        double ashOutputMultiplier = Math.min(GregTech_API.sOPStuff.get(ConfigCategories.Recipes.gregtechrecipes, "AshOutputMultiplier", 1.0), 1.0);
        if (GregTech_API.sOPStuff.get(ConfigCategories.Recipes.gregtechrecipes, "AshYieldsAlkaliMetalOxides", true)) {
            int[] outputChances = new int[]{(int) (9900 * ashOutputMultiplier), (int) (6400 * ashOutputMultiplier), (int) (6000 * ashOutputMultiplier), (int) (500 * ashOutputMultiplier), (int) (5000 * ashOutputMultiplier), (int) (2500 * ashOutputMultiplier)};
            GT_Values.RA.addCentrifugeRecipe(Materials.Ash.getDust(1), GT_Values.NI, GT_Values.NF, GT_Values.NF, Materials.Quicklime.getDustSmall(2), Materials.Potash.getDustSmall(1), Materials.Magnesia.getDustSmall(1), Materials.PhosphorousPentoxide.getDustTiny(1), Materials.SodaAsh.getDustTiny(1), Materials.BandedIron.getDustTiny(1), outputChances, 240, 30);
        } else {
            GT_Values.RA.addCentrifugeRecipe(Materials.Ash.getDust(1), GT_Values.NI, GT_Values.NF, GT_Values.NF, Materials.Carbon.getDust(1), GT_Values.NI, GT_Values.NI, GT_Values.NI, GT_Values.NI, GT_Values.NI, new int[]{(int) (10000 * ashOutputMultiplier), 0, 0, 0, 0, 0}, 40, 16);
        }
        if (gregtechproxy.mSortToTheEnd) {
            try {
                GT_Log.out.println("GT_Mod: Sorting GregTech to the end of the Mod List for further processing.");
                LoadController tLoadController = (LoadController) GT_Utility.getFieldContent(Loader.instance(), "modController", true, true);
                List<ModContainer> tModList = tLoadController.getActiveModList();
                List<ModContainer> tNewModsList = new ArrayList<>();
                ModContainer tGregTech = null;
                short tModList_sS= (short) tModList.size();
                for (short i = 0; i < tModList_sS; i = (short) (i + 1)) {
                    ModContainer tMod = tModList.get(i);
                    if (tMod.getModId().equalsIgnoreCase("gregtech")) {
                        tGregTech = tMod;
                    } else {
                        tNewModsList.add(tMod);
                    }
                }
                if (tGregTech != null) {
                    tNewModsList.add(tGregTech);
                }
                GT_Utility.getField(tLoadController, "activeModList", true, true).set(tLoadController, tNewModsList);
            } catch (Throwable e) {
                if (GT_Values.D1) {
                    e.printStackTrace(GT_Log.err);
                }
            }
        }
        GregTech_API.sPreloadFinished = true;
        GT_Log.out.println("GT_Mod: Preload-Phase finished!");
        GT_Log.ore.println("GT_Mod: Preload-Phase finished!");
        for (Runnable tRunnable : GregTech_API.sAfterGTPreload) {
            tRunnable.run();
        }
    }

    @Mod.EventHandler
    public void onLoad(FMLInitializationEvent aEvent) {
        if (GregTech_API.sLoadStarted) {
            return;
        }
        for (Runnable tRunnable : GregTech_API.sBeforeGTLoad) {
            tRunnable.run();
        }

        new GT_Bees();

        //Disable Low Grav regardless of config if Cleanroom is disabled.
        gregtechproxy.mLowGravProcessing = gregtechproxy.mEnableCleanroom;

        gregtechproxy.onLoad();
        if (gregtechproxy.mSortToTheEnd) {
            //new GT_ItemIterator().run();
            gregtechproxy.registerUnificationEntries(); //TODO REALLY?!
            new GT_FuelLoader().run();
        }
        GregTech_API.sLoadFinished = true;
        GT_Log.out.println("GT_Mod: Load-Phase finished!");
        GT_Log.ore.println("GT_Mod: Load-Phase finished!");
        for (Runnable tRunnable : GregTech_API.sAfterGTLoad) {
            tRunnable.run();
        }
    }

    @Mod.EventHandler
    public void onPostLoad(FMLPostInitializationEvent aEvent) {
        if (GregTech_API.sPostloadStarted) {
            return;
        }
        for (Runnable tRunnable : GregTech_API.sBeforeGTPostload) {
            tRunnable.run();
        }

        GregTech_API.sPostloadStarted = true;
        //gregtechproxy.onPostLoad();
        if (gregtechproxy.mSortToTheEnd) {
            //gregtechproxy.registerUnificationEntries();
        } else {
            //new GT_ItemIterator().run();
            //gregtechproxy.registerUnificationEntries();
            new GT_FuelLoader().run();
        }
        new GT_BookAndLootLoader().run();
        new GT_BlockResistanceLoader().run();
        new GT_RecyclerBlacklistLoader().run();
        new GT_MachineRecipeLoader().run();
        new GT_Worldgenloader().run();
        new GT_CoverLoader().run();
        new GT_AE2EnergyTunnelLoader().run();
        LoadArmorComponents.init();

        GT_Log.out.println("GT_Mod: Activating OreDictionary Handler, this can take some time, as it scans the whole OreDictionary");
        FMLLog.info("If your Log stops here, you were too impatient. Wait a bit more next time, before killing Minecraft with the Task Manager.");
        gregtechproxy.activateOreDictHandler();
        new GT_Loader_MaterialRecipes().run();
        FMLLog.info("Congratulations, you have been waiting long enough. Have a Cake.");
        GT_Log.out.println("GT_Mod: List of Lists of Tool Recipes: "+GT_ModHandler.sSingleNonBlockDamagableRecipeList_list.toString());
        GT_Log.out.println("GT_Mod: Vanilla Recipe List -> Outputs null or stackSize <=0: " + GT_ModHandler.sVanillaRecipeList_warntOutput.toString());
        GT_Log.out.println("GT_Mod: Single Non Block Damagable Recipe List -> Outputs null or stackSize <=0: " + GT_ModHandler.sSingleNonBlockDamagableRecipeList_warntOutput.toString());

        new GT_CraftingRecipeLoader().run();
        ItemStack ISdata0 = new ItemStack(Items.potionitem, 1, 0);
        ItemStack ILdata0 = new ItemStack(Items.glass_bottle, 1);
        for (FluidContainerRegistry.FluidContainerData tData : FluidContainerRegistry.getRegisteredFluidContainerData()) {
            if ((tData.filledContainer.getItem() == Items.potionitem) && (tData.filledContainer.getItemDamage() == 0)) {
                GT_Recipe.GT_Recipe_Map.sFluidCannerRecipes.addRecipe(true, new ItemStack[]{ILdata0}, new ItemStack[]{ISdata0}, null, new FluidStack[]{Materials.Water.getFluid(250)}, null, 4, 1, 0);
                GT_Recipe.GT_Recipe_Map.sFluidCannerRecipes.addRecipe(true, new ItemStack[]{ISdata0}, new ItemStack[]{ILdata0}, null, null, null, 4, 1, 0);
            } else {
                GT_Recipe.GT_Recipe_Map.sFluidCannerRecipes.addRecipe(true, new ItemStack[]{tData.emptyContainer}, new ItemStack[]{tData.filledContainer}, null, new FluidStack[]{tData.fluid}, null, tData.fluid.amount / 62, 1, 0);
                GT_Recipe.GT_Recipe_Map.sFluidCannerRecipes.addRecipe(true, new ItemStack[]{tData.filledContainer}, new ItemStack[]{GT_Utility.getContainerItem(tData.filledContainer, true)}, null, null, new FluidStack[]{tData.fluid}, tData.fluid.amount / 62, 1, 0);
            }
        }
        for (ICentrifugeRecipe tRecipe : RecipeManagers.centrifugeManager.recipes()) {
            Map<ItemStack, Float> outputs = tRecipe.getAllProducts();
            ItemStack[] tOutputs = new ItemStack[outputs.size()];
            int[] tChances = new int[outputs.size()];
            int i = 0;
            for (Map.Entry<ItemStack, Float> entry : outputs.entrySet()) {
                tChances[i] = (int) (entry.getValue() * 10000);
                tOutputs[i] = entry.getKey().copy();
                i++;
            }
            GT_Recipe.GT_Recipe_Map.sCentrifugeRecipes.addRecipe(true, new ItemStack[]{tRecipe.getInput()}, tOutputs, null, tChances, null, null, 128, 5, 0);
        }
        for (ISqueezerRecipe tRecipe : RecipeManagers.squeezerManager.recipes()) {
            if ((tRecipe.getResources().length == 1) && (tRecipe.getFluidOutput() != null)) {
                GT_Recipe.GT_Recipe_Map.sFluidExtractionRecipes.addRecipe(true, new ItemStack[]{tRecipe.getResources()[0]}, new ItemStack[]{tRecipe.getRemnants()}, null, new int[]{(int) (tRecipe.getRemnantsChance() * 10000)}, null, new FluidStack[]{tRecipe.getFluidOutput()}, 400, 2, 0);
            }
        }

        if (gregtechproxy.mNerfedVanillaTools) {
            GT_Log.out.println("GT_Mod: Nerfing Vanilla Tool Durability");
            Items.wooden_sword.setMaxDamage(12);
            Items.wooden_pickaxe.setMaxDamage(12);
            Items.wooden_shovel.setMaxDamage(12);
            Items.wooden_axe.setMaxDamage(12);
            Items.wooden_hoe.setMaxDamage(12);

            Items.stone_sword.setMaxDamage(48);
            Items.stone_pickaxe.setMaxDamage(48);
            Items.stone_shovel.setMaxDamage(48);
            Items.stone_axe.setMaxDamage(48);
            Items.stone_hoe.setMaxDamage(48);

            Items.iron_sword.setMaxDamage(256);
            Items.iron_pickaxe.setMaxDamage(256);
            Items.iron_shovel.setMaxDamage(256);
            Items.iron_axe.setMaxDamage(256);
            Items.iron_hoe.setMaxDamage(256);

            Items.golden_sword.setMaxDamage(24);
            Items.golden_pickaxe.setMaxDamage(24);
            Items.golden_shovel.setMaxDamage(24);
            Items.golden_axe.setMaxDamage(24);
            Items.golden_hoe.setMaxDamage(24);

            Items.diamond_sword.setMaxDamage(768);
            Items.diamond_pickaxe.setMaxDamage(768);
            Items.diamond_shovel.setMaxDamage(768);
            Items.diamond_axe.setMaxDamage(768);
            Items.diamond_hoe.setMaxDamage(768);
        }
        GT_Log.out.println("GT_Mod: Adding buffered Recipes.");
        GT_ModHandler.stopBufferingCraftingRecipes();

        GT_Log.out.println("GT_Mod: Saving Lang File.");
        GT_LanguageManager.sEnglishFile.save();
        GregTech_API.sPostloadFinished = true;
        GT_Log.out.println("GT_Mod: PostLoad-Phase finished!");
        GT_Log.ore.println("GT_Mod: PostLoad-Phase finished!");
        for (Runnable tRunnable : GregTech_API.sAfterGTPostload) {
            tRunnable.run();
        }
        GT_Log.out.println("GT_Mod: Adding Fake Recipes for NEI");
        if (ItemList.FR_Bee_Drone.get(1) != null) {
            GT_Recipe.GT_Recipe_Map.sScannerFakeRecipes.addFakeRecipe(false, new ItemStack[]{ItemList.FR_Bee_Drone.getWildcard(1)}, new ItemStack[]{ItemList.FR_Bee_Drone.getWithName(1, "Scanned Drone")}, null, new FluidStack[]{Materials.Honey.getFluid(100)}, null, 500, 2, 0);
        }
        if (ItemList.FR_Bee_Princess.get(1) != null) {
            GT_Recipe.GT_Recipe_Map.sScannerFakeRecipes.addFakeRecipe(false, new ItemStack[]{ItemList.FR_Bee_Princess.getWildcard(1)}, new ItemStack[]{ItemList.FR_Bee_Princess.getWithName(1, "Scanned Princess")}, null, new FluidStack[]{Materials.Honey.getFluid(100)}, null, 500, 2, 0);
        }
        if (ItemList.FR_Bee_Queen.get(1) != null) {
            GT_Recipe.GT_Recipe_Map.sScannerFakeRecipes.addFakeRecipe(false, new ItemStack[]{ItemList.FR_Bee_Queen.getWildcard(1)}, new ItemStack[]{ItemList.FR_Bee_Queen.getWithName(1, "Scanned Queen")}, null, new FluidStack[]{Materials.Honey.getFluid(100)}, null, 500, 2, 0);
        }
        if (ItemList.FR_Tree_Sapling.get(1) != null) {
            GT_Recipe.GT_Recipe_Map.sScannerFakeRecipes.addFakeRecipe(false, new ItemStack[]{ItemList.FR_Tree_Sapling.getWildcard(1)}, new ItemStack[]{ItemList.FR_Tree_Sapling.getWithName(1, "Scanned Sapling")}, null, new FluidStack[]{Materials.Honey.getFluid(100)}, null, 500, 2, 0);
        }
        if (ItemList.FR_Butterfly.get(1) != null) {
            GT_Recipe.GT_Recipe_Map.sScannerFakeRecipes.addFakeRecipe(false, new ItemStack[]{ItemList.FR_Butterfly.getWildcard(1)}, new ItemStack[]{ItemList.FR_Butterfly.getWithName(1, "Scanned Butterfly")}, null, new FluidStack[]{Materials.Honey.getFluid(100)}, null, 500, 2, 0);
        }
        if (ItemList.FR_Larvae.get(1) != null) {
            GT_Recipe.GT_Recipe_Map.sScannerFakeRecipes.addFakeRecipe(false, new ItemStack[]{ItemList.FR_Larvae.getWildcard(1)}, new ItemStack[]{ItemList.FR_Larvae.getWithName(1, "Scanned Larvae")}, null, new FluidStack[]{Materials.Honey.getFluid(100)}, null, 500, 2, 0);
        }
        if (ItemList.FR_Serum.get(1) != null) {
            GT_Recipe.GT_Recipe_Map.sScannerFakeRecipes.addFakeRecipe(false, new ItemStack[]{ItemList.FR_Serum.getWildcard(1)}, new ItemStack[]{ItemList.FR_Serum.getWithName(1, "Scanned Serum")}, null, new FluidStack[]{Materials.Honey.getFluid(100)}, null, 500, 2, 0);
        }
        if (ItemList.FR_Caterpillar.get(1) != null) {
            GT_Recipe.GT_Recipe_Map.sScannerFakeRecipes.addFakeRecipe(false, new ItemStack[]{ItemList.FR_Caterpillar.getWildcard(1)}, new ItemStack[]{ItemList.FR_Caterpillar.getWithName(1, "Scanned Caterpillar")}, null, new FluidStack[]{Materials.Honey.getFluid(100)}, null, 500, 2, 0);
        }
        if (ItemList.FR_PollenFertile.get(1) != null) {
            GT_Recipe.GT_Recipe_Map.sScannerFakeRecipes.addFakeRecipe(false, new ItemStack[]{ItemList.FR_PollenFertile.getWildcard(1)}, new ItemStack[]{ItemList.FR_PollenFertile.getWithName(1, "Scanned Pollen")}, null, new FluidStack[]{Materials.Honey.getFluid(100)}, null, 500, 2, 0);
        }
        GT_Recipe.GT_Recipe_Map.sScannerFakeRecipes.addFakeRecipe(false, new ItemStack[]{new ItemStack(Items.written_book, 1, 32767)}, new ItemStack[]{ItemList.Tool_DataStick.getWithName(1, "Scanned Book Data")}, ItemList.Tool_DataStick.getWithName(1, "Stick to save it to"), null, null, 128, 32, 0);
        GT_Recipe.GT_Recipe_Map.sScannerFakeRecipes.addFakeRecipe(false, new ItemStack[]{new ItemStack(Items.filled_map, 1, 32767)}, new ItemStack[]{ItemList.Tool_DataStick.getWithName(1, "Scanned Map Data")}, ItemList.Tool_DataStick.getWithName(1, "Stick to save it to"), null, null, 128, 32, 0);
        GT_Recipe.GT_Recipe_Map.sScannerFakeRecipes.addFakeRecipe(false, new ItemStack[]{ItemList.Tool_DataOrb.getWithName(1, "Orb to overwrite")}, new ItemStack[]{ItemList.Tool_DataOrb.getWithName(1, "Copy of the Orb")}, ItemList.Tool_DataOrb.getWithName(0, "Orb to copy"), null, null, 512, 32, 0);
        GT_Recipe.GT_Recipe_Map.sScannerFakeRecipes.addFakeRecipe(false, new ItemStack[]{ItemList.Tool_DataStick.getWithName(1, "Stick to overwrite")}, new ItemStack[]{ItemList.Tool_DataStick.getWithName(1, "Copy of the Stick")}, ItemList.Tool_DataStick.getWithName(0, "Stick to copy"), null, null, 128, 32, 0);
        GT_Recipe.GT_Recipe_Map.sScannerFakeRecipes.addFakeRecipe(false, new ItemStack[]{ItemList.Tool_DataStick.getWithName(1, "Raw Prospection Data")}, new ItemStack[]{ItemList.Tool_DataStick.getWithName(1, "Analyzed Prospection Data")}, null, null, null, 1000, 32, 0);

        if (!GT_MetaTileEntity_Massfabricator.sRequiresUUA) GT_Recipe.GT_Recipe_Map.sMassFabFakeRecipes.addFakeRecipe(false, null, null, null, null, new FluidStack[]{Materials.UUMatter.getFluid(1)}, GT_MetaTileEntity_Massfabricator.sDurationMultiplier, 256, 0);
        GT_Recipe.GT_Recipe_Map.sMassFabFakeRecipes.addFakeRecipe(false, null, null, null, new FluidStack[]{Materials.UUAmplifier.getFluid(GT_MetaTileEntity_Massfabricator.sUUAperUUM)}, new FluidStack[]{Materials.UUMatter.getFluid(1)}, GT_MetaTileEntity_Massfabricator.sDurationMultiplier / GT_MetaTileEntity_Massfabricator.sUUASpeedBonus, 256, 0);
        GT_Recipe.GT_Recipe_Map.sRockBreakerFakeRecipes.addFakeRecipe(false, new ItemStack[]{ItemList.Display_ITS_FREE.getWithName(0, "Place Lava on Side")}, new ItemStack[]{new ItemStack(Blocks.cobblestone, 1)}, null, null, null, 16, 32, 0);
        GT_Recipe.GT_Recipe_Map.sRockBreakerFakeRecipes.addFakeRecipe(false, new ItemStack[]{ItemList.Display_ITS_FREE.getWithName(0, "Place Lava on Top")}, new ItemStack[]{new ItemStack(Blocks.stone, 1)}, null, null, null, 16, 32, 0);
        GT_Recipe.GT_Recipe_Map.sRockBreakerFakeRecipes.addFakeRecipe(false, new ItemStack[]{GT_OreDictUnificator.get(OrePrefixes.dust, Materials.Redstone)}, new ItemStack[]{new ItemStack(Blocks.obsidian, 1)}, null, null, null, 128, 32, 0);

        if (GregTech_API.mOutputRF || GregTech_API.mInputRF) {
            GT_Utility.checkAvailabilities();
            if (!GT_Utility.RF_CHECK) {
                GregTech_API.mOutputRF = false;
                GregTech_API.mInputRF = false;
            }
        }

        achievements = new GT_Achievements();
        GT_Log.out.println("GT_Mod: Loading finished, deallocating temporary Init Variables.");
        GregTech_API.sBeforeGTPreload = null;
        GregTech_API.sAfterGTPreload = null;
        GregTech_API.sBeforeGTLoad = null;
        GregTech_API.sAfterGTLoad = null;
        GregTech_API.sBeforeGTPostload = null;
        GregTech_API.sAfterGTPostload = null;
    }

    @Mod.EventHandler
    public void onServerStarting(FMLServerStartingEvent aEvent) {
        for (Runnable tRunnable : GregTech_API.sBeforeGTServerstart) {
            tRunnable.run();
        }
        gregtechproxy.onServerStarting();
        GregTech_API.mServerStarted = true;
        GT_Log.out.println("GT_Mod: ServerStarting-Phase finished!");
        GT_Log.ore.println("GT_Mod: ServerStarting-Phase finished!");
        for (Runnable tRunnable : GregTech_API.sAfterGTServerstart) {
            tRunnable.run();
        }
    }

    @Mod.EventHandler
    public void onServerStarted(FMLServerStartedEvent aEvent) {
        gregtechproxy.onServerStarted();
    }

    @Mod.EventHandler
    public void onIDChangingEvent(FMLModIdMappingEvent aEvent) {
        GT_Utility.reInit();
        GT_Recipe.reInit();
        for (Iterator i$ = GregTech_API.sItemStackMappings.iterator(); i$.hasNext(); ) {
            Map tMap = (Map) i$.next();
            GT_Utility.reMap(tMap);
        }
    }

    @Mod.EventHandler
    public void onServerStopping(FMLServerStoppingEvent aEvent) {
        for (Runnable tRunnable : GregTech_API.sBeforeGTServerstop) {
            tRunnable.run();
        }
        gregtechproxy.onServerStopping();
        for (Runnable tRunnable : GregTech_API.sAfterGTServerstop) {
            tRunnable.run();
        }
    }

    public boolean isServerSide() {
        return gregtechproxy.isServerSide();
    }

    public boolean isClientSide() {
        return gregtechproxy.isClientSide();
    }

    public boolean isBukkitSide() {
        return gregtechproxy.isBukkitSide();
    }

    public EntityPlayer getThePlayer() {
        return gregtechproxy.getThePlayer();
    }

    public int addArmor(String aArmorPrefix) {
        return gregtechproxy.addArmor(aArmorPrefix);
    }

    public void doSonictronSound(ItemStack aStack, World aWorld, double aX, double aY, double aZ) {
        gregtechproxy.doSonictronSound(aStack, aWorld, aX, aY, aZ);
    }

    public static int calculateTotalGTVersion(int majorVersion, int minorVersion){
        return majorVersion * 1000 + minorVersion;
    }
}