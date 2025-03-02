package de.ellpeck.naturesaura.proxy;

import de.ellpeck.naturesaura.Helper;
import de.ellpeck.naturesaura.NaturesAura;
import de.ellpeck.naturesaura.blocks.ModBlocks;
import de.ellpeck.naturesaura.compat.Compat;
import de.ellpeck.naturesaura.entities.ModEntities;
import de.ellpeck.naturesaura.entities.render.RenderEffectInhibitor;
import de.ellpeck.naturesaura.entities.render.RenderMoverMinecart;
import de.ellpeck.naturesaura.entities.render.RenderStub;
import de.ellpeck.naturesaura.events.ClientEvents;
import de.ellpeck.naturesaura.gui.GuiEnderCrate;
import de.ellpeck.naturesaura.gui.ModContainers;
import de.ellpeck.naturesaura.items.ItemColorChanger;
import de.ellpeck.naturesaura.items.ModItems;
import de.ellpeck.naturesaura.particles.ParticleHandler;
import de.ellpeck.naturesaura.particles.ParticleMagic;
import de.ellpeck.naturesaura.reg.IColorProvidingBlock;
import de.ellpeck.naturesaura.reg.IColorProvidingItem;
import de.ellpeck.naturesaura.reg.ITESRProvider;
import de.ellpeck.naturesaura.reg.ModRegistry;
import de.ellpeck.naturesaura.renderers.PlayerLayerTrinkets;
import de.ellpeck.naturesaura.renderers.SupporterFancyHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

public class ClientProxy implements IProxy {

    @Override
    public void preInit(FMLCommonSetupEvent event) {
        MinecraftForge.EVENT_BUS.register(new ClientEvents());
        Compat.setupClient();
        MenuScreens.register(ModContainers.ENDER_CRATE, GuiEnderCrate::new);
        MenuScreens.register(ModContainers.ENDER_ACCESS, GuiEnderCrate::new);

        ItemProperties.register(ModItems.COLOR_CHANGER, new ResourceLocation(NaturesAura.MOD_ID, "fill_mode"),
                (stack, level, entity, i) -> ItemColorChanger.isFillMode(stack) ? 1 : 0);
        ItemProperties.register(ModItems.COLOR_CHANGER, new ResourceLocation(NaturesAura.MOD_ID, "has_color"),
                (stack, level, entity, i) -> ItemColorChanger.getStoredColor(stack) != null ? 1 : 0);
        for (var item : new Item[]{ModItems.SKY_AXE, ModItems.DEPTH_PICKAXE, ModItems.DEPTH_AXE}) {
            ItemProperties.register(item, new ResourceLocation(NaturesAura.MOD_ID, "enabled"),
                    (stack, level, entity, i) -> Helper.isToolEnabled(stack) ? 1 : 0);
        }
    }

    @Override
    public void init(FMLCommonSetupEvent event) {
        var skinMap = Minecraft.getInstance().getEntityRenderDispatcher().getSkinMap();
        for (var render : new EntityRenderer[]{skinMap.get("default"), skinMap.get("slim")}) {
            if (render instanceof PlayerRenderer living)
                living.addLayer(new PlayerLayerTrinkets(living));
        }
        new SupporterFancyHandler();

        for (var item : ModRegistry.ALL_ITEMS) {
            if (item instanceof IColorProvidingBlock color)
                this.addColorProvidingBlock(color);
            if (item instanceof IColorProvidingItem color)
                this.addColorProvidingItem(color);
            if (item instanceof ITESRProvider<?> provider)
                provider.registerTESR();
        }

        EntityRenderers.register(ModEntities.MOVER_CART, RenderMoverMinecart::new);
        EntityRenderers.register(ModEntities.EFFECT_INHIBITOR, RenderEffectInhibitor::new);
        EntityRenderers.register(ModEntities.LIGHT_PROJECTILE, RenderStub::new);
        EntityRenderers.register(ModEntities.STRUCTURE_FINDER, c -> new ThrownItemRenderer<>(c, 1, true));
    }

    @Override
    @SuppressWarnings("removal")
    public void postInit(FMLCommonSetupEvent event) {
        // TODO this uses a vanilla model so we can't just add the json entry to it
        ItemBlockRenderTypes.setRenderLayer(ModBlocks.GOLD_POWDER, RenderType.cutoutMipped());
    }

    @Override
    public void spawnMagicParticle(double posX, double posY, double posZ, double motionX, double motionY, double motionZ, int color, float scale, int maxAge, float gravity, boolean collision, boolean fade) {
        ParticleHandler.spawnParticle(() -> new ParticleMagic(Minecraft.getInstance().level,
                posX, posY, posZ,
                motionX, motionY, motionZ,
                color, scale, maxAge, gravity, collision, fade, ParticleHandler.depthEnabled), posX, posY, posZ);
    }

    @Override
    public void setParticleDepth(boolean depth) {
        ParticleHandler.depthEnabled = depth;
    }

    @Override
    public void setParticleSpawnRange(int range) {
        ParticleHandler.range = range;
    }

    @Override
    public void setParticleCulling(boolean cull) {
        ParticleHandler.culling = cull;
    }

    private void addColorProvidingItem(IColorProvidingItem item) {
        var colors = Minecraft.getInstance().getItemColors();
        var color = item.getItemColor();

        // TODO use event
        if (item instanceof Item) {
            colors.register(color, (Item) item);
        } else if (item instanceof Block) {
            colors.register(color, (Block) item);
        }
    }

    private void addColorProvidingBlock(IColorProvidingBlock block) {
        if (block instanceof Block)
            Minecraft.getInstance().getBlockColors().register(block.getBlockColor(), (Block) block);
    }

}